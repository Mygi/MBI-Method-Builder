package daris.client.model.object;

import java.util.List;

import arc.mf.client.file.LocalFile;
import arc.mf.client.util.ObjectUtil;
import arc.mf.client.util.ThrowableUtil;
import arc.mf.client.xml.XmlDoc;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.IDUtil;
import daris.client.model.dataobject.DataObject;
import daris.client.model.dataset.DataSet;
import daris.client.model.dataset.DerivationDataSet;
import daris.client.model.dataset.DicomDataSet;
import daris.client.model.dataset.PrimaryDataSet;
import daris.client.model.exmethod.ExMethod;
import daris.client.model.method.Method;
import daris.client.model.object.messages.CanEdit;
import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.object.messages.DObjectUpdate;
import daris.client.model.object.messages.ObjectAttach;
import daris.client.model.object.messages.ObjectAttachmentGet;
import daris.client.model.object.messages.ObjectAttachmentList;
import daris.client.model.object.messages.ObjectDetach;
import daris.client.model.project.Project;
import daris.client.model.rsubject.RSubject;
import daris.client.model.study.Study;
import daris.client.model.subject.Subject;

public abstract class DObject {

	private static int VERSION_LATEST = 0;
	private String _proute;
	private String _id;
	private String _assetId;
	private String _name;
	private String _description;
	private boolean _editable;
	private int _version;
	private boolean _isleaf;

	private XmlElement _meta = null;
	private XmlElement _metaForEdit = null;

	/**
	 * Constructor.
	 * 
	 * @param xe
	 *            XML Element object contains the object detail.
	 * @throws Throwable
	 */
	protected DObject(XmlElement xe) {

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
			try {
				_version = xe.intValue("@version", VERSION_LATEST);
			} catch (Throwable e) {
				_version = VERSION_LATEST;
			}
			_name = xe.value("name");
			_description = xe.value("description");
			try {
				_isleaf = xe.booleanValue("isleaf", false);
			} catch (Throwable e) {
				_isleaf = false;
			}
			XmlElement me = xe.element("meta");
			if (me != null) {
				if (me.element("metadata") != null) {
					_metaForEdit = me;
				} else {
					_meta = me;
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
			try {
				_version = xe.intValue("@version", VERSION_LATEST);
			} catch (Throwable e) {
				_version = VERSION_LATEST;
			}
			_name = xe.value("name");
			_description = xe.value("description");
			_isleaf = true;
		} else if (xe.name().equals("repository")) {
			// xe is the result of om.pssd.repository.describe
			_id = xe.value("id");
			_name = xe.value("name");
			_description = xe.value("description");
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
	protected DObject(String id, String proute, String name, String description, boolean editable, int version,
			boolean isleaf) {

		_id = id;
		_proute = proute;
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

		if (o instanceof DObject) {
			if (ObjectUtil.equals(_proute, ((DObject) o).proute())) {
				if (ObjectUtil.equals(_id, ((DObject) o).id())) {
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

	public void setName(String name) {

		_name = name;
	}

	public String description() {

		return _description;

	}

	public void setDescription(String description) {

		_description = description;
	}

	public boolean editable() {

		return _editable;
	}

	public String assetId() {

		return _assetId;
	}

	public XmlElement meta() {

		return _meta;
	}

	public void setMeta(XmlStringWriter w) {

		try {
			_meta = XmlDoc.parse(w.document());
		} catch (Throwable e) {
			ThrowableUtil.rethrowAsUnchecked(e);
		}
	}

	public XmlElement metaForEdit() {

		return _metaForEdit;
	}

	public void setMetaForEdit(XmlElement metaForEdit) {

		_metaForEdit = metaForEdit;
	}

	public boolean hasMeta() {

		if (_meta != null) {
			if (_meta.hasElements()) {
				return true;
			}
		}
		return false;
	}

	public static DObject create(XmlElement oe) {

		String type = oe.value("@type");
		if (type.equals(Project.TYPE_NAME)) {
			return new Project(oe);
		} else if (type.equalsIgnoreCase(Subject.TYPE_NAME)) {
			return new Subject(oe);
		} else if (type.equalsIgnoreCase(ExMethod.TYPE_NAME)) {
			return new ExMethod(oe);
		} else if (type.equalsIgnoreCase(Study.TYPE_NAME)) {
			return new Study(oe);
		} else if (type.equalsIgnoreCase(DataSet.TYPE_NAME)) {
			String sourceType = oe.value("source/type");
			if (sourceType.equals("derivation")) {
				String mimeType = oe.value("type");
				if (mimeType != null) {
					if (mimeType.equals("dicom/series")) {
						return new DicomDataSet(oe);
					}
				}
				return new DerivationDataSet(oe);
			} else if (sourceType.equals("primary")) {
				return new PrimaryDataSet(oe);
			}
		} else if (type.equalsIgnoreCase(DataObject.TYPE_NAME)) {
			return new DataObject(oe);
		} else if (type.equalsIgnoreCase(Method.TYPE_NAME)) {
			return new Method(oe);
		} else if (type.equalsIgnoreCase(RSubject.TYPE_NAME)) {
			return new RSubject(oe);
		}
		return null;
	}

	public abstract String typeName();

	protected abstract DObjectCreate objectCreateMessage(DObjectRef po);

	public void create(DObjectRef po, ObjectMessageResponse<DObjectRef> rh) {

		DObjectCreate msg = objectCreateMessage(po);
		if (msg != null) {
			msg.send(rh);
		}
	}

	public void createServiceArgs(XmlWriter w) {

		if (_name != null) {
			w.add("name", _name);
		}
		if (_description != null) {
			w.add("description", _description);
		}
		if (_meta != null) {
			w.add(_meta, true);
		}
	}

	protected abstract DObjectUpdate objectUpdateMessage();

	public void update(ObjectMessageResponse<Boolean> rh) {

		DObjectUpdate msg = objectUpdateMessage();
		if (msg != null) {
			msg.send(rh);
		}
	}

	public void updateServiceArgs(XmlWriter w) {

		w.add("id", _id);
		if (_name != null) {
			w.add("name", _name);
		}
		if (_description != null) {
			w.add("description", _description);
		}
		if (_meta != null) {
			w.add(_meta, true);
		}

	}

	public void attachments(ObjectMessageResponse<List<Attachment>> rh) {

		new ObjectAttachmentList(this).send(rh);
	}

	public void detach(List<Attachment> attachments, ObjectMessageResponse<Boolean> rh) {

		new ObjectDetach(this, attachments).send(rh);
	}

	public void attach(List<LocalFile> inputs, ObjectMessageResponse<List<Attachment>> rh) {

		new ObjectAttach(this, inputs).send(rh);
	}

	public void retrieveAttachments(List<Attachment> attachments, ObjectMessageResponse<Boolean> rh) {

		new ObjectAttachmentGet(this, attachments).send(rh);
	}

	public void editable(ObjectMessageResponse<Boolean> rh) {

		new CanEdit(this).send(rh);
	}
}
