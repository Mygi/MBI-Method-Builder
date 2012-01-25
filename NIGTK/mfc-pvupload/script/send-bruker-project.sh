#!/bin/bash
PVDATA=/pvdata/fangke/nmr
BRUKER_CLIENT=/opt/mflux-pssd/mfpvload-cluster.sh
LOGDIR=/tmp
TIMESTAMP=`date +%h%d%Y`
PROJECT_ID=$1

if [ -z "$1" ]; then
	echo Usage: $0 PROJECT_ID
	exit
fi

for SUBJECT in `ls -d ${PVDATA}/${PROJECT_ID}*`; 
do
        ${BRUKER_CLIENT} ${SUBJECT} &> ${LOGDIR}/send-bruker-pid${PROJECT_ID}_${TIMESTAMP}.log
	if [ $? != "0" ]; then
		FAILARRAY="${FAILARRAY} ${SUBJECT}"
	fi

	if [ $? == "0" ]; then
		SUCCESSARRAY="${SUCCESSARRAY} ${SUBJECT}"
	fi
done

echo "Attempted upload for all subjects in project ${PROJECT_ID}..."
echo "The following subjects uploaded successfully: ${SUCCESSARRAY}"
echo "The following subjects failed to upload: ${FAILARRAY}"
echo "Full log written to ${LOGDIR}/send-bruker-pid${PROJECT_ID}_${TIMESTAMP}.log"