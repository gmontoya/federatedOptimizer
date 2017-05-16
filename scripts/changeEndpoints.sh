#!/bin/bash

file=$1

x=8891
y=3030
for i in `seq 0 8`; do
    p=http://localhost:${x}/sparql
    q=http://localhost:${y}/sparql
    sed -i "s,${p},${q},g" $file
    x=$((${x}+1))
    y=$((${y}+1))
done
