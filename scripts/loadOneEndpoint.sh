#!/bin/bash

file=$1
dataset=$2
offset=$3

tmpFile=`mktemp`

cmdA="ld_add('${file}', 'http://${dataset}Fedbench');"
cmdB="rdf_loader_run();"
cmdC="checkpoint;"

p=$(($offset+1111))

isql_cmd="isql ${p} dba"
isql_pwd="dba"

${isql_cmd} ${isql_pwd} << EOF &> ${tmpFile}
    $cmdA
    $cmdB
    $cmdC
    exit;
EOF

cat ${tmpFile}
rm ${tmpFile}

