#!/bin/bash

. ./configFile

# SPLENDID, SemaGrow, and Hibiscus require some paths to be set in their configuration files
cp ${federationPath}/splendidFedBenchFederation.n3.base ${federationPath}/splendidFedBenchFederation.n3
sed -i "s,\$fedBenchDataPath,${fedBenchDataPath},g" ${federationPath}/splendidFedBenchFederation.n3

cp ${federationPath}/repository.ttl.base ${federationPath}/repository.ttl
sed -i "s,\$fedBenchDataPath,${fedBenchDataPath},g" ${federationPath}/repository.ttl

cp ${federationPath}/config.properties.base ${federationPath}/config.properties
sed -i "s,\$fedBenchDataPath,${fedBenchDataPath},g" ${federationPath}/config.properties
