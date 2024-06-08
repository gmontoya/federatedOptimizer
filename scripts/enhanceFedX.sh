#!/bin/bash
## This script assumes that FedX3.1.zip is in the folder ${federatedOptimizerPath}/lib

. ./configFile

folder=${federatedOptimizerPath}/lib
file=FedX3.1.zip

cd $folder 
mkdir FedX3.1

mv $file FedX3.1/

cd FedX3.1

unzip $file

cp ${federatedOptimizerPath}/engines/fedx/ExclusiveGroup.java $fedXPath/src/com/fluidops/fedx/algebra/
cp ${federatedOptimizerPath}/engines/fedx/FedXConnection.java $fedXPath/src/com/fluidops/fedx/
cp ${federatedOptimizerPath}/engines/fedx/ServiceOptimizer.java $fedXPath/src/com/fluidops/fedx/optimizer/
cp ${federatedOptimizerPath}/engines/fedx/ExclusiveStatement.java $fedXPath/src/com/fluidops/fedx/algebra/
cp ${federatedOptimizerPath}/engines/fedx/Optimizer.java $fedXPath/src/com/fluidops/fedx/optimizer/
cp ${federatedOptimizerPath}/engines/fedx/UnionOptimizer.java $fedXPath/src/com/fluidops/fedx/optimizer/

#next two lines are unnecessary, but there to keep the original files as backup
mkdir $fedXPath/lib_fedx3.1
mv $fedXPath/lib/openrdf-sesame-2.7.3-onejar.jar $fedXPath/lib_fedx3.1/

cp ${federatedOptimizerPath}/engines/fedx/sesame.jar $fedXPath/lib

cp ${federatedOptimizerPath}/engines/fedx/build.xml $fedXPath/

cd $fedXPath

ant jar

chmod 755 cli.sh

