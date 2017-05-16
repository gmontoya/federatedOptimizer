#!/bin/bash

datasets="ChEBI KEGG Drugbank Geonames DBpedia Jamendo NYTimes SWDF LMDB"
port=3100
host=http://172.19.2.112
folder=/home/roott/fedBenchData

cd /home/roott/fuseki/jena-fuseki-1.1.1
for d in ${datasets}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    ./fuseki-server --port=${port} --update --loc=$folder/${d}/${f}TDB /ds > outputFederationEndpoint${port} &
    pidFE=$!
    sleep 10
    #./s-put ${host}:${port}/ds/data default $folder/${d}/${f}ObjSorted.n3
    echo "${d} $pidFE"
    port=$(($port+1))
done
