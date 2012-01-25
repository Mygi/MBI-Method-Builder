package daris.model.project;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import daris.model.method.Method;
import daris.model.object.PSSDObject;

public class Project extends PSSDObject {

	public static final String PROJECT_ADMIN_ROLE_PREFIX = "pssd.project.admin.";

	public static final String PROJECT_ADMIN_ROLE_TYPE = "project-administrator";

	public static final String PROJECT_GUEST_ROLE_PREFIX = "pssd.project.guest.";

	public static final String PROJECT_GUEST_ROLE_TYPE = "guest";

	public static final String PROJECT_MEMBER_ROLE_PREFIX = "pssd.project.member.";

	public static final String PROJECT_MEMBER_ROLE_TYPE = "member";

	public static final String SUBJECT_ADIMIN_ROLE_PREFIX = "pssd.project.subject.admin.";

	public static final String SUBJECT_ADMIN_ROLE_TYPE = "subject-administrator";

	public static final String TYPE_NAME = "project";

	private String _dataUse;

	private List<Method> _methods;

	public Project(XmlElement xe) throws Throwable {

		super(xe);

		String type = xe.value("@type");
		assert (Project.TYPE_NAME.equals(type));

		_dataUse = xe.value("data-use");

		List<XmlElement> mdes = xe.elements("method");
		if (mdes != null) {
			addMethods(mdes);
		}

	}

	public String typeName() {

		return Project.TYPE_NAME;

	}

	private void addMethod(XmlElement mde) throws Throwable {

		if (_methods == null) {
			_methods = new Vector<Method>();
		}
		_methods.add(new Method(mde));

	}

	private void addMethods(List<XmlElement> mdes) throws Throwable {

		if (mdes != null) {
			for (int i = 0; i < mdes.size(); i++) {
				XmlElement mde = mdes.get(i);
				addMethod(mde);
			}
		}

	}

	public String adminRole() {

		return Project.PROJECT_ADMIN_ROLE_PREFIX + id();

	}

	public String dataUse() {

		return _dataUse;

	}

	public String guestRole() {

		return Project.PROJECT_GUEST_ROLE_PREFIX + id();

	}

	public String memberRole() {

		return Project.PROJECT_MEMBER_ROLE_PREFIX + id();

	}

	public List<Method> methods() {

		return _methods;

	}

	public void setDataUse(String dataUse) {

		_dataUse = dataUse;

	}

	public void setMethods(Vector<Method> methods) {

		_methods = methods;

	}

	public String subjectAdminRole() {

		return Project.SUBJECT_ADIMIN_ROLE_PREFIX + id();

	}

}
