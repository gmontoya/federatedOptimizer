#preprocessFolder: combines the files in the folder into one file in N-Triple format
#!/bin/bash
. ./configFile
pathToFiles=$1
outputFile=$2

okay="ntn3"

for f in `ls $pathToFiles/*`; do
    ext=`echo ${f##*\.}`
    fileName=`echo ${f%\.*}`
    x=`echo ${okay#*$ext}`
    x=${#x}
    if [ ${x} -lt ${#okay} ]; then
        n=`grep "\@prefix" ${f}  | wc -l`
	if [ $n -gt 0 ]; then
            ${JENA_HOME}/bin/turtle ${fileName}.${ext} >> ${outputFile}
        else 
            cat ${f} >> ${outputFile}
        fi
    else 
        ${JENA_HOME}/bin/rdfparse ${fileName}.${ext} >> ${outputFile}
    fi
done
