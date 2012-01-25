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


DOMAIN=nig
# logon to soma mediaflux
read -p "Domain:" MF_ADMIN_DOMAIN
read -p "Username:" MF_ADMIN_USERNAME
read -s -p "Password:" MF_ADMIN_PASSWORD
$MFCOMMAND logon $MF_ADMIN_DOMAIN $MF_ADMIN_USERNAME $MF_ADMIN_PASSWORD

# create user
$MFCOMMAND user.list :domain $DOMAIN

# logoff from soma mediaflux
$MFCOMMAND logoff 2>/dev/null
