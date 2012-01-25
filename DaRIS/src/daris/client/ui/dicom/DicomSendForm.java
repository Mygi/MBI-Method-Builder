package daris.client.ui.dicom;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.IntegerType;
import arc.mf.dtype.StringType;
import arc.mf.object.BackgroundObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.dicom.DicomAE;
import daris.client.model.dicom.DicomAEEnum;
import daris.client.model.dicom.LocalAERef;
import daris.client.model.dicom.messages.DicomSend;
import daris.client.model.dicom.messages.DicomSend.PatientNameAction;
import daris.client.model.object.DObjectRef;

public class DicomSendForm extends ValidatedInterfaceComponent implements
		AsynchronousAction {

	private Form _form;
	private String _pid;
	// TODO: change to DObjectRef _root
	// private DObjectRef _root;

	private String _localAET;
	private String _remoteAET;
	private String _remoteHost;
	private int _remotePort;
	private PatientNameAction _patientNameAction = PatientNameAction.unchanged;

	public DicomSendForm(DObjectRef root) {

		this(root.id());
	}

	public DicomSendForm(String pid) {

		_pid = pid;
		_form = new Form();

		Field<String> rootIdField = new Field<String>(new FieldDefinition(
				"Root ID", ConstantType.DEFAULT,
				"Id of the root/parent object that contains DICOM datasets.",
				null, 1, 1));
		rootIdField.setValue(_pid);
		_form.add(rootIdField);

		FieldGroup localAEFieldGroup = new FieldGroup(new FieldDefinition(
				"Local AE", ConstantType.DEFAULT, null, null, 1, 1));
		final Field<String> localAETField = new Field<String>(
				new FieldDefinition("AE Title", StringType.DEFAULT, null, null,
						1, 1));
		localAETField.addListener(new FormItemListener<String>() {

			@Override
			public void itemValueChanged(FormItem<String> f) {

				_localAET = f.value();
			}

			@Override
			public void itemPropertyChanged(FormItem<String> f,
					Property property) {

			}
		});
		LocalAERef.instance().resolve(new ObjectResolveHandler<DicomAE>() {

			@Override
			public void resolved(DicomAE o) {

				if (o != null) {
					localAETField.setValue(o.aet());
				}
			}
		});
		localAEFieldGroup.add(localAETField);
		_form.add(localAEFieldGroup);
		FieldGroup remoteAEFieldGroup = new FieldGroup(new FieldDefinition(
				"Remote AE", ConstantType.DEFAULT, null, null, 1, 1));
		Field<DicomAE> remoteAESelectField = new Field<DicomAE>(
				new FieldDefinition("Select AE", new EnumerationType<DicomAE>(
						DicomAEEnum.instance()), null, null, 0, 1));
		remoteAEFieldGroup.add(remoteAESelectField);
		final Field<String> remoteAETField = new Field<String>(
				new FieldDefinition("AE Title", StringType.DEFAULT, null, null,
						1, 1));
		remoteAETField.addListener(new FormItemListener<String>() {

			@Override
			public void itemValueChanged(FormItem<String> f) {

				_remoteAET = f.value();
			}

			@Override
			public void itemPropertyChanged(FormItem<String> f,
					Property property) {

			}
		});
		remoteAEFieldGroup.add(remoteAETField);
		final Field<String> remoteHostField = new Field<String>(
				new FieldDefinition("Host/IP Address", StringType.DEFAULT,
						null, null, 1, 1));
		remoteHostField.addListener(new FormItemListener<String>() {

			@Override
			public void itemValueChanged(FormItem<String> f) {

				_remoteHost = f.value();
			}

			@Override
			public void itemPropertyChanged(FormItem<String> f,
					Property property) {

			}
		});
		remoteAEFieldGroup.add(remoteHostField);
		final Field<Integer> remotePortField = new Field<Integer>(
				new FieldDefinition("Port", IntegerType.DEFAULT, null, null, 1,
						1));
		remotePortField.addListener(new FormItemListener<Integer>() {

			@Override
			public void itemValueChanged(FormItem<Integer> f) {

				_remotePort = f.value();
			}

			@Override
			public void itemPropertyChanged(FormItem<Integer> f,
					Property property) {

			}
		});
		remoteAEFieldGroup.add(remotePortField);
		remoteAESelectField.addListener(new FormItemListener<DicomAE>() {

			@Override
			public void itemValueChanged(FormItem<DicomAE> f) {

				DicomAE ae = f.value();
				if (ae != null) {
					remoteAETField.setValue(ae.aet());
					remoteHostField.setValue(ae.host());
					remotePortField.setValue(ae.port());
				}
			}

			@Override
			public void itemPropertyChanged(FormItem<DicomAE> f,
					Property property) {

			}
		});

		_form.add(remoteAEFieldGroup);

		Field<PatientNameAction> patientNameActionField = new Field<PatientNameAction>(
				new FieldDefinition(
						"Patient Name",
						PatientNameAction.asEnumerationType(),
						"Sets the action performed on the patient name field of the DICOM file header before sending. Defaults to unchanged. Note: it will not change the local objects but only the intermediate files extracted from the objects.",
						null, 0, 1));
		patientNameActionField.setValue(_patientNameAction);
		patientNameActionField
				.addListener(new FormItemListener<PatientNameAction>() {

					@Override
					public void itemValueChanged(FormItem<PatientNameAction> f) {

						_patientNameAction = f.value();
					}

					@Override
					public void itemPropertyChanged(
							FormItem<PatientNameAction> f, Property property) {

					}
				});
		_form.add(patientNameActionField);

		addMustBeValid(_form);

//		_form.fitToParent();
		_form.setMarginLeft(20);

	}

	@Override
	public Widget gui() {

		_form.render();
		return _form;
	}

	@Override
	public void execute(final ActionListener l) {

		new DicomSend(_pid, _localAET, _remoteAET, _remoteHost, _remotePort,
				_patientNameAction).send(new BackgroundObjectMessageResponse() {

			@Override
			public void responded(Long id) {

				l.executed(id != null);

				new DicomSendMonitorDialog(id, _pid, _remoteAET + "@"
						+ _remoteHost + ":" + _remotePort, _form.window())
						.show();
			}
		});
	}
}
