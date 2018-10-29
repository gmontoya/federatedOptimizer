#!/bin/bash

fedBenchDataPath=/home/roott/fedbBenchData
datasets="ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"

for d in ${datasets}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    dump="${fedBenchDataPath}/${d}/${f}Sorted.n3"
    ./generate_void_description.sh ${dump} ${fedBenchDataPath}/${d}/${f}_void.n3
done
