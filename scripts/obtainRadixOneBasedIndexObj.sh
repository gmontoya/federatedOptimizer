#!/bin/bash

finished=""

toDo="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB Geonames DBpedia"

cd /home/roott/federatedOptimizer/code

for d in ${toDo}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    /usr/bin/time -f "%e %P %t %M" java obtainRadixOneBasedIndex /home/roott/fedBenchData/${d}/statistics${d}_iio_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_iis_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_rtree_obj_100_10_reduced10000CS_1 100 10 -1 10
    ls -lh /home/roott/fedBenchData/${d}/statistics${d}_rtree_obj_100_10_reduced10000CS_1

    for e in ${finished}; do
        /usr/bin/time -f "%e %P %t %M" java addInterDatasetLinksRTreeOne /home/roott/fedBenchData/${d}/statistics${d}_rtree_obj_100_10_reduced10000CS_1 /home/roott/fedBenchData/${e}/statistics${e}_rtree_obj_100_10_reduced10000CS_1 /home/roott/fedBenchData/cps_${d}_${e}_one_rtree_obj_reduced10000CS_1 /home/roott/fedBenchData/cps_${e}_${d}_one_rtree_obj_reduced10000CS_1
        ls -lh /home/roott/fedBenchData/cps_${d}_${e}_one_rtree_obj_reduced10000CS_1 /home/roott/fedBenchData/cps_${e}_${d}_one_rtree_obj_reduced10000CS_1
        #java evaluateCPs /home/roott/fedBenchData/cps_${d}_${e}_obj_reduced10000CS /home/roott/fedBenchData/cps_${d}_${e}_one_rtree_obj_reduced10000CS_1
        #java evaluateCPs /home/roott/fedBenchData/cps_${e}_${d}_obj_reduced10000CS /home/roott/fedBenchData/cps_${e}_${d}_one_rtree_obj_reduced10000CS_1
    done
    finished=`echo "${finished} ${d}"`
    echo "finished with ${d}"
done
