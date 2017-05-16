#!/bin/bash

m=60
toDo="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB DBpedia"

cd /home/roott/federatedOptimizer/code

for d in ${toDo}; do
    e=${d}
    /usr/bin/time -f "%e %P %t %M" java -Xmx${m}g addInterDatasetLinks /home/roott/fedBenchData/${d}/statistics${d}_iio_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_iis_obj_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_iio_obj_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_iis_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_cps_obj_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_cps_obj_reduced10000CS_double > outputAddInterDatasetLinks${d}${e}_obj 2> errorAddInterDatasetLinks${d}${e}_obj
    echo "finished with ${d}"
done
