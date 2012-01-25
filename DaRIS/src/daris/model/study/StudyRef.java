package daris.model.study;

import java.util.List;

import arc.mf.client.util.ThrowableUtil;
import arc.mf.client.xml.XmlAttribute;
import arc.mf.client.xml.XmlDoc;
import arc.mf.client.xml.XmlDocMaker;
import arc.mf.client.xml.XmlElement;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.IDUtil;
import daris.model.dataset.DerivationDataSetRef;
import daris.model.dataset.PrimaryDataSetRef;
import daris.model.dataset.messages.DerivationDataSetCreate;
import daris.model.dataset.messages.PrimaryDataSetCreate;
import daris.model.exmethod.ExMethodRef;
import daris.model.object.PSSDObject;
import daris.model.object.PSSDObjectMeta;
import daris.model.object.PSSDObjectRef;
import daris.model.object.messages.ObjectCreate;
import daris.model.object.messages.ObjectUpdate;
import daris.model.study.messages.StudyMetaDescribe;
import daris.model.study.messages.StudyUpdate;

public class StudyRef extends PSSDObjectRef {

	private boolean _createPrimaryDataSet = false;

	private String _studyType;

	private String _methodId;

	private String _methodStep;

	private XmlElement _emetaMethod;

	private XmlElement _vmetaMethod;

	private StudyMeta _metaForCreate;

	private XmlElement _methodMetaToSave;

	public StudyRef(XmlElement oe) {

		super(oe);

	}

	public StudyRef(ExMethodRef parent) {

		this(parent.proute(), parent.id() + ".0");
		setMethodId(parent.id());
	}

	public StudyRef(String proute, String id) {

		this(proute, id, null, null, false);

	}

	public StudyRef(String proute, String id, String name, String description,
			boolean isleaf) {

		super(proute, id, name, description, isleaf);

	}

	@Override
	protected void parse(XmlElement oe) {

		super.parse(oe);

		_studyType = oe.value("type");
		_methodId = oe.value("method/id");
		_methodStep = oe.value("method/step");
		XmlElement mme = oe.element("method/meta");
		if (mme != null) {
			if (mme.element("metadata") != null) {
				_emetaMethod = mme;
			} else {
				_vmetaMethod = mme;
			}
		}
	}

	public String methodStep() {

		return _methodStep;
	}

	public void setMethodStep(String step) {
		_methodStep = step;
	}

	public String methodId() {

		return _methodId;
	}

	public void setMethodId(String id) {
		_methodId = id;
	}
	

	@Override
	protected ObjectCreate getChildCreateMessage(PSSDObjectRef dataset) {

		if (_createPrimaryDataSet) {
			return new PrimaryDataSetCreate(this, (PrimaryDataSetRef) dataset);
		} else {
			return new DerivationDataSetCreate(this,
					(DerivationDataSetRef) dataset);
		}

	}

	public void setCreatePrimaryDataSet() {

		_createPrimaryDataSet = true;

	}

	public void setCreateDerivationDataSet() {

		_createPrimaryDataSet = false;

	}

	@Override
	protected ObjectUpdate getUpdateMessage() {

		return new StudyUpdate(this);

	}

	@Override
	public String referentTypeName() {

		return "study";
	}

	public String studyType() {

		return _studyType;
	}

	public void setStudyType(String studyType) {

		_studyType = studyType;
	}

	public void metaForCreate(final ObjectResolveHandler<XmlElement> rh) {

		if (_metaForCreate != null) {
			rh.resolved(_metaForCreate.getMeta());
			return;
		}
		new StudyMetaDescribe(this)
				.send(new ObjectMessageResponse<PSSDObjectMeta>() {

					@Override
					public void responded(PSSDObjectMeta r) {

						if (r != null) {
							_metaForCreate = (StudyMeta) r;
							rh.resolved(_metaForCreate.getMeta());
							return;
						}
						rh.resolved(null);
					}
				});
	}

	public void methodMetaForCreate(final ObjectResolveHandler<XmlElement> rh) {

		if (_metaForCreate != null) {
			rh.resolved(_metaForCreate.getMethodMeta());
			return;
		}
		new StudyMetaDescribe(this)
				.send(new ObjectMessageResponse<PSSDObjectMeta>() {

					@Override
					public void responded(PSSDObjectMeta r) {

						if (r != null) {
							_metaForCreate = (StudyMeta) r;
							rh.resolved(_metaForCreate.getMethodMeta());
							return;
						}
						rh.resolved(null);
					}
				});
	}

	public void methodMetaForView(final ObjectResolveHandler<XmlElement> rh,
			boolean refresh) {

		setForEdit(false);
		if (refresh) {
			reset();
		} else {
			if (_vmetaMethod != null) {
				rh.resolved(_vmetaMethod);
				return;
			}
		}
		resolve(new ObjectResolveHandler<PSSDObject>() {

			@Override
			public void resolved(PSSDObject o) {

				_vmetaMethod = ((Study) o).methodMetaForView();
				rh.resolved(_vmetaMethod);
			}
		});
	}

	public void methodMetaForEdit(final ObjectResolveHandler<XmlElement> rh,
			boolean refresh) {

		setForEdit(true);
		if (refresh) {
			reset();
		} else {
			if (_emetaMethod != null) {
				rh.resolved(_emetaMethod);
				return;
			}
		}
		resolve(new ObjectResolveHandler<PSSDObject>() {

			@Override
			public void resolved(PSSDObject o) {

				_emetaMethod = ((Study) o).methodMetaForEdit();
				rh.resolved(_emetaMethod);
			}
		});
	}

	public XmlElement metaToSave() {

		if (super.metaToSave() == null) {
			return _methodMetaToSave;
		}
		if (_methodMetaToSave == null) {
			return super.metaToSave();
		}

		XmlDocMaker dm = new XmlDocMaker("meta");
		dm.add(super.metaToSave(), false);
		dm.add(_methodMetaToSave, false);
		return dm.root();

	}

	public void setMethodMetaToSave(XmlElement methodMetaToSave) {

		_methodMetaToSave = methodMetaToSave;
	}

	public void setMethodMetaToSave(String methodMetaToSave) {

		String pid = IDUtil.getParentId(id());
		String step = methodStep();
		String ns = pid + "_" + step;
		try {
			XmlElement e = XmlDoc.parse(methodMetaToSave);
			if (e != null) {
				XmlDocMaker dm = new XmlDocMaker("meta");
				for (XmlElement de : e.elements()) {
					dm.add(addAttribute(de, "ns", ns));
				}
				setMethodMetaToSave(dm.root());
			}

		} catch (Throwable t) {
			ThrowableUtil.rethrowAsUnchecked(t);
		}
	}

	private XmlElement addAttribute(XmlElement e, String attrName,
			String attrValue) {
		/*
		 * merge attributes and store the attributes in String[]
		 */
		String[] attrs;
		List<XmlAttribute> as = e.attributes();
		if (as == null) {
			attrs = new String[] { attrName, attrValue };
		} else {
			XmlAttribute attrToReplace = null;
			for (XmlAttribute a : as) {
				if (a.name().equals(attrName)) {
					attrToReplace = a;
					break;
				}
			}
			if (attrToReplace != null) {
				as.remove(attrToReplace);
			}
			attrs = new String[(as.size() + 1) * 2];
			int i = 0;
			for (XmlAttribute a : as) {
				attrs[i++] = a.name();
				attrs[i++] = a.value();
			}
			attrs[i++] = attrName;
			attrs[i++] = attrValue;
		}
		XmlDocMaker dm = new XmlDocMaker(e.name(), attrs);
		dm.add(e, false);
		return dm.root();
	}

}
