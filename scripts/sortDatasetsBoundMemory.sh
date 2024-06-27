#!/bin/bash

. ./configFile
. ./setFederation

ext=nt
suffix=Data
# maximum number of lines per file to sort
m=10000000

# sorted by subject?
subj=true
#datasets="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB DBpedia Geonames"
#datasets="Geonames"

cd ${federatedOptimizerPath}/code
files=${dumpFolder}/${federation}Data

if [ "$subj" = "true" ]; then
   s=Sorted
else 
   s=ObjSorted
fi 

for i in `seq 1 ${number}`; do
    h=$(($i-1))
    d=${endpoints[${h}]}
    #f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    dump="${files}/${d}/${d}${suffix}.${ext}"
    outputFile="${files}/${d}/${d}${suffix}${s}.${ext}"

    n=`wc -l ${dump} | sed 's/^[ ^t]*//' | cut -d' ' -f1`
    if [ "$n" -gt "$m" ]; then
        accFile=`mktemp`
        tmpFile=`mktemp`
        split -l $m ${dump} /tmp/tmp${d}
        for g in `ls /tmp/tmp${d}*`; do
            t=`wc -l ${g} | sed 's/^[ ^t]*//' | cut -d' ' -f1`
	    echo "java orderDataset ${g} ${t} ${subj} > ${g}_sorted"
            java orderDataset ${g} ${t} ${subj} > ${g}_sorted
            rm ${g}
        done
        started=0
        for g in `ls /tmp/tmp${d}*_sorted`; do
            if [ "$started" -gt "0" ]; then
                echo "java mergeDataset ${accFile} ${g} ${subj} > ${tmpFile}"
		java mergeDataset ${accFile} ${g} ${subj} > ${tmpFile}
                mv ${tmpFile} ${accFile}
                rm ${g}
            else 
                mv ${g} ${accFile}
                started=1
            fi  
        done
        mv  ${accFile} ${outputFile}
    else 
	echo "java orderDataset ${dump} ${n} ${subj} > ${outputFile}"
        java orderDataset ${dump} ${n} ${subj} > ${outputFile}
    fi
done

