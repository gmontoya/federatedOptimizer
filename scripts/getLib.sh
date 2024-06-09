#!/bin/bash

. ./configFile

mkdir -p ${federatedOptimizerPath}/lib
cd ${federatedOptimizerPath}/lib/
wget https://archive.apache.org/dist/jena/binaries/apache-jena-2.13.0.tar.gz
gunzip apache-jena-2.13.0.tar.gz
tar xvf apache-jena-2.13.0.tar

wget https://archive.apache.org/dist/httpcomponents/httpclient/binary/httpcomponents-client-4.5.3-bin.tar.gz
gunzip httpcomponents-client-4.5.3-bin.tar.gz
tar xvf httpcomponents-client-4.5.3-bin.tar

# hibiscus
cd ${federatedOptimizerPath}/lib
git clone https://github.com/AKSW/HiBISCuS.git
cd HiBISCuS
unzip FedX-HiBISCus.zip

cp ${federatedOptimizerPath}/engines/hibiscus/CLI.java  ${federatedOptimizerPath}/lib/HiBISCuS/FedX-HiBISCus/src/com/fluidops/fedx/
cp ${federatedOptimizerPath}/engines/hibiscus/FedXConnection.java ${federatedOptimizerPath}/lib/HiBISCuS/FedX-HiBISCus/src/com/fluidops/fedx/
cp ${federatedOptimizerPath}/engines/hibiscus/Optimizer.java  ${federatedOptimizerPath}/lib/HiBISCuS/FedX-HiBISCus/src/com/fluidops/fedx/optimizer/
cp ${federatedOptimizerPath}/engines/hibiscus/SourceSelection.java ${federatedOptimizerPath}/lib/HiBISCuS/FedX-HiBISCus/src/com/fluidops/fedx/optimizer/

cp ${federatedOptimizerPath}/engines/fedx/build.xml ${federatedOptimizerPath}/lib/HiBISCuS/FedX-HiBISCus/

cd ${federatedOptimizerPath}/lib/HiBISCuS/FedX-HiBISCus/

ant jar
chmod 755 cli.sh

#semagrow
cd ${federatedOptimizerPath}/lib
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export PATH=$PATH:$JAVA_HOME/bin

git clone https://github.com/semagrow/semagrow.git
git checkout a3f0de4
cp ${federatedOptimizerPath}/engines/semagrow/CliMain.java ${federatedOptimizerPath}/lib/semagrow/sail/src/main/java/eu/semagrow/cli/
cp ${federatedOptimizerPath}/engines/semagrow/SemagrowSailConnection.java ${federatedOptimizerPath}/lib/semagrow/sail/src/main/java/eu/semagrow/sail/

cd ${federatedOptimizerPath}/lib/semagrow/

mvn clean package -Dmaven.test.skip

#splendid
cd ${federatedOptimizerPath}/lib
git clone https://github.com/goerlitz/rdffederator.git

cp ${federatedOptimizerPath}/engines/splendid/SPLENDID.java ${federatedOptimizerPath}/lib/rdffederator/src/de/uni_koblenz/west/splendid/
cp ${federatedOptimizerPath}/engines/splendid/FederationSailConnection.java  ${federatedOptimizerPath}/lib/rdffederator/src/de/uni_koblenz/west/splendid/
cp ${federatedOptimizerPath}/engines/splendid/build.xml  ${federatedOptimizerPath}/lib/rdffederator/

cd ${federatedOptimizerPath}/lib/rdffederator/
ant jar

