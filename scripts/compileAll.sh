. ./configFile

cd ${federatedOptimizerPath}/code
rm *.class
javac -cp .:${JENA_HOME}/lib/*:${fedXPath}/lib/* *.java

cd ${federatedOptimizerPath}/proxy
rm *.class
javac -cp .:${httpcomponentsClientPath}/lib/* SingleEndpointProxy2.java
