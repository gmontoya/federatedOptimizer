fedBench=/home/roott/queries/fedBench
newQueries=/home/roott/queries/fedBench_1_1

group=LSD
l="3 4 5 6"

cd ../code/
for i in $l; do
    query=${group}${i}
    java -cp .:/home/roott/apache-jena-2.13.0/lib/* produceJoinOrderingFederation $fedBench/$query /home/roott/datasets /home/roott/fedBenchData 100000000 true false $newQueries/$query
done
