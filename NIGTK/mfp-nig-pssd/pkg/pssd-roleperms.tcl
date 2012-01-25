# Standard Document Types
set pssd_doc_perms { { document hfi.pssd.subject ACCESS } \
						{ document hfi.pssd.subject PUBLISH } \
						{ document hfi.pssd.subject.exclusion ACCESS } \
						{ document hfi.pssd.subject.exclusion PUBLISH } \
						{ document hfi.pssd.animal.subject ACCESS } \
						{ document hfi.pssd.animal.subject PUBLISH } \
						{ document hfi.pssd.animal.disease ACCESS } \
						{ document hfi.pssd.animal.disease PUBLISH } \
						{ document hfi.pssd.human.subject ACCESS } \
						{ document hfi.pssd.human.subject PUBLISH } \
						{ document hfi.pssd.human.education ACCESS } \
						{ document hfi.pssd.human.education PUBLISH } \
						{ document hfi.pssd.identity ACCESS } \
						{ document hfi.pssd.identity PUBLISH } \
						{ document hfi.pssd.human.identity ACCESS } \
						{ document hfi.pssd.human.identity PUBLISH } \
						{ document hfi.pssd.animal.genetics ACCESS } \
						{ document hfi.pssd.animal.genetics PUBLISH } \
						{ document hfi.pssd.animal.modified-genetics ACCESS } \
						{ document hfi.pssd.animal.modified-genetics PUBLISH } \
						{ document hfi.pssd.anaesthetic ACCESS } \
						{ document hfi.pssd.anaesthetic PUBLISH } \
						{ document hfi.pssd.recovery ACCESS } \
						{ document hfi.pssd.recovery PUBLISH } \
						{ document hfi.pssd.animal.kill ACCESS } \
						{ document hfi.pssd.animal.kill PUBLISH } \
						{ document hfi.pssd.project ACCESS } \
						{ document hfi.pssd.project PUBLISH } \
						{ document hfi.pssd.ethics ACCESS } \
						{ document hfi.pssd.ethics PUBLISH } \
						{ document hfi.pssd.time-point ACCESS } \
						{ document hfi.pssd.time-point PUBLISH } \
						{ document hfi.pssd.ImageHD.combined ACCESS } \
						{ document hfi.pssd.ImageHD.combined PUBLISH } }

# EAE document types
set pssd_EAE_doc_perms { { document hfi.pssd.EAE.perfusion ACCESS } \
						{ document hfi.pssd.EAE.perfusion PUBLISH } \
						{ document hfi.pssd.EAE.stain ACCESS } \
						{ document hfi.pssd.EAE.stain PUBLISH } \
						{ document hfi.pssd.EAE.optic-nerve.removal ACCESS } \
						{ document hfi.pssd.EAE.optic-nerve.removal PUBLISH } \
						{ document hfi.pssd.EAE.optic-nerve.section ACCESS } \
						{ document hfi.pssd.EAE.optic-nerve.section PUBLISH } \
						{ document hfi.pssd.EAE.microscopy ACCESS } \
						{ document hfi.pssd.EAE.microscopy PUBLISH } }

set pssd_svc_perms { { service nig.* ACCESS } \
		     { service nig.* MODIFY } \
                     { service server.database.describe ACCESS } }


#set model_user_role               pssd.model.user
set project_creator_role          pssd.project.create   
set subject_creator_role          pssd.subject.create
set r_subject_admin_role          pssd.r-subject.admin
set r_subject_guest_role          pssd.r-subject.guest
set object_admin_role             pssd.object.admin
set object_guest_role             pssd.object.guest

# User
set domain_model_user_role        nig.pssd.model.user
createRole     $domain_model_user_role
grantRolePerms $domain_model_user_role $pssd_doc_perms
grantRolePerms $domain_model_user_role $pssd_EAE_doc_perms
grantRolePerms $domain_model_user_role $pssd_svc_perms

# DICOM
set dicom_ingest_doc_perms {                    { document hfi.pssd.subject ACCESS } \
						{ document hfi.pssd.subject.exclusion ACCESS } \
						{ document hfi.pssd.animal.subject ACCESS } \
						{ document hfi.pssd.animal.subject PUBLISH } \
						{ document hfi.pssd.animal.disease ACCESS } \
						{ document hfi.pssd.human.subject ACCESS } \
						{ document hfi.pssd.human.education ACCESS } \
						{ document hfi.pssd.identity ACCESS } \
						{ document hfi.pssd.identity PUBLISH } \
						{ document hfi.pssd.human.identity ACCESS } \
						{ document hfi.pssd.animal.genetics ACCESS } \
						{ document hfi.pssd.animal.modified-genetics ACCESS } \
						{ document hfi.pssd.anaesthetic ACCESS } \
						{ document hfi.pssd.recovery ACCESS } \
						{ document hfi.pssd.animal.kill ACCESS } \
						{ document hfi.pssd.ethics ACCESS } \
						{ document hfi.pssd.time-point ACCESS } \
						{ document hfi.pssd.ImageHD.combined ACCESS } }
set dicom_ingest_service_perms { { service nig.pssd.subject.meta.set MODIFY } }
#
set domain_dicom_ingest_role      nig.pssd.dicom-ingest
createRole     $domain_dicom_ingest_role
grantRolePerms $domain_dicom_ingest_role $dicom_ingest_doc_perms
grantRolePerms $domain_dicom_ingest_role $dicom_ingest_service_perms

