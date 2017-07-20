#!/bin/bash

i=0

while read line; do
    echo "$line" > oneEndpoint
    /usr/bin/time -f "%e %P %t %M" java -cp .:lib/* generateHibiscusStatistics stats${i} oneEndpoint
    echo "done with ${i}: ${line}"
    i=$(($i+1))
done < federation
