package daris.gui.sc;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.FieldValidHandler;
import arc.gui.form.FieldValueValidator;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItemListener;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.StringType;
import arc.mf.object.ObjectResolveHandler;
import daris.model.sc.Destination;
import daris.model.sc.DestinationEnum;
import daris.model.sc.Layout;
import daris.model.sc.LayoutEnum;
import daris.model.sc.MetadataOutput;
import daris.model.sc.ShoppingCartRef;
import daris.model.sc.Status;
import daris.model.sc.archive.Archive;
import daris.model.transcode.Transcode;
import daris.model.transcode.TranscodeEnum;

public class ShoppingCartSettingsForm extends Form {

	private ShoppingCartRef _cart;

	private Field<String> _nameField;

	private Field<Layout> _layoutField;

	private Field<Archive.Type> _archiveTypeField;

	private Field<Destination> _destinationField;

	private Field<MetadataOutput> _metadataOutputField;

	public ShoppingCartSettingsForm(ShoppingCartRef cart, boolean create) {

		super(
				create ? FormEditMode.CREATE
						: (cart.status().value() == Status.Value.editable ? FormEditMode.UPDATE
								: FormEditMode.READ_ONLY));
		_cart = cart;
		assert _cart != null;
		initForm();
	}

	private void initForm() {

		/*
		 * id
		 */
		if (editMode() != FormEditMode.CREATE) {
			Field<String> idField = new Field<String>(new FieldDefinition("id",
					ConstantType.DEFAULT, null, null, 1, 1));
			idField.setValue(_cart.id());
			add(idField);
		}
		/*
		 * status
		 */
		if (editMode() != FormEditMode.CREATE) {
			Field<Status.Value> statusValueField = new Field<Status.Value>(
					new FieldDefinition("status", ConstantType.DEFAULT, null,
							null, 1, 1));
			statusValueField.setValue(_cart.status().value());
			add(statusValueField);
			if (_cart.status().log() != null) {
				Field<String> statusLogField = new Field<String>(
						new FieldDefinition("log", ConstantType.DEFAULT, null,
								null, 1, 1));
				statusLogField.setValue(_cart.status().log());
				add(statusLogField);
			}
		}
		/*
		 * order
		 */
		if (editMode() != FormEditMode.CREATE) {
			if (_cart.orderId() != null) {
				Field<String> orderField = new Field<String>(
						new FieldDefinition("order", ConstantType.DEFAULT,
								null, null, 1, 1));
				orderField.setValue(_cart.orderId());
				add(orderField);
			}
		}
		/*
		 * name
		 */
		_nameField = new Field<String>(new FieldDefinition("name",
				StringType.DEFAULT, null, null, 0, 1));
		_nameField.setValue(_cart.name());
		if (editMode() != FormEditMode.READ_ONLY) {
			_nameField.addValueValidator(new FieldValueValidator<String>() {

				@Override
				public void validate(Field<String> f, FieldValidHandler vh) {

					String v = (String) f.value();
					if (v.matches("[^\\/:*?\"'<>| ]*")) {
						vh.setValid();
					} else {
						vh.setInvalid("Name contains invalid characters: \\/:*?\"'<> |");
					}
				}
			});
			_nameField.addListener(new FormItemListener<String>() {

				@Override
				public void itemValueChanged(FormItem<String> f) {

					_cart.setName(f.value());
				}

				@Override
				public void itemPropertyChanged(FormItem<String> f,
						FormItem.Property p) {

				}
			});
		}
		add(_nameField);
		/*
		 * destination
		 */
		_destinationField = new Field<Destination>(new FieldDefinition(
				"destination", new EnumerationType<Destination>(
						DestinationEnum.get()), null, null, 1, 1));
		_destinationField.setValue(_cart.destination());
		if (editMode() == FormEditMode.CREATE) {
			Destination
					.defaultDestination(new ObjectResolveHandler<Destination>() {

						@Override
						public void resolved(Destination d) {

							_cart.setDestination(d);
							_destinationField.setValue(_cart.destination());
						}
					});
		}
		if (editMode() != FormEditMode.READ_ONLY) {
			_destinationField.addListener(new FormItemListener<Destination>() {

				@Override
				public void itemValueChanged(FormItem<Destination> f) {

					_cart.setDestination(f.value());
				}

				@Override
				public void itemPropertyChanged(FormItem<Destination> f,
						FormItem.Property p) {

				}
			});
		}
		add(_destinationField);
		/*
		 * archive
		 */
		_archiveTypeField = new Field<Archive.Type>(new FieldDefinition("archive",
				Archive.Type.asEnumerationType(), null, null, 1, 1));
		_archiveTypeField.setValue(_cart.archive().type());
		if (editMode() != FormEditMode.READ_ONLY) {
			_archiveTypeField.addListener(new FormItemListener<Archive.Type>() {

				@Override
				public void itemValueChanged(FormItem<Archive.Type> f) {

					_cart.setArchiveType(f.value());
				}

				@Override
				public void itemPropertyChanged(FormItem<Archive.Type> f,
						FormItem.Property p) {

				}
			});
		}
		add(_archiveTypeField);
		/*
		 * layout
		 */
		_layoutField = new Field<Layout>(
				new FieldDefinition("layout", new EnumerationType<Layout>(
						LayoutEnum.get()), null, null, 1, 1));
		_layoutField.setValue(_cart.layout());
		if (editMode() == FormEditMode.CREATE) {
			Layout.defaultLayout(new ObjectResolveHandler<Layout>() {

				@Override
				public void resolved(Layout l) {

					_cart.setLayout(l);
					_layoutField.setValue(_cart.layout());
				}
			});
		}
		if (editMode() != FormEditMode.READ_ONLY) {
			_layoutField.addListener(new FormItemListener<Layout>() {

				@Override
				public void itemValueChanged(FormItem<Layout> f) {

					_cart.setLayout(f.value());
				}

				@Override
				public void itemPropertyChanged(FormItem<Layout> f,
						FormItem.Property p) {

				}
			});
		}
		add(_layoutField);
		/*
		 * metadata-output
		 */
		_metadataOutputField = new Field<MetadataOutput>(new FieldDefinition(
				"metadata-output", MetadataOutput.enumerationType(), null,
				null, 1, 1));
		_metadataOutputField.setValue(_cart.metadataOutput());
		_metadataOutputField
				.addListener(new FormItemListener<MetadataOutput>() {

					@Override
					public void itemValueChanged(FormItem<MetadataOutput> f) {

						_cart.setMetadataOutput(f.value());
					}

					@Override
					public void itemPropertyChanged(FormItem<MetadataOutput> f,
							FormItem.Property p) {

					}
				});
		add(_metadataOutputField);
		/*
		 * content
		 */
		if (_cart.content() != null) {
			FieldGroup contentFieldGroup = new FieldGroup(new FieldDefinition(
					"content", ConstantType.DEFAULT, null, null, 1, 1));
			Field<Integer> countField = new Field<Integer>(new FieldDefinition(
					"count", ConstantType.DEFAULT, null, null, 1, 1));
			countField.setValue(_cart.content().totalCount());
			contentFieldGroup.add(countField);
			Field<Long> sizeField = new Field<Long>(new FieldDefinition("size",
					ConstantType.DEFAULT, "Units: " + _cart.content().units(),
					null, 1, 1));
			sizeField.setValue(_cart.content().totalSize());
			contentFieldGroup.add(sizeField);
			FieldGroup mimeTypeFieldGroup = new FieldGroup(new FieldDefinition(
					"mime-types", ConstantType.DEFAULT, null, null, 1, 1));
			if (_cart.content().mimeTypes() != null) {
				for (String mimeType : _cart.content().mimeTypes()) {
					Field<String> mimeTypeField = new Field<String>(
							new FieldDefinition(
									"mime-type(count:"
											+ _cart.content().itemCount(
													mimeType) + ")",
									ConstantType.DEFAULT, null, null, 1, 1));
					mimeTypeField.setValue(mimeType);
					mimeTypeFieldGroup.add(mimeTypeField);
				}
				contentFieldGroup.add(mimeTypeFieldGroup);
			}
			add(contentFieldGroup);
		}
		/*
		 * transcodes
		 */
		if (_cart.transcodes() != null) {
			FieldGroup transcodesFieldGroup = new FieldGroup(
					new FieldDefinition("data-transformation",
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
}
