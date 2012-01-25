#!/bin/bash

if [ -f "/usr/share/modules/init/sh" ]; then
   . /usr/share/modules/init/sh   
fi

module load dcmtools
DCMODIFY=`which DCMODIFY`
#echo "[dcmodify]=$DCMODIFY"
if [ -z $DCMODIFY ]; then
	echo "Error: cannot find DCMODIFY. Please check whether dcmtools has been properly installed"
	exit 1
fi

##
##
##


if [ -z $1 ]; then
	echo "Usage: `basename $0` <patient_name> <dicom_files>"
fi


VAL=$1
shift

until [ -z $1 ]
do
	$DCMODIFY \(0020,0010\)="$VAL" $1 &> /dev/null
	shift
done
