#!/bin/bash

mv /home/roott/federatedOptimizer/lib/fedX3.1/slf4j-log4j12-1.5.2.jar /home/roott/federatedOptimizer/lib/fedX3.1/lib/

./executeQueries.sh > outputExecuteQueries 2> errorExecuteQueries

./executeQueriesColdCache.sh > outputExecuteQueriesColdCache 2> errorExecuteQueriesColdCache

mv /home/roott/federatedOptimizer/lib/fedX3.1/lib/slf4j-log4j12-1.5.2.jar /home/roott/federatedOptimizer/lib/fedX3.1/

./executeOurQueries.sh > outputExecuteOurQueries 2> errorExecuteOurQueries

./executeQueriesVOIDWithProxies.sh > outputExecuteQueriesVOIDWithProxies 2> errorExecuteQueriesVOIDWithProxies

./executeQueriesSPLENDID.sh > outputExecuteQueriesSPLENDID 2> errorExecuteQueriesSPLENDID

./executeQueriesVOID.sh > outputExecuteQueriesVOID 2> errorExecuteQueriesVOID


