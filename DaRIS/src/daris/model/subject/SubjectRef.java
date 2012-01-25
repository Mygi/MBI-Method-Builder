package daris.model.subject;

import java.util.List;
import java.util.Vector;

import arc.mf.client.util.ThrowableUtil;
import arc.mf.client.xml.XmlDoc;
import arc.mf.client.xml.XmlElement;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.IDUtil;
import daris.model.exmethod.ExMethodRef;
import daris.model.exmethod.messages.ExMethodCreate;
import daris.model.method.Method;
import daris.model.method.MethodRef;
import daris.model.object.PSSDObject;
import daris.model.object.PSSDObjectMeta;
import daris.model.object.PSSDObjectRef;
import daris.model.object.messages.ObjectCreate;
import daris.model.object.messages.ObjectUpdate;
import daris.model.project.Project;
import daris.model.project.ProjectRef;
import daris.model.subject.messages.SubjectMetaDescribe;
import daris.model.subject.messages.SubjectUpdate;

public class SubjectRef extends PSSDObjectRef {

	private String _dataUse;

	private MethodRef _method;

	private XmlElement _vmetaPrivate;

	private XmlElement _vmetaPublic;

	private XmlElement _emetaPrivate;

	private XmlElement _emetaPublic;

	private SubjectMeta _metaForCreate;
	
	private XmlElement _privateMetaToSave;
	
	private XmlElement _publicMetaToSave;

	public SubjectRef(XmlElement oe) {

		super(oe);

	}

	public SubjectRef(ProjectRef parent) {

		this(parent.proute(), parent.id() + ".0");
	}

	public SubjectRef(String proute, String id) {

		this(proute, id, null, null, false);
	}

	public SubjectRef(String proute, String id, String name,
			String description, boolean isleaf) {

		super(proute, id, name, description, isleaf);

	}

	@Override
	protected ObjectCreate getChildCreateMessage(PSSDObjectRef exmethod) {

		return new ExMethodCreate(this, (ExMethodRef) exmethod);

	}

	@Override
	protected ObjectUpdate getUpdateMessage() {

		return new SubjectUpdate(this);

	}

	@Override
	public String referentTypeName() {

		return "subject";
	}

	@Override
	protected void parse(XmlElement oe) {

		super.parse(oe);

		_dataUse = oe.value("data-use");

		XmlElement me = oe.element("method");
		if (me != null) {
			MethodRef m = new MethodRef(me.value("id"), me.value("name"),
					me.value("description"));
			setMethod(m);
		}
		XmlElement mePrivate = oe.element("private");
		if (mePrivate != null) {
			if (mePrivate.element("metadata") == null) {
				_vmetaPrivate = mePrivate;
			} else {
				_emetaPrivate = mePrivate;
			}
		}
		XmlElement mePublic = oe.element("public");
		if (mePublic != null) {
			if (mePublic.element("metadata") == null) {
				_vmetaPublic = mePublic;
			} else {
				_emetaPublic = mePublic;
			}
		}
	}

	public String dataUse() {

		return _dataUse;
	}

	public void setDataUse(String dataUse) {

		_dataUse = dataUse;
	}
	
	public XmlElement privateMetaToSave() {

		return _privateMetaToSave;
	}

	public void setPrivateMetaToSave(XmlElement privateMetaToSave) {

		_privateMetaToSave = privateMetaToSave;
	}

	public void setPrivateMetaToSave(String privateMetaToSave) {

		try {
			setPrivateMetaToSave(XmlDoc.parse(privateMetaToSave));
		} catch (Throwable t) {
			ThrowableUtil.rethrowAsUnchecked(t);
		}
	}
	
	public XmlElement publicMetaToSave() {

		return _publicMetaToSave;
	}

	public void setPublicMetaToSave(XmlElement publicMetaToSave) {

		_publicMetaToSave = publicMetaToSave;
	}

	public void setPublicMetaToSave(String publicMetaToSave) {

		try {
			setPublicMetaToSave(XmlDoc.parse(publicMetaToSave));
		} catch (Throwable t) {
			ThrowableUtil.rethrowAsUnchecked(t);
		}
	}

	public MethodRef method() {

		return _method;
	}

	public void setMethod(MethodRef m) {

		if (!m.equals(_method)) {
			_method = m;
		}
	}

	public void availableMethods(final ObjectResolveHandler<List<MethodRef>> rh) {

		ProjectRef projectRef = new ProjectRef(proute(),
				IDUtil.getParentId(id()));
		projectRef.resolve(new ObjectResolveHandler<PSSDObject>() {

			@Override
			public void resolved(PSSDObject o) {

				if (o != null) {
					List<Method> ms = ((Project) o).methods();
					if (ms != null) {
						List<MethodRef> mrs = new Vector<MethodRef>(ms.size());
						for (Method m : ms) {
							mrs.add(new MethodRef(m.id(), m.name(), m
									.description()));
						}
						if (mrs.size() > 0) {
							rh.resolved(mrs);
							return;
						}
					}
				}
				rh.resolved(null);
			}
		});
	}

	public void privateMetaForCreate(final ObjectResolveHandler<XmlElement> rh) {

		if (_metaForCreate != null) {
			rh.resolved(_metaForCreate.getPrivateMeta());
			return;
		}
		new SubjectMetaDescribe(this)
				.send(new ObjectMessageResponse<PSSDObjectMeta>() {

					@Override
					public void responded(PSSDObjectMeta r) {

						if (r != null) {
							_metaForCreate = (SubjectMeta) r;
							rh.resolved(_metaForCreate.getPrivateMeta());
							return;
						}
						rh.resolved(null);
					}
				});
	}

	public void publicMetaForCreate(final ObjectResolveHandler<XmlElement> rh) {

		if (_metaForCreate != null) {
			rh.resolved(_metaForCreate.getPublicMeta());
			return;
		}
		new SubjectMetaDescribe(this)
				.send(new ObjectMessageResponse<PSSDObjectMeta>() {

					@Override
					public void responded(PSSDObjectMeta r) {

						if (r != null) {
							_metaForCreate = (SubjectMeta) r;
							rh.resolved(_metaForCreate.getPublicMeta());
							return;
						}
						rh.resolved(null);
					}
				});
	}

	public void publicMetaForView(final ObjectResolveHandler<XmlElement> rh,
			boolean refresh) {

		setForEdit(false);
		if (refresh) {
			reset();
		} else {
			if (_vmetaPublic != null) {
				rh.resolved(_vmetaPublic);
				return;
			}
		}
		resolve(new ObjectResolveHandler<PSSDObject>() {

			@Override
			public void resolved(PSSDObject o) {

				_vmetaPublic = ((Subject) o).publicMetaForView();
				rh.resolved(_vmetaPublic);
			}
		});
	}

	public void publicMetaForEdit(final ObjectResolveHandler<XmlElement> rh,
			boolean refresh) {

		setForEdit(true);
		if (refresh) {
			reset();
		} else {
			if (_emetaPublic != null) {
				rh.resolved(_emetaPublic);
				return;
			}
		}
		resolve(new ObjectResolveHandler<PSSDObject>() {

			@Override
			public void resolved(PSSDObject o) {

				_emetaPublic = ((Subject) o).publicMetaForEdit();
				rh.resolved(_emetaPublic);
			}
		});
	}

	public void privateMetaForView(final ObjectResolveHandler<XmlElement> rh,
			boolean refresh) {

		setForEdit(false);
		if (refresh) {
			reset();
		} else {
			if (_vmetaPrivate != null) {
				rh.resolved(_vmetaPrivate);
				return;
			}
		}
		resolve(new ObjectResolveHandler<PSSDObject>() {

			@Override
			public void resolved(PSSDObject o) {

				_vmetaPrivate = ((Subject) o).privateMetaForView();
				rh.resolved(_vmetaPrivate);
			}
		});
	}

	public void privateMetaForEdit(final ObjectResolveHandler<XmlElement> rh,
			boolean refresh) {

		setForEdit(true);
		if (refresh) {
			reset();
		} else {
			if (_emetaPrivate != null) {
				rh.resolved(_emetaPrivate);
				return;
			}
		}
		resolve(new ObjectResolveHandler<PSSDObject>() {

			@Override
			public void resolved(PSSDObject o) {

				_emetaPrivate = ((Subject) o).privateMetaForEdit();
				rh.resolved(_emetaPrivate);
			}
		});
	}

}
