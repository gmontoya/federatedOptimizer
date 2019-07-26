#!/bin/bash

prefix=$1
first=$2
last=$3

ir=0
nc=0

for i in  `seq ${first} ${last}`; do
   fn="${prefix}_${i}"
   line=`tail -n 1 ${fn}`
   x=${line% *}
   y=${line#* }
   ir=$(($ir+$x))
   nc=$(($nc+$y))  
done

echo "$ir $nc"
