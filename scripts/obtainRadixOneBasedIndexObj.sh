#!/bin/bash

. ./configFile

finished=""

toDo="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB Geonames DBpedia"

cd ${federatedOptimizerPath}/code

for d in ${toDo}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    /usr/bin/time -f "%e %P %t %M" java obtainRadixOneBasedIndex ${fedBenchDataPath}/statistics${d}_iio_obj_reduced10000CS ${fedBenchDataPath}/statistics${d}_iis_obj_reduced10000CS ${fedBenchDataPath}/statistics${d}_rtree_obj_100_10_reduced10000CS_1 100 10 -1 10
    ls -lh ${fedBenchDataPath}/statistics${d}_rtree_obj_100_10_reduced10000CS_1

    for e in ${finished}; do
        /usr/bin/time -f "%e %P %t %M" java addInterDatasetLinksRTreeOne ${fedBenchDataPath}/statistics${d}_rtree_obj_100_10_reduced10000CS_1 ${fedBenchDataPath}/statistics${e}_rtree_obj_100_10_reduced10000CS_1 ${fedBenchDataPath}/cps_${d}_${e}_one_rtree_obj_reduced10000CS_1 ${fedBenchDataPath}/cps_${e}_${d}_one_rtree_obj_reduced10000CS_1
        ls -lh ${fedBenchDataPath}/cps_${d}_${e}_one_rtree_obj_reduced10000CS_1 ${fedBenchDataPath}/cps_${e}_${d}_one_rtree_obj_reduced10000CS_1
    done
    finished=`echo "${finished} ${d}"`
    echo "finished with ${d}"
done
