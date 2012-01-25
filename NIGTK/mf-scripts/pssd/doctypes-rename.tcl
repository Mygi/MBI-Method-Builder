
# This script was used to rename doctypes from hfi.* to nig.*
# The code that creates the doctypes is changed separately
if { [xvalue exists [asset.doc.type.exists :type "hfi-bruker-series"]] == "true" } {
   asset.doc.type.rename :new-type nig-bruker-series :old-type "hfi-bruker-series"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi-bruker-study"]] == "true" } {
   asset.doc.type.rename :new-type nig-bruker-study :old-type "hfi-bruker-study"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.project"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.project :old-type "hfi.pssd.project"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.ImageHD.combined"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.ImageHD.combined :old-type "hfi.pssd.ImageHD.combined"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.PET.study"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.PET.study :old-type "hfi.pssd.PET.study"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.anaesthetic"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.anaesthetic  :old-type "hfi.pssd.anaesthetic"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.animal.disease"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.animal.disease :old-type "hfi.pssd.animal.disease"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.animal.genetics"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.animal.genetics   :old-type "hfi.pssd.animal.genetics"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.animal.kill"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.animal.kill   :old-type "hfi.pssd.animal.kill"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.animal.modified-genetics"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.animal.modified-genetics  :old-type "hfi.pssd.animal.modified-genetics"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.animal.subject"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.animal.subject  :old-type "hfi.pssd.animal.subject"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.dicom.series"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.dicom.series   :old-type "hfi.pssd.dicom.series"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.ethics"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.ethics :old-type "hfi.pssd.ethics"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.human.contact"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.human.contact  :old-type "hfi.pssd.human.contact"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.human.education"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.human.education  :old-type "hfi.pssd.human.education"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.human.identity"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.human.identity  :old-type "hfi.pssd.human.identity"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.human.name"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.human.name   :old-type "hfi.pssd.human.name"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.human.subject"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.human.subject   :old-type "hfi.pssd.human.subject"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.identity"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.identity  :old-type "hfi.pssd.identity"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.recovery"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.recovery  :old-type "hfi.pssd.recovery"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.subject"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.subject   :old-type "hfi.pssd.subject"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.subject.exclusion"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.subject.exclusion   :old-type "hfi.pssd.subject.exclusion"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.time-point"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.time-point  :old-type "hfi.pssd.time-point"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.EAE.perfusion"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.EAE.perfusion  :old-type "hfi.pssd.EAE.perfusion"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.EAE.optic-nerve.removal"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.EAE.optic-nerve.removal :old-type  "hfi.pssd.EAE.optic-nerve.removal"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.EAE.optic-nerve.section"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.EAE.optic-nerve.section  :old-type "hfi.pssd.EAE.optic-nerve.section"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.EAE.stain"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.EAE.stain  :old-type "hfi.pssd.EAE.stain"
}
if { [xvalue exists [asset.doc.type.exists :type "hfi.pssd.EAE.microscopy"]] == "true" } {
   asset.doc.type.rename :new-type nig.pssd.EAE.microscopy  :old-type "hfi.pssd.EAE.microscopy"
}
