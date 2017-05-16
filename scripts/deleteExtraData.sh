#!/bin/bash

offset=$1
port=$((${offset}+8890))
cmdA="SPARQL WITH <http://www.openlinksw.com/schemas/virtrdf#> DELETE { ?s ?p ?o }     WHERE { graph <http://www.openlinksw.com/schemas/virtrdf#> { ?s ?p ?o }};"
cmdB="SPARQL WITH <http://www.w3.org/ns/ldp#> DELETE { ?s ?p ?o }     WHERE { graph <http://www.w3.org/ns/ldp#> { ?s ?p ?o }};"
cmdC="SPARQL WITH <http://localhost:${port}/sparql> DELETE { ?s ?p ?o }     WHERE { graph <http://localhost:${port}/sparql> { ?s ?p ?o }};"
cmdD="SPARQL WITH <http://localhost:${port}/DAV/> DELETE { ?s ?p ?o }     WHERE { graph <http://localhost:${port}/DAV/> { ?s ?p ?o }};"
cmdE="SPARQL WITH <http://www.w3.org/2002/07/owl#> DELETE { ?s ?p ?o }     WHERE { graph <http://www.w3.org/2002/07/owl#> { ?s ?p ?o }};"
cmdF="checkpoint;"

p=$(($offset+1111))

cd /home/roott/virtuosoInstallation/bin/
isql_cmd="./isql ${p} dba"
isql_pwd="dba"

${isql_cmd} ${isql_pwd} << EOF &> /home/roott/tmp/linking.log
    $cmdA
    $cmdB
    $cmdC
    $cmdD
    $cmdE
    $cmdF
    exit;
EOF
