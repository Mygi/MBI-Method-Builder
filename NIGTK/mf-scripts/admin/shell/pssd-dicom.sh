#!/bin/sh

usage () {
    echo "USAGE: `basename $0` -id <cid> -f <zip file> -m <mode> -h <host> -p <port>"
    echo "DESCRIPTION:"
    echo "   Uploads DICOM data to the specified DICOM server.  If the server is a Mediaflux"
    echo "       DICOM server, it will ingest the data into the correct Project if a CID (-id) is given"
    echo " "
    echo "                  -id     Citeable ID of parent object (1.5.P.S or 1.5.P.S.Ex or 1.5.P.S.ExM.S)"
    echo "                          If mode=client, may be not given, and then the data are uploaded with no meta-data edit"
    echo "                  -full-id The DICOM server (client mode only) expects a full S.N.P.S etc CID. Otherwise (and the default) it"
    echo "                          will strip off the first 2 integers S.N"
    echo "                  -f      If mode=service, specifies  a single Zip file holding the DICOM files (no subfolders) or"
    echo "                                     a directory which holds DICOM files (can have subdirectories)"
    echo "                          If mode=client, specifies a directory which holds DICOM files (can have subdirectories)  or"
    echo "                                 a list of DICOM files to be uploaded; wildcards in the list must be escaped"
    echo "                  -m      Upload mode; 'service' or 'client' (default)"
    echo "                          service: data are zipped up if needed and directly uploaded. Restricted"
    echo "                               to content size of 2GB; upload 1 Series at a time if too large"
    echo "                               Requires Mediaflux login and data cannot be uploaded to unauthorized Project"
    echo "                          client: a DICOM client is used; data are copied, meta-data edited and"  
    echo "                               then uploaded.  No Mediaflux login and data can be uploaded"
    echo "                               to an unauthorized Project"
    echo "                  -h      host running DICOM server to upload to (defaults to localhost; use dicom-soma.ee.unimelb.edu.au for external soma host)"
    echo "                  -p      port for DICOM server  (defaults to 6666; use 6667 for external soma port)"
    echo "                  -k      Do not clean up temporary files."
    echo "                  -nochk  Don't check that input files are valid DICOM files (will speed up process)"
    echo "                  -calledAET  If using client mode, the called (i.e. the server's) AETitle. Defaults to HFI"
    echo "                  -callingAET   If using client mode, the calling (i.e. the client's) AETitle. Defaults to HFI-DICOM-TEST"
    exit
}


findUtilities ()
{

# Try to find Mediaflux shell wrapper - only needed for mode='service'
MFLUX_HOME=/usr/local/MediaFlux/mflux
MFCOMMAND=/usr/local/MediaFlux/bin/mfcommand


# Set up paths to utilities.  Cope with machines with 'modules' and not
# If no modules, then user must have set path already.
# DCMTOOLS usually in /usr/local/dcmtools/bin

	if [ -f "/usr/share/modules/init/sh" ]; then
	   . /usr/share/modules/init/sh   
	fi
	if [ ! -z $MODULESHOME ]; then
	
	   # Use modules to locate
	   module load dcmtools
	fi
   
# Edit meta-data with DCMODIFY
   DCMODIFY=`which DCMODIFY`       # DCMTOOLS
   DCMCHECK=`which DCMCHK`         # not always required
   #
   if [ -z $DCMODIFY ]; then
        echo "Error: cannot find DCMODIFY binary"
        exit 1
   fi
   
   # DICOM client
   STORESCU=`which STORESCU`
   if [ -z $STORESCU ]; then
        echo "Error: cannot find STORESCU binary"
        exit 1
   fi
   
}

mf_login ()
{
# logon to soma mediaflux
echo -n "Mediaflux Username:"
read MF_USERNAME
echo -n "Mediaflux Password:"
read -s MF_PASSWORD
MF_DOMAIN=nig
if [ ${MF_USERNAME} = "manager" ]; then
   MF_DOMAIN="system"
fi
$MFCOMMAND logon $MF_DOMAIN $MF_USERNAME $MF_PASSWORD
echo ""

}

copyValidDICOMFile ()
{
   local doCheck="${1}"
   local file="${2}"
   if [ $doCheck = "1" ]; then
       chk=`${DCMCHECK} ${file} | grep "isDicomFile = true"`
       l=${#chk}               # Length will be 0 (false) or >0 (true)     
       if [ $l = "0" ]; then
          echo "Dropping non DICOM file" $file
       else
          cp "${file}" "${TEMP_DIR}"/${COUNT}.dcm   
          let COUNT=$COUNT+1                 
       fi
    else
       cp "${file}" "${TEMP_DIR}"/${COUNT}.dcm 
       let COUNT=$COUNT+1      
    fi  
}

#
# Copy all the input files to a temporary location
# ready for zipping or editing
#
copyFilesToTmp () 
{

# Create temporary directory

   dateNow=`date`
   tF0=/tmp/pssd-dicom_"${dateNow}"
   tF1=$(echo "${tF0}" | sed 's/ /_/g')             # Remove nasty characters
   TEMP_DIR=$(echo "${tF1}" | sed 's/:/-/g')
   mkdir "${TEMP_DIR}"
   if [ ! -d "${TEMP_DIR}" ] ; then
      echo "  Failed to make temporary directory; exiting"
      exit
   fi
   
# See if we are going to check DICOM files for validity

   local doCheck=1  
   if [ ${DOCHECK} = "1" ]; then
      if [ -z $DCMCHECK ]; then
         echo "DCMCHECK binary not found; files will not be checked to be valid DICOM files"
         doCheck=0
      fi
   else
      doCheck=0
   fi
  
   COUNT=0
   if [ -d "${FILE}" ]; then

# The use has given us a directory. Copy all the files and flatten out any folder structure
# Because files will most likely be degenerately named in the original, change their output names  to a counter


      echo "Copy  files to  " $TEMP_DIR
      local files=`find "${FILE}" -type f`
      IFS=$'\n'     # Set delimieter to EOL not space do FOR loop can handle files with spaces 
      for file in $files ; do
         copyValidDICOMFile $doCheck $file
      done
   else 

# If we have a file or a list of files copy to the  temporary directory
# If wild carded, must be escaped on calling pssd-dicom

      files=`ls $FILE`
      echo "Copy  files to  " $TEMP_DIR
      IFS=$'\n'
      for file in $files ; do
          copyValidDICOMFile $doCheck $file
      done
   fi
   if [ $COUNT = "0" ]; then
      echo "There are no valid DICOM files to upload"
      exit 1
   fi

# Change to the temporary directory and fix any dodgy permissions

   cd "${TEMP_DIR}"
   chmod u=rwx *.dcm

}


dicom_service  ()
# 
# Upload via dicom.ingest service
#
{

   tt=`pwd` 
   cleanZip=0

# See if we have a folder or a directory
# Zip up folders else upload directly


   zipFile=${FILE}
   if [ -d "${FILE}" ]; then
    
      # Copy files to /tmp and cd to temporary directory
      copyFilesToTmp

      # Create zip
      dateNow=`date`
      zipFile0=/tmp/pssd-dicom_"${dateNow}".zip
      zipFile1=$(echo "${zipFile0}" | sed 's/ /_/g')             # Remove nasty characters
      zipFile=$(echo "${zipFile1}" | sed 's/:/-/g')
      echo "zip up files in folder to " $zipFile   
      zip "${zipFile}" *.dcm
      cleanZip=1
#
      if [ ! -f "${zipFile}" ]; then
         echo "Zip file not created"
         exit
      fi
   else
      echo "Direct upload of zip file"
   fi

# Upload

   mf_login
   $MFCOMMAND dicom.ingest :engine nig.dicom :arg -name nig.dicom.id.citable "${CID}" :in file:"${zipFile}"
   $MFCOMMAND logoff 2>/dev/null

# Clean up

   if [ ${KEEP_FILES} = 0 ];then
       rm -fr "${TEMP_DIR}"
      if [ $cleanZip = 1 ]; then
           rm "${zipFile}"
      fi
   fi

#
   cd "${tt}"
}




dicom_client  ()
# 
# Upload via dicom client
#
{
   local tt=`pwd`

# Strip off the first two values of the CID if requested. This
# is because the user has provided a fully qualified CID, but the DICOM
# engine may add the "1.5" prefix on soma (this is bad).

   cid2=$CID
   if [ ${FULL_ID} = 0 ];then
      cid1=${CID#*.}
      cid2=${cid1#*.}
   fi

# Copy files to /tmp and cd to temporary directory

   copyFilesToTmp

#  Edit meta-data Patient Name field to hold the CID  

   if [ "${CID}" ]; then
   echo "cid2=" $cid2
       name="^${cid2}"
 	   echo "Modify meta-data with DCMTOOLS"       
	   $DCMODIFY "(0010,0010)=${name}"  *.dcm                 # DCMTOOLS does all in one call :-)
     else 
       echo "Meta-data unmodified"
    fi

# Upload
	
    echo "Uploading " $COUNT " files to Mediaflux on server " $DICOM_SERVER " with port " $DICOM_PORT
    "${STORESCU}" -callingAETitle $callingAET -calledAETitle $calledAET -host $DICOM_SERVER  -port $DICOM_PORT  *.dcm

# Clean up

   if [ ${KEEP_FILES} = 0 ];then
	   rm -fr "${TEMP_DIR}"
   fi
   cd "${tt}"
}



# Main 
# Script to allow users to upload DICOM data to the specified CID


# Set defaults
host=`hostname`
DICOM_SERVER=localhost
DICOM_PORT=6666
#if [ "${host}" != "soma" ]; then
#   DICOM_PORT=6667
#   DICOM_SERVER=dicom-soma.ee.unimelb.edu.au
#fi
MODE=client
KEEP_FILES=0
DOCHECK=1
callingAET=HFI-DICOM-TEST
calledAET=HFI
FULL_ID=0

# Parse inputs
until [ -z "$1" ]
do
    case "$1" in
        "--id" | "-id" )
        CID=$2
        shift
        ;;
        "--full-id" | "-full-id" )
        FULL_ID=1
        shift
        ;;
        "--file" | "-f" )
        FILE=$2
        shift
        ;;
        "--mode" | "-m" )
        MODE=$2
        shift
        ;;
        "--host" | "-h" )
        DICOM_SERVER=$2
        shift
        ;;
        "--port" | "-p" )
        DICOM_PORT=$2
        shift
        ;;
        "--callingAET" | "-callingAET" )
        callingAET=$2
        shift
        ;;
        "--calledAET" | "-calledAET" )
        calledAET=$2
        shift
        ;;
		"--keep" | "-k" )
		KEEP_FILES=1
		shift
		;;
		"--nochk" | "-nochk" )
		DOCHECK=0
		shift
		;;
        * )
        shift
        ;;
    esac
done
if [ -z "$FILE" -o -z "$MODE" ]; then
   echo "Error: -f and  -m must be specified"
   usage
fi
if [ "${MODE}" = "service" -a -z "$CID" ]; then
   echo "Error: -id must be specified with -m = service"
   usage
fi

echo "Arguments:"
echo "  id =" $CID
echo "  full-id = " $FULL_ID
echo "  file =" $FILE
echo "  mode =" $MODE
echo "  keep =" $KEEP_FILES
echo "  check =" $DOCHECK
echo "  dicom server =" $DICOM_SERVER
echo "  dicom port =" $DICOM_PORT
echo "  callingAET =" $callingAET
echo "  calledAET  =" $calledAET
echo ""


# Find utility binaries
findUtilities

if [ $MODE = "client" ]; then
   echo "Using DICOM client"
   dicom_client
else
   echo "Using DICOM service"
   dicom_service
fi