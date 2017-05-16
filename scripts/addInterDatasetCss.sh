#!/bin/bash

finished=""
toDo="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB DBpedia Geonames"
cd /home/roott/federatedOptimizer/code
for d in ${toDo}; do
    for e in ${finished}; do
        echo "${d}_${e}"
        /usr/bin/time -f "%e %P %t %M" java addInterDatasetCss /home/roott/fedBenchData/${d}/statistics${d}_iis_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_iis_reduced10000CS /home/roott/fedBenchData/css_${d}_${e}_reduced10000CS
        ls -lh /home/roott/fedBenchData/css_${d}_${e}_reduced10000CS
    done
    finished=`echo "${finished} ${d}"`
    echo "finished with ${d}"
done
