#!/bin/bash
. ./configFile
. ./setFederation

p=`pwd`
cd ${dumpFolder}/${federation}Data/

for i in `seq 1 ${number}`; do
    endpoint=${endpoints[${i}-1]}
    cd $endpoint
    for f in `ls *.gz`; do
        gunzip ${f}
    done
    for f in `ls *.7z`; do
	7z x ${f}
    done	
    for f in `ls *.zip`; do
        unzip ${f}
	rm ${f}
    done
    cd ..
done
cd ${p}
