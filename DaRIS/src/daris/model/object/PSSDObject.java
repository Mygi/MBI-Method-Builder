package daris.model.object;

import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;
import daris.client.model.IDUtil;

public abstract class PSSDObject {

	private static int VERSION_LATEST = 0;
	private String _proute;
	private String _id;
	private String _name;
	private String _description;
	private boolean _editable;
	private int _version;
	private boolean _isleaf;
	private String _assetId;

	private XmlElement _vmeta = null;
	private XmlElement _emeta = null;

	/**
	 * Constructor.
	 * 
	 * @param xe
	 *            XML Element object contains the object detail.
	 * @throws Throwable
	 */
	protected PSSDObject(XmlElement xe) throws Throwable {

		if (xe.name().equals("object")) {
			// xe is the result of om.pssd.object.describe
			_id = xe.value("id");
			if (_id == null) {
				_id = xe.value("@id");
			}
			_assetId = xe.value("id/@asset");
			_proute = xe.value("id/@proute");
			try {
				_editable = xe.booleanValue("@editable", false);
			} catch (Throwable e) {
				_editable = false;
			}
			_version = xe.intValue("@version", VERSION_LATEST);
			_name = xe.value("name");
			_description = xe.value("description");
			_isleaf = xe.booleanValue("isleaf", false);
			XmlElement me = xe.element("meta");
			if (me != null) {
				if (me.element("metadata") != null) {
					_emeta = me;
				} else {
					_vmeta = me;
				}
			}
		} else if (xe.name().equals("method")) {
			_id = xe.value("id");
			if (_id == null) {
				_id = xe.value("@id");
			}
			_proute = xe.value("@proute");
			if (_proute == null) {
				_proute = xe.value("id/@proute");
			}
			_editable = false;
			_version = xe.intValue("@version", VERSION_LATEST);
			_name = xe.value("name");
			_description = xe.value("description");
			_isleaf = true;
		} else if (xe.name().equals("server")) {
			// xe is the result of server.identity
			_proute = xe.value("@proute");
			_id = xe.value("uuid");
			_name = xe.value("name");
			_description = "Organization:" + xe.value("organization");
			_isleaf = false;
		}

	}

	/**
	 * Constructor.
	 * 
	 * @param proute
	 * @param id
	 * @param name
	 * @param description
	 * @param editable
	 * @param version
	 * @param isleaf
	 */
	protected PSSDObject(String proute, String id, String name, String description, boolean editable, int version,
			boolean isleaf) {

		_proute = proute;
		_id = id;
		_name = name;
		_description = description;
		_editable = editable;
		_version = version;
		_isleaf = isleaf;

	}

	/**
	 * The asset/object version.
	 * 
	 * @return
	 */
	public int version() {

		return _version;

	}

	/**
	 * True if the object has no children.
	 * 
	 * @return
	 */
	public boolean isleaf() {

		return _isleaf;

	}

	/**
	 * The parent ID, if any.
	 * 
	 * @return
	 */
	public String pid() {

		return IDUtil.getParentId(_id);

	}

	/**
	 * Equality.
	 */
	public boolean equals(Object o) {

		if (o instanceof PSSDObject) {
			if (ObjectUtil.equals(_proute, ((PSSDObject) o).proute())) {
				if (ObjectUtil.equals(_id, ((PSSDObject) o).id())) {
					return true;
				}
			}
		}
		return false;

	}

	public int hashCode() {

		return (_id + _proute).hashCode();
	}

	/**
	 * Is the object in some other repository other than the one we are
	 * connected to?
	 * 
	 * @return
	 */
	public boolean isRemote() {

		if (proute() == null) {
			return false;
		}
		return true;

	}

	/**
	 * Identity of the remote server.
	 * 
	 * @return
	 */
	public String remoteServerID() {

		return IDUtil.getLastSection(_proute);

	}

	/**
	 * The path route to the repository where this object is located.
	 * 
	 * @return
	 */
	public String proute() {

		return _proute;

	}

	/**
	 * The identify of this object.
	 * 
	 * @return
	 */
	public String id() {

		return _id;

	}

	public String name() {

		return _name;

	}

	public String description() {

		return _description;

	}

	public boolean editable() {

		return _editable;
	}

	public String assetId() {

		return _assetId;
	}

	public XmlElement metaForView() {

		return _vmeta;
	}

	public XmlElement metaForEdit() {

		return _emeta;
	}

	void setMetaForEdit(XmlElement emeta) {

		_emeta = emeta;

	}

	void setMetaForView(XmlElement vmeta) {

		_vmeta = vmeta;

	}

	public abstract String typeName();

}
