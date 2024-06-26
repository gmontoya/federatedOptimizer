# Steps to reproduce the results presented in the ISWC 17 paper "The Odyssey Approach for Optimizing Federated SPARQL Queries"

* Modify the file `scripts/configFile` and replace `MYFOLDER` with the path of the folder where you have this repository
* Modify the file `scripts/configFile` and replace `"HOST1" "HOST2" "HOST3" "HOST4" "HOST5" "HOST6" "HOST7" "HOST8" "HOST9"` with the addresses of the machines that host the endpoints, if all the endpoints are hosted at the same machine include only one time the address of the machine, e.g., `"localhost"`
* Execute script `scripts/addEndpoints.sh` to replace the address and port of the endpoints in the files that describe the federation for the different approaches
  * If the endpoints are hosted by different machines, each endpoint is expected to be available at port 8891
  * If the endpoints are hosted at the same machine, they are expected to be available at ports 8891-8899
  * Ports can be modified in the file `script/addEndpoints.sh`
* Make sure that `numRums` is set to `10` and `timeoutValue` is set to `1800`
* Execute the script `scripts/getLib.sh` to make sure that the libraries and engines are available
  * This script will download Apache Jena 2.13, httpcomponents-client-4.5.3
  * It will also download some engines used in the empirical evaluation from their github repositories and enhance them with some modified files available at `engines` to include time measurements used in the empirical evaluation
* Execute the script `scripts/compileAll.sh` to compile the Java files (requires Java 8)
* Execute the script `scripts/setPathFederationFiles.sh` to include absolute paths in some of the files used by the engines
* Execute the systems you are interested in using the appropriate scripts. 
  * To have the engines accessing the endpoints directly (without using proxies), use the scripts (available in the folder `scripts`):
```
./executeQueriesOdyssey.sh
./executeQueriesFedX.sh
./executeQueriesHibiscus.sh
./executeQueriesSPLENDID.sh
./executeQueriesSemaGrow.sh
```
  * To have the engines accessing the endpoints through proxies and count the number of intermediate results and requests, you will have to use the script `scripts/changeToProxies.sh` as
```
./changeToProxies.sh
```
    * This will replace the ports used by the endpoints 8891-8899 with the ports used by the proxies 3030-3038 in the relevant files used by the engines. 
    * Changing back the ports used by the proxies to the ports used by the endpoints can be done using the script `scripts/changeToEndpoints.sh`
  * And execute the engines using the relevant scripts:
```
./executeQueriesOdysseyWithProxies.sh
./executeQueriesFedXWithProxies.sh
./executeQueriesHibiscusWithProxies.sh
./executeQueriesSPLENDIDWithProxies.sh
./executeQueriesSemaGrowWithProxies.sh
```
  * To use Odyssey's plans with FedX's optimizations for join order, you can use the scripts `executeOurPlansFedXOrder.sh` or `executeOurPlansFedXOrderWithProxies.sh`
  * To use FedX's source selection with Odyssey's decomposition and join order, you can use the scripts `executeFedXSelectionOurDecompositionAndOrder.sh` or `executeFedXSelectionOurDecompositionAndOrderWithProxies.sh`
* The scripts will write to standard output the measurements taken during the evaluation, some examples of outputs are available at `results`, e.g., `results/outputExecuteQueriesOdysseyWithProxies20170718`
  * The output includes one line for each execution of each query following the format: 
    * *query*, *number of selected sources*, *number of subqueries*, *optimization time*, *execution time*, *number of tuples sent from the endpoints to the engine*, *number of requests*, *number of results*    
    * Examples
```
CD7 6 5 157 4743 638 549 2
LS3 7 5 6849 26390 19760 3103 9054
```

### Requirements
* Our code has been tested using Java 8, Python 2.7.12. 

# Generating Odyssey's statistics
* The core statistics used by Odyssey are available at `fedbench/statistics*`
  * This includes the information about local characteristic sets and pairs at each endpoint (`fedbench/statistics*_css_*` and `fedbench/statistics*_cps_*`) and the entity summaries that represent subject and object at each endpoint (`statistics*_rtree_*`)
  * These core statistics were generated using the script `scripts/generateStatisticsIndividualSources.sh`
    * The current code to generate core statistics requires that the dataset's triples are available in .n3 or .nt format with one triple per line and sorted by subject
      * Scripts to sort .nt files per subject are available at `scripts/sortDatasets.sh`and `scripts/sortDatasetsBoundMemory.sh` (used to sort large datasets)
      * The script `script/preprocessFolders.sh` can be used to collect multiple RDF files (in different formats) provided in one folder (`${dumpFolder}/files/${federation}Data/$endpoint`) into one N-triples file (`${dumpFolder}/${federation}Data/${endpoint}/${endpoint}Data.nt`)
        * (variable `dumpFolder` is set in the file `scripts/configFile` and `federation` is set in the file `scripts/setFederation`
* Use the core statistics to compute federated statistics based on the entity summaries using the script `scripts/generateStatisticsFederated.sh`


# Other comments:
* Void's statistics were generated using the script: generateVoidStatistics.sh

This code is available in this [repository](https://github.com/gmontoya/federatedOptimizer)

