#!/bin/bash

. ./configFile
. ./setFederation
#federation=fedBench
flag=-Xmx48g
#toDo="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB Geonames DBpedia"

ext=nt
plus=Data${suffix}.${ext}

cd ${federatedOptimizerPath}/code
 
for i in `seq 1 ${number}`; do
    h=$(($i-1))
    d=${endpoints[${h}]}
    #f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    echo "generateStatistics"
    echo "/usr/bin/time -f "%e %P %t %M" java ${flag} generateStatistics ${dumpFolder}/${federation}Data/${d}/${d}${plus} ${odysseyStatsPath}/statistics${d} > outputGenerateStatistics${d} 2> errorGenerateStatistics${d}"
    break
    echo "mergeCharacteristicSetsPlus"
    /usr/bin/time -f "%e %P %t %M" java ${flag} mergeCharacteristicSetsPlus ${odysseyStatsPath}/statistics${d}_css ${odysseyStatsPath}/statistics${d}_css_reduced10000CS 10000 ${odysseyStatsPath}/statistics${d}_cps ${odysseyStatsPath}/statistics${d}_cps_reduced10000CS ${odysseyStatsPath}/statistics${d}_iis ${odysseyStatsPath}/statistics${d}_iis_reduced10000CS ${odysseyStatsPath}/statistics${d}_iio ${odysseyStatsPath}/statistics${d}_iio_reduced10000CS  > outputMergeCharacteristicSetsPlus${d} 2> errorMergeCharacteristicSetsPlus${d}
    files="_css _cps _iis _iio"
    for g in ${files}; do
        if ! [ -a "${odysseyStatsPath}/statistics${d}${g}_reduced10000CS" ]; then   
            mv ${odysseyStatsPath}/statistics${d}${g} ${odysseyStatsPath}/statistics${d}${g}_reduced10000CS
        fi
    done
    mv ${odysseyStatsPath}/statistics${d}_cps_reduced10000CS ${odysseyStatsPath}/statistics${d}_cps_reduced10000CS_
    java ${flag} changeCPS ${odysseyStatsPath}/statistics${d}_cps_reduced10000CS ${odysseyStatsPath}/statistics${d}_cps_reduced10000CS_
    echo "obtainRadixOneBasedIndex"
    /usr/bin/time -f "%e %P %t %M" java ${flag} obtainRadixOneBasedIndex ${odysseyStatsPath}/statistics${d}_iis_reduced10000CS ${odysseyStatsPath}/statistics${d}_iio_reduced10000CS ${odysseyStatsPath}/statistics${d}_rtree_100_10_reduced10000CS_1 100 10 -1 10
   
    rm -f ${odysseyStatsPath}/statistics${d}_cps_reduced10000CS_ 
    rm -f ${odysseyStatsPath}/statistics${d}_css ${odysseyStatsPath}/statistics${d}_cps ${odysseyStatsPath}/statistics${d}_iis ${odysseyStatsPath}/statistics${d}_iio ${odysseyStatsPath}/statistics${d}_iis_reduced10000CS ${odysseyStatsPath}/statistics${d}_iio_reduced10000CS
    echo "finished with ${d}"
done

