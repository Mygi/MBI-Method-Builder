
# This script was used to replace doctypes from hfi.* to nig.* (it is part
# of the rebranding process) inside the currently existing Method objects. 

proc replace {cid} {
	nig.asset.doc.string.replace :cid $cid :old "hfi.pssd." :new "nig.pssd." :exact false

}

# Animal-MRI-Simple
proc animalSimple {} {
  set r [xvalue id [om.pssd.method.find :text Animal-MRI-Simple]]
  replace $r
}

# Animal-MRI-Simple-pvt
proc animalSimplePvt {} {
  set r [xvalue id [om.pssd.method.find :text Animal-MRI-Simple-pvt]]
  replace $r
}

# Human-MRI-Simple
proc humanSimple {} {
  set r [xvalue id [om.pssd.method.find :text Human-MRI-Simple]]
  replace $r
}

# Human-MRI-Simple-noRS
proc humanSimplenoRS {} {
  set r [xvalue id [om.pssd.method.find :text Human-MRI-Simple-noRS ]]
  replace $r
}

######### ImageHD ##############
# "Image HD: MRI Studies (Acquisitions)"
proc imageHDMR {} {
  set r [xvalue id [om.pssd.method.find :text "Image HD: MRI Studies (Acquisitions)"]]
  replace $r
}



# "Image HD: Combined Results (CR) Studies"
proc imageHDCR {} {
  set r [xvalue id [om.pssd.method.find :text "Image HD: Combined Results (CR) Studies"]]
  replace $r
}

# "ImageHD Overall Method for Control Group" 
proc imageHDControl {} {
  set r [xvalue id [om.pssd.method.find :text "ImageHD Overall Method for Control Group"]]
  replace $r
}

# "ImageHD Overall Method for Patient Group" 
proc imageHDPatient {} {
  set r [xvalue id [om.pssd.method.find :text "ImageHD Overall Method for Patient Group"]]
  replace $r
}

proc doAllImageHD {} {
	imageHDMR
	imageHDCR
	imageHDControl
	imageHDPatient
}

############# EAE ##############
# "EAE MRI"
proc eaeMR {} {
# The standard query find matches with EAE and MRI so do it the hardway.
#set r [xvalue id [om.pssd.method.find :text "EAE MRI"]]
  set r [xvalue id [asset.query :where (xpath(pssd-object/type)='method' and xpath(pssd-object/name)='EAE MRI')]]
  set t [xvalue cid [nig.asset.cid.get :id $r]]
  replace $t
}

# "EAE Optic Nerve Fix"
proc eaeOpticNerveFix {} {
  set r [xvalue id [om.pssd.method.find :text "EAE Optic Nerve Fix" ]]
  replace $r
}

# "EAE Optical Microscopy"
proc eaeOpticalMicroscopy {} {
  set r [xvalue id [om.pssd.method.find :text "EAE Optical Microscopy"]]
  replace $r
}

# "EAE Electron Microscopy" 
proc eaeElectronMicroscopy {} {
  set r [xvalue id [om.pssd.method.find :text "EAE Electron Microscopy"]]
  replace $r
}

# "EAE Overall Optic Nerve Method" 
proc eaeOverall {} {
  set r [xvalue id [om.pssd.method.find :text "EAE Overall Optic Nerve Method"]]
  replace $r
}

proc doAllEAE {} {
	eaeMR
	eaeOpticNerveFix
	eaeOpticalMicroscopy
	eaeElectronMicroscopy
	eaeOverall
}


#####################################

proc doAll {} {
	animalSimple
	animalSimplePvt
	humanSimple
	humanSimplenoRS
	doAllImageHD
	doAllEAE
}