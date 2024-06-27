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
# for 4GB available
NumberOfBuffers=340000
MaxDirtyBuffers=250000
mem="4g"
memswap="8g"

ResultSetMaxRows=100000
MaxQueryCostEstimationTime=600
MaxQueryExecutionTime=600

#for federation in ${federations}; do
#federation=fedBench
mkdir -p ${folder}/endpoints

l="3 4 10 11 12"
declare -a passwords=($l)

names="GeoNames DBPedia-Subset LinkedTCGA-E LinkedTCGA-M Affymetrix"
declare -a endpoints=($names)
number=${#endpoints[@]}
#dumpFolder=${folder}/datasets

for i in `seq 1 ${number}`; do
#for i in ${l}; do
    cd ${folder}/endpoints
    #j=$(($i+$suffix))
    h=$(($i-1))
    endpoint=${endpoints[${h}]}
    p=${passwords[${h}]} ##
    j=$(($p+1)) ##
    name=${federation}${endpoint}
    pw=passwordep${p} ##h
    portA=$((1111+$j)) ##i
    portB=$((8890+$j)) ##i
    rm -rf ${name}
    mkdir ${name}
    cd ${name}
    docker create \
      --name ${name} \
      --cpus="1" \
      --memory=${mem} \
      --memory-swap=${memswap} \
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

