#!/bin/sh

JAVA=`which java`
if [[ -z "${JAVA}" ]]; then
        echo "Error: could not find java." >&2
        exit 1
fi

JAR=`dirname $0`/integration-test.jar
if [[ ! -f "${JAR}" ]]; then
        echo "Error: could not find file integration-test.jar." >&2
        exit 1
fi

# MF_HOST specifies the host name or IP address of the Mediaflux
# server.
#MF_HOST=172.23.65.3
MF_HOST=localhost

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



# Uncomment the following to enable general tracing.
# MF_VERBOSE=-verbose

# Configuration [END]:
# =========================

# Basic command
$JAVA -Dmf.host=$MF_HOST -Dmf.port=$MF_PORT -Dmf.transport=$MF_TRANSPORT -jar $JAR

RETVAL=$?
exit $RETVAL
