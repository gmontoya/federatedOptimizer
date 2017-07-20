#!/bin/bash

folder=/home/roott/queries/fedBench
#folder=/home/roott/queries/complexQueries
s=`seq 1 11`
l=""
n=1
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

#s=`seq 1 10`

#for i in ${s}; do
#    l="${l} C${i}"
#done

l="LD1"

for query in ${l}; do
    f=0
    for j in `seq 1 ${n}`; do
        cd /home/roott/semagrow
        queryStr=`tr "\n" " " < ${folder}/${query}`
        /usr/bin/time -f "%e %P %t %M" timeout ${w}s java -Xmx4096m -cp .:http/target/semagrow-http-1.5.1/WEB-INF/lib/* eu.semagrow.cli.CliMain /etc/default/semagrow/repository.ttl "${queryStr}" outputOther.json > outputFileOther 2> timeFileOther
        x=`tail -n 1 timeFileOther`
        y=`echo ${x%% *}`
        x=`echo ${y%%.*}`
        if [ "$x" -ge "$w" ]; then
            t=`echo $y`
            t=`echo "scale=2; $t*1000" | bc`
            f=$(($f+1))
            /home/roott/federatedOptimizer/scripts/fixJSONAnswer.sh outputOther.json
        else
            x=`grep "duration=" outputFileOther`
            y=`echo ${x##*duration=}`
            t=`echo ${y%%ms*}`
        fi
        x=`grep "planning=" outputFileOther`
        y=`echo ${x##*planning=}`
        if [ -n "$y" ]; then
            s=`echo ${y%%ms*}`
        else
            s=-1
        fi
        nr=`python /home/roott/federatedOptimizer/scripts/formatJSONFile.py outputOther.json | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`

        /home/roott/federatedOptimizer/scripts/processSemaGrowPlansNSS.sh outputFileOther > xxx
        nss=`cat xxx`
        /home/roott/federatedOptimizer/scripts/processSemaGrowPlansNSQ.sh outputFileOther > xxx
        ns=`cat xxx`
        rm xxx
        echo "${query} ${nss} ${ns} ${s} ${t} ${nr}"

        if [ "$f" -ge "2" ]; then
            break
        fi
    done
done
