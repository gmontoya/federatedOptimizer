#!/bin/bash

. ./configFile

semagrowFederationFile=${federationPath}/repository.ttl

s=`seq 1 11`
l=""

auxFile=`mktemp`
answerFile=`mktemp --suffix=".json"`
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
        cd ${SEMAGROW_HOME}
        queryStr=`tr "\n" " " < ${queriesFolder}/${query}`
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s java -Xmx4096m -cp .:http/target/semagrow-http-1.5.1/WEB-INF/lib/* eu.semagrow.cli.CliMain ${semagrowFederationFile} "${queryStr}" ${answerFile} > ${outputFile} 2> ${errorFile}
        x=`tail -n 1 ${errorFile}`
        y=`echo ${x%% *}`
        x=`echo ${y%%.*}`
        if [ "$x" -ge "$w" ]; then
            t=`echo $y`
            t=`echo "scale=2; $t*1000" | bc`
            f=$(($f+1))
            ${federatedOptimizerPath}/scripts/fixJSONAnswer.sh ${answerFile}
        else
            x=`grep "duration=" ${outputFile}`
            y=`echo ${x##*duration=}`
            t=`echo ${y%%ms*}`
        fi
        x=`grep "planning=" ${outputFile}`
        y=`echo ${x##*planning=}`
        if [ -n "$y" ]; then
            s=`echo ${y%%ms*}`
        else
            s=-1
        fi
        nr=`python ${federatedOptimizerPath}/scripts/formatJSONFile.py ${answerFile} | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`

        ${federatedOptimizerPath}/scripts/processSemaGrowPlansNSS.sh ${outputFile} > ${auxFile}
        nss=`cat ${auxFile}`
        ${federatedOptimizerPath}/scripts/processSemaGrowPlansNSQ.sh ${outputFile} > ${auxFile}
        ns=`cat ${auxFile}`
        rm ${auxFile}
        echo "${query} ${nss} ${ns} ${s} ${t} ${nr}"

        if [ "$f" -ge "2" ]; then
            break
        fi
    done
done

rm -f ${outputFile}
rm -f ${errorFile}
