#!/bin/sh
# Use this script to extract assets from a Mediaflux .aar archive 
# It creates a new directory with the unzipped content (usually DICOM)
# ready for upload with pssd-dicom

usage () {
    echo "USAGE: `basename $0` <archive file>"
    echo "DESCRIPTION:"
    echo "   Extracts the content from a Mediaflux asset archive into a directory ready for upload with pssd-dicom"
    exit
}

# Get input
archive=""
if [ "${1}" ]; then
  archive="${1}"
  if [ ! -e "${archive}" ]; then
     echo "Input file " ${archive} " does not exist - exiting"
     exit
  fi
else
  usage
  exit
fi
# 


# Extract from archive into temporary directory
dateNow=`date`
tF0=./extracted_archive_"${dateNow}"
tF1=$(echo "${tF0}" | sed 's/ /_/g')             # Remove nasty characters
TEMP_DIR=$(echo "${tF1}" | sed 's/:/-/g')
mkdir "${TEMP_DIR}"
if [ ! -d "${TEMP_DIR}" ] ; then
   echo "  Failed to make temporary directory; exiting"
   exit
fi
java -jar $MFLUX_HOME/bin/aar.jar -extract "${archive}" "${TEMP_DIR}"
 
# Remove stuff we don't want
cd "${TEMP_DIR}"
rm -fr ARCHIVE-INF

# Descend (carefully)
if [ ! -d assets ]; then
   echo "Unexpected directory structure; expecting assets/pssd"
   exit
fi
cd assets
if [ ! -d pssd ]; then
   echo "Unexpected directory structure; expecting assets/pssd"
   exit
fi
cd pssd
rm -f *xml

# Extract
files=`ls *.zip`
for file in $files; do
   name=${file%.*}
   dd=../../"${name}"
   mkdir "${dd}"
   unzip $file -d "${dd}"
done
   
# Clean up
cd ../..
if [ ! -d assets ]; then
   echo "Unexpected directory structure; expecting assets/pssd"
   exit
fi
rm -fr assets
cd ..
      
#
echo "Created directory" $TEMP_DIR