#!/bin/bash
. ./configFile
. ./setFederation

p=`pwd`

cd ${dumpFolder}/${federation}Data 

for i in `seq 1 ${number}`; do
    endpoint=${endpoints[${i}-1]}
    cd $endpoint
    mkdir isql
    file=isql/load${endpoint}.isql
    echo "delete from DB.DBA.load_list;" > ${file}
    echo "ld_dir ('/inputFiles', '*.*', 'http://${federation}${endpoint}');" >> ${file}
    echo "rdf_loader_run();" >> ${file}
    echo "checkpoint;" >> ${file}
    cd ..
done 

cd ${p}
