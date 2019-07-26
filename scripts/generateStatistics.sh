#!/bin/bash

. ./configFile

finished=""

toDo="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB Geonames DBpedia"

cd ${federatedOptimizerPath}/code
 
for d in ${toDo}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    echo "generateStatistics"
    /usr/bin/time -f "%e %P %t %M" java generateStatistics ${fedBenchDataPath}/${f}${suffix}.n3 ${fedBenchDataPath}/statistics${d} > outputGenerateStatistics${d} 2> errorGenerateStatistics${d}
    echo "mergeCharacteristicSetsPlus"
    /usr/bin/time -f "%e %P %t %M" java mergeCharacteristicSetsPlus ${fedBenchDataPath}/statistics${d}_css ${fedBenchDataPath}/statistics${d}_css_reduced10000CS 10000 ${fedBenchDataPath}/statistics${d}_cps ${fedBenchDataPath}/statistics${d}_cps_reduced10000CS ${fedBenchDataPath}/statistics${d}_iis ${fedBenchDataPath}/statistics${d}_iis_reduced10000CS ${fedBenchDataPath}/statistics${d}_iio ${fedBenchDataPath}/statistics${d}_iio_reduced10000CS  > outputMergeCharacteristicSetsPlus${d} 2> errorMergeCharacteristicSetsPlus${d}
    files="_css _cps _iis _iio"
    for g in ${files}; do
        if ! [ -a "${fedBenchDataPath}/statistics${d}${g}_reduced10000CS" ]; then   
            ln -s ${fedBenchDataPath}/statistics${d}${g} ${fedBenchDataPath}/statistics${d}${g}_reduced10000CS
        fi
    done
    mv ${fedBenchDataPath}/statistics${d}_cps_reduced10000CS ${fedBenchDataPath}/statistics${d}_cps_reduced10000CS_
    java changeCPS ${fedBenchDataPath}/statistics${d}_cps_reduced10000CS ${fedBenchDataPath}/statistics${d}_cps_reduced10000CS_
    echo "obtainRadixOneBasedIndex"
    /usr/bin/time -f "%e %P %t %M" java obtainRadixOneBasedIndex ${fedBenchDataPath}/statistics${d}_iis_reduced10000CS ${fedBenchDataPath}/statistics${d}_iio_reduced10000CS ${fedBenchDataPath}/statistics${d}_rtree_100_10_reduced10000CS_1 100 10 -1 10
    echo "obtainHierarchicalCharacterization"
    /usr/bin/time -f "%e %P %t %M" java obtainHierarchicalCharacterization ${fedBenchDataPath}/statistics${d}_css_reduced10000CS ${fedBenchDataPath}/statistics${d}_hc_reduced10000CS ${fedBenchDataPath}/statistics${d}_cost_reduced10000CS ${fedBenchDataPath}/statistics${d}_pi_reduced10000CS ${fedBenchDataPath}/statistics${d}_as_reduced10000CS > outputObtainHierarchicalCharacterization${d} 2> errorObtainHierarchicalCharacterization${d}
    for e in ${finished}; do
        echo "addInterDatasetLinksRTreeOne $d $e"
        /usr/bin/time -f "%e %P %t %M" java addInterDatasetLinksRTreeOne ${fedBenchDataPath}/statistics${d}_rtree_100_10_reduced10000CS_1 ${fedBenchDataPath}/statistics${e}_rtree_100_10_reduced10000CS_1 ${fedBenchDataPath}/cps_${d}_${e}_one_rtree_reduced10000CS_1 ${fedBenchDataPath}/cps_${e}_${d}_one_rtree_reduced10000CS_1
        echo "addInterDatasetCSsRTreeOne $d $e"
        /usr/bin/time -f "%e %P %t %M" java addInterDatasetCSsRTreeOne ${fedBenchDataPath}/statistics${d}_rtree_100_10_reduced10000CS_1 ${fedBenchDataPath}/statistics${e}_rtree_100_10_reduced10000CS_1 ${fedBenchDataPath}/css_${d}_${e}_one_rtree_reduced10000CS_1
    done
    finished=`echo "${finished} ${d}"`
    echo "finished with ${d}"
done
