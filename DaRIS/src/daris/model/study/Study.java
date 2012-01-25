package daris.model.study;

import arc.mf.client.xml.XmlElement;
import daris.model.object.PSSDObject;

/**
 * The study class.
 * 
 * @author wilson
 * 
 */
public class Study extends PSSDObject {

	public static final String TYPE_NAME = "study";

	private String _methodId;
	private XmlElement _vmetaMethod;
	private XmlElement _emetaMethod;
	private String _methodStep;
	private String _studyType;

	/**
	 * Constructor.
	 * 
	 * @param xe
	 *            The xml element represents the study object.
	 * @throws Throwable
	 */
	public Study(XmlElement xe) throws Throwable {
		super(xe);
		_studyType = xe.value("type");
		_methodId = xe.value("method/id");
		_methodStep = xe.value("method/step");
		XmlElement mme = xe.element("method/meta");
		if (mme != null) {
			if (mme.element("metadata") != null) {
				_emetaMethod = mme;
			} else {
				_vmetaMethod = mme;
			}
		}
	}

	/**
	 * Returns the id of the study method.
	 * 
	 * @return
	 */
	public String methodId() {
		return _methodId;
	}

	/**
	 * Returns the method metadata of the study.
	 * 
	 * @return
	 */
	public XmlElement methodMetaForView() {
		return _vmetaMethod;
	}

	void setMethodMetaForView(XmlElement mme) {
		_vmetaMethod = mme;
	}

	public XmlElement methodMetaForEdit() {
		return _emetaMethod;
	}

	void setMethodMetaForEdit(XmlElement mme) {
		_emetaMethod = mme;
	}

	/**
	 * Returns the method step path.
	 * 
	 * @return
	 */
	public String methodStep() {
		return _methodStep;
	}

	/**
	 * Returns the study path.
	 * 
	 * @return
	 */
	public String studyType() {
		return _studyType;
	}

	@Override
	public String typeName() {
		return Study.TYPE_NAME;
	}

}
