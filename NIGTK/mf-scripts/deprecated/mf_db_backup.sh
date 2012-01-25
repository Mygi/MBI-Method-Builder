#!/bin/bash

##
## This script is to backup the Mediaflux XML database file.
##

#
# Configuration
#

MFLUX_HOME=/usr/local/MediaFlux/mflux
MFLUX_DB_DIR=${MFLUX_HOME}/volatile/databases
MFLUX_DB=MFLUXDB.db
BACKUP_DIR=/store/mediaflux/backup
BACKUP_DB_DIR=$BACKUP_DIR
BACKUP_DB=${MFLUX_DB}.tar.gz
TAR=`which tar`

#
# Action
#

# Remove the old backup file.
rm ${BACKUP_DB_DIR}/${BACKUP_DB}
cd ${MFLUX_DB_DIR}

# Create the new backup.
${TAR} czvf ${BACKUP_DB_DIR}/${BACKUP_DB} ${MFLUX_DB}
