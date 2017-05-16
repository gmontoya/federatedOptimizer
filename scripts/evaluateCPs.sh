#!/bin/bash

finished=""
toDo="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB"

cd ../code
for d in ${toDo}; do
    for e in ${finished}; do
        echo "${d}_${e}"
        java evaluateCPs /home/roott/fedBenchData/cps_${d}_${e}_reduced10000CS /home/roott/fedBenchData/cps_${d}_${e}_one_rtree_reduced10000CS
        java evaluateCPs /home/roott/fedBenchData/cps_${e}_${d}_reduced10000CS /home/roott/fedBenchData/cps_${e}_${d}_one_rtree_reduced10000CS
    done
    finished=`echo "${finished} ${d}"`
    echo "finished with ${d}"
done
