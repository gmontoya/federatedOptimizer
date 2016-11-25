#!/bin/bash

datasets="ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"
virtuosoPath=/home/roott/virtuosoInstallation
export PATH=.:${virtuosoPath}/bin:$PATH
export codehome=/home/roott/federatedOptimizer

for d in ${datasets}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    cd ${virtuosoPath}/var/lib/virtuoso/${f}
    virtuoso-t -f > output 2> error &
    pid=$!
    sleep 1m
    echo "${f} ${pid}"
done
