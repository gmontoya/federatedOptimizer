!/bin/bash

. ./configFile
. ./setFederation

p=`pwd`

mkdir -p ${dumpFolder}/${federation}Data

cd ${dumpFolder}/files/${federation}Data/


for i in `seq 1 ${number}`; do
    endpoint=${endpoints[${i}-1]}
    cd $endpoint
    f=`pwd`
    mkdir -p ${dumpFolder}/${federation}Data/${endpoint}
    ${federatedOptimizerPath}/scripts/preprocessFolder.sh ${f} ${dumpFolder}/${federation}Data/${endpoint}/${endpoint}Data.nt
    cd ..
    rm -r ${f}
done
cd ${p}
