#!/bin/bash

. ./setFederation

l="3 4 10 11 12"
declare -a passwords=($l)

names="GeoNames DBPedia-Subset LinkedTCGA-E LinkedTCGA-M Affymetrix"
declare -a endpoints=($names)
number=${#endpoints[@]}

for i in `seq 1 ${number}`; do
    h=$(($i-1))
    endpoint=${endpoints[${h}]}
    name=${federation}${endpoint}
    p=${passwords[${h}]} ##
    pw=passwordep${p} ##h
    docker exec ${name} isql 1111 -P ${pw} exec="sparql delete { graph ?g { ?s ?p ?o } } where { graph ?g { ?s ?p ?o } };"
    docker exec ${name} isql 1111 -P ${pw} exec="checkpoint;"
    docker exec ${name} isql 1111 -P ${pw} exec="LOAD /inputFiles/isql/load${endpoint}.isql"
done
