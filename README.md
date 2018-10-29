Implementation used to evaluate the Odyssey approach. It is mainly developed in Java, with some scripts in bash and python

* Odyssey was executed using the script evaluateQueriesOdyssey.sh (compile the java sources first)
* Other approaches were executed using the scripts: evaluateQueries${approach}.sh
* Proxies were used to measure the number of transferred tuples and calls using the scripts: evaluateQueries${approach}WithProxies.sh, this requires the httpcomponents-client library, we used the version 4.5.3. We assume it is available at ${ODYSSEY_HOME}/proxy/httpcomponents-client-4.5.3 (source file should be compiled first)
* FedX3.1 is assumed to be available at ${ODYSSEY_HOME}/lib/fedX3.1
* Statistics were generated with the script: generateStatistics.sh
* If datasets need sorting use the script: sortDatasets.sh
* Few modifications were done to some of the engines to ensure a fair comparison, details are available in the folder engines

This code is available at https://github.com/gmontoya/federatedOptimizer

Compiling the source code:
* Edit the files code/evaluateOurPlansWithFedXOrder.java, code/evaluateFedXSelection.java, code/evaluateFedXPlan.java, code/evaluateSPARQLQuery.java to set the correct path for fedxConfig and dataConfig:
        String fedxConfig = ...+"/federatedOptimizer/lib/fedX3.1/config2";
        String dataConfig = ...+"/fedBenchFederation.ttl";
* The file config2 includes the configuration of the FedX engine, it can be found at otherFiles/config2
* The file fedBenchFederation.ttl includes the FedX description of the federation, see otherFiles/fedBenchFederationVirtuoso.ttl for a template
* Given that the variables JENA_HOME and FEDX_HOME are propertly set, the code can be compiled as:
  javac -cp .:${JENA_HOME}/lib/*:${FEDX_HOME}/lib/* *.java
  
Other minor changes may be necessary in the scripts files at scripts/, namely appropriately set the variables:
* ODYSSEY_HOME=/home/roott/federatedOptimizer # the path of the Odyssey code
* JENA_HOME=/home/roott/apache-jena-2.13.0 # the path of Jena, we used the version 2.13.0
* fedBenchDataPath=/home/roott/fedbBenchData # the path of the datasets and Odyssey's statistics, this folder should contain a subfolder for each of the datsets d in {ChEBI KEGG Drugbank Jamendo NYTimes SWDF LMDB Geonames DBpedia}, in this folder a file with all the triples in the dataset is available, it is assumed to be ChEBI/chebi.n3, ..., DBpedia/dbpedia.n3. If the triples are sorted by subject, append Sorted after the name of the dataset as in ChEBI/chebiSorted.n3 and skip the call to the sortDatasets.sh script
* fedBench=/home/roott/queries/fedBench # the path of the queries
* datasets=/home/roott/datasetsVirtuoso # the path of the datasets file with the endpoints available for each dataset, see otherFiles/datasetsVirtuoso for a template
* proxyFederationFile=/home/roott/tmp/proxyFederation # the path of the file with the proxy information, it is created by the script startProxies2.sh
* federationDescriptionFile=/home/roott/fedBenchFederation.ttl # the path of the description of the federation, see otherFiles/fedBenchFederationVirtuoso.ttl for a template
* FEDX_HIBISCUS_HOME=/home/roott/FedX-HiBISCus # the path of the FEDX_HIBISCUS engine
* configFileHibiscus=${FEDX_HIBISCUS_HOME}/config.properties # the path of the FEDX_HIBISCUS configuration file, see otherFiles/config.properties for a template
* splendidDescriptionFile=/home/roott/splendidFedBenchFederation.n3 # the path of the Splendid federation description, see otherFiles/splendidFedBenchFederation.n3 for a template, the statistics were generated with the script generateVoidStatistics.sh
* SPLENDID_HOME=/home/roott/rdffederator # the path of the SPLENDID engine
* SEMAGROW_HOME=/home/roott/semagrow # the path of the SEMAGROW engine
