package daris.client.ui.sc;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.client.util.Validity;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.StringType;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.sc.Archive;
import daris.client.model.sc.DeliveryDestination;
import daris.client.model.sc.DeliveryDestinationEnum;
import daris.client.model.sc.DeliveryMethod;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.Status;
import daris.client.model.sc.messages.ShoppingCartUpdate;
import daris.client.model.transcode.Transcode;
import daris.client.model.transcode.TranscodeEnum;

public class ShoppingCartSettingsForm extends Form implements
		AsynchronousAction {

	private ShoppingCart _cart;

	private Field<DeliveryDestination> _destinationField;
	private Field<Archive.Type> _archiveTypeField;

	public ShoppingCartSettingsForm(ShoppingCart cart) {
		super(Status.editable.equals(cart.status()) ? FormEditMode.UPDATE
				: FormEditMode.READ_ONLY);
		_cart = cart;
		// Field<String> statusField = new Field<String>(new FieldDefinition(
		// "Status", ConstantType.DEFAULT, "Status of the shopping cart",
		// null, 1, 1));
		// statusField.setValue(_cart.status().toString());
		// add(statusField);
		// if (_cart.totalNumberOfContentItems() > 0) {
		// Field<Integer> nbItemsField = new Field<Integer>(
		// new FieldDefinition("Number of Datasets",
		// ConstantType.DEFAULT,
		// "Total number of datasets in the shopping cart",
		// null, 1, 1));
		// nbItemsField.setValue(_cart.totalNumberOfContentItems());
		// add(nbItemsField);
		// }
		// if (_cart.totalSizeOfContentItems() > 0) {
		// Field<String> totalSizeField = new Field<String>(
		// new FieldDefinition("Total Size", ConstantType.DEFAULT,
		// "Total size of the datasets in the shopping cart",
		// null, 1, 1));
		// totalSizeField.setValue(ByteUtil.humanReadableByteCount(
		// _cart.totalSizeOfContentItems(), true));
		// add(totalSizeField);
		// }
		Field<String> nameField = new Field<String>(new FieldDefinition("Name",
				StringType.DEFAULT, "Name for the shopping-cart", null, 0, 1));
		if (_cart.name() != null) {
			nameField.setValue(_cart.name());
		}
		nameField.addListener(new FormItemListener<String>() {

			@Override
			public void itemValueChanged(FormItem<String> f) {
				_cart.setName(f.value());
			}

			@Override
			public void itemPropertyChanged(FormItem<String> f,
					Property property) {

			}
		});
		add(nameField);

		_destinationField = new Field<DeliveryDestination>(new FieldDefinition(
				"Destination", new EnumerationType<DeliveryDestination>(
						DeliveryDestinationEnum.instance()), "Destination", null, 1, 1));
		add(_destinationField);
		_destinationField
				.addListener(new FormItemListener<DeliveryDestination>() {

					@Override
					public void itemValueChanged(FormItem<DeliveryDestination> f) {
						DeliveryDestination destination = f.value();
						_cart.setDestination(destination);
						if (_archiveTypeField == null) {
							return;
						}
						if (_archiveTypeField.value() == null) {
							return;
						}
						if (destination.method() == DeliveryMethod.deposit) {
							if (_archiveTypeField.value() != Archive.Type.none) {
								_archiveTypeField.setValue(Archive.Type.none);
							}
						} else {
							if (_archiveTypeField.value() == Archive.Type.none) {
								_archiveTypeField.setValue(Archive.Type.zip);
							}
						}
					}

					@Override
					public void itemPropertyChanged(
							FormItem<DeliveryDestination> f, Property property) {

					}
				});
		_destinationField.setValue(_cart.destination());

		FieldGroup archiveFieldGroup = new FieldGroup(new FieldDefinition(
				"Archive", ConstantType.DEFAULT, "Archive", null, 1, 1));
		_archiveTypeField = new Field<Archive.Type>(new FieldDefinition("Type",
				Archive.Type.asEnumerationType(), "Archive type", null, 1, 1));
		_archiveTypeField.addListener(new FormItemListener<Archive.Type>() {

			@Override
			public void itemValueChanged(FormItem<Archive.Type> f) {
				_cart.setArchive(new Archive(f.value()));
			}

			@Override
			public void itemPropertyChanged(FormItem<Archive.Type> f,
					Property property) {

			}
		});
		_archiveTypeField.setValue(_cart.archive().type());
		archiveFieldGroup.add(_archiveTypeField);
		add(archiveFieldGroup);

		if (_cart.transcodes() != null) {
			FieldGroup transcodesFieldGroup = new FieldGroup(
					new FieldDefinition("Data Transformation",
							ConstantType.DEFAULT, null, null, 1, 1));
			for (Transcode t : _cart.transcodes()) {
				FieldGroup transcodeFieldGroup = new FieldGroup(
						new FieldDefinition("transcode", ConstantType.DEFAULT,
								null, null, 1, 1));
				Field<String> transcodeFromField = new Field<String>(
						new FieldDefinition("from", ConstantType.DEFAULT, null,
								null, 1, 1));
				transcodeFromField.setValue(t.from());
				transcodeFieldGroup.add(transcodeFromField);
				Field<Transcode> transcodeToField = new Field<Transcode>(
						new FieldDefinition("to",
								new EnumerationType<Transcode>(
										new TranscodeEnum(t.from())), null,
								null, 1, 1));
				transcodeToField.setValue(t);
				transcodeToField.addListener(new FormItemListener<Transcode>() {

					@Override
					public void itemValueChanged(FormItem<Transcode> f) {

						_cart.addTranscode(f.value());
					}

					@Override
					public void itemPropertyChanged(FormItem<Transcode> f,
							FormItem.Property p) {

					}
				});
				transcodeFieldGroup.add(transcodeToField);
				transcodesFieldGroup.add(transcodeFieldGroup);
			}
			add(transcodesFieldGroup);
		}
	}

	public Validity valid() {
		Validity valid = super.valid();
		if (!valid.valid()) {
			return valid;
		}
		final DeliveryDestination destination = _destinationField.value();
		final DeliveryMethod deliveryMethod = destination.method();
		Archive.Type archiveType = _archiveTypeField.value();
		if (DeliveryMethod.deposit.equals(deliveryMethod)
				&& !Archive.Type.none.equals(archiveType)) {
			return new Validity() {

				@Override
				public boolean valid() {
					return false;
				}

				@Override
				public String reasonForIssue() {
					return "Destination: " + destination.name() + " is an "
							+ deliveryMethod
							+ " destination. The archive type must be none";
				}
			};
		}
		if (DeliveryMethod.download.equals(deliveryMethod)
				&& Archive.Type.none.equals(archiveType)) {
			return new Validity() {

				@Override
				public boolean valid() {
					return false;
				}

				@Override
				public String reasonForIssue() {
					return "Destination: " + destination.name() + " is an "
							+ deliveryMethod
							+ " destination. The archive type must not be none";
				}
			};
		}
		return valid;
	}

	@Override
	public void execute(final ActionListener l) {
		if (!Status.editable.equals(_cart.status())) {
			if (l != null) {
				l.executed(false);
			}
			return;
		}
		new ShoppingCartUpdate(_cart)
				.send(new ObjectMessageResponse<Boolean>() {

					@Override
					public void responded(Boolean r) {
						if (l == null) {
							return;
						}
						if (r != null) {
							l.executed(r);
						} else {
							l.executed(false);
						}
					}
				});
	}
}
