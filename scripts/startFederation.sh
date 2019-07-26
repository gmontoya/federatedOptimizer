#!/bin/bash

. ./configFile

datasets="ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"

export PATH=.:${virtuosoPath}/bin:$PATH

for d in ${datasets}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    cd ${virtuosoPath}/var/lib/virtuoso/${f}
    virtuoso-t -f > output 2> error &
    pid=$!
    sleep 30s
    echo "${pid}"
done
