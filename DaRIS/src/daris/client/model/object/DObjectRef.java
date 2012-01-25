package daris.client.model.object;

import java.util.List;

import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectRef;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.object.lock.LockToken;
import daris.client.model.IDUtil;
import daris.client.model.Model;
import daris.client.model.dataobject.DataObject;
import daris.client.model.dataset.DataSet;
import daris.client.model.exmethod.ExMethod;
import daris.client.model.object.messages.DObjectExists;
import daris.client.model.object.messages.DObjectLockToken;
import daris.client.model.project.Project;
import daris.client.model.repository.Repository;
import daris.client.model.study.Study;
import daris.client.model.subject.Subject;

public class DObjectRef extends ObjectRef<DObject> implements
		Comparable<DObjectRef> {

	private String _id;
	private String _proute;
	private boolean _foredit;
	private boolean _testLeaf;

	private boolean _resolved = false;

	private DObjectChildrenRef _childrenRef;

	public DObjectRef(String id) {

		this(id, null, false, false);
	}

	public DObjectRef(String id, String proute, boolean foredit,
			boolean testLeaf) {

		_id = id;
		_proute = proute;
		_foredit = foredit;
		_testLeaf = testLeaf;
		_childrenRef = new DObjectChildrenRef(_id, _proute, null);
	}

	protected DObjectRef(DObject o, boolean foredit, boolean testLeaf) {

		super(o);
		_id = o.id();
		_proute = o.proute();
		_foredit = foredit;
		_testLeaf = testLeaf;
		_childrenRef = new DObjectChildrenRef(_id, _proute, null);
	}

	public DObjectChildrenRef childrenRef() {

		return _childrenRef;
	}

	public void setForEdit(boolean foredit) {

		if (_foredit != foredit) {
			reset();
		}
		_foredit = foredit;
	}

	public boolean forEdit() {
		return _foredit;
	}

	public String id() {

		return _id;
	}

	public String proute() {

		return _proute;
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		resolveServiceArgs(w, false);
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w, boolean lock) {

		if (_proute != null) {
			w.add("id", new String[] { "proute", _proute }, _id);
		} else {
			w.add("id", _id);
		}
		w.add("foredit", _foredit);
		w.add("isleaf", _testLeaf);

		if (lock) {
			// Can only lock the latest version.
			w.add("lock",
					new String[] { "type", "transient", "timeout", "60" }, true);
		}
	}

	@Override
	public boolean supportLocking() {

		return true;
	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.object.describe";
	}

	@Override
	protected DObject instantiate(XmlElement xe) throws Throwable {

		DObject o = null;
		if (xe != null) {
			XmlElement oe = xe.element("object");
			if (oe != null) {
				_proute = oe.value("id/@proute");
				o = DObject.create(oe);
				_resolved = true;
			}
		}
		return o;
	}

	@Override
	protected LockToken instantiateLockToken(XmlElement xe) throws Throwable {

		// object lock does not have an id -- use the object citeable id.
		String id = xe.value("object/id");

		XmlElement le = xe.element("object/lock");
		if (le == null) {
			return null;
		}

		return new DObjectLockToken(id);
	}

	@Override
	public boolean resolved() {

		if (_resolved == false) {
			return false;
		}
		return super.resolved();
	}

	@Override
	public String referentTypeName() {

		if (referent() != null) {
			return referent().typeName();
		}
		if (_id == null) {
			return Repository.TYPE_NAME;
		} else if (IDUtil.isProjectId(_id)) {
			return Project.TYPE_NAME;
		} else if (IDUtil.isSubjectId(_id)) {
			return Subject.TYPE_NAME;
		} else if (IDUtil.isExMethodId(_id)) {
			return ExMethod.TYPE_NAME;
		} else if (IDUtil.isStudyId(_id)) {
			return Study.TYPE_NAME;
		} else if (IDUtil.isDataSetId(_id)) {
			return DataSet.TYPE_NAME;
		} else if (IDUtil.isDataObjectId(_id)) {
			return DataObject.TYPE_NAME;
		}
		return "pssd-object";
	}

	@Override
	public String idToString() {

		return _id;
	}

	@Override
	public boolean equals(Object o) {

		if (o != null) {
			if (o instanceof DObjectRef) {
				DObjectRef r = (DObjectRef) o;
				return ObjectUtil.equals(_id, r.id())
						&& ObjectUtil.equals(_proute, r.proute());
			}
		}
		return false;
	}

	@Override
	public int hashCode() {

		return (_id + _proute).hashCode();
	}

	@Override
	public int compareTo(DObjectRef o) {

		if (o == null) {
			return 1;
		}
		if (_id != null) {
			if (o.id() != null) {
				return IDUtil.compare(_id, o.id());
			} else {
				return -1;
			}
		} else {
			if (o.id() != null) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public void refresh(final boolean recursive) {

		new DObjectExists(_id, _proute)
				.send(new ObjectMessageResponse<Boolean>() {

					@Override
					public void responded(Boolean exists) {

						if (exists) {
							reset();
							resolve(new ObjectResolveHandler<DObject>() {
								@Override
								public void resolved(DObject o) {

									Model.objectUpdated(DObjectRef.this);
								}
							});
							if (recursive) {
								List<DObjectRef> children = _childrenRef
										.referent();
								if (children != null) {
									for (DObjectRef co : children) {
										Model.objectDestroyed(co);
									}
								}
								childrenRef().reset();
								childrenRef()
										.resolve(
												new ObjectResolveHandler<List<DObjectRef>>() {
													@Override
													public void resolved(
															List<DObjectRef> children) {

														if (children != null) {
															for (DObjectRef co : children) {
																Model.objectCreated(co);
															}
														}
													}
												});
							}
						} else {
							Model.objectDestroyed(DObjectRef.this);
						}
					}
				});
	}

	@Override
	public String toString() {

		return referentTypeName() + (_id == null ? "" : (" " + _id));
	}

	public DObject createEmptyChildObject() {

		/**
		 * only support creating project, subject and study objects.
		 */
		if (referent() != null) {
			if (referent() instanceof Repository) {
				return new Project();
			} else if (referent() instanceof Project) {
				return new Subject();
			} else if (referent() instanceof ExMethod) {
				return new Study(id());
			}
		} else {
			if (_id == null) {
				return new Project();
			} else {
				String type = IDUtil.childTypeNameFromId(_id);
				if (type.equals(Subject.TYPE_NAME)) {
					return new Subject();
				} else if (type.equals(Study.TYPE_NAME)) {
					return new Study(id());
				}
			}
		}
		return null;
	}

}
