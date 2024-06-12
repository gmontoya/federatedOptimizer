#!/bin/bash

. ./configFile
. ./setFederation

mkdir -p ${dumpFolder}/${federation}Data

currentFolder=${dumpFolder}/files/${federation}Data


for i in `seq 1 ${number}`; do
    endpoint=${endpoints[${i}-1]}
    f=${currentFolder}/$endpoint
    mkdir -p ${dumpFolder}/${federation}Data/${endpoint}
    ./preprocessFolder.sh ${f} ${dumpFolder}/${federation}Data/${endpoint}/${endpoint}Data.nt
    #rm -r ${f}
done
