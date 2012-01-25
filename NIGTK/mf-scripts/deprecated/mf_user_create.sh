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
		"--roles" | "-r" )
		#replace comma with space
		ROLES=${2//\,/\ }
		shift
		shift
		;;
		* )
		shift
		;;
	esac
done
if [[ -z "$DOMAIN" || -z "$MFUSER" || -z "$PASSWORD" || -z "$EMAIL" || -z "$NAME_LAST" || -z "$NAME_FIRST" ]]; then
	echo "USAGE: `basename $0` -d <domain> -u <user> -p <password> -m <email> -fn <firstName> -ln <lastName> [-mn <middleName>] [-r <role1,role2...>]"
	echo "DESCRIPTION:      -d,--domain             authentication domain of the user"
	echo "                  -u,--user               username"
	echo "                  -p,--password           user's password"
	echo "                  -fn,--firstname         user's first name"
	echo "                  -ln,--lastname          user's last name"
	echo "                  -mn,--middlename        user's middle name"
	echo "                  -m,--email              user's email"
	echo "                  -r,--roles              roles to assign to the user, separated by comma"
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

# create user
$MFCOMMAND user.create :domain $DOMAIN :user $MFUSER :password $PASSWORD :meta \< :mf-user \< :name -type first $NAME_FIRST :name -type last $NAME_LAST :email $EMAIL \> \>

# grant role(s)
for ROLE in $ROLES
do
	$MFCOMMAND actor.grant :type user :name $DOMAIN:$MFUSER :role -type role $ROLE
done

# logoff from soma mediaflux
$MFCOMMAND logoff 2>/dev/null
