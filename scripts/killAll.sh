#!/bin/bash
file=$1

while read pid; do
  kill ${pid}
done < "$file"
