fedBench=/home/roott/queries/fedBench
newQueries=/home/roott/queries/fedBench_1_1
generalPreds=/home/roott/generalPredicates
group=LD
l=`seq 1 1`

cd ../code/
for i in $l; do
    query=${group}${i}
    java -cp .:/home/roott/apache-jena-2.13.0/lib/* evaluateSPARQLQuery $fedBench/$query /home/roott/datasets /home/roott/fedBenchData 100000000 true false $newQueries/$query $generalPreds
done
