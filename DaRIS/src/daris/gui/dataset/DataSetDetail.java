package daris.gui.dataset;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.xml.XmlElement;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.StringType;
import arc.mf.object.ObjectResolveHandler;
import daris.client.ui.dicom.DicomImageNavigator;
import daris.client.ui.form.XmlMetaForm;
import daris.gui.object.ObjectDetail;
import daris.model.datacontent.DataContent;
import daris.model.dataset.DataSetRef;
import daris.model.dataset.DerivationDataSetRef;
import daris.model.dataset.DicomDerivationDataSet;
import daris.model.dataset.PrimaryDataSetRef;
import daris.model.object.PSSDObject;

public class DataSetDetail extends ObjectDetail {

	public static final String META_TAB_NAME = "meta";
	public static final String DICOM_TAB_NAME = "dicom";

	private Form _metaForm;

	public DataSetDetail(DataSetRef dataset, FormEditMode mode) {

		super(dataset, mode);

		//
		// Meta tab
		//
		updateMetaTab();

		//
		// DICOM tab;
		//
		updateDicomTab();
	}

	public DataSetRef dataset() {

		return (DataSetRef) object();
	}

	private void updateMetaTab() {

		if (mode() == FormEditMode.CREATE) {
			// TODO:
			// Creating datasets is not currently Supported
		} else if (mode() == FormEditMode.READ_ONLY) {
			dataset().metaForView(new ObjectResolveHandler<XmlElement>() {

				@Override
				public void resolved(XmlElement me) {

					if (me != null) {
						_metaForm = XmlMetaForm.formFor(me, mode());
						putTab(META_TAB_NAME, new ScrollPanel(_metaForm, ScrollPolicy.AUTO), false);
					}
				}
			}, false);
		} else if (mode() == FormEditMode.UPDATE) {
			dataset().metaForEdit(new ObjectResolveHandler<XmlElement>() {

				@Override
				public void resolved(XmlElement me) {

					if (me != null) {
						_metaForm = XmlMetaForm.formFor(me, mode());
						putTab(META_TAB_NAME, new ScrollPanel(_metaForm, ScrollPolicy.AUTO), false);
					}
				}
			}, false);
		}
	}

	private void updateDicomTab() {

		if (dataset() instanceof DerivationDataSetRef) {
			dataset().resolve(new ObjectResolveHandler<PSSDObject>() {
				@Override
				public void resolved(PSSDObject o) {

					if (o != null) {
						if (o instanceof DicomDerivationDataSet) {
							DicomDerivationDataSet ds = (DicomDerivationDataSet) o;
							putTab(DICOM_TAB_NAME, new DicomImageNavigator(ds.assetId(), ds.size()), false);
						}
					}
				}
			});
		}
	}

	@Override
	protected void addToInterfaceForm(Form interfaceForm) {

		Field<String> mimeTypeField = new Field<String>(new FieldDefinition("type", StringType.DEFAULT, "MIME Type",
				null, 1, 1));
		mimeTypeField.setValue(dataset().type());
		interfaceForm.add(mimeTypeField);

		FieldGroup fg = new FieldGroup(new FieldDefinition("source", ConstantType.DEFAULT, null, null, 1, 1));
		Field<String> sourceTypeField = new Field<String>(new FieldDefinition("type", StringType.DEFAULT,
				"Source Type", null, 1, 1));
		sourceTypeField.setValue(dataset().sourceType());
		fg.add(sourceTypeField);
		interfaceForm.add(fg);
		Field<String> vidField = new Field<String>(new FieldDefinition("vid", StringType.DEFAULT, null, null, 1, 1));
		vidField.setValue(dataset().vid());
		interfaceForm.add(vidField);
		if (dataset() instanceof PrimaryDataSetRef) {
			FieldGroup fgAcquisition = new FieldGroup(new FieldDefinition("acquisition", ConstantType.DEFAULT, null,
					null, 1, 1));
			FieldGroup fgSubject = new FieldGroup(
					new FieldDefinition("subject", ConstantType.DEFAULT, null, null, 1, 1));
			Field<String> subjectIdField = new Field<String>(new FieldDefinition("id", StringType.DEFAULT, null, null,
					1, 1));
			subjectIdField.setValue(((PrimaryDataSetRef) dataset()).subjectId());
			fgSubject.add(subjectIdField);
			Field<String> subjectStateField = new Field<String>(new FieldDefinition("state", StringType.DEFAULT, null,
					null, 1, 1));
			subjectStateField.setValue(((PrimaryDataSetRef) dataset()).subjectState());
			fgSubject.add(subjectStateField);
			fgAcquisition.add(fgSubject);
			interfaceForm.add(fgAcquisition);
		}
		if (dataset() instanceof DerivationDataSetRef) {
			FieldGroup fgDerivation = new FieldGroup(new FieldDefinition("derivation", ConstantType.DEFAULT, null,
					null, 1, 1));
			FieldGroup fgMethod = new FieldGroup(new FieldDefinition("method", ConstantType.DEFAULT, null, null, 1, 1));
			Field<String> methodIdField = new Field<String>(new FieldDefinition("id", StringType.DEFAULT, "Method id",
					null, 1, 1));
			methodIdField.setValue(((DerivationDataSetRef) dataset()).methodId());
			fgMethod.add(methodIdField);
			Field<String> methodStepField = new Field<String>(new FieldDefinition("step", StringType.DEFAULT,
					"Method step", null, 1, 1));
			methodStepField.setValue(((DerivationDataSetRef) dataset()).methodStep());
			fgMethod.add(methodStepField);
			fgDerivation.add(fgMethod);
			interfaceForm.add(fgDerivation);
		}
		if (dataset().data() != null) {
			interfaceForm.add(createDataContentFieldGroup(dataset().data()));
		}
	}

	private static FieldGroup createDataContentFieldGroup(DataContent o) {

		FieldGroup fg = new FieldGroup(new FieldDefinition("data", ConstantType.DEFAULT, null, null, 1, 1));
		Field<String> typeField = new Field<String>(new FieldDefinition("type", StringType.DEFAULT, null, null, 1, 1));
		typeField.setValue(o.mimeType());
		fg.add(typeField);
		Field<String> extField = new Field<String>(new FieldDefinition("extension", StringType.DEFAULT, null, null, 1,
				1));
		extField.setValue(o.extension());
		fg.add(extField);
		Field<Long> sizeField = new Field<Long>(new FieldDefinition("size", StringType.DEFAULT, "(units: " + o.units()
				+ ")", null, 1, 1));
		sizeField.setValue(o.size());
		fg.add(sizeField);
		Field<String> csumField = new Field<String>(new FieldDefinition("csum", StringType.DEFAULT, "(base: "
				+ o.checksum().base() + ")", null, 1, 1));
		csumField.setValue(o.checksum().toString());
		fg.add(csumField);
		Field<String> storeField = new Field<String>(new FieldDefinition("store", StringType.DEFAULT, null, null, 1, 1));
		storeField.setValue(o.store());
		fg.add(storeField);
		Field<String> urlField = new Field<String>(new FieldDefinition("url", StringType.DEFAULT, null, null, 1, 1));
		urlField.setValue(o.url());
		fg.add(urlField);
		return fg;
	}

	public Form metaForm() {

		return _metaForm;
	}

}
