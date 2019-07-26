#!/bin/bash

. ./configFile

finished=""
toDo="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB DBpedia Geonames"
cd ${federatedOptimizerPath}/code
for d in ${toDo}; do
    for e in ${finished}; do
        echo "${d}_${e}"
        /usr/bin/time -f "%e %P %t %M" java addInterDatasetCSsRTreeOne ${fedBenchDataPath}/statistics${d}_rtree_100_10_reduced10000CS_1 ${fedBenchDataPath}/statistics${e}_rtree_100_10_reduced10000CS_1 ${fedBenchDataPath}/css_${d}_${e}_one_rtree_reduced10000CS_1
        ## use to check if they are correctly generated
        ##ls -lh ${fedBenchDataPath}/css_${d}_${e}_one_rtree_reduced10000CS_1
        ##java evaluateCSs ${fedBenchDataPath}/css_${d}_${e}_reduced10000CS ${fedBenchDataPath}/css_${d}_${e}_one_rtree_reduced10000CS_1
    done
    finished=`echo "${finished} ${d}"`
    echo "finished with ${d}"
done

