package daris.model.rsubject;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import daris.model.object.PSSDObject;

public class RSubject extends PSSDObject {

	public static final String TYPE_NAME = "r-subject";

	private List<XmlElement> _idMeta;
	private List<XmlElement> _publicMeta;

	public RSubject(XmlElement xe) throws Throwable {
		super(xe);

		XmlElement e = xe.element("identity");
		if (e != null) {
			_idMeta = e.elements();
		}
		e = xe.element("public");
		if (e != null) {
			_publicMeta = e.elements();
		}

	}

	public List<XmlElement> identityMeta() {
		return _idMeta;
	}

	public List<XmlElement> publicMeta() {
		return _publicMeta;
	}

	public void setIdentityMeta(List<XmlElement> idMeta) {
		_idMeta = idMeta;
	}

	public void setPublicMeta(List<XmlElement> publicMeta) {
		_publicMeta = publicMeta;
	}

	@Override
	public String typeName() {
		return RSubject.TYPE_NAME;
	}

}
