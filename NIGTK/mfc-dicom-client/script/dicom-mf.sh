#!/bin/sh

JAVA=`which java`
if [[ -z "${JAVA}" ]]; then
        echo "Error: could not find java." >&2
        exit 1
fi

JAR=`dirname $0`/dicom-client.jar
if [[ ! -f "${JAR}" ]]; then
        echo "Error: could not find file dicom-client.jar." >&2
        exit 1
fi

# Do it
$JAVA -Dmf.host=$MF_HOST -Dmf.port=$MF_PORT -Dmf.transport=$MF_TRANSPORT  -cp $JAR  nig.mf.pssd.client.dicom.DicomMF $@

RETVAL=$?
exit $RETVAL
