#!/bin/bash

m=5000000
subj=false
#datasets="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB DBpedia Geonames"
datasets="Geonames"
export codehome=/home/roott/federatedOptimizer
cd ${codehome}/code

for d in ${datasets}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    dump="/home/roott/fedBenchData/${d}/${f}Sorted.n3"
    n=45000000
    #n=`wc -l ${dump} | sed 's/^[ ^t]*//' | cut -d' ' -f1`
    if [ "$n" -gt "$m" ]; then
        accFile=`mktemp`
        tmpFile=`mktemp`
        #split -l $m ${dump} tmp${d}
        #for g in `ls tmp${d}*`; do
        #    t=`wc -l ${g} | sed 's/^[ ^t]*//' | cut -d' ' -f1`
        #    java orderDataset ${g} ${t} ${subj} > ${g}_sorted
        #    rm ${g}
        #done
        started=0
        for g in `ls tmp${d}*_sorted`; do
            if [ "$started" -gt "0" ]; then
                java mergeDataset ${accFile} ${g} ${subj} > ${tmpFile}
                mv ${tmpFile} ${accFile}
                rm ${g}
            else 
                mv ${g} ${accFile}
                started=1
            fi  
        done
        mv  ${accFile} /home/roott/fedBenchData/${d}/${f}ObjSorted.n3
    else 
        java orderDataset ${dump} ${n} ${subj} > /home/roott/fedBenchData/${d}/${f}ObjSorted.n3
    fi
done
