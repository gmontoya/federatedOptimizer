#!/bin/bash

. ./setFederation

for i in `seq 1 ${number}`; do
    h=$(($i-1))
    endpoint=${endpoints[${h}]}
    name=${federation}${endpoint}
    pw=passwordep${h}
    docker exec ${name} isql 1111 -P ${pw} exec="sparql delete { graph ?g { ?s ?p ?o } } where { graph ?g { ?s ?p ?o } };"
    docker exec ${name} isql 1111 -P ${pw} exec="checkpoint;"
    docker exec ${name} isql 1111 -P ${pw} exec="LOAD /inputFiles/isql/load${endpoint}.isql"
done
