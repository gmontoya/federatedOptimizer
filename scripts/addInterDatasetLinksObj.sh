#!/bin/bash

finished="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB"
toDo="DBpedia Geonames"
cd /home/roott/federatedOptimizer/code
for d in ${toDo}; do
    for e in ${finished}; do
        echo "${d}_${e}"
        /usr/bin/time -f "%e %P %t %M" java addInterDatasetLinks /home/roott/fedBenchData/${d}/statistics${d}_iio_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_iis_obj_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_iio_obj_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_iis_obj_reduced10000CS /home/roott/fedBenchData/cps_${d}_${e}_obj_reduced10000CS /home/roott/fedBenchData/cps_${e}_${d}_obj_reduced10000CS
        ls -lh /home/roott/fedBenchData/cps_${d}_${e}_obj_reduced10000CS /home/roott/fedBenchData/cps_${e}_${d}_obj_reduced10000CS
    done
    finished=`echo "${finished} ${d}"`
    echo "finished with ${d}"
done
