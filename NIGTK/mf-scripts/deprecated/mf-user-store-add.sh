#!/bin/bash
# This script adds a Mediaflux user to /store/mediaflux/users
# and creates a symlink, mf-transient, in their home directory
#

if [ "`whoami`" != "root" ]; then
	echo "Error: only root user can run this script."
	exit
fi

if [ -z $1 ]; then
   echo "USAGE: `basename $0`  < unix user name >"
   exit
fi


MF_HOME=/store/mediaflux/users



USER=$1
USER_ID=`id -u $USER`
if [ -n "$USER_ID" ]; then
	GROUP=mf-$USER
	GROUP_ID=$((20000+USER_ID))
	WORKSPACE=$MF_HOME/$USER
	/usr/sbin/groupadd --gid $GROUP_ID $GROUP
	/usr/sbin/groupmod --add-user $MFLUX_USER $GROUP
	/usr/sbin/groupmod --add-user $USER $GROUP
	mkdir -p $WORKSPACE
	chown -R $MFLUX_USER:$GROUP $WORKSPACE
	chmod -R 2775 $WORKSPACE
	su -c "ln -s $WORKSPACE /home/$USER/mf-transient" $USER
fi
