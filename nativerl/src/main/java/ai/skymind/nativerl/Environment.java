// Targeted by JavaCPP version 1.5.3: DO NOT EDIT THIS FILE

package ai.skymind.nativerl;

import java.nio.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

import static ai.skymind.nativerl.NativeRL.*;


/**
 * The pure virtual (abstract) interface of a "native" environment. This gets mapped,
 * for example, with JavaCPP and implemented by a Java class. The implementation needs
 * to export functions to create and release Environment objects. In the case of JavaCPP,
 * the createEnvironment() and releaseEnvironment() are available in the generated
 * jniNativeRL.h header file.
 * <p>
 * However, we can just as well implement it in pure C++, which we would do in the case of,
 * for example, ROS or MATLAB Simulink.
 * <p>
 * On the Python side, these functions are picked up by, for example, pybind11 and used
 * to implement Python interfaces of environments, such as gym.Env, for RLlib, etc.
 */
@Namespace("nativerl") @Properties(inherit = ai.skymind.nativerl.NativeRLPresets.class)
public class Environment extends Pointer {
    static { Loader.load(); }
    /** Default native constructor. */
    public Environment() { super((Pointer)null); allocate(); }
    /** Native array allocator. Access with {@link Pointer#position(long)}. */
    public Environment(long size) { super((Pointer)null); allocateArray(size); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public Environment(Pointer p) { super(p); }
    private native void allocate();
    private native void allocateArray(long size);
    @Override public Environment position(long position) {
        return (Environment)super.position(position);
    }

    //    /** Passes a new random seed that should be used for reproducibility. */
    //    virtual void setSeed(long long seed) = 0;
    /** Returns the action Space supported. */
    @Virtual(true) public native @Const Space getActionSpace();
    /** Returns the action mask Space supported. */
    @Virtual(true) public native @Const Space getActionMaskSpace();
    /** Returns the observation Space supported. */
    @Virtual(true) public native @Const Space getObservationSpace();
    /** Returns the current state of the possible actions. */
    @Virtual(true) public native @Const @ByRef Array getActionMask();
    /** Returns the current state of the simulation. */
    @Virtual(true) public native @Const @ByRef Array getObservation();
    /** Indicates when a simulation episode is over. */
    @Virtual(true) public native @Cast("bool") boolean isDone();
    /** Used to reset the simulation, preferably starting a new random sequence. */
    @Virtual(true) public native void reset();
    /** Can be used to run the simulation for a single agent with a discrete action space. */
    @Virtual(true) public native float step(@Const @ByRef Array action);
    /** Can be used to run the simulation for a continuous action space and/or with multiple agents. */
//    virtual const Array& step(const Array& action) = 0;
    /** Returns the last values of observationForReward() */
    @Virtual(true) public native @Const @ByRef Array getMetrics();
}
