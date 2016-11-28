fedBench=/home/roott/queries/fedBench
newQueries=/home/roott/queries/fedBench_1_1_VOID

cd ../code/
for i in `seq 8 11`; do
    query=LD${i}
    java -cp .:/home/roott/apache-jena-2.13.0/lib/* produceJoinOrderingVOID $fedBench/$query /home/roott/datasets /home/roott/fedBenchData $newQueries/$query
done
