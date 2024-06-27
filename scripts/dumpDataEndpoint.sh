#!/bin/bash
. ./configFile
. ./setFederation

# input corresponds to the index of the dataset in the endpoints array (defined in setFederation file)
h=$1

endpoint=${endpoints[${h}]}
name=${federation}${endpoint}
pw=passwordep${h}
graph=http://${federation}${endpoint}
prefix=/inputFiles/files

docker exec ${name} isql 1111 -P ${pw} exec="dump_one_graph ('$graph', '$prefix/${endpoint}Data', 1000000000);"

