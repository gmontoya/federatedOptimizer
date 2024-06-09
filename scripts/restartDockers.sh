#!/bin/bash
. ./configFile
. ./setFederation

for i in `seq 1 ${number}`; do
  endpoint=${endpoints[${i}-1]}
  name=${federation}${endpoint}
  docker start ${name}
done

sleep 30

p=`pwd`
cd ${federatedOptimizerPath}/scripts

./clearOtherGraphs.sh 

cd $p

sleep 30
