#!/bin/bash

firstPort=$1
lastPort=$2
firstProxyPort=$3
datasets="${4}"

last=$(($lastPort-$firstPort))
tmpFile=`mktemp --tmpdir=/home/roott/tmp`

p=`pwd`
echo "$tmpFile"
i=0
rm  /home/roott/tmp/proxyFederation

for d in ${datasets}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    localPort=$(($firstPort+$i))
    localProxyPort=$(($firstProxyPort+$i))
    graph="http://${f}Endpoint"
    ./startOneProxy.sh ${localPort} ${localProxyPort} ${tmpFile}_$i ${graph} >> /home/roott/tmp/proxyFederation
    i=$(($i+1))
done
