#!/bin/bash

. ./configFile
. ./setFederation

# maximum number of lines per file to sort
m=5000000

# sorted by subject?
subj=true
datasets="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB DBpedia Geonames"
datasets="Geonames"

cd ${federatedOptimizerPath}/code
files=${dumpFolder}/${federation}Data

if [ "$subj" = "true" ]; then
   s=Sorted
else 
   s=ObjSorted
fi 

for d in ${datasets}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    dump="${files}/${d}/${f}.n3"

    n=`wc -l ${dump} | sed 's/^[ ^t]*//' | cut -d' ' -f1`
    if [ "$n" -gt "$m" ]; then
        accFile=`mktemp`
        tmpFile=`mktemp`
        split -l $m ${dump} tmp${d}
        for g in `ls tmp${d}*`; do
            t=`wc -l ${g} | sed 's/^[ ^t]*//' | cut -d' ' -f1`
            java orderDataset ${g} ${t} ${subj} > ${g}_sorted
            rm ${g}
        done
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
        mv  ${accFile} ${files}/${d}/${f}${s}.n3
    else 
        java orderDataset ${dump} ${n} ${subj} > ${files}/${d}/${f}${s}.n3
    fi
done

