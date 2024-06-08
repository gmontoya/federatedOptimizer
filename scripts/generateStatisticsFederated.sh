#!/bin/bash

. ./configFile

finished=""

toDo="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB Geonames DBpedia"

cd ${federatedOptimizerPath}/code
 
for d in ${toDo}; do
    for e in ${finished}; do
        echo "addInterDatasetLinksRTreeOne $d $e"
        /usr/bin/time -f "%e %P %t %M" java addInterDatasetLinksRTreeOne ${fedBenchDataPath}/statistics${d}_rtree_100_10_reduced10000CS_1 ${fedBenchDataPath}/statistics${e}_rtree_100_10_reduced10000CS_1 ${fedBenchDataPath}/cps_${d}_${e}_one_rtree_reduced10000CS_1 ${fedBenchDataPath}/cps_${e}_${d}_one_rtree_reduced10000CS_1
        #echo "addInterDatasetCSsRTreeOne $d $e"
        #/usr/bin/time -f "%e %P %t %M" java addInterDatasetCSsRTreeOne ${fedBenchDataPath}/statistics${d}_rtree_100_10_reduced10000CS_1 ${fedBenchDataPath}/statistics${e}_rtree_100_10_reduced10000CS_1 ${fedBenchDataPath}/css_${d}_${e}_one_rtree_reduced10000CS_1
    done
    finished=`echo "${finished} ${d}"`
    echo "finished with ${d}"
done
