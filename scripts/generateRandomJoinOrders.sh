#!/bin/bash

sed -i "s,optimize=.*,optimize=false," /home/roott/fedX3.1/config2
cp /home/roott/fedBenchFederationVirtuoso.ttl /home/roott/fedBenchFederation.ttl
queries=/home/roott/queries/fedBench_1_1
randomQueries=/home/roott/queries/fedBench_1_1_random
#fedBench=/home/roott/queries/complexQueries
#newQueries=/home/roott/queries/complexQueries_1_1
generalPreds=/home/roott/generalPredicates
datasets=/home/roott/datasetsVirtuoso

l="LD1 LD2 LD3 LD4 LD5 LD6 LD7 LD8 LD9 LD10 LD11 CD2 CD4 CD5 CD6 CD7 LS1 LS3 LS4 LS6 LS7"
n=5
#l="LD1"

for query in ${l}; do
    i=1
    z=0
    for j in `seq 1 ${n}`; do
        repeated=false
        cd /home/roott/eswcCode/federatedOptimizer/code
        java -cp .:/home/roott/apache-jena-2.13.0/lib/* TransformerRandomJoinOrder $queries/${query} false > $randomQueries/${query}_${i}
        m=$(($i-1))
        for k in `seq 1 ${m}`; do
            diff $randomQueries/${query}_${i} $randomQueries/${query}_${k} > diff_x
            lines=`wc -l diff_x | sed 's/^[ ^t]*//' | cut -d' ' -f1`
            if [ $lines -eq 0 ]; then
                rm $randomQueries/${query}_${i}
                repeated=true
                break
            fi
        done
        if [ $repeated = "true" ]; then
            z=$(($z+1))
        else 
            i=$(($i+1))
            z=0
        fi
        if [ $z -gt 10 ]; then 
            break
        fi
    done
    z=0
    for j in `seq 1 ${n}`; do
        repeated=false
        cd /home/roott/eswcCode/federatedOptimizer/code
        java -cp .:/home/roott/apache-jena-2.13.0/lib/* TransformerRandomJoinOrder $queries/${query} true > $randomQueries/${query}_${i}
        m=$(($i-1))
        for k in `seq 1 ${m}`; do
            diff $randomQueries/${query}_${i} $randomQueries/${query}_${k} > diff_x
            lines=`wc -l diff_x | sed 's/^[ ^t]*//' | cut -d' ' -f1`
            if [ $lines -eq 0 ]; then
                rm $randomQueries/${query}_${i}
                repeated=true
                break
            fi
        done
        if [ $repeated = "true" ]; then
            z=$(($z+1))
        else
            i=$(($i+1))
            z=0
        fi
        if [ $z -gt 10 ]; then
            break
        fi
    done
done
