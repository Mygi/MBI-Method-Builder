#!/bin/sh

##############################################################################
##############################################################################
##                                                                          ## 
##                               SETTINGS                                   ##
##                                                                          ##
##############################################################################
##############################################################################

JAVA_HOME=/usr/local/MediaFlux/java/jre

PATH=$JAVA_HOME/bin:$PATH

export JAVA_HOME=$JAVA_HOME
export PATH=$PATH

HOSTNAME=`hostname`

MFLUX_HOME=/usr/local/MediaFlux/mflux-xodb

MFLUX_SID_FILE=~/.MFLUX_SID_$HOSTNAME

MFLUX_HOST=soma

MFLUX_PORT=8443

MFLUX_TRANSPORT=HTTPS

DEFAULT_MFLUX_NAMESPACES="dicom system/users pssd"

DEFAULT_BACKUP_ROOT=/store/mediaflux/backup

DEFAULT_BACKUP_PREFIX=mf_xodb_export

DEFAULT_BACKUP_PARTS=all

## If it is enabled with yes, the backup path will be save to the backup list file which is located at $BACKUP_ROOT/${BACKUP_PREFIX}-backup.list
SAVE_TO_BACKUP_LIST=no

##############################################################################
##############################################################################
##                                                                          ## 
##                               FUNCTIONS                                  ##
##                                                                          ##
##############################################################################
##############################################################################

# Function: logon
#
logon() {
   
    if test -f "$MFLUX_SID_FILE" 
    then {
      logoff
    }
    fi

    MFLUX_SID=`java -Djava.net.preferIPv4Stack=true -Dmf.host=$MFLUX_HOST -Dmf.port=$MFLUX_PORT -Dmf.transport=$MFLUX_TRANSPORT -cp $MFLUX_HOME/bin/aterm.jar arc.mf.command.Execute logon $1 $2 $3`
    RETVAL=$?

    case $RETVAL in 
      0) echo $MFLUX_SID >> "$MFLUX_SID_FILE"
      ;;
      2) echo "Authentication failure"
      ;;
    esac
}

# Function: execute
#
#  This executes an arbitrary command.
#
execute() {

    if test -f "$MFLUX_SID_FILE"
    then {
      
      MFLUX_SID=`cat "$MFLUX_SID_FILE"`

      java -Djava.net.preferIPv4Stack=true -Dmf.host=$MFLUX_HOST -Dmf.port=$MFLUX_PORT -Dmf.transport=$MFLUX_TRANSPORT -Dmf.sid=$MFLUX_SID -Dmf.result=shell -cp $MFLUX_HOME/bin/aterm.jar arc.mf.command.Execute execute $* 

      RETVAL=$?

      case $RETVAL in 
        3) echo "Session has timed out - need to logon again."; 
           rm -f "$MFLUX_SID_FILE"
        ;;
      esac

    } else {

      echo "Not logged on"

      RETVAL=1      
    }
    fi

}


# Function: logoff
#
logoff() {

    if test -f "$MFLUX_SID_FILE"
    then {
      MFLUX_SID=`cat "$MFLUX_SID_FILE"`

      # Remove the file now..
      rm -f "$MFLUX_SID_FILE"

      java -Djava.net.preferIPv4Stack=true -Dmf.host=$MFLUX_HOST -Dmf.port=$MFLUX_PORT -Dmf.transport=$MFLUX_TRANSPORT -Dmf.sid=$MFLUX_SID -cp $MFLUX_HOME/bin/aterm.jar arc.mf.command.Execute logoff

      RETVAL=$?
    } else {

      echo "Not logged on"

      RETVAL=1      
    }
    fi

}


##############################################################################
##############################################################################
##                                                                          ## 
##                               MAIN                                       ##
##                                                                          ##
##############################################################################
##############################################################################

##
## Retrieve arguments
##
until [ -z "$1" ]
do
	case "$1" in
		"--domain" | "-d" )
		MFLUX_AUTH_DOMAIN=$2
		shift
		shift
		;;
		"--user" | "-u" )
		MFLUX_AUTH_USER=$2
		shift
		shift
		;;
		"--password" | "-p" )
		MFLUX_USER_PASSWD=$2
		shift
		shift
		;;
		"--backup-root-dir" )
		BACKUP_ROOT=$2
		shift
		shift
		;;
		"--backup-prefix" )
		BACKUP_PREFIX=$2
		shift
		shift
		;;
		"--namespaces" )
		MFLUX_NAMESPACES=${2//\,/\ }
		shift
		shift
		;;
		"--save-to-backup-list" )
		SAVE_TO_BACKUP_LIST=yes
		shift
		;;
		"--backup-parts" )
		BACKUP_PARTS=$2
		shift
		shift
		;;
		* )
		shift
		;;
	esac
done

##
## Mediaflux System Administrator Login is required
##
if [[ -z "$MFLUX_AUTH_DOMAIN" ]]; then
	read -p "Mediaflux LOGON -- Domain:"      MFLUX_AUTH_DOMAIN
fi
if [[ -z "$MFLUX_AUTH_USER" ]]; then
	read -p "Mediaflux LOGON -- Username:"    MFLUX_AUTH_USER
fi
if [[ -z "$MFLUX_USER_PASSWD" ]]; then
	read -s -p "Mediaflux LOGON -- Password:" MFLUX_USER_PASSWD
fi

##
## Namespaces to export
## 
if [[ -z "$MFLUX_NAMESPACES" ]]; then
	MFLUX_NAMESPACES=$DEFAULT_MFLUX_NAMESPACES
fi

##
## The directory to store the backup
##
if [[ -z "$BACKUP_ROOT" ]]; then
	BACKUP_ROOT=$DEFAULT_BACKUP_ROOT
fi

##
## The prefix of the backup
##
if [[ -z "$BACKUP_PREFIX" ]]; then
	BACKUP_PREFIX=$DEFAULT_BACKUP_PREFIX
fi

##
## Select which parts to backup (all, content, meta, default is all)
##
if [[ -z "$BACKUP_PARTS" ]]; then
	BACKUP_PARTS=$DEFAULT_BACKUP_PARTS
fi

##
## The backup folder
##
BACKUP_DIR=$BACKUP_ROOT/$BACKUP_PREFIX-`date +%Y%m%d%H%M%S`

##
## The backup list file
##
BACKUP_LIST=$BACKUP_ROOT/${BACKUP_PREFIX}-backup.list

##
## Actions
##
logon $MFLUX_AUTH_DOMAIN $MFLUX_AUTH_USER $MFLUX_USER_PASSWD
mkdir -p $BACKUP_DIR
# execute server.database.lock :action wait :msg "Backuping database, please wait..."
for namespace in $MFLUX_NAMESPACES
do
	echo "backing up $namespace..."
	execute  asset.archive.create :parts $BACKUP_PARTS :cref true :where "namespace>=$namespace" :out $BACKUP_DIR/MF_Archive-${namespace//\//_}.aar
	echo "done."
	echo -n  "encrypting backup file..."
	gpg -o $BACKUP_DIR/MF_Archive-${namespace//\//_}.aar.gpg --encrypt -r 5C3F7182 $BACKUP_DIR/MF_Archive-${namespace//\//_}.aar
	rm -f $BACKUP_DIR/MF_Archive-${namespace//\//_}.aar
	echo "done"
done
# execute server.database.unlock
##
## Backup logs
##
tar -czvf $BACKUP_DIR/logs.tar.gz $MFLUX_HOME/volatile/logs/*

if [[ "$SAVE_TO_BACKUP_LIST" == "yes" ]]; then
	echo "$BACKUP_DIR" >> $BACKUP_LIST
fi
logoff
