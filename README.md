Implementation used to evaluate the Odyssey approach ("The Odyssey Approach for Optimizing Federated SPARQL Queries", ISWC2017)
* First, you will need to update the file scripts/configFile with the appropriate paths for your machine and available endpoints, also update the timeout value and number of runs if needed
* Compile the sources, it can be done with the script compileAll.sh as:
```
./compileAll.sh
```
* Set the correct paths in the systems that require that, it can be done with the script setPathFederationFiles.sh as:
```
./setPathFederationFiles.sh
```
* Set the endpoint addresses in the federation files (you must have included the addresses in configFile). It can be done with the script addEndpoints.sh as:
```
./addEndpoints.sh
```
* Execute the systems you are interested in using the appropriate scripts. For instance, for Odyssey it can be done with the script executeQueriesOdyssey.sh as:
```
./executeQueriesOdyssey.sh
```
* There are scripts available for the other systems as well (Hibiscus, FedX, SemaGrow, SPLENDID)
* To execute the systems using the proxies, change the addresses from endpoints to proxies for the federations, it can be done with the script changeToProxies.sh as:
```
./changeToProxies.sh
```
* Execute the systems you are interested in using the appropriate scripts. For instance, for Odyssey it can be done with the script executeQueriesOdysseyWithProxies.sh as:
```
./executeQueriesOdysseyWithProxies.sh
```
* There are scripts available to execute the other systems using proxies as well (Hibiscus, FedX, SemaGrow, SPLENDID)
* If you are interested in executing the systems without proxies and you have already changed to proxies. You can change the addresses from proxies to endpoints for the federations using the script changeToEndpoints.sh as:
```
./changeToEndpoints.sh 
```
* Execute the systems you are interested in using the appropriate scripts. For instance, for Odyssey it can be done with the script executeQueriesOdyssey.sh as:
```
./executeQueriesOdyssey.sh
```

Requirements:
* Our code has been tested using Java 8, Python 2.7.12. 
* Our code uses libraries: [HttpComponents Clients version 4.5.3](http://archive.apache.org/dist/httpcomponents/httpclient/binary/httpcomponents-client-4.5.3-bin.tar.gz) and [Jena version 2.13.0](http://archive.apache.org/dist/jena/binaries/apache-jena-2.13.0.tar.gz). Set the appropriate locations in the file scripts/configFile
* Evaluated systems such as FedX3.1, Hibiscus, Splendid, and SemaGrow should be available locally and their paths should be updated in the file scripts/configFile

Other comments:
* Odyssey's statistics are available [here](http://qweb.cs.aau.dk/pipe/data.tar.gz). All the files with the statistics (statistics*, cps*, css*) should be put in the folder fedbench/
* Odyssey's statistics were generated using the script: generateStatistics.sh
* Datasets used by generateStatistics.sh should be sorted by subject. This can be done using the script: sortDatasets.sh
* Few modifications were done to some of the engines to ensure a fair comparison, details are available in the folder engines
* Void's statistics were generated using the script: generateVoidStatistics.sh

This code is available in this [repository](https://github.com/gmontoya/federatedOptimizer)

