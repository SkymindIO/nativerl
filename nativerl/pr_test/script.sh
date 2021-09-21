set -eo pipefail ;
aws s3 cp s3://${ENVIRONMENT}-training-static-files.pathmind.com/jdk/8_222/OpenJDK8U-jdk_x64_linux_hotspot_8u222b10.tar.gz OpenJDK8U-jdk_x64_linux_hotspot_8u222b10.tar.gz > /dev/null ;
aws s3 cp s3://${ENVIRONMENT}-training-static-files.pathmind.com/conda/0_8_7/rllibpack.tar.gz rllibpack.tar.gz > /dev/null ;
aws s3 cp s3://${ENVIRONMENT}-training-static-files.pathmind.com/nativerl/1_7_1/nativerl-1.7.1-SNAPSHOT-bin.zip nativerl-1.7.1-SNAPSHOT-bin.zip > /dev/null ;
aws s3 cp s3://${ENVIRONMENT}-training-static-files.pathmind.com/anylogic/8_6_1/baseEnv.zip baseEnv.zip > /dev/null ;
aws s3 cp s3://${ENVIRONMENT}-training-static-files.pathmind.com/pathmindhelper/1_2_0/PathmindPolicy.jar PathmindPolicy.jar > /dev/null ;
aws s3 cp s3://dh-training-dynamic-files.pathmind.com/model_file/386 model.zip > /dev/null ;
tar xf OpenJDK8U-jdk_x64_linux_hotspot_8u222b10.tar.gz > /dev/null ;
rm -rf OpenJDK8U-jdk_x64_linux_hotspot_8u222b10.tar.gz ;
export JAVA_HOME=`pwd`/jdk8u222-b10 ;
export JDK_HOME=$JAVA_HOME ;
export JRE_HOME=$JAVA_HOME/jre ;
export PATH=$JAVA_HOME/bin:$PATH ;
export LD_LIBRARY_PATH=$JAVA_HOME/jre/lib/amd64/server:$JAVA_HOME/jre/lib/amd64/:$LD_LIBRARY_PATH ;
mkdir -p conda ;
cd conda ;
tar xf ../rllibpack.tar.gz > /dev/null ;
rm ../rllibpack.tar.gz ;
source bin/activate ;
cd .. ;
mkdir -p work ;
cd work ;
unzip ../nativerl-1.7.1-SNAPSHOT-bin.zip > /dev/null ;
rm ../nativerl-1.7.1-SNAPSHOT-bin.zip ;
mv nativerl-bin/* . ;
mv examples/train.sh . ;
cd .. ;
unzip baseEnv.zip > /dev/null ;
rm baseEnv.zip ;
mv baseEnv/* work/ ;
rm -r baseEnv ;
mv PathmindPolicy.jar work/lib/ ;
cd work ;
unzip ../model.zip > /dev/null ;
rm ../model.zip ;
export CLASS_SNIPPET='' ;
export RESET_SNIPPET='' ;
export REWARD_SNIPPET='reward += after.fuelRemaining - before.fuelRemaining;
reward += Math.abs(before.distanceToX) - Math.abs(after.distanceToX);
reward += Math.abs(before.distanceToY) - Math.abs(after.distanceToY);
reward += before.distanceToZ - after.distanceToZ;

reward += after.landed == 1 ? 3 : 0;
reward -= after.crashed == 1 ? 0.3 : 0;
reward -= after.gotAway == 1 ? 1 : 0;

reward -= before.distanceToZ <= 100. / 1500. && Math.abs(after.speedX) > 200 ? 0.01 : 0;
reward -= before.distanceToZ <= 100. / 1500. && Math.abs(after.speedY) > 200 ? 0.01 : 0;
reward -= before.distanceToZ <= 100. / 1500. && Math.abs(after.speedZ) > 200 ? 0.01 : 0;' ;
export OBSERVATION_SNIPPET='out = new double[9];
out[0] = in.powerXYZ[0];
out[1] = in.powerXYZ[1];
out[2] = in.powerXYZ[2];
out[3] = in.moduleXYZ[0];
out[4] = in.moduleXYZ[1];
out[5] = in.moduleXYZ[2];
out[6] = in.distanceXYZ[0];
out[7] = in.distanceXYZ[1];
out[8] = in.distanceXYZ[2];' ;
export METRICS_SNIPPET='' ;
export MAX_ITERATIONS='500' ;
export TEST_ITERATIONS='0' ;
export MAX_TIME_IN_SEC='43200' ;
export NUM_SAMPLES='4' ;
export MULTIAGENT='false' ;
export RESUME=${RESUME:='false'} ;
export CHECKPOINT_FREQUENCY='50' ;
export EPISODE_REWARD_RANGE='0.01' ;
export ENTROPY_SLOPE='0.01' ;
export VF_LOSS_RANGE='0.1' ;
export VALUE_PRED='1' ;
export USER_LOG='false' ;
export DEBUGMETRICS='true' ;
export NAMED_VARIABLE='true' ;
echo > setup.sh ;
mkdir -p database ;
touch database/db.properties ;
source train.sh ;
exit $?
