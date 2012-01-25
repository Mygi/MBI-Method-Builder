proc install_fcp { namespace name description } {
	if { [xvalue exists [asset.exists :id path=$namespace/$name]] == "true" } {
		asset.set :id path=$namespace/$name \
		          :url archive:$name \
		          :type application/arc-fcp \
		          :description $description
	} else {
		asset.create :url archive:$name \
		          :type application/arc-fcp \
		          :namespace -create true $namespace \
		          :name $name \
		          :description $description
    }
}

proc uninstall_fcp { namespace name } {
	if { [xvalue exists [asset.exists :id path=$namespace/$name]] == "true" } {
		asset.destroy :id path=$namespace/$name
	}
}