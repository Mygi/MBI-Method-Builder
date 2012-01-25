#!/bin/sh

#######################################
#######################################
##  Author: Wilson Liu               ##
##  Email: wilson@hfi.unimelb.edu.au ##
##  Date: 23/5/2007                  ## 
#######################################
#######################################
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
		"--firstname" | "-fn" )
		NAME_FIRST=$2
		shift
		shift
		;;
		"--lastname" | "-ln" )
		NAME_LAST=$2
		shift
		shift
		;;
		"--middlename" | "-mn" )
		NAME_MIDDLE=$2
		shift
		shift
		;;
		"--email" | "-m" )
		EMAIL=$2
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
if [[ -z "$MFUSER" || -z "$PASSWORD" || -z "$EMAIL" || -z "$NAME_LAST" || -z "$NAME_FIRST" ]]; then
	echo "USAGE: `basename $0` -u <user> -p <password> -m <email> -fn <firstName> -ln <lastName> [-mn <middleName>]"
	echo "DESCRIPTION:"
	echo "                  -u,--user               username"
	echo "                  -p,--password           user's password"
	echo "                  -fn,--firstname         user's first name"
	echo "                  -ln,--lastname          user's last name"
	echo "                  -mn,--middlename        user's middle name"
	echo "                  -m,--email              user's email"
	exit
fi

# logon to soma mediaflux
read -p "Domain:" MF_ADMIN_DOMAIN
read -p "User:" MF_ADMIN_USERNAME
read -s -p "Password:" MF_ADMIN_PASSWORD
$MFCOMMAND logon $MF_ADMIN_DOMAIN $MF_ADMIN_USERNAME $MF_ADMIN_PASSWORD

# create user
DOMAIN=nig
$MFCOMMAND nig.user.create :domain $DOMAIN :user $MFUSER :password $PASSWORD :name -type first $NAME_FIRST :name -type last $NAME_LAST :email $EMAIL

# logoff from soma mediaflux
$MFCOMMAND logoff 2>/dev/null
