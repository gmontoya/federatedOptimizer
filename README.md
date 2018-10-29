Implementation used to evaluate the Odyssey approach. It is mainly developed in Java, with some scripts in bash and python

* Odyssey was executed using the script evaluateQueriesOdyssey.sh (compile the java sources first)
* Other approaches were executed using the scripts: evaluateQueries${approach}.sh
* Proxies were used to measure the number of transferred tuples and calls using the scripts: evaluateQueries${approach}WithProxies.sh, this requires the httpcomponents-client library, we used the version 4.5.3. (source file should be compiled first)
* Statistics were generated with the script: generateStatistics.sh
* If datasets need sorting use the script: sortDatasets.sh
* Few modifications were done to some of the engines to ensure a fair comparison, details are available in the folder engines

This code is available at https://github.com/gmontoya/federatedOptimizer
