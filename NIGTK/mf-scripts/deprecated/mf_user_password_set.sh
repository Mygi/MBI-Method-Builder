#!/bin/sh

#######################################
#######################################
##  Author: Wilson Liu               ##
##  Email: wilson@hfi.unimelb.edu.au ##
##  Date: 5/5/2007                   ## 
#######################################
#######################################
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


until [ -z "$1" ]
do
	case "$1" in
		"--user" | "-u" )
		MFUSER=$2
		shift
		shift
		;;
		"--domain" | "-d" )
		DOMAIN=$2
		shift
		shift
		;;
		"--oldpassword" | "-op" )
		OLDPASSWORD=$2
		shift
		shift
		;;
		"--password" | "-p" )
		PASSWORD=$2
		shift
		shift
		;;
		* )
		shift
		;;
	esac
done
if [[ -z "$DOMAIN" || -z "$MFUSER" || -z "$PASSWORD" ]]; then
	echo "USAGE: `basename $0` -d <domain> -u <user> -op <oldpassword> -p <password> -m <email>"
	echo "DESCRIPTION:      -d,--domain             authentication domain of the user"
	echo "                  -u,--user               username"
	echo "                  -op,--oldpassword       user's old password. required if you are not a domain manager."
	echo "                  -p,--password           user's password"
	exit
fi

# logon to soma mediaflux
echo -n "Domain:"
read MF_ADMIN_DOMAIN
echo -n "Username:"
read MF_ADMIN_USERNAME
echo -n "Password:"
read MF_ADMIN_PASSWORD
$MFCOMMAND logon $MF_ADMIN_DOMAIN $MF_ADMIN_USERNAME $MF_ADMIN_PASSWORD

# set user password
if [[ -z "$OLDPASSWORD" ]]; then
	$MFCOMMAND user.password.set :domain $DOMAIN :user $MFUSER :password $PASSWORD
else
	$MFCOMMAND user.password.set :domain $DOMAIN :user $MFUSER :old-password $OLDPASSWORD :password $PASSWORD
fi

# logoff from soma mediaflux
$MFCOMMAND logoff 2>/dev/null
