#!/bin/bash
. ./setFederation

graphs="urn:activitystreams-owl:map http://www.openlinksw.com/schemas/virtrdf# http://www.w3.org/ns/ldp#"

for i in `seq 1 ${number}`; do
    h=$(($i-1))
    endpoint=${endpoints[${h}]}
    name=${federation}${endpoint}
    pw=passwordep${h}
    portA=$((1111+$i))
    for g in ${graphs}; do
        docker exec -it ${name} isql 1111 -P ${pw} exec="sparql delete { graph <${g}> { ?s ?p ?o } } where { graph <${g}> { ?s ?p ?o } };"
        docker exec -it ${name} isql 1111 -P ${pw} exec="checkpoint;"
    done
done
