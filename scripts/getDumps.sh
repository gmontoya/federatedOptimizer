#!/bin/bash

. ./configFile
. ./setFederation

cd $dumpFolder
mkdir -p ${federation}Data
cd ${federation}Data

for n in ${names}; do
   mkdir -p $n    
   cd $n 
   wget https://zenodo.org/records/6395247/files/${n}.nt.zst
   unzstd ${n}.nt.zst
   cd ..
done
