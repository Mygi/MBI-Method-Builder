#!/bin/sh

#######################################
#######################################
##  Author: Wilson Liu               ##
##  Email: wilson@hfi.unimelb.edu.au ##
##  Date: 13/7/2009                  ##
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


MFDOMAIN=nig

##
## Parse arguments
##

until [ -z "$1" ]
do
        case "$1" in
                "--user" | "-u" )
                MFUSER=$2
                shift
                shift
                ;;
                "--domain" | "-d" )
                MFDOMAIN=$2
                shift
                shift
                ;;
                * )
                shift
                ;;
        esac
done

if [[ -z "$MFUSER" || -z "$MFDOMAIN" ]]; then
        echo "USAGE: `basename $0` -u <user>"
        echo "DESCRIPTION:"
        echo "                  -u,--user               username"
        exit
fi


##
## logon to soma mediaflux
##

read -p "Domain(of Admin User):" MF_ADMIN_DOMAIN
read -p "Username(of Admin User):" MF_ADMIN_USERNAME
read -s -p "Password(of Admin User):" MF_ADMIN_PASSWORD
$MFCOMMAND logon $MF_ADMIN_DOMAIN $MF_ADMIN_USERNAME $MF_ADMIN_PASSWORD

# Remove user from PSSD projects and destroy
$MFCOMMAND om.pssd.user.remove :domain $MFDOMAIN :user $MFUSER
$MFCOMMAND user.destroy :domain $MFDOMAIN :user $MFUSER


# logoff from soma mediaflux
$MFCOMMAND logoff 2>/dev/null