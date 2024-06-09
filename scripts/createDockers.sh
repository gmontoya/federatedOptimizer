#!/bin/bash

. ./configFile
. ./setFederation

# for 16GB available
NumberOfBuffers=1360000
MaxDirtyBuffers=1000000
mem="16g"
# for 2GB available
NumberOfBuffers=170000
MaxDirtyBuffers=130000
mem="2g"

ResultSetMaxRows=100000
MaxQueryCostEstimationTime=600
MaxQueryExecutionTime=600

#for federation in ${federations}; do
#federation=fedBench
mkdir -p ${folder}/endpoints

for i in `seq 1 ${number}`; do
    cd ${folder}/endpoints
    #j=$(($i+$suffix))
    h=$(($i-1))
    endpoint=${endpoints[${h}]}
    name=${federation}${endpoint}
    pw=passwordep${h}
    portA=$((1111+$i))
    portB=$((8890+$i))
    rm -rf ${name}
    mkdir ${name}
    cd ${name}
    docker create \
      --name ${name} \
      --cpus="1" \
      --memory=${mem} \
      --env DBA_PASSWORD=${pw} \
      --env VIRT_Parameters_DirsAllowed="., ../vad, /inputFiles" \
      --env VIRT_Parameters_NumberOfBuffers=${NumberOfBuffers} \
      --env VIRT_Parameters_MaxDirtyBuffers=${MaxDirtyBuffers} \
      --env VIRT_SPARQL_ResultSetMaxRows=${ResultSetMaxRows} \
      --env VIRT_SPARQL_MaxQueryCostEstimationTime=${MaxQueryCostEstimationTime} \
      --env VIRT_SPARQL_MaxQueryExecutionTime=${MaxQueryExecutionTime} \
      --publish ${portA}:1111 \
      --publish ${portB}:8890 \
      --volume `pwd`:/database \
      --volume ${dumpFolder}/${federation}Data/${endpoint}:/inputFiles \
          openlink/virtuoso-opensource-7:latest &
    PID=$!
    echo $PID
    sleep 10
done
#done

