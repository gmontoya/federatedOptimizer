#!/bin/bash

. ./configFile

firstPort=8891

files="${federationFile} ${federationPath}/fedBenchFederation.ttl ${fedBenchDataPath}/statsHibiscus.n3 ${fedBenchDataPath}/semagrowMetadata.ttl ${fedBenchDataPath}/*_void.n3 ${federationPath}/splendidFedBenchFederation.n3"


for file in ${files}; do
  for i in `seq 0 8`; do
    j=$(($i+1))
    address2=HOST$j
    port2=PORT$j
    if [ "${numHosts}" -gt "1" ]; then
        address1=${addresses[$i]}
        localPort=$firstPort
    else
        address1=${addresses[0]}
        localPort=$(($firstPort+$i))
    fi

    sed -i "s,${address1}:${localPort},${address2}:${port2},g" ${file}
  done
done
