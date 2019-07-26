. ./configFile

cd ${federatedOptimizerPath}/code
javac -cp .:${JENA_HOME}/lib/*:${fedXPath}/lib/* *.java

cd ${federatedOptimizerPath}/proxy
javac -cp .:${httpcomponentsClientPath}/lib/* SingleEndpointProxy2.java
