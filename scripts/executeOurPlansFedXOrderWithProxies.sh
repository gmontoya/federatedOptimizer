#!/bin/bash

. ./configFile

sed -i "s,optimize=.*,optimize=false," ${fedXConfigFile}
cold=true

auxFile=`mktemp`
outputFile=`mktemp`
errorFile=`mktemp`

s=`seq 1 11`
l=""
n=${numRuns}
w=${timeoutValue}

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
    for j in `seq 1 ${n}`; do
        cd ${federatedOptimizerPath}/scripts
        #tmpFile=`./startProxies.sh 8891 8899 3030 "ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"`
        tmpFile=`./startProxies2.sh "172.19.2.123 172.19.2.106 172.19.2.100 172.19.2.115 172.19.2.107 172.19.2.118 172.19.2.111 172.19.2.113 172.19.2.120" 3030`
        sleep 1s
        cd ${federatedOptimizerPath}/code
        if [ "$cold" = "true" ] && [ -f ${fedXPath}/cache.db ]; then
            rm ${fedXPath}/cache.db
        fi
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s java -Xmx4096m -cp .:${JENA_HOME}/lib/*:${fedXPath}/lib/* evaluateOurPlansWithFedXOrder ${queriesFolder}/$query ${federationFile} ${fedBenchDataPath} 100000000 true false > ${outputFile} 2> ${errorFile}
        x=`grep "planning=" ${outputFile}`
        y=`echo ${x##*planning=}`

        if [ -n "$y" ]; then
            d=`echo ${y%%ms*}`
        else
            d=-1
        fi

        x=`grep "NumberSelectedSources=" ${outputFile}`
        y=`echo ${x##*NumberSelectedSources=}`

        if [ -n "$y" ]; then
            nss=`echo ${y}`
        else
            ${federatedOptimizerPath}/scripts/processFedXPlansNSS.sh ${outputFile} > ${auxFile}
            nss=`cat ${auxFile}`
            rm ${auxFile}
        fi

        x=`grep "NumberServices=" ${outputFile}`
        y=`echo ${x##*NumberServices=}`

        if [ -n "$y" ]; then
            ns=`echo ${y}`
        else
            ${federatedOptimizerPath}/scripts/processFedXPlansNSQ.sh ${outputFile} > ${auxFile}
            ns=`cat ${auxFile}`
            rm ${auxFile}
        fi

        x=`tail -n 1 ${errorFile}`
        y=`echo ${x%% *}`
        x=`echo ${y%%.*}`
        if [ "$x" -ge "$w" ]; then
            t=`echo $y`
            t=`echo "scale=2; $t*1000" | bc`
            f=$(($f+1))
            nr=`grep "^\[" ${outputFile} | grep "\]$" | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`
        else
            x=`grep "duration=" ${outputFile}`
            y=`echo ${x##*duration=}`
            t=`echo ${y%%ms*}`
            x=`grep "results=" ${outputFile}`
            nr=`echo ${x##*results=}`
        fi
        cd ${federatedOptimizerPath}/scripts
        ./killAll.sh ${proxyFederationFile}
        sleep 10s
        pi=`./processProxyInfo.sh ${tmpFile} 0 8`
        echo "${query} ${nss} ${ns} ${d} ${t} ${pi} ${nr}"
        
        if [ "$f" -ge "2" ]; then
            break
        fi
    done
done

rm -f ${outputFile}
rm -f ${errorFile}
