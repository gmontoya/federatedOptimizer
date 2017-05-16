#!/bin/bash

finished="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB"

toDo="DBpedia Geonames" #"ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB Geonames DBpedia"

 
for d in ${toDo}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    /usr/bin/time -f "%e %P %t %M" java obtainRadixOneBasedIndex /home/gabriela/statistics${d}_iis_reduced10000CS /home/gabriela/statistics${d}_iio_reduced10000CS /home/gabriela/statistics${d}_rtree_100_10_reduced10000CS 100 10 -1 >> outputObtainROneTreeBasedIndex${d}NoSmart 2>> errorObtainROneTreeBasedIndex${d}NoSmart
    ls -lh /home/gabriela/statistics${d}_rtree_100_10_reduced10000CS
    for e in ${finished}; do
        /usr/bin/time -f "%e %P %t %M" java addInterDatasetLinksRTreeOne /home/gabriela/statistics${d}_rtree_100_10_reduced10000CS /home/gabriela/${e}/statistics${e}_rtree_100_10_reduced10000CS /home/gabriela/cps_${d}_${e}_one_rtree_reduced10000CSNoSmart /home/gabriela/cps_${e}_${d}_one_rtree_reduced10000CSNoSmart  >> outputAddInterDatasetLinks${d}${e}ROneTreeNoSmart 2>> errorAddInterDatasetLinks${d}${e}ROneTreeNoSmart
        ls -lh /home/gabriela/cps_${d}_${e}_one_rtree_reduced10000CSNoSmart /home/gabriela/cps_${e}_${d}_one_rtree_reduced10000CSNoSmart
    done
    finished=`echo "${finished} ${d}"`
    echo "finished with ${d}"
done
