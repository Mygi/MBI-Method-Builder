#!/bin/sh
# Wrapper for pvconv.pl which converts Bruker to Analyze format
# It is used with the transcoding framework and should
# be located in $MFLUX_HOME/plugin/bin
# This wrapper sets up the environment on host soma for pvconv.pl 
# Modify it as need be for other hosts.

if [ -f "/usr/share/modules/init/sh" ]; then
   . /usr/share/modules/init/sh   
fi

module load pvconv
/usr/local/pvconv/bin/pvconv.pl $@