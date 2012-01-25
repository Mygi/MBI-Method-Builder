#!/bin/sh

##############################################################################
##############################################################################
##                                                                          ## 
##                               SETTINGS                                   ##
##                                                                          ##
##############################################################################
##############################################################################


BACKUP_ROOT=/store/mediaflux/backup

BACKUP_PREFIX=mf_xodb_export

BACKUP_LIST=$BACKUP_ROOT/${BACKUP_PREFIX}-backup.list

NUM_BACKUPS_TO_KEEP=7

if [ ! -f $BACKUP_LIST ]; then
	echo "Error: Backup list file:$BACKUP_LIST does not exist."
	exit 0
fi


NUM_BACKUPS=`wc -l $BACKUP_LIST | awk '{print $1}'`


if [ "$NUM_BACKUPS" -le "$NUM_BACKUPS_TO_KEEP" ]; then
	exit 1
fi

NUM_BACKUPS_TO_DELETE=$((NUM_BACKUPS-NUM_BACKUPS_TO_KEEP))

for b in `head -$NUM_BACKUPS_TO_DELETE $BACKUP_LIST`
do
	rm -fr $b	
done

tail -n $NUM_BACKUPS_TO_KEEP $BACKUP_LIST > $BACKUP_LIST.tmp
cat $BACKUP_LIST.tmp > $BACKUP_LIST
rm $BACKUP_LIST.tmp

