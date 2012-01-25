package nig.mf.plugin.pssd;

import java.util.Collection;
import java.util.Vector;

import nig.mf.plugin.pssd.dicom.DicomAssetHandlerFactory;
import nig.mf.plugin.pssd.services.*;
import arc.mf.plugin.ConfigurationResolver;
import arc.mf.plugin.PluginModule;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dicom.DicomAssetEngineRegistry;
import arc.mf.plugin.event.FilterRegistry;

public class PSSDPluginModule implements PluginModule {

	private Collection<PluginService> _services = null;

	public String description() {

		return "PSSD Object Model.";
	}

	@Override
	public void initialize(ConfigurationResolver config) throws Throwable {

		_services = new Vector<PluginService>();

		_services.add(new SvcRoleTypeDescribe());
		_services.add(new SvcRoleList());
		_services.add(new SvcRoleCleanup());

		_services.add(new SvcUserList());
		_services.add(new SvcUserDescribe());
		_services.add(new SvcUserCanCreate());
		_services.add(new SvcUserCanEdit());
		_services.add(new SvcUserRemove());
		_services.add(new SvcUserRoleGrant());
		_services.add(new SvcUserRoleRevoke());
		_services.add(new SvcUserCreate());

		_services.add(new SvcModelTypesList());
		_services.add(new SvcTypeMetadataSet());
		// _services.add(new SvcTypeMetadataRemove());
		_services.add(new SvcTypeMetadataDescribe());

		_services.add(new SvcMethodCreate());
		_services.add(new SvcMethodForSubjectCreate());
		_services.add(new SvcMethodUpdate());
		_services.add(new SvcMethodForSubjectUpdate());
		_services.add(new SvcMethodList());
		_services.add(new SvcMethodDescribe());
		_services.add(new SvcMethodSubjectMetadataDescribe());
		_services.add(new SvcMethodDestroy());
		_services.add(new SvcMethodUseCount());
		_services.add(new SvcMethodFind());

		_services.add(new SvcProjectRootId());

		_services.add(new SvcProjectCreate());
		_services.add(new SvcProjectDestroy());
		_services.add(new SvcProjectInternalize());
		_services.add(new SvcProjectMembersList());
		_services.add(new SvcProjectMembersReplace());
		_services.add(new SvcProjectMembersRemove());
		_services.add(new SvcProjectMembersAdd());

		_services.add(new SvcProjectUpdate());
		_services.add(new SvcProjectRoles());
		_services.add(new SvcProjectDataUseRoles());
		// _services.add(new SvcProjectSetCid()); // Service has some flaws. See code [nebk]
		_services.add(new SvcProjectRolesCreate());
		_services.add(new SvcProjectSetModel());
		_services.add(new SvcProjectMailSend());
		_services.add(new SvcProjectRSubjectFind());
		_services.add(new SvcProjectMetaDataHarvest());
		//
		_services.add(new SvcProjectMethodReplace());      // Method migration service
		_services.add(new SvcProjectMembersMetaRemove()); // One off transition service

		_services.add(new SvcRSubjectCreate());
		_services.add(new SvcRSubjectFind());
		_services.add(new SvcRSubjectAdminAdd());
		_services.add(new SvcRSubjectAdminRemove());
		_services.add(new SvcRSubjectGuestAdd());
		_services.add(new SvcRSubjectGuestRemove());
		_services.add(new SvcRSubjectCleanup());

		_services.add(new SvcSubjectCreate());
		_services.add(new SvcSubjectUpdate());
		// _services.add(new SvcSubjectStateCreate()); // Service not utilised
		_services.add(new SvcSubjectStateSet());
		_services.add(new SvcSubjectMethodFind());

		_services.add(new SvcExMethodCreate());
		_services.add(new SvcExMethodUpdate());

		_services.add(new SvcExMethodStepDescribe());
		_services.add(new SvcExMethodStepList());
		_services.add(new SvcExMethodStepUpdate());
		_services.add(new SvcExMethodStudyStepFind());
		_services.add(new SvcExMethodStepStudyFind());
		_services.add(new SvcExMethodStudyTypeList());
		_services.add(new SvcExMethodSubjectStepFind());
		_services.add(new SvcExMethodSubjectStepUpdate());
		
		_services.add(new SvcExMethodReplaceMethod());

		_services.add(new SvcStudyCreate());
		_services.add(new SvcStudyUpdate());
		_services.add(new SvcStudyMove());
		_services.add(new SvcStudyRename());
		// _services.add(new SvcStudiesPreCreate()); // Service not fully
		// implemented; nebk

		_services.add(new SvcDataSetPrimaryCreate());
		_services.add(new SvcDataSetPrimaryUpdate());
		_services.add(new SvcDataSetDerivationCreate());
		_services.add(new SvcDataSetDerivationUpdate());
		_services.add(new SvcDataSetMove());
		_services.add(new SvcDataSetCount());

		_services.add(new SvcDataObjectCreate());
		_services.add(new SvcObjectMetaCopy());
		_services.add(new SvcObjectCSVExport());
		_services.add(new SvcCollectionMembers());
		_services.add(new SvcCollectionMemberList());
		_services.add(new SvcObjectExists());
		_services.add(new SvcObjectType());
		_services.add(new SvcObjectDescribe());
		_services.add(new SvcObjectDestroy());
		_services.add(new SvcObjectFind());
		_services.add(new SvcObjectUpdate());
		_services.add(new SvcObjectIconGet());
		_services.add(new SvcObjectIsReplica());
		_services.add(new SvcObjectHasRemoteChildren());
		_services.add(new SvcObjectLock());
		_services.add(new SvcObjectUnlock());
		_services.add(new SvcObjectSessionLock());
		_services.add(new SvcObjectSessionLocked());
		_services.add(new SvcObjectSessionUnlock());

		_services.add(new SvcObjectAttach());
		_services.add(new SvcObjectDetach());
		_services.add(new SvcObjectAttachmentGet());
		_services.add(new SvcObjectAttachmentList());

		_services.add(new SvcStudyTypeCreate());
		_services.add(new SvcStudyTypeDestroy());
		_services.add(new SvcStudyTypeDescribe());
		_services.add(new SvcStudyFind());

		_services.add(new SvcRoleMemberRegAdd());
		_services.add(new SvcRoleMemberRegRemove());
		_services.add(new SvcRoleMemberRegList());
		_services.add(new SvcRoleMemberRegDestroy());
		_services.add(new SvcRoleMemberRegID());

		_services.add(new SvcObjectDMFGet());
		_services.add(new SvcObjectDMFPut());
		_services.add(new SvcObjectDMFStatus());

		_services.add(new SvcProjectMetadataDescribe());
		_services.add(new SvcSubjectMetadataDescribe());
		_services.add(new SvcStudyMetadataDescribe());

		_services.add(new SvcShoppingCartTemplateCreate());
		_services.add(new SvcShoppingCartTemplateDestroy());
		_services.add(new SvcShoppingCartCreate());
		_services.add(new SvcShoppingCartUpdate());
		_services.add(new SvcShoppingCartDestroy());
		_services.add(new SvcShoppingCartDescribe());
		_services.add(new SvcShoppingCartList());
		_services.add(new SvcShoppingCartContentList());
		_services.add(new SvcShoppingCartContentAdd());
		_services.add(new SvcShoppingCartContentRemove());
		_services.add(new SvcShoppingCartContentClear());
		_services.add(new SvcShoppingCartOrder());
		_services.add(new SvcShoppingCartOutputRetrieve());
		_services.add(new SvcShoppingCartDestinationList());
		_services.add(new SvcShoppingCartCleanup());

		_services.add(new SvcRepositoryDescriptionSet());
		_services.add(new SvcRepositoryDescribe());
		_services.add(new SvcRepositoryDescriptionDestroy());
		_services.add(new SvcRepositoryDescriptionID());

		_services.add(new SvcDICOMSend());
		_services.add(new SvcDomainGrant());
		_services.add(new SvcDomainRevoke());
		_services.add(new SvcDICOMAERegID());
		_services.add(new SvcDICOMAEAdd());
		_services.add(new SvcDICOMAERemove());
		_services.add(new SvcDICOMAERegList());
		_services.add(new SvcDICOMAERegDestroy());
		_services.add(new SvcDICOMDataSetCount());
		
		//
		_services.add(new SvcDICOMControls());
		_services.add(new SvcDICOMUserCreate());
		
		_services.add(new SvcFCPList());
		
		// Register a DICOM handler specific to NIG.
		DicomAssetEngineRegistry.register(new DicomAssetHandlerFactory());

		// register system events
		registerSystemEvents();
	}

	protected void registerSystemEvents() throws Throwable {

		FilterRegistry.remove(PSSDObjectEvent.EVENT_TYPE);
		FilterRegistry.add(PSSDObjectEvent.EVENT_TYPE, PSSDObjectEventFilterFactory.INSTANCE);
	}

	public boolean isCompatible(ConfigurationResolver config) throws Throwable {

		return false;
	}

	public void shutdown(ConfigurationResolver config) throws Throwable {

		// Unregister system events
		unregisterSystemEvents();
	}

	protected void unregisterSystemEvents() throws Throwable {

		FilterRegistry.remove(PSSDObjectEvent.EVENT_TYPE);
	}

	public String vendor() {

		return "Arcitecta Pty. Ltd.";
	}

	public String version() {

		return "1.0";
	}

	public Collection<PluginService> services() {

		return _services;
	}

}