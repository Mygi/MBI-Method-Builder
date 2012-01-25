
# This script was used to replace doctypes from hfi.* to nig.* (it is part
# of the rebranding process) inside all currently existing ExMethod objects. 


asset.query :where xpath(pssd-object/type)='ex-method'  :action pipe :service -name nig.asset.doc.string.replace < :old "hfi.pssd." :new "nig.pssd." :exact false >