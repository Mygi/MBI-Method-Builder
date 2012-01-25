package daris.model.subject;

import arc.mf.client.xml.XmlElement;
import daris.model.method.Method;
import daris.model.object.PSSDObject;

public class Subject extends PSSDObject {
	public static final String TYPE_NAME = "subject";

	private XmlElement _vmetaPrivate;

	private XmlElement _vmetaPublic;

	private XmlElement _emetaPrivate;

	private XmlElement _emetaPublic;

	private Method _method;

	private String _dataUse;

	public Subject(XmlElement xe) throws Throwable {

		super(xe);
		_dataUse = xe.value("data-use");
		String type = xe.value("@type");
		if (!type.equalsIgnoreCase(Subject.TYPE_NAME)) {
			throw new Exception("Subject type should be " + Subject.TYPE_NAME
					+ ". Found " + type + ".");
		}
		XmlElement me = xe.element("method");
		_method = me == null ? null : new Method(me);
		XmlElement mePrivate = xe.element("private");
		if (mePrivate != null) {
			if (mePrivate.element("metadata") == null) {
				_vmetaPrivate = mePrivate;
			} else {
				_emetaPrivate = mePrivate;
			}
		}
		XmlElement mePublic = xe.element("public");
		if (mePublic != null) {
			if (mePublic.element("metadata") == null) {
				_vmetaPublic = mePublic;
			} else {
				_emetaPublic = mePublic;
			}
		}
	}

	public XmlElement privateMetaForView() {

		return _vmetaPrivate;
	}

	void setPrivateMetaForView(XmlElement e) {

		_vmetaPrivate = e;
	}

	public XmlElement privateMetaForEdit() {

		return _emetaPrivate;
	}

	void setPrivateMetaForEdit(XmlElement e) {

		_emetaPrivate = e;
	}

	public XmlElement publicMetaForView() {

		return _vmetaPublic;
	}

	void setPublicMetaForView(XmlElement e) {

		_vmetaPublic = e;
	}

	public XmlElement publicMetaForEdit() {

		return _emetaPublic;
	}

	void setPublicMetaForEdit(XmlElement e) {

		_emetaPublic = e;
	}

	public Method method() {

		return _method;
	}

	public void setMethod(Method m) {

		_method = m;
	}

	public String dataUse() {

		return _dataUse;
	}

	public void setDataUse(String dataUse) {

		_dataUse = dataUse;
	}

	@Override
	public String typeName() {

		return Subject.TYPE_NAME;
	}
}
