#!/bin/bash

. ./configFile

finished=""
toDo="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB DBpedia Geonames"

cd ${federatedOptimizerPath}/code
for d in ${toDo}; do
    for e in ${finished}; do
        echo "${d}_${e}"
        /usr/bin/time -f "%e %P %t %M" java addInterDatasetLinks ${fedBenchDataPath}/statistics${d}_iio_obj_reduced10000CS ${fedBenchDataPath}/statistics${d}_iis_obj_reduced10000CS ${fedBenchDataPath}/statistics${e}_iio_obj_reduced10000CS ${fedBenchDataPath}/statistics${e}_iis_obj_reduced10000CS ${fedBenchDataPath}/cps_${d}_${e}_obj_reduced10000CS ${fedBenchDataPath}/cps_${e}_${d}_obj_reduced10000CS
        ls -lh ${fedBenchDataPath}/cps_${d}_${e}_obj_reduced10000CS ${fedBenchDataPath}/cps_${e}_${d}_obj_reduced10000CS
    done
    finished=`echo "${finished} ${d}"`
    echo "finished with ${d}"
done
