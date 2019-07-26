#!/bin/bash

. ./configFile

d=$1

files="_css _cps _iis _iio"
for g in ${files}; do
    if ! [ -a "${fedBenchDataPath}/statistics${d}${g}_reduced10000CS" ]; then   
        ln -s ${fedBenchDataPath}/statistics${d}${g} ${fedBenchDataPath}/statistics${d}${g}_reduced10000CS
    fi
done
