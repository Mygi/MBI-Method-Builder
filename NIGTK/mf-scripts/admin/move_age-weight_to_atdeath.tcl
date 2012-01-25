########################################################################################################################################
# Move hfi.pssd.animal.subject/age to age-at-death
# Move hfi.pssd.animal.subject/weight to weight-at-death
#
# 1. Modify the hfi.pssd.animal.subject Document Type so that it contains both
#     elements age & age-at-death and weight & weight-at-death
# 2. Run this script to migrate and remove 'age' and 'weight' from all assets
# 3. Modify the  hfi.pssd.animal.subject Document Type so that it ontains only
#     elements age-at-death and weight-at-death
########################################################################################################################################


proc doIt  { docType oldName newName } {

	# Find  assets
    set queryString "${docType}/${oldName}"	
	set assetIds [xvalues id [asset.query :size infinity :where ${queryString} has value]]
	
	# Loop over assets and rename
	foreach assetId $assetIds {	
	    nig.asset.doc.element.rename :id $assetId :doc $docType :old $oldName :new $newName    
	 }
}
    
    
doIt "hfi.pssd.animal.subject" "age" "age-at-death"
doIt "hfi.pssd.animal.subject" "weight" "weight-at-death"
