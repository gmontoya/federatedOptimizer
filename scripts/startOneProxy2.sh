#!/bin/bash

address=$1
localPort=$2
localProxyPort=$3
tmpFileNR=$4

p=`pwd`

cd ../proxy
#echo "starting proxy at address $address with localport $localPort and localproxyport $localProxyPort, using *${tmpFileNR}* to store output"
java -cp .:${ODYSSEY_HOME}/proxy/httpcomponents-client-4.5.3/lib/* SingleEndpointProxy2 ${address} ${localPort} localhost ${localProxyPort} > ${tmpFileNR} &
pidProxy=$!
echo $pidProxy

cd ${p}
