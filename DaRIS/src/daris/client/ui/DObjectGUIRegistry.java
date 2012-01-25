package daris.client.ui;

import arc.gui.object.register.SystemObjectGUIRegistry;
import arc.mf.client.dti.file.DTIDirectory;
import arc.mf.client.dti.file.DTIFile;
import arc.mf.client.dti.task.DTITask;
import daris.client.model.object.Attachment;
import daris.client.model.object.DObjectRef;
import daris.client.model.project.ProjectMember;
import daris.client.model.project.ProjectRoleMember;
import daris.client.model.sc.ContentItem;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.user.RoleUser;
import daris.client.model.user.User;
import daris.client.ui.dti.DTITaskGUI;
import daris.client.ui.dti.file.DTIDirectoryGUI;
import daris.client.ui.dti.file.DTIFileGUI;
import daris.client.ui.object.AttachmentGUI;
import daris.client.ui.object.DObjectGUI;
import daris.client.ui.project.ProjectMemberGUI;
import daris.client.ui.project.ProjectRoleMemberGUI;
import daris.client.ui.sc.ContentItemGUI;
import daris.client.ui.sc.ShoppingCartGUI;
import daris.client.ui.user.RoleUserGUI;
import daris.client.ui.user.UserGUI;

public class DObjectGUIRegistry {

	private static boolean _registered = false;

	public static SystemObjectGUIRegistry get() {

		SystemObjectGUIRegistry registry = SystemObjectGUIRegistry.get();
		if (!_registered) {
			registry.add(DObjectRef.class, DObjectGUI.INSANCE);
			registry.add(Attachment.class, AttachmentGUI.INSTANCE);
			registry.add(User.class, UserGUI.INSTANCE);
			registry.add(RoleUser.class, RoleUserGUI.INSTANCE);
			registry.add(ProjectMember.class, ProjectMemberGUI.INSTANCE);
			registry.add(ProjectRoleMember.class, ProjectRoleMemberGUI.INSTANCE);
			registry.add(Attachment.class, AttachmentGUI.INSTANCE);
			registry.add(DTIFile.class, DTIFileGUI.INSTANCE);
			registry.add(DTIDirectory.class, DTIDirectoryGUI.INSTANCE);
			registry.add(ShoppingCartRef.class, ShoppingCartGUI.INSANCE);
			registry.add(ContentItem.class, ContentItemGUI.INSANCE);
			registry.add(DTITask.class, DTITaskGUI.INSTANCE);
			_registered = true;
		}
		return registry;
	}
}
