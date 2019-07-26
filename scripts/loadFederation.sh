#!/bin/bash

. ./configFile

datasets="ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"

export PATH=.:${virtuosoPath}/bin:$PATH
i=1

for d in ${datasets}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    dump="${fedBenchDataPath}/${f}${suffix}.n3"
    mkdir ${virtuosoPath}/var/lib/virtuoso/${f}
    cp -r ${virtuosoPath}/var/lib/virtuoso/db/* ${virtuosoPath}/var/lib/virtuoso/${f}/
    sed -i "s,${virtuosoPath}/var/lib/virtuoso/db/virtuoso,${virtuosoPath}/var/lib/virtuoso/${f}/virtuoso,g" ${virtuosoPath}/var/lib/virtuoso/${f}/virtuoso.ini
    b=8890
    p=$(($b+i))
    sed -i "s,8890,$p,g" ${virtuosoPath}/var/lib/virtuoso/${f}/virtuoso.ini
    b=1111
    p=$(($b+i))
    sed -i "s,1111,$p,g" ${virtuosoPath}/var/lib/virtuoso/${f}/virtuoso.ini
    cd ${virtuosoPath}/var/lib/virtuoso/${f}
    virtuoso-t -f > output 2> error &
    pid=$!
    sleep 1m
    cd ${federatedOptimizerPath}/scripts
    ./loadOneEndpoint.sh ${dump} $f $i
    echo "${f} loaded"
    kill $pid
    sleep 1m
    i=$((i+1))
done
