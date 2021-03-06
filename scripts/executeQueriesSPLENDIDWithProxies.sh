#!/bin/bash

. ./configFile

splendidFederationFile=${federationPath}/splendidFedBenchFederation.n3

s=`seq 1 11`
l=""

tmpFile=`mktemp`
outputFile=`mktemp`
errorFile=`mktemp`

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
        tmpFile=`./startProxies.sh 8891 3030 "ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"`
        sleep 10s

        cd ${SPLENDID_HOME}
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s ./SPLENDID.sh ${splendidFederationFile} ${queriesFolder}/${query} > ${outputFile} 2> ${errorFile}

        x=`tail -n 1 ${errorFile}`
        y=`echo ${x%% *}`
        x=`echo ${y%%.*}`
        if [ "$x" -ge "$w" ]; then
            t=`echo $y`
            t=`echo "scale=2; $t*1000" | bc`
            f=$(($f+1))
            ${federatedOptimizerPath}/scripts/fixJSONAnswer.sh ${outputFile}
        else
            x=`grep "duration=" ${errorFile}`
            y=`echo ${x##*duration=}`
            t=`echo ${y%%ms*}`
        fi
        x=`grep "planning=" ${errorFile}`
        y=`echo ${x##*planning=}`

        if [ -n "$y" ]; then
            s=`echo ${y%%ms*}`
        else
            s=-1
        fi

        nr=`python ${federatedOptimizerPath}/scripts/formatJSONFile.py ${outputFile} | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`

        auxFile=`mktemp`
        ${federatedOptimizerPath}/scripts/processPlansSplendidNSS.sh ${errorFile} > ${auxFile}
        nss=`cat ${auxFile}`
        ${federatedOptimizerPath}/scripts/processPlansSplendidNSQ.sh ${errorFile} > ${auxFile}
        ns=`cat ${auxFile}`
        rm ${auxFile}

        cd ${federatedOptimizerPath}/scripts
        ./killAll.sh ${proxyFederationFile}
        sleep 10s
        pi=`./processProxyInfo.sh ${tmpFile} 0 8`

        echo "${query} ${nss} ${ns} ${s} ${t} ${pi} ${nr}"
        if [ "$f" -ge "2" ]; then
            break
        fi
    done
done

#echo ${tmpFile}
#echo ${outputFile}
#echo ${errorFile} 

rm -f ${tmpFile}*
rm -f ${outputFile}
rm -f ${errorFile}
