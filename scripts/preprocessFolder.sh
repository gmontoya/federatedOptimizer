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
        cat ${f} >> ${outputFile}
    else 
        ${JENA_HOME}/bin/rdfparse ${fileName}.${ext} >> ${outputFile}
    fi
done
