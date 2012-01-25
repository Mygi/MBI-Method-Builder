#!/bin/sh

# Script to allow users to call the nig.pssd.project.dmf service from the commandline

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

if [ -z $JAVA_HOME ]; then
   echo "Please set up the MFLUX_HOME variable"
  exit
fi
export PATH=$JAVA_HOME/bin:$PATH

until [ -z "$1" ]
do
	case "$1" in
		"--id" | "-id" )
		CID=$2
		shift
		;;
		"--action" | "-a" )
		ACTION=$2
		shift
		;;
		* )
		shift
		;;
	esac
done
if [[ -z "$CID" || -z "$ACTION" ]]; then
	echo "USAGE: `basename $0` -id <cid> -a <action>"
	echo "DESCRIPTION:"
        echo "   Moves Mediaflux data managed by DMF between offline (tape) and online (disk)"
        echo " "
	echo "                  -id     Citeable ID of parent object (all children are moved as well)"
        echo "                          Can be specified at any level in hierarchy"
        echo "                          E.g. a  Project such as 1.5.60 (take care, could retrieve a lot of data)"
        echo "                          E.g. a  Subject such as 1.5.60.2"
        echo "                          E.g. an ExMethod such as 1.5.60.2.1"
        echo "                          E.g. a  Study such as 1.5.60.2.1.7"
        echo "                          E.g. a  DataSet such as 1.5.60.2.1.7.12"
	echo "                  -a      Action to take 'offline',  'online' or 'status'"
	exit
fi

# The cid will be checked by the service, but we can check the action here.
if [ ! "${ACTION}" = "online" -a ! "${ACTION}" = "offline" -a ! "${ACTION}" = "status" ]; then
   echo "The action argument must be 'online' or 'offline'"
   exit
fi

# logon to soma mediaflux
echo -n "Mediaflux Username:"
read MF_USERNAME
echo -n "Mediaflux Password:"
read -s MF_PASSWORD
$MFCOMMAND logon "nig" $MF_USERNAME $MF_PASSWORD


# Do the work
if [ "${ACTION}" = "online" -o "${ACTION}" = "offline" ]; then
   echo "Moving all data for " $CID " and children " $ACTION
else
   echo "Finding status for all data for " $CID " and children "
fi
if [ "${ACTION}" = "online" ]; then
   $MFCOMMAND om.pssd.dmf.get :id $CID
elif [ "${ACTION}" = "offline" ]; then
   $MFCOMMAND om.pssd.dmf.put :id $CID
else
   $MFCOMMAND om.pssd.dmf.status :id $CID :recursive true
fi

# logoff 
$MFCOMMAND logoff 2>/dev/null

