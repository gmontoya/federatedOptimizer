#!/bin/bash

finished=""

toDo="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB Geonames DBpedia"

cd /home/roott/federatedOptimizer/code
 
for d in ${toDo}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    #/usr/bin/time -f "%e %P %t %M" java generateStatistics /home/roott/fedBenchData/${d}/${f}Sorted.n3 /home/roott/fedBenchData/${d}/statistics${d} > outputGenerateStatistics${d} 2> errorGenerateStatistics${d}
    #/usr/bin/time -f "%e %P %t %M" java mergeCharacteristicSetsPlus /home/roott/fedBenchData/${d}/statistics${d}_css /home/roott/fedBenchData/${d}/statistics${d}_css_reduced10000CS 10000  /home/roott/fedBenchData/${d}/statistics${d}_cps  /home/roott/fedBenchData/${d}/statistics${d}_cps_reduced10000CS  /home/roott/fedBenchData/${d}/statistics${d}_iis  /home/roott/fedBenchData/${d}/statistics${d}_iis_reduced10000CS  /home/roott/fedBenchData/${d}/statistics${d}_iio  /home/roott/fedBenchData/${d}/statistics${d}_iio_reduced10000CS  > outputMergeCharacteristicSetsPlus${d} 2> errorMergeCharacteristicSetsPlus${d}
    #files="_css _cps _iis _iio"
    #for g in ${files}; do
    #    if ! [ -a "/home/roott/fedBenchData/${d}/statistics${d}${g}_reduced10000CS" ]; then   
    #        ln -s /home/roott/fedBenchData/${d}/statistics${d}${g} /home/roott/fedBenchData/${d}/statistics${d}${g}_reduced10000CS
    #    fi
    #done
    /usr/bin/time -f "%e %P %t %M" java obtainRadixOneBasedIndex /home/roott/fedBenchData/${d}/statistics${d}_iis_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_iio_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_rtree_100_10_reduced10000CS_1 100 10 -1 10
    ls -lh /home/roott/fedBenchData/${d}/statistics${d}_rtree_100_10_reduced10000CS_1
    #/usr/bin/time -f "%e %P %t %M" java obtainRadixBasedIndex /home/roott/fedBenchData/${d}/statistics${d}_iis_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_is_rtree_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_iio_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_io_rtree_reduced10000CS 100 10 50 > outputObtainRTreeBasedIndex${d} 2> errorObtainRTreeBasedIndex${d}
    #/usr/bin/time -f "%e %P %t %M" java obtainRadixTreeBasedIndex /home/roott/fedBenchData/${d}/statistics${d}_iio_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_io_obj_rtree_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_iis_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_is_obj_rtree_reduced10000CS 100 10 50 > outputObtainRTreeBasedObjIndex${d} 2> errorObtainRTreeBasedObjIndex${d}
    #/usr/bin/time -f "%e %P %t %M" java obtainHierarchicalCharacterization /home/roott/fedBenchData/${d}/statistics${d}_css_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_hc_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_cost_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_pi_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_as_reduced10000CS > outputObtainHierarchicalCharacterization${d} 2> errorObtainHierarchicalCharacterization${d}
    for e in ${finished}; do
        /usr/bin/time -f "%e %P %t %M" java addInterDatasetLinksRTreeOne /home/roott/fedBenchData/${d}/statistics${d}_rtree_100_10_reduced10000CS_1 /home/roott/fedBenchData/${e}/statistics${e}_rtree_100_10_reduced10000CS_1 /home/roott/fedBenchData/cps_${d}_${e}_one_rtree_reduced10000CS_1 /home/roott/fedBenchData/cps_${e}_${d}_one_rtree_reduced10000CS_1
        ls -lh /home/roott/fedBenchData/cps_${d}_${e}_one_rtree_reduced10000CS_1 /home/roott/fedBenchData/cps_${e}_${d}_one_rtree_reduced10000CS_1
        java evaluateCPs /home/roott/fedBenchData/cps_${d}_${e}_reduced10000CS /home/roott/fedBenchData/cps_${d}_${e}_one_rtree_reduced10000CS_1
        java evaluateCPs /home/roott/fedBenchData/cps_${e}_${d}_reduced10000CS /home/roott/fedBenchData/cps_${e}_${d}_one_rtree_reduced10000CS_1
        #/usr/bin/time -f "%e %P %t %M" java addInterDatasetLinksQTree /home/roott/fedBenchData/${d}/statistics${d}_is_qtree_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_io_qtree_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_is_qtree_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_io_qtree_reduced10000CS /home/roott/fedBenchData/cps_${d}_${e}_qtree_reduced10000CS /home/roott/fedBenchData/cps_${e}_${d}_qtree_reduced10000CS > outputAddInterDatasetLinks${d}${e}QTree 2> errorAddInterDatasetLinks${d}${e}QTree
        #/usr/bin/time -f "%e %P %t %M" java addInterDatasetLinksQTree /home/roott/fedBenchData/${d}/statistics${d}_io_obj_qtree_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_is_obj_qtree_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_io_obj_qtree_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_is_obj_qtree_reduced10000CS /home/roott/fedBenchData/cps_${d}_${e}_obj_qtree_reduced10000CS /home/roott/fedBenchData/cps_${e}_${d}_obj_qtree_reduced10000CS > outputAddInterDatasetLinksObjectCS${d}${e}QTree 2> errorAddInterDatasetLinksObjectCS${d}${e}QTree
        #/usr/bin/time -f "%e %P %t %M" java addInterDatasetLinksQTree /home/roott/fedBenchData/${d}/statistics${d}_io_obj_qtree_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_io_qtree_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_io_obj_qtree_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_io_qtree_reduced10000CS /home/roott/fedBenchData/cps_${d}_${e}_subj_obj_qtree_reduced10000CS /home/roott/fedBenchData/cps_${e}_${d}_subj_obj_qtree_reduced10000CS > outputAddInterDatasetLinks${d}${e}_subj_objQTree 2> errorAddInterDatasetLinks${d}${e}_subj_objQTree
        #/usr/bin/time -f "%e %P %t %M" java addInterDatasetLinksMIPs /home/roott/fedBenchData/${d}/statistics${d}_is_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_io_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_is_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_io_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_css_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_css_reduced10000CS /home/roott/fedBenchData/cps_${d}_${e}_reduced10000CS_MIP /home/roott/fedBenchData/cps_${e}_${d}_reduced10000CS_MIP > outputObtainHierarchicalCharacterization${d}${e} 2> errorObtainHierarchicalCharacterization${d}${e}
    done
    finished=`echo "${finished} ${d}"`
    echo "finished with ${d}"
done
