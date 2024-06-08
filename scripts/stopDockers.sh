#!/bin/bash
. ./setFederation

for i in `seq 1 ${number}`; do
  endpoint=${endpoints[${i}-1]}
  name=${federation}${endpoint}
  docker stop ${name}
done
sleep 10
