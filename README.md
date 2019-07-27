Implementation used to evaluate the Odyssey approach ("The Odyssey Approach for Optimizing Federated SPARQL Queries", ISWC2017)

* First, you will need to compile the sources, it can be done with the script compileAll.sh as:
```
./compileAll.sh
```
* Update the file scripts/configFile with the appropriate paths for your machine and available endpoints, also update the timeout value and number of runs if needed
* Set the correct paths in the systems that require that, it can be done with the script setPathFederationFiles.sh as:
```
./setPathFederationFiles.sh
```
* Set the endpoint addresses in the federation files (you must have include the endpoints addresses in configFile), it can be done with the script addEndpoints.sh as:
```
./addEndpoints.sh
```
* Execute the systems you are interested in, using the approprite scripts. For instance for Odyssey it can be done with the script executeQueriesOdyssey.sh as:
```
./executeQueriesOdyssey.sh
```
* There are scripts available for the other systems as well (Hibiscus, FedX, SemaGrow, SPLENDID)
* To execute the systems using the proxies, change the addresses from endpoints to proxies for the federations, it can be done with the script changeToProxies.sh as:
```
./changeToProxies.sh
```
* Execute the systems you are interested inusing the approprite scripts. For i
nstance for Odyssey it can be done with the script executeQueriesOdysseyWithProxies.sh as:
```
./executeQueriesOdysseyWithProxies.sh
```
* There are scripts available for the other systems as well (Hibiscus, FedX, SemaGrow, SPLENDID)
* If you are interested in executing the systems without proxies and you have change the to proxies already, change the addresses from proxies to endpoints for the federations, it can be done with the script changeToEndpoints.sh as:
```
./changeToEndpoints.sh 
```
* Execute the systems you are interested in, using the approprite scripts. For i
nstance for Odyssey it can be done with the script executeQueriesOdyssey.sh as:
```
./executeQueriesOdyssey.sh
```

Some requirements:
* Proxies were used to measure the number of transferred tuples and calls, this requires the httpcomponents-client library, we used the version 4.5.3. Set the appropriate location in the file scripts/configFile
* Our code uses the library Jena 2.13.0 to parse queries. Set the appropriate location in the file scripts/configFile
* Evaluated systems such as FedX3.1, Hibiscus, Splendid, and SemaGrow should be available and their paths should be updated in the file scripts/configFile

Other comments:
* Odyssey's statistics are available [here](http://qweb.cs.aau.dk/pipe/data.tar.gz). All the files with the statistics (statistics*, cps*, css*) should be put in the folder fedbench/
* Odyssey's statistics were generated using the script: generateStatistics.sh
* Dataset passed to generateStatistics should be sorted by subject. This can be done using the script: sortDatasets.sh
* Few modifications were done to some of the engines to ensure a fair comparison, details are available in the folder engines
* Void's statistics were generated using the script: generateVoidStatistics.sh

This code is available in this [repository](https://github.com/gmontoya/federatedOptimizer)

