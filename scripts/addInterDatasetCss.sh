#!/bin/bash

. ./configFile

finished=""
toDo="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB DBpedia Geonames"
cd ${federatedOptimizerPath}/code
for d in ${toDo}; do
    for e in ${finished}; do
        echo "${d}_${e}"
        /usr/bin/time -f "%e %P %t %M" java addInterDatasetCss ${fedBenchDataPath}/statistics${d}_iis_reduced10000CS ${fedBenchDataPath}/statistics${e}_iis_reduced10000CS ${fedBenchDataPath}/css_${d}_${e}_reduced10000CS
        ls -lh ${fedBenchDataPath}/css_${d}_${e}_reduced10000CS
    done
    finished=`echo "${finished} ${d}"`
    echo "finished with ${d}"
done
