package daris.client.model.subject;

import java.util.List;

import arc.mf.client.util.ActionListener;
import arc.mf.client.util.ThrowableUtil;
import arc.mf.client.xml.XmlDoc;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.method.MethodRef;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.object.messages.DObjectUpdate;
import daris.client.model.project.DataUse;
import daris.client.model.project.Project;
import daris.client.model.subject.messages.SubjectCreate;
import daris.client.model.subject.messages.SubjectMetadataDescribe;
import daris.client.model.subject.messages.SubjectUpdate;

public class Subject extends DObject {

	public static final String TYPE_NAME = "subject";

	private XmlElement _privateMetaForEdit;

	private XmlElement _privateMeta;

	private XmlElement _publicMetaForEdit;

	private XmlElement _publicMeta;

	private MethodRef _method;

	private DataUse _dataUse;

	public Subject() {

		super(null, null, null, null, false, 0, false);
	}

	public Subject(XmlElement oe) {

		super(oe);

		assert (Subject.TYPE_NAME.equals(oe.value("@type")));

		_dataUse = DataUse.parse(oe.value("data-use"));
		XmlElement me = oe.element("method");
		if (me != null) {
			_method = new MethodRef(me.value("id"), me.value("name"), me.value("description"));
		}
		XmlElement mePrivate = oe.element("private");
		if (mePrivate != null) {
			if (mePrivate.element("metadata") == null) {
				_privateMeta = mePrivate;
			} else {
				_publicMetaForEdit = mePrivate;
			}
		}
		XmlElement mePublic = oe.element("public");
		if (mePublic != null) {
			if (mePublic.element("metadata") == null) {
				_publicMeta = mePublic;
			} else {
				_publicMetaForEdit = mePublic;
			}
		}
	}

	public DataUse dataUse() {

		return _dataUse;
	}

	public void setDataUse(DataUse dataUse) {

		_dataUse = dataUse;
	}

	public MethodRef method() {

		return _method;
	}

	public void setMethod(MethodRef m) {

		_method = m;
	}

	public XmlElement publicMeta() {

		return _publicMeta;
	}

	public void setPublicMeta(XmlStringWriter w) {

		try {
			_publicMeta = XmlDoc.parse(w.document());
		} catch (Throwable e) {
			ThrowableUtil.rethrowAsUnchecked(e);
		}
	}

	public XmlElement publicMetaForEdit() {

		return _publicMetaForEdit;
	}

	public void setPublicMetaForEdit(XmlElement publicMetaForEdit) {

		_publicMetaForEdit = publicMetaForEdit;
	}

	public XmlElement privateMeta() {

		return _privateMeta;
	}

	public void setPrivateMeta(XmlStringWriter w) {

		try {
			_privateMeta = XmlDoc.parse(w.document());
		} catch (Throwable e) {
			ThrowableUtil.rethrowAsUnchecked(e);
		}
	}

	public XmlElement privateMetaForEdit() {

		return _privateMetaForEdit;
	}

	public void setPrivateMetaForEdit(XmlElement privateMetaForEdit) {

		_privateMetaForEdit = privateMetaForEdit;
	}

	@Override
	public String typeName() {

		return Subject.TYPE_NAME;
	}

	@Override
	protected DObjectCreate objectCreateMessage(DObjectRef po) {

		return new SubjectCreate(po, this);
	}

	@Override
	public void createServiceArgs(XmlWriter w) {

		super.createServiceArgs(w);
		w.add("data-use", _dataUse);
		if (_method != null) {
			w.add("method", _method.id());
		}
		if (_privateMeta != null) {
			w.add(_privateMeta, true);
		}
		if (_publicMeta != null) {
			w.add(_publicMeta, true);
		}
	}

	@Override
	protected DObjectUpdate objectUpdateMessage() {

		return new SubjectUpdate(this);
	}

	@Override
	public void updateServiceArgs(XmlWriter w) {

		super.updateServiceArgs(w);
		w.add("data-use", _dataUse);
		if (_privateMeta != null) {
			w.add(_privateMeta, true);
		}
		if (_publicMeta != null) {
			w.add(_publicMeta, true);
		}
	}

	public static void projectMethods(String pid, String proute, final ObjectMessageResponse<List<MethodRef>> rh) {

		new DObjectRef(pid, proute, false, false).resolve(new ObjectResolveHandler<DObject>() {

			@Override
			public void resolved(DObject o) {

				if (o == null) {
					rh.responded(null);
					return;
				}
				rh.responded(((Project) o).methods());
			}
		});
	}

	public static void setMetaForEdit(final Subject o, String projectId, String methodId, final ActionListener al) {

		new SubjectMetadataDescribe(projectId, methodId).send(new ObjectMessageResponse<XmlElement>() {

			@Override
			public void responded(XmlElement xe) {

				if (xe == null) {
					o.setPublicMetaForEdit(null);
					o.setPrivateMetaForEdit(null);
				} else {
					o.setPublicMetaForEdit(xe.element("public"));
					o.setPrivateMetaForEdit(xe.element("private"));
				}
				al.executed(true);
			}
		});
	}

}
