proc addPSSDModels { } {
    
    asset.model.update :create true :name om.pssd.project :entity -type om.pssd.project < :member * >
    asset.model.update :create true :name om.pssd.subject :entity -type om.pssd.subject < :member * >
    asset.model.update :create true :name om.pssd.r-subject :entity -type om.pssd.r-subject < :member * >
    asset.model.update :create true :name om.pssd.study :entity -type om.pssd.study < :member * > 
    asset.model.update :create true :name om.pssd.ex-method :entity -type om.pssd.ex-method < :member * >
    asset.model.update :create true :name om.pssd.dataset :entity -type om.pssd.dataset < :member * >
    asset.model.update :create true :name om.pssd.data-object :entity -type om.pssd.data-object < :member * >
    asset.model.update :create true :name om.pssd.method :entity -type om.pssd.method < :member * >
    
}

proc registerModelMetaData {} {
	om.pssd.type.metadata.set :type project :mtype -requirement optional pssd-notification
}