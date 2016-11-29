#!/bin/bash

finished="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB"
m=62
toDo="Geonames DBpedia"

cd /home/roott/federatedOptimizer/code
 
for d in ${toDo}; do
    f=`echo "$d" | tr '[:upper:]' '[:lower:]'`
    /usr/bin/time -f "%e %P %t %M" java -Xmx${m}g generateStatisticsObjectCS /home/roott/fedBenchData/${d}/${f}ObjSorted.n3 /home/roott/fedBenchData/${d}/statistics${d} > outputGenerateStatisticsObjectCS${d} 2> errorGenerateStatisticsObjectCS${d}
    /usr/bin/time -f "%e %P %t %M" java -Xmx${m}g mergeCharacteristicSetsPlus /home/roott/fedBenchData/${d}/statistics${d}_css_obj /home/roott/fedBenchData/${d}/statistics${d}_css_obj_reduced10000CS 10000  /home/roott/fedBenchData/${d}/statistics${d}_cps_obj  /home/roott/fedBenchData/${d}/statistics${d}_cps_obj_reduced10000CS  /home/roott/fedBenchData/${d}/statistics${d}_iio_obj  /home/roott/fedBenchData/${d}/statistics${d}_iio_obj_reduced10000CS  /home/roott/fedBenchData/${d}/statistics${d}_iis_obj  /home/roott/fedBenchData/${d}/statistics${d}_iis_obj_reduced10000CS  > outputMergeCharacteristicSetsPlusObjectCS${d} 2> errorMergeCharacteristicSetsPlusObjectCS${d}
    files="_css _cps _iis _iio"
    for g in ${files}; do
        if ! [ -a "/home/roott/fedBenchData/${d}/statistics${d}${g}_obj_reduced10000CS" ]; then   
            ln -s /home/roott/fedBenchData/${d}/statistics${d}${g}_obj /home/roott/fedBenchData/${d}/statistics${d}${g}_obj_reduced10000CS
        fi
    done
    /usr/bin/time -f "%e %P %t %M" java -Xmx${m}g obtainMIPBasedIndex /home/roott/fedBenchData/${d}/statistics${d}_iio_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_io_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_iis_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_is_obj_reduced10000CS 100 > outputObtainMIPBasedIndexObjectCS${d} 2> errorObtainMIPBasedIndexObjectCS${d}
    /usr/bin/time -f "%e %P %t %M" java -Xmx${m}g obtainHierarchicalCharacterization /home/roott/fedBenchData/${d}/statistics${d}_css_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_hc_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_cost_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_pi_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_as_obj_reduced10000CS > outputObtainHierarchicalCharacterizationObjectCS${d} 2> errorObtainHierarchicalCharacterizationObjectCS${d}
    for e in ${finished}; do
        /usr/bin/time -f "%e %P %t %M" java -Xmx${m}g addInterDatasetLinksMIPs /home/roott/fedBenchData/${d}/statistics${d}_io_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_is_obj_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_io_obj_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_is_obj_reduced10000CS /home/roott/fedBenchData/${d}/statistics${d}_css_obj_reduced10000CS /home/roott/fedBenchData/${e}/statistics${e}_css_obj_reduced10000CS /home/roott/fedBenchData/cps_${d}_${e}_obj_reduced10000CS_MIP /home/roott/fedBenchData/cps_${e}_${d}_obj_reduced10000CS_MIP > outputAddInterDatasetLinksMIPsObjectCS${d}${e} 2> errorAddInterDatasetLinksMIPsObjectCS${d}${e}
    done
    finished=`echo "${finished} ${d}"`
    echo "finished with ${d}"
done
