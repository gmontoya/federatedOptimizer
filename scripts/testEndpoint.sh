#!/bin/bash

file=/home/roott/federatedOptimizer/lib/fedX3.1/queries4

spids=""
i=0
while read line; do
    q=`echo ${line#QUERY:}`
    #echo $q
    echo $q > tmp${i}
    /home/roott/apache-jena-2.13.0/bin/rsparql --service=http://localhost:8891/sparql  --file tmp${i} > answer${i} &
    pid=$!
    i=$(($i+1))
    spids="$spids $pid"
    if [ "$i" -gt "20" ]; then
        for e in $spids; do
            wait $e
        done
        i=0
        spids=""
        cat answer* >> accAnswer4
        rm answer* tmp*
    fi
done < $file

for e in $spids; do
    wait $e
done
cat answer* >> accAnswer4
rm answer* tmp*
