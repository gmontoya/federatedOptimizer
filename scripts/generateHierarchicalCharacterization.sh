#!/bin/bash

. ./configFile

toDo="ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB Geonames DBpedia"

cd ${federatedOptimizerPath}/code
 
for d in ${toDo}; do
    /usr/bin/time -f "%e %P %t %M" java obtainHierarchicalCharacterization ${fedBenchDataPath}/statistics${d}_css_reduced10000CS ${fedBenchDataPath}/statistics${d}_hc_reduced10000CS ${fedBenchDataPath}/statistics${d}_cost_reduced10000CS ${fedBenchDataPath}/statistics${d}_pi_reduced10000CS ${fedBenchDataPath}/statistics${d}_as_reduced10000CS > outputObtainHierarchicalCharacterization${d} 2> errorObtainHierarchicalCharacterization${d}
    echo "finished with ${d}"
done
