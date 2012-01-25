#!/bin/bash
# MFLUX_HOME is the location of the mediaflux installation to be
# controlled by this script.
if [ -z $MFLUX_HOME ]; then
  echo "Please set up the MFLUX_HOME variable"
  exit
fi
if [ -z $MFLUX_BIN ]; then
  echo "Please set up the MFLUX_BIN variable"
  exit
fi
MFCOMMAND=$MFLUX_BIN/mfcommand


EXP_DOMAIN=nig
OUTFILE=./exported_users_`date +%s`.tcl

### Argument ###
if [ -n "$1" ]; then
	EXP_DOMAIN=$1
fi
if [ -n "$2" ]; then
	OUTFILE=$2
fi

### Logon ###
read -p "Mediaflux LOGON -- Domain:"      MF_AUTH_DOMAIN
read -p "Mediaflux LOGON -- Username:"    MF_USER
read -s -p "Mediaflux LOGON -- Password:" MF_USER_PASSWD
echo ""

$MFCOMMAND logon $MF_AUTH_DOMAIN $MF_USER $MF_USER_PASSWD

### Export ###
EXP_LIST=`psql mfluxdb -c "select * from authentication_users" | grep ${EXP_DOMAIN} | awk '{printf "%s,%s,%s,\n",$1,$3,$5}'`
for token in $EXP_LIST
do
	mf_auth_domain=`echo $token | cut -d , -f 1`
	mf_user=`echo $token | cut -d , -f 2`
	mf_user_password=`echo $token | cut -d , -f 3`
	mf_user_firstname=""
	mf_user_lastname=""
	mf_user_email=""
	echo -n "processing $mf_auth_domain:$mf_user..."
	## user.create ##
	echo "user.create :domain $mf_auth_domain :user $mf_user :password \"$mf_user_password\"" >> $OUTFILE
	## user.set ##
	$MFCOMMAND user.describe :domain $mf_auth_domain :user $mf_user |
	while read line
	do
		if [ "`echo $line | awk '{print $3}'`" == "\"first\""  ]; then
			mf_user_firstname=`echo $line | awk '{print $4}'`;mf_user_firstname=${mf_user_firstname##\"};mf_user_firstname=${mf_user_firstname%%\"}
			continue
		fi
		if [ "`echo $line | awk '{print $3}'`" == "\"last\""  ]; then
			mf_user_lastname=`echo $line | awk '{print $4}'`;mf_user_lastname=${mf_user_lastname##\"};mf_user_lastname=${mf_user_lastname%%\"}
			continue
		fi
		if [ "`echo $line | awk '{print $1}'`" == ":email"  ]; then
			mf_user_email=`echo $line | awk '{print $2}'`;mf_user_email=${mf_user_email##\"};mf_user_email=${mf_user_email%%\"}
			## user.set ##
			echo "user.set :domain $mf_auth_domain :user $mf_user :meta < :mf-user < :name -type first \"$mf_user_firstname\" :name -type last \"$mf_user_lastname\" :email $mf_user_email > >" >> $OUTFILE
			continue
		fi
	done
	## actor.grant ##
	$MFCOMMAND actor.describe :type user :name $mf_auth_domain:$mf_user | 
	while read line
	do
		if [ "`echo $line | awk '{print $3}'`" == "\"role\""  ]; then
                        role=`echo $line | awk '{print $4}'`;role=${role##\"};role=${role%%\"}
			## authorization.role.create
			echo "authorization.role.create :ifexists ignore :role $role" >> $OUTFILE
			## actor.grant ##
			echo "actor.grant :type user :role -type role $role :name $mf_auth_domain:$mf_user" >> $OUTFILE 
                        continue
                fi
	done
	echo "done"
done

### Logoff ###
$MFCOMMAND logoff
