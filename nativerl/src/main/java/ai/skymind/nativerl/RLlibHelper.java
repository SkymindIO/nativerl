package ai.skymind.nativerl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bytedeco.cpython.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.indexer.*;
import org.bytedeco.numpy.*;
import static org.bytedeco.cpython.global.python.*;
import static org.bytedeco.numpy.global.numpy.*;

/**
 * This is a helper class to help users use an implementation of
 * the reinforcement learning Environment interface using RLlib.
 * The output is a Python script that can executed with an existing
 * installation of RLlib.
 * <p>
 * Currently available algorithms according to RLlib's registry.py:
 *   "DDPG" *
 *   "APEX_DDPG" *
 *   "TD3" *
 *   "PPO"
 *   "ES"
 *   "ARS"
 *   "DQN"
 *   "APEX"
 *   "A3C"
 *   "A2C"
 *   "PG"
 *   "IMPALA"
 *   "QMIX" **
 *   "APEX_QMIX" **
 *   "APPO"
 *   "MARWIL"
 * <p>
 *   * Works only with continuous actions (doesn't work with discrete ones)
 *   ** Requires PyTorch (doesn't work with TensorFlow)
 * <p>
 * The parameters exposed with arrays are available for hyperparameter tuning using a grid search.
 * Currently, the 3 such parameters available are gammas, learningRates, and traingBatchSizes.
 */
public class RLlibHelper {

    /**
     * A PolicyHelper for RLlib, which can load its checkpoint files.
     * Requires CPython and comes with all its limitations, such as the GIL.
     */
    public static class PythonPolicyHelper implements PolicyHelper {
        PyObject globals = null;
        PyArrayObject obsArray = null;
        FloatPointer obsData = null;

        public PythonPolicyHelper(File[] rllibpaths, String algorithm, File checkpoint, Environment env) throws IOException {
            this(rllibpaths, algorithm, checkpoint, env.getClass().getSimpleName(), env.getActionSpace(), env.getObservationSpace());
        }
        public PythonPolicyHelper(File[] rllibpaths, String algorithm, File checkpoint, String name, long discreteActions, long continuousObservations) throws IOException {
            this(rllibpaths, algorithm, checkpoint, name, AbstractEnvironment.getDiscreteSpace(discreteActions),
                    AbstractEnvironment.getContinuousSpace(continuousObservations));
        }
        public PythonPolicyHelper(File[] rllibpaths, String algorithm, File checkpoint, String name, Space actionSpace, Space obsSpace) throws IOException {
            File[] paths = org.bytedeco.numpy.global.numpy.cachePackages();
            paths = Arrays.copyOf(paths, paths.length + rllibpaths.length);
            System.arraycopy(rllibpaths, 0, paths, paths.length - rllibpaths.length, rllibpaths.length);
            Py_SetPath(paths);
            Pointer program = Py_DecodeLocale(name, null);
            Py_SetProgramName(program);
            Py_Initialize();
            PySys_SetArgvEx(1, program, 0);
            if (_import_array() < 0) {
                PyErr_Print();
                PyErr_Clear();
                throw new RuntimeException("numpy.core.multiarray failed to import");
            }
            PyObject module = PyImport_AddModule("__main__");
            globals = PyModule_GetDict(module);

            Discrete discreteActionSpace = (Discrete)actionSpace;
            Continuous continuousObsSpace = (Continuous)obsSpace;
            float[] obsLow = continuousObsSpace.low().get();
            float[] obsHigh = continuousObsSpace.high().get();
            long[] obsShape = continuousObsSpace.shape().get();

            PyRun_StringFlags("import gym, inspect, numpy, ray, sys\n"
                    + "from ray.rllib.agents import registry\n"
                    + "\n"
                    + "class " + name + "(gym.Env):\n"
                    + "    def __init__(self, env_config):\n"
                    + "        self.action_space = gym.spaces.Discrete(" + discreteActionSpace.n() + ")\n"
                    + "        low = " + (obsLow.length == 1 ? obsLow[0] : "numpy.array(" + Arrays.toString(obsLow) + ")") + "\n"
                    + "        high = " + (obsHigh.length == 1 ? obsHigh[0] : "numpy.array(" + Arrays.toString(obsHigh) + ")") + "\n"
                    + "        shape = " + (obsShape.length == 0 ? "None" : "numpy.array(" + Arrays.toString(obsShape) + ")") + "\n"
                    + "        self.observation_space = gym.spaces.Box(low, high, shape=shape, dtype=numpy.float32)\n"
                    + "\n"
                    + "ray.init(local_mode=True)\n"
                    + "cls = registry.get_agent_class(\"" + algorithm + "\")\n"
                    + "config = inspect.getmodule(cls).DEFAULT_CONFIG.copy()\n"
                    + "config[\"num_gpus\"] = 0\n"
                    + "config[\"num_workers\"] = 1\n"
                    + "trainer = cls(config=config, env=" + name + ")\n"
                    + "\n"
                    + "# Can optionally call trainer.restore(path) to load a checkpoint.\n"
                    + "trainer.restore(\"" + checkpoint.getAbsolutePath() + "\")\n", Py_file_input, globals, globals, null);

            if (PyErr_Occurred() != null) {
                PyErr_Print();
                PyErr_Clear();
                PyRun_StringFlags("sys.stderr.flush()", Py_file_input, globals, globals, null);
                throw new RuntimeException("Python error occurred");
            }

            if (obsShape.length == 0) {
                obsShape = new long[] {obsLow.length};
            }
            obsArray = new PyArrayObject(PyArray_New(PyArray_Type(), obsShape.length, new SizeTPointer(obsShape),
                                                     NPY_FLOAT, null, null, 0, 0, null));
            obsData = new FloatPointer(PyArray_BYTES(obsArray)).capacity(PyArray_Size(obsArray));
            PyDict_SetItemString(globals, "obs", obsArray);
        }

        @Override public float[] computeContinuousAction(float[] state) {
            throw new UnsupportedOperationException();
        }

        @Override public long computeDiscreteAction(float[] state) {
            obsData.put(state);
            PyRun_StringFlags("action = trainer.compute_action(obs)\n", Py_file_input, globals, globals, null);

            if (PyErr_Occurred() != null) {
                PyErr_Print();
                PyErr_Clear();
                PyRun_StringFlags("sys.stderr.flush()", Py_file_input, globals, globals, null);
                throw new RuntimeException("Python error occurred");
            }
            return PyLong_AsLongLong(PyDict_GetItemString(globals, "action"));
        }
    }

    /** The paths where to find RLlib itself and all of its Python dependencies. */
    File[] rllibpaths = null;
    /** The algorithm to use with RLlib for training and the PythonPolicyHelper. */
    String algorithm = "PPO";
    /** The directory where to output the logs of RLlib. */
    File outputDir = null;
    /** The RLlib checkpoint to restore for the PythonPolicyHelper or to start training from instead of a random policy. */
    File checkpoint = null;
    /** A concrete instance of a subclass of Environment to use as environment for training and/or with PythonPolicyHelper. */
    Environment environment = null;
    /** The number of GPUs to let RLlib use during training. */
    int numGPUs = 0;
    /** The number of CPU cores to let RLlib use during training. */
    int numWorkers = 1;
    /** A random seed that we can set to obtain reproducible results. */
    long randomSeed = 0;
    /** The values for the gamma hyperparameter to tune for. */
    double[] gammas = {0.99};
    /** The values for the learning rate hyperparameter to tune for. */
    double[] learningRates = {5e-5};
    /** The values for the train batch sizes hyperparameter to tune for. */
    int[] trainBatchSizes = {128};
    /** The number of hidden layers in the MLP to use for the learning model. */
    int numHiddenLayers = 2;
    /** The number of nodes per layer in the MLP to use for the learning model. */
    int numHiddenNodes = 256;
    /** The number of samples, which must be <= trainBatchSize, to pick from each worker, one after the other to reach trainBatchSize. */
    int sampleBatchSize = 32;
    /** The maximum number of training iterations as a stopping criterion. */
    int maxIterations = 500;
    /** The maximum value for the mean cumulative reward as a stopping criterion. */
    double maxRewardMean = Double.POSITIVE_INFINITY;
    /** The frequency at which policies should be saved to files, given as an interval in the number of training iterations. */
    int savePolicyInterval = 100;
    /** The address of the Redis server for distributed training sessions. */
    String redisAddress = null;
    /** Any number custom parameters written in Python appended to the config of ray.tune.run() as is. */
    String customParameters = "";
    /** Indicates that we need multiagent support with the Environment class provided, but where all agents share the same policy. */
    boolean multiAgent = false;

    static <T> List<List<T>> subcombinations(List<T> input, int length, int start, List<T> temp) {
        List<List<T>> output = new ArrayList<List<T>>();
        if (temp == null) {
            temp = new ArrayList<T>(length);
            for (int i = 0; i < length; i++) {
                temp.add(null);
            }
        }
        if (length == 0) {
            output.add(new ArrayList<T>(temp));
            return output;
        }
        for (int i = start; i <= input.size() - length; i++) {
            temp.set(temp.size() - length, input.get(i));
            List<List<T>> o = subcombinations(input, length - 1, i + 1, temp);
            if (o != null && o.size() > 0) {
                output.addAll(o);
            }
        }
        return output;
    }

    static <T> List<List<List<T>>> subcombinations(List<T> list) {
        List<List<List<T>>> output = new ArrayList<List<List<T>>>(list.size());
        for (int i = 0; i < list.size(); i++) {
            output.add(subcombinations(list, i + 1, 0, null));
        }
        return output;
    }

    static List<List<List<Integer>>> subcombinations(int[] array) {
        List<Integer> list = new ArrayList<Integer>(array.length);
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return subcombinations(list);
    }

    static List<List<List<Double>>> subcombinations(double[] array) {
        List<Double> list = new ArrayList<Double>(array.length);
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return subcombinations(list);
    }

    public RLlibHelper() {
    }

    public RLlibHelper(RLlibHelper copy) {
        this.rllibpaths = copy.rllibpaths;
        this.algorithm = copy.algorithm;
        this.outputDir = copy.outputDir;
        this.checkpoint = copy.checkpoint;
        this.environment = copy.environment;
        this.numGPUs = copy.numGPUs;
        this.numWorkers = copy.numWorkers;
        this.randomSeed = copy.randomSeed;
        this.gammas = copy.gammas;
        this.learningRates = copy.learningRates;
        this.trainBatchSizes = copy.trainBatchSizes;
        this.numHiddenLayers = copy.numHiddenLayers;
        this.numHiddenNodes = copy.numHiddenNodes;
        this.sampleBatchSize = copy.sampleBatchSize;
        this.maxIterations = copy.maxIterations;
        this.maxRewardMean = copy.maxRewardMean;
        this.savePolicyInterval = copy.savePolicyInterval;
        this.redisAddress = copy.redisAddress;
        this.customParameters = copy.customParameters;
    }

    /**
     * Returns "subcombinations" of the parameters with which we can do hyperparameter tuning, for example:
     *
     <pre>{@code
         $ java -cp nativerl-1.0.0-SNAPSHOT.jar ai.skymind.nativerl.RLlibHelper --gammas 0.99 --learning-rates 5e-4,5e-5 --train-batch-sizes 128,256 --subcombinations
         RLlibHelper[numberOfTrials=1, ..., gammas=[0.99], learningRates=[5.0E-4], trainBatchSizes=[128], ...]
         RLlibHelper[numberOfTrials=1, ..., gammas=[0.99], learningRates=[5.0E-4], trainBatchSizes=[256], ...]
         RLlibHelper[numberOfTrials=1, ..., gammas=[0.99], learningRates=[5.0E-5], trainBatchSizes=[128], ...]
         RLlibHelper[numberOfTrials=1, ..., gammas=[0.99], learningRates=[5.0E-5], trainBatchSizes=[256], ...]
         RLlibHelper[numberOfTrials=2, ..., gammas=[0.99], learningRates=[5.0E-4], trainBatchSizes=[128, 256], ...]
         RLlibHelper[numberOfTrials=2, ..., gammas=[0.99], learningRates=[5.0E-5], trainBatchSizes=[128, 256], ...]
         RLlibHelper[numberOfTrials=2, ..., gammas=[0.99], learningRates=[5.0E-4, 5.0E-5], trainBatchSizes=[128], ...]
         RLlibHelper[numberOfTrials=2, ..., gammas=[0.99], learningRates=[5.0E-4, 5.0E-5], trainBatchSizes=[256], ...]
         RLlibHelper[numberOfTrials=4, ..., gammas=[0.99], learningRates=[5.0E-4, 5.0E-5], trainBatchSizes=[128, 256], ...]
     }</pre>
     *
     */
    public List<RLlibHelper> createSubcombinations() {
        List<List<List<Double>>> gammaSubcombinations = subcombinations(gammas);
        List<List<List<Double>>> learningRateSubcombinations = subcombinations(learningRates);
        List<List<List<Integer>>> trainBatchSizeSubcombinations = subcombinations(trainBatchSizes);

        List<RLlibHelper> subcombinations = new ArrayList<RLlibHelper>();
        for (int i = 0; i < gammaSubcombinations.size(); i++) {
            for (int j = 0; j < learningRateSubcombinations.size(); j++) {
                for (int k = 0; k < trainBatchSizeSubcombinations.size(); k++) {
                    List<List<Double>> gammaCombinations = gammaSubcombinations.get(i);
                    List<List<Double>> learningRateCombinations = learningRateSubcombinations.get(j);
                    List<List<Integer>> trainBatchSizeCombinations = trainBatchSizeSubcombinations.get(k);
                    for (int ii = 0; ii < gammaCombinations.size(); ii++) {
                        for (int jj = 0; jj < learningRateCombinations.size(); jj++) {
                            for (int kk = 0; kk < trainBatchSizeCombinations.size(); kk++) {
                                List<Double> gammas = gammaCombinations.get(ii);
                                List<Double> learningRates = learningRateCombinations.get(jj);
                                List<Integer> trainBatchSizes = trainBatchSizeCombinations.get(kk);
                                RLlibHelper r = new RLlibHelper(this);
                                r.gammas = new double[gammas.size()];
                                r.learningRates = new double[learningRates.size()];
                                r.trainBatchSizes = new int[trainBatchSizes.size()];
                                for (int n = 0; n < gammas.size(); n++) {
                                    r.gammas[n] = gammas.get(n);
                                }
                                for (int n = 0; n < learningRates.size(); n++) {
                                    r.learningRates[n] = learningRates.get(n);
                                }
                                for (int n = 0; n < trainBatchSizes.size(); n++) {
                                    r.trainBatchSizes[n] = trainBatchSizes.get(n);
                                }
                                subcombinations.add(r);
                            }
                        }
                    }
                }
            }
        }
        return subcombinations;
    }

    /** The number of trials required based on the parameters available for hyperparameter tuning and a grid search. */
    public int numberOfTrials() {
        return gammas.length * learningRates.length * trainBatchSizes.length;
    }

    @Override public String toString() {
        return "RLlibHelper[numberOfTrials=" + numberOfTrials() + ", "
                + "rllibpaths=" + Arrays.deepToString(rllibpaths) + ", "
                + "algorithm=" + algorithm + ", "
                + "outputDir=" + outputDir + ", "
                + "checkpoint=" + checkpoint + ", "
                + "environment=" + environment + ", "
                + "numGPUs=" + numGPUs + ", "
                + "numWorkers=" + numWorkers + ", "
                + "randomSeed=" + randomSeed + ", "
                + "gammas=" + Arrays.toString(gammas) + ", "
                + "learningRates=" + Arrays.toString(learningRates) + ", "
                + "trainBatchSizes=" + Arrays.toString(trainBatchSizes) + ", "
                + "numHiddenLayers=" + numHiddenLayers + ", "
                + "numHiddenNodes=" + numHiddenNodes  + ", "
                + "sampleBatchSize=" + sampleBatchSize + ", "
                + "maxIterations=" + maxIterations + ", "
                + "maxRewardMean=" + maxRewardMean + ", "
                + "savePolicyInterval=" + savePolicyInterval + ", "
                + "redisAddress=" + redisAddress + ", "
                + "customParameters=" + customParameters + "]";
    }

    public File[] rllibpaths() {
        return rllibpaths;
    }
    public RLlibHelper rllibpaths(File[] rllibpaths) {
        this.rllibpaths = rllibpaths;
        return this;
    }
    public RLlibHelper rllibpaths(String[] rllibpaths) {
        File[] files = new File[rllibpaths.length];
        for (int i = 0; i < files.length; i++) {
            files[i] = new File(rllibpaths[i]);
        }
        this.rllibpaths = files;
        return this;
    }

    public String algorithm() {
        return algorithm;
    }
    public RLlibHelper algorithm(String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public File outputDir() {
        return outputDir;
    }
    public RLlibHelper outputDir(File outputDir) {
        this.outputDir = outputDir;
        return this;
    }
    public RLlibHelper outputDir(String outputDir) {
        this.outputDir = new File(outputDir);
        return this;
    }

    public File checkpoint() {
        return checkpoint;
    }
    public RLlibHelper checkpoint(File checkpoint) {
        this.checkpoint = checkpoint;
        return this;
    }
    public RLlibHelper checkpoint(String checkpoint) {
        this.checkpoint = new File(checkpoint);
        return this;
    }

    public Environment environment() {
        return environment;
    }
    public RLlibHelper environment(Environment environment) {
        this.environment = environment;
        return this;
    }

    public int numGPUs() {
        return numGPUs;
    }
    public RLlibHelper numGPUs(int numGPUs) {
        this.numGPUs = numGPUs;
        return this;
    }

    public int numWorkers() {
        return numWorkers;
    }
    public RLlibHelper numWorkers(int numWorkers) {
        this.numWorkers = numWorkers;
        return this;
    }

    public long randomSeed() {
        return randomSeed;
    }
    public RLlibHelper randomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
        return this;
    }

    public double[] gammas() {
        return gammas;
    }
    public RLlibHelper gammas(double[] gammas) {
        this.gammas = gammas;
        return this;
    }

    public double[] learningRates() {
        return learningRates;
    }
    public RLlibHelper learningRates(double[] learningRates) {
        this.learningRates = learningRates;
        return this;
    }

    public int[] trainBatchSizes() {
        return trainBatchSizes;
    }
    public RLlibHelper trainBatchSizes(int[] trainBatchSizes) {
        this.trainBatchSizes = trainBatchSizes;
        return this;
    }

    public int numHiddenLayers() {
        return numHiddenLayers;
    }
    public RLlibHelper numHiddenLayers(int numHiddenLayers) {
        this.numHiddenLayers = numHiddenLayers;
        return this;
    }

    public int numHiddenNodes() {
        return numHiddenNodes;
    }
    public RLlibHelper numHiddenNodes(int numHiddenNodes) {
        this.numHiddenNodes = numHiddenNodes;
        return this;
    }

    public int sampleBatchSize() {
        return sampleBatchSize;
    }
    public RLlibHelper sampleBatchSize(int sampleBatchSize) {
        this.sampleBatchSize = sampleBatchSize;
        return this;
    }

    public int maxIterations() {
        return maxIterations;
    }
    public RLlibHelper maxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
        return this;
    }

    public double maxRewardMean() {
        return maxRewardMean;
    }
    public RLlibHelper maxRewardMean(double maxRewardMean) {
        this.maxRewardMean = maxRewardMean;
        return this;
    }

    public int savePolicyInterval() {
        return savePolicyInterval;
    }
    public RLlibHelper savePolicyInterval(int savePolicyInterval) {
        this.savePolicyInterval = savePolicyInterval;
        return this;
    }

    public String redisAddress() {
        return redisAddress;
    }
    public RLlibHelper redisAddress(String redisAddress) {
        this.redisAddress = redisAddress;
        return this;
    }

    public String customParameters() {
        return customParameters;
    }
    public RLlibHelper customParameters(String customParameters) {
        this.customParameters = customParameters;
        return this;
    }

    public boolean isMultiAgent() {
        return multiAgent;
    }
    public RLlibHelper setMultiAgent(boolean multiAgent) {
        this.multiAgent = multiAgent;
        return this;
    }

    public String hiddenLayers() {
        String s = "[";
        for (int i = 0; i < numHiddenLayers; i++) {
            s += numHiddenNodes + (i < numHiddenLayers - 1 ? ", " : "]");
        }
        return s;
    }

    /** Returns an instance of {@link PythonPolicyHelper} based on the parameters from an instance of this class. */
    public PolicyHelper createPythonPolicyHelper() throws IOException {
        return new PythonPolicyHelper(rllibpaths, algorithm, checkpoint, environment);
    }

    /** Calls {@link #generatePythonTrainer()} and writes the result to a File. */
    public void generatePythonTrainer(File file) throws IOException {
        File directory = file.getParentFile();
        if (directory != null) {
            directory.mkdirs();
        }
        Files.write(file.toPath(), generatePythonTrainer().getBytes());
    }

    /** Takes the parameters from an instance of this class, and returns a Python script that uses RLlib for training. */
    public String generatePythonTrainer() {
        if (environment == null) {
            throw new IllegalStateException("Environment is null.");
        }
        String trainer = "import glob, gym, nativerl, numpy, ray, sys, os\n"
            + "from ray.rllib.env import MultiAgentEnv\n"
            + "from ray.rllib.agents.registry import get_agent_class\n"
            + "from ray.rllib.utils import seed\n"
            + "\n"
            + "jardir = os.getcwd()\n"
            + "\n"
            + "class " + environment.getClass().getSimpleName() + "(" + (multiAgent ? "MultiAgentEnv" : "gym.Env") + "):\n"
            + "    def __init__(self, env_config):\n"
            + "        # Put all JAR files found here in the class path\n"
            + "        jars = glob.glob(jardir + '/**/*.jar', recursive=True)\n"
            + "        nativerl.init(['-Djava.class.path=' + os.pathsep.join(jars + [jardir])]);\n"
            + "\n"
            + "        self.nativeEnv = nativerl.createEnvironment('" + environment.getClass().getName() + "')\n"
            + "        actionSpace = self.nativeEnv.getActionSpace()\n"
            + "        observationSpace = self.nativeEnv.getObservationSpace()\n"
            + "        if isinstance(actionSpace, nativerl.Discrete):\n"
            + "            self.action_space = gym.spaces.Discrete(actionSpace.n)\n"
            + "        else:\n"
            + "            self.action_space = gym.spaces.Box(actionSpace.low[0], actionSpace.high[0], numpy.array(actionSpace.shape), dtype=numpy.float32)\n"
            + "        self.observation_space = gym.spaces.Box(observationSpace.low[0], observationSpace.high[0], numpy.array(observationSpace.shape), dtype=numpy.float32)\n"
            + "        self.id = '" + environment.getClass().getSimpleName() + "'\n"
            + "        self.max_episode_steps = " + Integer.MAX_VALUE + "\n"
            + (multiAgent ? "" : "        self.unwrapped.spec = self\n")
            + "    def seed(self, seed):\n"
            + "        self.nativeEnv.setSeed(seed)\n"
            + "    def reset(self):\n"
            + "        self.nativeEnv.reset()\n"
            + (multiAgent
                ? "        obs = numpy.array(self.nativeEnv.getObservation())\n"
                + "        obsdict = {}\n"
                + "        for i in range(0, obs.shape[0]):\n"
                + "            obsdict[str(i)] = obs[i]\n"
                + "        return obsdict\n"

                : "        return numpy.array(self.nativeEnv.getObservation())\n")
            + "    def step(self, action):\n"
            + (multiAgent
                ? "        if isinstance(self.action_space, gym.spaces.Discrete):\n"
                + "            n = 1\n"
                + "        else:\n"
                + "            n = self.action_space.shape[0]\n"
                + "        actionarray = numpy.ndarray(shape=(len(action), n), dtype=numpy.float32)\n"
                + "        for i in range(0, actionarray.shape[0]):\n"
                + "            actionarray[i,:] = action[str(i)].astype(numpy.float32)\n"
                + "        reward = numpy.array(self.nativeEnv.step(nativerl.Array(actionarray)))\n"
                + "        obs = numpy.array(self.nativeEnv.getObservation())\n"
                + "        obsdict = {}\n"
                + "        rewarddict = {}\n"
                + "        for i in range(0, obs.shape[0]):\n"
                + "            obsdict[str(i)] = obs[i]\n"
                + "            rewarddict[str(i)] = reward[i]\n"
                + "        return obsdict, rewarddict, {'__all__' : self.nativeEnv.isDone()}, {}\n"

                : "        if isinstance(self.action_space, gym.spaces.Discrete):\n"
                + "            reward = self.nativeEnv.step(action)\n"
                + "        else:\n"
                + "            reward = self.nativeEnv.step(nativerl.Array(action.astype(numpy.float32)))\n"
                + "        return numpy.array(self.nativeEnv.getObservation()), reward, self.nativeEnv.isDone(), {}\n")
            + "\n"
            + "# Make sure multiple processes can read the database from AnyLogic\n"
            + "with open('database/db.properties', 'r+') as f:\n"
            + "    lines = f.readlines()\n"
            + "    if 'hsqldb.lock_file=false\\n' not in lines:\n"
            + "        f.write('hsqldb.lock_file=false\\n')\n"
            + "\n"
            + "ray.init(" + (redisAddress != null ? "redis_address='" + redisAddress + "'" : "") + ")\n"
            + "seed.seed(" + randomSeed + ")\n"
            + "model = ray.rllib.models.MODEL_DEFAULTS.copy()\n"
            + "model['fcnet_hiddens'] = " + hiddenLayers() + "\n"
            + "trials = ray.tune.run(\n"
            + "    '" + algorithm + "',\n"
            + "    stop={\n"
            + "         'training_iteration': " + maxIterations + ",\n"
            + (Double.isFinite(maxRewardMean) ? "         'episode_reward_mean': " + maxRewardMean + ",\n" : "")
            + "    },\n"
            + "    config={\n"
            + "        'env': " + environment.getClass().getSimpleName() + ",\n"
// 'seed' only available starting with Ray 0.7.3: https://github.com/ray-project/ray/commit/0af07bd4934d6cd0d578683da7a6e1e2917014f6
            + "        'seed': " + randomSeed + ",\n"
            + "        'num_gpus': " + numGPUs + ",\n"
            + "        'num_workers': " + numWorkers + ",\n"
            + "        'gamma': ray.tune.grid_search(" +  Arrays.toString(gammas) + "),\n"
            + (algorithm.contains("DDPG") || algorithm.contains("TD3")
                    ? "        'critic_lr': ray.tune.grid_search(" +  Arrays.toString(learningRates) + "),\n"
                    + "        'actor_lr': ray.tune.function(lambda spec: spec.config.critic_lr),\n"
                    : !algorithm.contains("ES") && !algorithm.contains("ARS")
                            ? "        'lr': ray.tune.grid_search(" +  Arrays.toString(learningRates) + "),\n"
                            : "        # no learning rate\n")
            + "        'train_batch_size': ray.tune.grid_search(" + Arrays.toString(trainBatchSizes) + "),\n"
            + "        'model': model,\n"
            + "        'sample_batch_size': " + sampleBatchSize + "," + customParameters + "\n"
            + "    },\n"
            + (outputDir != null ? "    local_dir='" + outputDir.getAbsolutePath() + "',\n" : "")
            + (checkpoint != null ? "    restore='" + checkpoint.getAbsolutePath() + "',\n" : "")
            + "    checkpoint_freq=" + savePolicyInterval + ",\n"
            + "    export_formats=['model'], # Export TensorFlow SavedModel as well\n"
            + ")\n"
            + "\n"
            + "print('Trials: ', trials)\n"
            + "\n"
            + "# Export all checkpoints to TensorFlow SavedModel as well\n"
            + "cls = get_agent_class('" + algorithm + "')\n"
            + "agent = cls(env=" + environment.getClass().getSimpleName() + ")\n"
            + "checkpoints = glob.glob('" + outputDir.getAbsolutePath() + "/**/checkpoint_*/', recursive=True)\n"
            + "for c in checkpoints:\n"
            + "    i = c[c.rindex('_') + 1 : -1]\n"
            + "    agent.restore(c + '/checkpoint-' + i)\n"
            + "    policy = agent.get_policy()\n"
            + "    policy.export_model(c + '/model-' + i)\n";
        return trainer;
    }

    /** The command line interface of this helper. */
    public static void main(String[] args) throws Exception {
        RLlibHelper helper = new RLlibHelper();
        File output = new File("rllibtrain.py");
        boolean subcombinations = false;
        for (int i = 0; i < args.length; i++) {
            if ("-help".equals(args[i]) || "--help".equals(args[i])) {
                System.out.println("usage: RLlibHelper [options] [output]");
                System.out.println();
                System.out.println("options:");
                System.out.println("    --rllibpaths");
                System.out.println("    --algorithm");
                System.out.println("    --checkpoint");
                System.out.println("    --environment");
                System.out.println("    --num-gpus");
                System.out.println("    --num-workers");
                System.out.println("    --random-seed");
                System.out.println("    --gammas");
                System.out.println("    --learning-rates");
                System.out.println("    --train-batch-sizes");
                System.out.println("    --num-hidden-layers");
                System.out.println("    --num-hidden-nodes");
                System.out.println("    --sample-batch-size");
                System.out.println("    --max-iterations");
                System.out.println("    --max-reward-mean");
                System.out.println("    --save-policy-interval");
                System.out.println("    --redis-address");
                System.out.println("    --custom-parameters");
                System.out.println("    --multi-agent");
                System.out.println("    --subcombinations");
                System.exit(0);
            } else if ("--rllibpaths".equals(args[i])) {
                helper.rllibpaths(args[++i].split(File.pathSeparator));
            } else if ("--algorithm".equals(args[i])) {
                helper.algorithm(args[++i]);
            } else if ("--output-dir".equals(args[i])) {
                helper.outputDir(args[++i]);
            } else if ("--checkpoint".equals(args[i])) {
                helper.checkpoint(args[++i]);
            } else if ("--environment".equals(args[i])) {
                helper.environment(Class.forName(args[++i]).asSubclass(Environment.class).newInstance());
            } else if ("--num-gpus".equals(args[i])) {
                helper.numGPUs(Integer.parseInt(args[++i]));
            } else if ("--num-workers".equals(args[i])) {
                helper.numWorkers(Integer.parseInt(args[++i]));
            } else if ("--random-seed".equals(args[i])) {
                helper.randomSeed(Long.parseLong(args[++i]));
            } else if ("--gammas".equals(args[i])) {
                String[] strings = args[++i].split(",");
                double[] doubles = new double[strings.length];
                for (int j = 0; j < doubles.length; j++) {
                    doubles[j] = Double.parseDouble(strings[j]);
                }
                helper.gammas(doubles);
            } else if ("--learning-rates".equals(args[i])) {
                String[] strings = args[++i].split(",");
                double[] doubles = new double[strings.length];
                for (int j = 0; j < doubles.length; j++) {
                    doubles[j] = Double.parseDouble(strings[j]);
                }
                helper.learningRates(doubles);
            } else if ("--train-batch-sizes".equals(args[i])) {
                String[] strings = args[++i].split(",");
                int[] ints = new int[strings.length];
                for (int j = 0; j < ints.length; j++) {
                    ints[j] = Integer.parseInt(strings[j]);
                }
                helper.trainBatchSizes(ints);
            } else if ("--num-hidden-layers".equals(args[i])) {
                helper.numHiddenLayers(Integer.parseInt(args[++i]));
            } else if ("--num-hidden-nodes".equals(args[i])) {
                helper.numHiddenNodes(Integer.parseInt(args[++i]));
            } else if ("--sample-batch-size".equals(args[i])) {
                helper.sampleBatchSize(Integer.parseInt(args[++i]));
            } else if ("--max-iterations".equals(args[i])) {
                helper.maxIterations(Integer.parseInt(args[++i]));
            } else if ("--max-reward-mean".equals(args[i])) {
                helper.maxRewardMean(Double.parseDouble(args[++i]));
            } else if ("--save-policy-interval".equals(args[i])) {
                helper.savePolicyInterval(Integer.parseInt(args[++i]));
            } else if ("--redis-address".equals(args[i])) {
                helper.redisAddress(args[++i]);
            } else if ("--custom-parameters".equals(args[i])) {
                helper.customParameters(args[++i]);
            } else if ("--multi-agent".equals(args[i])) {
                helper.multiAgent = true;
            } else if ("--subcombinations".equals(args[i])) {
                subcombinations = true;
            } else {
                output = new File(args[i]);
            }
        }
        if (subcombinations) {
            for (RLlibHelper subcombination : helper.createSubcombinations()) {
                System.out.println(subcombination);
            }
        } else {
            helper.generatePythonTrainer(output);
        }
    }
}
