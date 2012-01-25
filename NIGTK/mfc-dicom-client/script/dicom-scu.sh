#!/bin/sh

JAVA=`which java`
if [ -z "${JAVA}" ]; then
        echo "Error: could not find java." >&2
        exit 1
fi

JAR=`dirname $0`/dicom-client.jar


# Command
$JAVA -cp $JAR nig.mf.pssd.client.dicom.DicomSCU $@

RETVAL=$?
exit $RETVAL
