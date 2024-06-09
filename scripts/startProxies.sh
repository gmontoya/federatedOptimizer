#!/bin/bash

. ./configFile

firstPort=$1
firstProxyPort=$2
datasets="${3}"

tmpFile=`mktemp`

p=`pwd`
echo "$tmpFile"
i=0

rm ${proxyFederationFile}

for d in ${datasets}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    localProxyPort=$(($firstProxyPort+$i))
    graph="http://fedBench${d}"
    if [ "${numHosts}" -gt "1" ]; then
        a=${addresses[$i]}
        localPort=$firstPort
    else 
        a=${addresses[0]}
        localPort=$(($firstPort+$i))
    fi
    ./startOneProxy.sh ${a} ${localPort} ${localProxyPort} ${tmpFile}_$i ${graph} >> ${proxyFederationFile}
    i=$(($i+1))
done
