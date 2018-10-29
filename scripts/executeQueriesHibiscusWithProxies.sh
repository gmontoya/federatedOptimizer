#!/bin/bash

folder=/home/roott/queries/fedBench
FEDX_HIBISCUS_HOME=/home/roott/FedX-HiBISCus
configFileHibiscus=${FEDX_HIBISCUS_HOME}/config.properties
ODYSSEY_HOME=/home/roott/federatedOptimizer
proxyFederationFile=/home/roott/tmp/proxyFederation

cold=true
s=`seq 1 11`
l=""
n=10
w=1800

for i in ${s}; do
    l="${l} LD${i}"
done

s=`seq 1 7`

for i in ${s}; do
    l="${l} CD${i}"
done

for i in ${s}; do
    l="${l} LS${i}"
done

for query in ${l}; do
    f=0
    rm ${FEDX_HIBISCUS_HOME}/cache.db
    for j in `seq 1 ${n}`; do
        cd ${ODYSSEY_HOME}/scripts
        tmpFile=`./startProxies2.sh "172.19.2.123 172.19.2.106 172.19.2.100 172.19.2.115 172.19.2.107 172.19.2.118 172.19.2.111 172.19.2.113 172.19.2.120" 3030`
        sleep 2s
        cd ${FEDX_HIBISCUS_HOME}
        if [ -f ${FEDX_HIBISCUS_HOME}/summaries*?/* ]; then
            rm ${FEDX_HIBISCUS_HOME}/summaries*?/*
        fi
        if [ "$cold" = "true" ] && [ -f cache.db ]; then
            rm cache.db
        fi
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s ./cli.sh -c ${configFileHibiscus} @q ${folder}/${query} > outputFile 2> timeFile
        x=`grep "planning=" outputFile`
        y=`echo ${x##*planning=}`
        if [ -n "$y" ]; then
            s=`echo ${y%%ms*}`
        else
            s=-1
        fi

        ${ODYSSEY_HOME}/scripts/processFedXPlansNSS.sh outputFile > xxx
        nss=`cat xxx`
        ${ODYSSEY_HOME}/scripts/processFedXPlansNSQ.sh outputFile > xxx
        ns=`cat xxx`
        rm xxx

        x=`tail -n 1 timeFile`
        y=`echo ${x%% *}`
        x=`echo ${y%%.*}`
        if [ "$x" -ge "$w" ]; then
            t=`echo $y`
            t=`echo "scale=2; $t*1000" | bc`
            f=$(($f+1))
            nr=`grep "^\[" outputFile | grep "\]$" | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`
        else
            x=`grep "duration=" outputFile`
            y=`echo ${x##*duration=}`
            t=`echo ${y%%ms*}`
            x=`grep "results=" outputFile`
            nr=`echo ${x##*results=}`
        fi
        cd ${ODYSSEY_HOME}/scripts
        ./killAll.sh ${proxyFederationFile}
        sleep 10s
        pi=`./processProxyInfo.sh ${tmpFile} 0 8`

        echo "${query} ${nss} ${ns} ${s} ${t} ${pi} ${nr}"
        if [ "$f" -ge "2" ]; then
            break
        fi
    done
done
