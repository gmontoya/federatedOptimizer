#!/bin/bash

localPort=$1
localProxyPort=$2
tmpFileNR=$3
graph=$4

p=`pwd`

cd ../proxy
#echo "starting proxy at address $address with localport $localPort and localproxyport $localProxyPort, using *${tmpFileNR}* to store output"
java -cp .:/home/roott/federatedOptimizer/proxy/httpcomponents-client-4.5.3/lib/* SingleEndpointProxy2 localhost ${localPort} localhost ${localProxyPort} "${graph}" > ${tmpFileNR} &
pidProxy=$!
echo $pidProxy

cd ${p}
