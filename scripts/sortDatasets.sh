#!/bin/bash

. ./configFile

# sorted by subject?
subj=true
datasets="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB DBpedia Geonames"

cd ${federatedOptimizerPath}/code

if [ "$subj" = "true" ]; then
   s=Sorted
else
   s=ObjSorted
fi

for d in ${datasets}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    dump="${fedBenchDataPath}/${f}.n3"
    n=`wc -l ${dump} | sed 's/^[ ^t]*//' | cut -d' ' -f1`
    java orderDataset ${dump} ${n} ${subj} > ${fedBenchDataPath}/${f}${s}.n3
done
