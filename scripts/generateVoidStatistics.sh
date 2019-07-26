#!/bin/bash

. ./configFile

datasets="ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"

for d in ${datasets}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    dump="${fedBenchDataPath}/${f}${suffix}.n3"
    ./generate_void_description.sh ${dump} ${fedBenchDataPath}/${f}_void.n3
done
