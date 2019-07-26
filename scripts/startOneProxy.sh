#!/bin/bash

. ./configFile

address=$1
localPort=$2
localProxyPort=$3
tmpFileNR=$4
graph=$5

p=`pwd`

cd ../proxy
#echo "starting proxy at localhost with localProxyPort $localProxyPort, using *${tmpFileNR}* to store output"
java -cp .:${httpcomponentsClientPath}/lib/* SingleEndpointProxy2 ${address} ${localPort} localhost ${localProxyPort} "${graph}" > ${tmpFileNR} &
pidProxy=$!
echo $pidProxy

cd ${p}
