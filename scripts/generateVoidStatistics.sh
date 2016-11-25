#!/bin/bash

#datasets="ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"
datasets="Jamendo NYTimes SWDF LMDB DBpedia Geonames"
for d in ${datasets}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    dump="/home/roott/fedBenchData/${d}/${f}Sorted.n3"
    ./generate_void_description.sh ${dump} /home/roott/fedBenchData/${d}/${f}_void.n3
done
