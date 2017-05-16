#!/bin/bash

finished=""
m=60
toDo="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB DBpedia Geonames"

cd /home/roott/federatedOptimizer/code
 
for d in ${toDo}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    #/usr/bin/time -f "%e %P %t %M" java -Xmx${m}g addIntraDatasetSOCPs /home/roott/fedBenchData/${d}/statistics${d}_iio_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_iio_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_css_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_cps_subj_obj_reduced10000CS > outputAddIntraDatasetSOCPs${d} 2> errorAddIntraDatasetSOCPs${d}
    for e in ${finished}; do
        /usr/bin/time -f "%e %P %t %M" java -Xmx${m}g addInterDatasetLinksMIPs /home/roott/fedBenchData/${d}/statistics${d}_io_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_io_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_io_obj_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_io_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_css_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_css_reduced10000CS /home/roott/fedBenchData/cps_${d}_${e}_subj_obj_reduced10000CS_MIP /home/roott/fedBenchData/cps_${e}_${d}_subj_obj_reduced10000CS_MIP > outputAddInterDatasetLinksMIPs${d}${e}_subj_obj 2> errorAddInterDatasetLinksMIPs${d}${e}_subj_obj

    done
    finished=`echo "${finished} ${d}"`
    echo "finished with ${d}"
done
