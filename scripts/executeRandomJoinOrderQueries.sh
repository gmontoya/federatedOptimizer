#!/bin/bash

sed -i "s,optimize=.*,optimize=false," /home/roott/fedX3.1/config2
cp /home/roott/fedBenchFederationVirtuoso.ttl /home/roott/fedBenchFederation.ttl
randomQueries=/home/roott/queries/fedBench_1_1_random
w=1800

for query in `ls $randomQueries/*`; do
    q=`echo ${query%_*}`
    q=`echo ${q##*/}`
    cd /home/roott/fedX3.1
    /usr/bin/time -f "%e %P %t %M" timeout 1800s ./cli.sh -c config2 -d /home/roott/fedBenchFederation.ttl @q ${query} > outputFile 2> timeFile

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
    echo "${q} ${t} ${nr}"
done
