#!/bin/bash

. ./configFile

finished="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB"
toDo="DBpedia Geonames"
cd ${federatedOptimizerPath}/code
for d in ${toDo}; do
    for e in ${finished}; do
        echo "${d}_${e}"
        /usr/bin/time -f "%e %P %t %M" java addInterDatasetLinks ${fedBenchDataPath}/statistics${d}_iis_reduced10000CS ${fedBenchDataPath}/statistics${d}_iio_reduced10000CS ${fedBenchDataPath}/statistics${e}_iis_reduced10000CS ${fedBenchDataPath}/statistics${e}_iio_reduced10000CS ${fedBenchDataPath}/cps_${d}_${e}_reduced10000CS ${fedBenchDataPath}/cps_${e}_${d}_reduced10000CS
        ls -lh ${fedBenchDataPath}/cps_${d}_${e}_reduced10000CS ${fedBenchDataPath}/cps_${e}_${d}_reduced10000CS
    done
    finished=`echo "${finished} ${d}"`
    echo "finished with ${d}"
done
