#!/bin/bash

. ./setFederation

./restartDockers.sh $federation
sleep 60
./loadFederationsData.sh $federation
sleep 60
./stopDockers.sh $federation

