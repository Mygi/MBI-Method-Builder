#!/bin/sh 

######################################################

upload () 
{
   ex_id=${1}    # CID of parent Ex-Method
      echo "   ExM ID = " $ex_id
#
   MFCOMMAND="/usr/local/MediaFlux/bin/mfcommand"

# Low Mag (Study 3)

   if [ -e "Low-mag" ]; then
      cd "Low-mag"
      St_id="${ex_id}".3
      echo "   Study ID = " $St_id
      file=`ls`          # Only one file
      ${MFCOMMAND} om.pssd.dataset.primary.create :pid ${St_id} :description "Low  magnification Optical Microscopy"  :name "Low magnification images" :type "dataset/tiff" :in file:"${file}"
      cd ..
   fi

# High Mag (Study 4)

   if [ -e "High-mag" ]; then
      cd "High-mag"
      St_id="${ex_id}".4
      echo "   Study ID = " $St_id
      file=`ls`          # Only one file
      ${MFCOMMAND} om.pssd.dataset.primary.create :pid ${St_id} :description "High  magnification Optical Microscopy" :name "High magnification images" :type "dataset/tiff" :in file:"${file}"
      cd ..
   fi

}


doit () {

curDir=`pwd`

# Parse

name="${1}"
ex_id="${2}"
control="${3}"
#
echo "Subject Name    : " $name
echo "  Ex-Method     : " $ex_id
echo "   Control      : " $control

# Descend to Microscopy  data, if any

cd "${name}"
if [ -e "Optical-Microscopy" ]; then
   cd "Optical-Microscopy"

# Descend further for non-controls
   if [ "${control}" = "N" ]; then
      day=`ls`
      cd "${day}"
   fi

   upload ${ex_id}
fi

cd "${curDir}"
}




######################################################

# You must be located in the directory above the 
# "Controls" and "EAE" directories
# Script auto-detects presence of Longitudinal and/or Coronal data
# Script auto-detects presence of Day-<N> layer
#
# Input:
#  $1 is the manaer password
#
###########

#
# Parse inputs
#
pw="${1}"
# Full CID for Project ID
proj_id="${2}"    
#proj_id=1.5.44
#
# Set MFLUX_HOST for remote systems
MFCOMMAND="${MFLUX_HOME}/bin/mfcommand"
${MFCOMMAND} logon system manager "${pw}"

# Controls first

cd Controls
control=Y
subj_id="${proj_id}".1
ex_id="${subj_id}".1
subj_name=MO0025MBH1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".2
subj_name=MO0026MBH1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".3
subj_name=MO0027MBH1  
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".4
subj_name=MO0028MBH1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".5
subj_name=MO0029MBH1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".6
subj_name=MO0030MBH1  
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".7
subj_name=MO0031MBH1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".8
subj_name=MO0032MBH1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".9
subj_name=MO0033MHB1  
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".10
subj_name=MO0034MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".11
subj_name=MO0053MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".12
subj_name=MO0054MHB1  
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".13
subj_name=MO0055MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".14
subj_name=MO0056MHB1  
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".15
subj_name=MO0057MHB1  
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".16
subj_name=MOOCHBC1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".17
subj_name=MOOCHBC2  
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".18
subj_name=MOOCHBC3
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".19
subj_name=MOOCHBC4
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".20
subj_name=MOOCHBC5
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"

# Create EAE Subjects 

cd ..
cd EAE
control=N
#
subj_id="${proj_id}".21
subj_name=MO0009CHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".22
subj_name=MO0010CHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".23
subj_name=MO0014MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".24
subj_name=MO0015MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".25
subj_name=MO0017MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".26
subj_name=MO0018MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".27
subj_name=MO0019MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".28
subj_name=MO0020MBH1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".29
subj_name=MO0021MBH1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".30
subj_name=MO0022MBH1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".31
subj_name=MO0023MBH1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".32
subj_name=MO0024MBH1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".33
subj_name=MO0035MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".34
subj_name=MO0037MBH1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".35
subj_name=MO0040MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".36
subj_name=MO0042MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".37
subj_name=MO0045MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".38
subj_name=MO0046MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".39
subj_name=MO0047MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".40
subj_name=MO0048MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".41
subj_name=MO0049MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".42
subj_name=MO0050MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".43
subj_name=MO0052MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".44
subj_name=MO0058MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".45
subj_name=MO0059MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".46
subj_name=MO0060MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".47
subj_name=MO0061MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".48
subj_name=MO0062MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".49
subj_name=MO0063MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".50
subj_name=MO0064MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".51
subj_name=MO0065MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".52
subj_name=MO0066MHB1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".53
subj_name=MOOCHBE1
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".54
subj_name=MOOCHBE11
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".55
subj_name=MOOCHBE2
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
#
subj_id="${proj_id}".56
subj_name=MOOCHBE3
ex_id="${subj_id}".1
doit "${subj_name}" "${ex_id}" "${control}"
