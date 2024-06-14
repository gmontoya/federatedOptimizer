#!/bin/bash

. ./setFederation
missing=4

for i in ${missing}; do
    h=$(($i-1))
    endpoint=${endpoints[${h}]}
    name=${federation}${endpoint}
    pw=passwordep${i} 
    #h
    echo "$name"
    docker exec ${name} isql 1111 -P ${pw} exec="rdf_loader_run();"
    docker exec ${name} isql 1111 -P ${pw} exec="checkpoint;"
done
