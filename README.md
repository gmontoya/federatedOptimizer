Implementation used to evaluate the Odyssey approach

** Odyssey can be executed using the script evaluateQueriesOdyssey.sh

** Other approaches can be evaluated using the scripts:
* evaluateQueries${approach}.sh

** Proxies can be used to measure the number of transferred tuples and calls using the scripts: evaluateQueries${approach}WithProxies.sh, this requires the httpcomponents-client library, we used the version 4.5.3.

** Statistics can be generated with script: generateStatistics.sh

** Few modifications were done to some of the engines to ensure a fair comparison, details are available in the folder engines
