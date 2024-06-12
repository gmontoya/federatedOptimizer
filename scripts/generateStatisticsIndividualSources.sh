#!/bin/bash

. ./configFile
federation=fedBench
flag=-Xmx48g
toDo="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB Geonames DBpedia"

cd ${federatedOptimizerPath}/code
 
for d in ${toDo}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    echo "generateStatistics"
    /usr/bin/time -f "%e %P %t %M" java ${flag} generateStatistics ${dumpFolder}/${federation}Data/${d}/${f}${suffix}.n3 ${fedBenchDataPath}/statistics${d} > outputGenerateStatistics${d} 2> errorGenerateStatistics${d}
    echo "mergeCharacteristicSetsPlus"
    /usr/bin/time -f "%e %P %t %M" java ${flag} mergeCharacteristicSetsPlus ${fedBenchDataPath}/statistics${d}_css ${fedBenchDataPath}/statistics${d}_css_reduced10000CS 10000 ${fedBenchDataPath}/statistics${d}_cps ${fedBenchDataPath}/statistics${d}_cps_reduced10000CS ${fedBenchDataPath}/statistics${d}_iis ${fedBenchDataPath}/statistics${d}_iis_reduced10000CS ${fedBenchDataPath}/statistics${d}_iio ${fedBenchDataPath}/statistics${d}_iio_reduced10000CS  > outputMergeCharacteristicSetsPlus${d} 2> errorMergeCharacteristicSetsPlus${d}
    files="_css _cps _iis _iio"
    for g in ${files}; do
        if ! [ -a "${fedBenchDataPath}/statistics${d}${g}_reduced10000CS" ]; then   
            mv ${fedBenchDataPath}/statistics${d}${g} ${fedBenchDataPath}/statistics${d}${g}_reduced10000CS
        fi
    done
    mv ${fedBenchDataPath}/statistics${d}_cps_reduced10000CS ${fedBenchDataPath}/statistics${d}_cps_reduced10000CS_
    java ${flag} changeCPS ${fedBenchDataPath}/statistics${d}_cps_reduced10000CS ${fedBenchDataPath}/statistics${d}_cps_reduced10000CS_
    echo "obtainRadixOneBasedIndex"
    /usr/bin/time -f "%e %P %t %M" java ${flag} obtainRadixOneBasedIndex ${fedBenchDataPath}/statistics${d}_iis_reduced10000CS ${fedBenchDataPath}/statistics${d}_iio_reduced10000CS ${fedBenchDataPath}/statistics${d}_rtree_100_10_reduced10000CS_1 100 10 -1 10
   
    rm -f ${fedBenchDataPath}/statistics${d}_cps_reduced10000CS_ 
    rm -f ${fedBenchDataPath}/statistics${d}_css ${fedBenchDataPath}/statistics${d}_cps ${fedBenchDataPath}/statistics${d}_iis ${fedBenchDataPath}/statistics${d}_iio ${fedBenchDataPath}/statistics${d}_iis_reduced10000CS ${fedBenchDataPath}/statistics${d}_iio_reduced10000CS
    echo "finished with ${d}"
done

