#!/bin/bash

file=$1
dataset=$2
offset=$3

cmdA="ld_add('${file}', 'http://${dataset}Endpoint');"
cmdB="rdf_loader_run();"
cmdC="checkpoint;"

p=$(($offset+1111))

isql_cmd="isql ${p} dba"
isql_pwd="dba"

${isql_cmd} ${isql_pwd} << EOF &> /home/roott/tmp/linking.log
    $cmdA
    $cmdB
    $cmdC
    exit;
EOF
