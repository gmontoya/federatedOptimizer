#!/bin/bash

finished=""
toDo="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB DBpedia Geonames"
cd /home/roott/federatedOptimizer/code
for d in ${toDo}; do
    for e in ${finished}; do
        echo "${d}_${e}"
        /usr/bin/time -f "%e %P %t %M" java addInterDatasetCSsRTreeOne /home/roott/fedBenchData/${d}/statistics${d}_rtree_100_10_reduced10000CS_1 /home/roott/fedBenchData/${e}/statistics${e}_rtree_100_10_reduced10000CS_1 /home/roott/fedBenchData/css_${d}_${e}_one_rtree_reduced10000CS_1
        ls -lh /home/roott/fedBenchData/css_${d}_${e}_one_rtree_reduced10000CS_1
        java evaluateCSs /home/roott/fedBenchData/css_${d}_${e}_reduced10000CS /home/roott/fedBenchData/css_${d}_${e}_one_rtree_reduced10000CS_1
    done
    finished=`echo "${finished} ${d}"`
    echo "finished with ${d}"
done
