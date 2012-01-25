#!/bin/bash

CMD=`which pssd-dicom`
echo "[pssd-dicom]=$CMD"
if [ -z $CMD ]; then
    echo "Error: cannot find pssd-dicom. Please check whether its has been installed properly"
    exit 1
fi


## Data

CWD=`pwd`
cd `dirname $0`/../
DIR=`pwd`
DATA_DIR=$DIR/data
cd $CWD

# Dicom data to be sent to the server
DICOM_DATA_DIR=$DATA_DIR/RCH

# Upload
$CMD -m client -f $DICOM_DATA_DIR/\*.dcm -h 192.168.20.201 -p 6666
