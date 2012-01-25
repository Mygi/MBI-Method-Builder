#!/bin/sh
#
# This wrapper calls the Bruker upload Java client. This scripts sets a
# number of parameters which can be changed by editing.  Anything that is
# an argument to the Jar but not supplied directly by this script can 
# also be passed in.   The syntax is
# mfpvload.sh <options> <source directory>
#
# For example:
# mfpvload.sh -id 81.2 <source directory>
#
# specifies a CID of 81.2
#
JAVA=`which java`
if [[ -z "${JAVA}" ]]; then
        echo "Error: could not find java." >&2
        exit 1
fi

JAR=`dirname $0`/pvupload.jar
if [[ ! -f "${JAR}" ]]; then
        echo "Error: could not find file pvupload.jar." >&2
        exit 1
fi

# MF_HOST specifies the host name or IP address of the Mediaflux
# server.
MF_HOST=172.23.65.3
#MF_HOST=localhost

# MF_PORT specifies the port number for the Mediaflux server.
MF_PORT=8443

# MF_TRANSPORT specifies the type of tranport to use. Transport is
# one of:
#
#   HTTP
#   HTTPS
#   TCPIP
#
MF_TRANSPORT=HTTPS

# The authentication domain.
MF_DOMAIN=system

# The authentication user.
MF_USER=manager

# The authentication password. Fill this is on deployment of script to secure area
MF_PASSWORD=

# The amount of time to wait to see if a corresponding DICOM series
# appears in the server. Specified in seconds.
MF_WAIT=60

# Uncomment the following to enable general tracing.
# MF_VERBOSE=-verbose

# Configuration [END]:
# =========================

# Auto create subjects from CID
# Comment out to disable
AUTO_SUBJECT_CREATE=-auto-subject-create

# Parse NIG-specific meta-data and locate on Subject
# Comment out to disable
NIG_META=-nig-subject-meta-add

# Basic command
CMD=`echo $JAVA -Dmf.host=$MF_HOST -Dmf.port=$MF_PORT -Dmf.transport=$MF_TRANSPORT -Dmf.domain=$MF_DOMAIN -Dmf.user=$MF_USER -Dmf.password=$MF_PASSWORD -jar  $JAR -wait $MF_WAIT $MF_VERBOSE $NIG_META $AUTO_SUBJECT_CREATE $1`

# Do the upload
$CMD
#
RETVAL=$?
exit $RETVAL
