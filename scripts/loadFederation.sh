#!/bin/bash

datasets="ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"
virtuosoPath=/home/roott/virtuosoInstallation
dataPath=/home/roott/fedBenchData
export PATH=.:${virtuosoPath}/bin:$PATH
i=1

scriptsPath=`pwd`

for d in ${datasets}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    dump="${dataPath}/${d}/${f}Sorted.n3"
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
    sleep 2m
    cd ${scriptsPath}
    ./loadOneEndpoint.sh ${dump} $f $i
    echo "${f} loaded"
    kill $pid
    sleep 2m
    i=$((i+1))
done
