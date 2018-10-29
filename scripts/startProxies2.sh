#!/bin/bash

proxyFederationFile=/home/roott/tmp/proxyFederation

addresses="${1}"
firstProxyPort=$2
localPort=8890

#last=$(($lastPort-$firstPort))
tmpFile=`mktemp`

p=`pwd`
echo "$tmpFile"
i=0
rm  ${proxyFederationFile}

for a in ${addresses}; do
    #f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    #localPort=$(($firstPort+$i))
    localProxyPort=$(($firstProxyPort+$i))
    #graph="http://${f}Endpoint"
    ./startOneProxy2.sh ${a} ${localPort} ${localProxyPort} ${tmpFile}_$i >> ${proxyFederationFile} 
    i=$(($i+1))
done
