#!/bin/bash

. ./configFile

firstProxyPort=3030
firstPort=8891

files="${federationFile} ${federationPath}/fedBenchFederation.ttl ${fedBenchDataPath}/statsHibiscus.n3 ${fedBenchDataPath}/semagrowMetadata.ttl ${fedBenchDataPath}/*_void.n3 ${federationPath}/splendidFedBenchFederation.n3"


for file in ${files}; do
  for i in `seq 0 8`; do
    address1=localhost
    localProxyPort=$(($firstProxyPort+$i))
    if [ "${numHosts}" -gt "1" ]; then
        address2=${addresses[$i]}
        localPort=$firstPort
    else
        address2=${addresses[0]}
        localPort=$(($firstPort+$i))
    fi

    sed -i "s,${address1}:${localProxyPort},${address2}:${localPort},g" ${file}
  done
done
