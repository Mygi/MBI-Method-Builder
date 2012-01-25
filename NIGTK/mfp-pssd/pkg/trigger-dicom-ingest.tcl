##
## This script is triggered when an NON-PSSD style dicom study is created. It sends
## a email notification to the system administrator to remind the data arrival.
##
set namespace    /dicom
set asset_detail [asset.get :id $id]
set asset_type   [xvalue asset/type  $asset_detail]
set asset_model  [xvalue asset/model $asset_detail]
if { $asset_type == "dicom/study" && $asset_model == "" } {
	# NON-PSSD data received
	set subject "NON-PSSD DICOM study(Asset id=${id}) received in ${namespace}. Retrofit is required."
	set content "Namespace: ${namespace}"
	set content "${content}\nType: dicom/study"
	set content "${content}\nAssetId: ${id}"
	notification.generate :object -type dicom ingest :event results :data < :subject $subject :body $content >
}