package daris.client.ui.dataset;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.StringType;
import daris.client.model.dataset.DataSet;
import daris.client.model.dataset.DerivationDataSet;
import daris.client.model.dataset.DicomDataSet;
import daris.client.model.dataset.PrimaryDataSet;
import daris.client.model.dataset.SourceType;
import daris.client.model.object.DObjectRef;
import daris.client.ui.dicom.DicomImageNavigator;
import daris.client.ui.object.DObjectDetails;
import daris.client.ui.object.DataContentFieldGroup;

public class DataSetDetails extends DObjectDetails {

	private int _dicomTabId = 0;

	public DataSetDetails(DObjectRef po, DataSet o, FormEditMode mode) {

		super(po, o, mode);

		updateDicomTab();
	}

	private void removeDicomTab() {

		if (_dicomTabId > 0) {
			tabs().removeTabById(_dicomTabId);
			_dicomTabId = 0;
		}
	}

	private void updateDicomTab() {

		if (mode().equals(FormEditMode.CREATE)) {
			return;
		}
		DataSet ds = (DataSet) object();
		if (ds instanceof DicomDataSet) {
			DicomDataSet dds = (DicomDataSet) ds;
			if (_dicomTabId > 0) {
				tabs().setTabContent(_dicomTabId, new DicomImageNavigator(dds.assetId(), dds.size()));
			} else {
				_dicomTabId = tabs().addTab("DICOM", "DICOM Metadata & Image Navigator",
						new DicomImageNavigator(dds.assetId(), dds.size()));
			}
		}
	}

	@Override
	protected void addInterfaceFields(Form interfaceForm) {

		super.addInterfaceFields(interfaceForm);

		DataSet dso = (DataSet) object();
		Field<String> mimeTypeField = new Field<String>(new FieldDefinition("type", StringType.DEFAULT, "MIME Type",
				null, 1, 1));
		mimeTypeField.setValue(dso.type());
		interfaceForm.add(mimeTypeField);

		FieldGroup fg = new FieldGroup(new FieldDefinition("source", ConstantType.DEFAULT, null, null, 1, 1));
		Field<SourceType> sourceTypeField = new Field<SourceType>(new FieldDefinition("type", StringType.DEFAULT,
				"Source Type", null, 1, 1));
		sourceTypeField.setValue(dso.sourceType());
		fg.add(sourceTypeField);
		interfaceForm.add(fg);
		Field<String> vidField = new Field<String>(new FieldDefinition("vid", StringType.DEFAULT, null, null, 1, 1));
		vidField.setValue(dso.vid());
		interfaceForm.add(vidField);
		if (dso instanceof PrimaryDataSet) {
			FieldGroup fgAcquisition = new FieldGroup(new FieldDefinition("acquisition", ConstantType.DEFAULT, null,
					null, 1, 1));
			FieldGroup fgSubject = new FieldGroup(
					new FieldDefinition("subject", ConstantType.DEFAULT, null, null, 1, 1));
			Field<String> subjectIdField = new Field<String>(new FieldDefinition("id", StringType.DEFAULT, null, null,
					1, 1));
			subjectIdField.setValue(((PrimaryDataSet) dso).subjectId());
			fgSubject.add(subjectIdField);
			Field<String> subjectStateField = new Field<String>(new FieldDefinition("state", StringType.DEFAULT, null,
					null, 1, 1));
			subjectStateField.setValue(((PrimaryDataSet) dso).subjectState());
			fgSubject.add(subjectStateField);
			fgAcquisition.add(fgSubject);
			interfaceForm.add(fgAcquisition);
		}
		if (dso instanceof DerivationDataSet) {
			FieldGroup fgDerivation = new FieldGroup(new FieldDefinition("derivation", ConstantType.DEFAULT, null,
					null, 1, 1));
			FieldGroup fgMethod = new FieldGroup(new FieldDefinition("method", ConstantType.DEFAULT, null, null, 1, 1));
			Field<String> methodIdField = new Field<String>(new FieldDefinition("id", StringType.DEFAULT, "Method id",
					null, 1, 1));
			methodIdField.setValue(((DerivationDataSet) dso).methodId());
			fgMethod.add(methodIdField);
			Field<String> methodStepField = new Field<String>(new FieldDefinition("step", StringType.DEFAULT,
					"Method step", null, 1, 1));
			methodStepField.setValue(((DerivationDataSet) dso).methodStep());
			fgMethod.add(methodStepField);
			fgDerivation.add(fgMethod);
			interfaceForm.add(fgDerivation);
		}
		if (dso.data() != null) {
			interfaceForm.add(DataContentFieldGroup.fieldGroupFor(dso.data()));
		}
	}

}
