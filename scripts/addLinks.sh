#!/bin/bash
d=$1

files="_css _cps _iis _iio"
for g in ${files}; do
    if ! [ -a "/home/roott/fedBenchData/${d}/statistics${d}${g}_reduced10000CS" ]; then   
        ln -s /home/roott/fedBenchData/${d}/statistics${d}${g} /home/roott/fedBenchData/${d}/statistics${d}${g}_reduced10000CS
    fi
done
