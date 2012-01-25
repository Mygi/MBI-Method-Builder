package daris.gui.sc;

import java.util.List;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.FieldValidHandler;
import arc.gui.form.FieldValueValidator;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItemListener;
import arc.gui.form.FormListener;
import arc.gui.gwt.dnd.DropCheck;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.dnd.DropListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.window.Window;
import arc.gui.window.WindowProperties;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.StringType;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import daris.client.ui.widget.CTabPanel;
import daris.client.ui.widget.CTabPanel.Tab;
import daris.model.object.PSSDObjectRef;
import daris.model.sc.ContentItem;
import daris.model.sc.Destination;
import daris.model.sc.DestinationEnum;
import daris.model.sc.Layout;
import daris.model.sc.LayoutEnum;
import daris.model.sc.MetadataOutput;
import daris.model.sc.ShoppingCartListener;
import daris.model.sc.ShoppingCartManager;
import daris.model.sc.ShoppingCartRef;
import daris.model.sc.Status;
import daris.model.sc.archive.Archive;
import daris.model.transcode.Transcode;
import daris.model.transcode.TranscodeEnum;

public class ShoppingCartDialog implements ShoppingCartListener {

	private static ShoppingCartDialog _instance;

	public static void show() {

		if (_instance == null) {
			_instance = new ShoppingCartDialog();
		}
		_instance.showMe();
	}

	private ShoppingCartRef _cart;
	private CTabPanel _tp;
	private ShoppingCartContentGrid _contentsGrid;
	private Button _removeButton;
	private Button _clearButton;
	private Button _downloadButton;
	private SimplePanel _settingsSP;
	private Form _settingsForm;
	private Button _applyButton;

	private Window _win;
	private WindowProperties _wp;

	private ShoppingCartDialog() {

		/*
		 * Layout
		 */
		_tp = new CTabPanel();
		_tp.fitToParent();
		//
		// Content Tab
		//
		VerticalPanel contentsVP = new VerticalPanel();
		contentsVP.fitToParent();
		_contentsGrid = new ShoppingCartContentGrid();
		contentsVP.add(_contentsGrid);
		ButtonBar contentsBB = new ButtonBar(ButtonBar.Position.BOTTOM,
				ButtonBar.Alignment.CENTER);
		contentsBB.setWidth100();
		contentsBB.setHeight(28);
		contentsBB.setColourEnabled(false);
		contentsBB.setMargin(0);
		contentsBB.setBackgroundColour("#DDDDDD");
		_removeButton = new Button("Remove");
		_removeButton.disable();
		_removeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				if (_contentsGrid.selections() != null) {
					if (!_contentsGrid.selections().isEmpty()) {
						_cart.removeContentItems(_contentsGrid.selections());
					}
				}
			}
		});
		contentsBB.add(_removeButton);
		_clearButton = new Button("Clear");
		_clearButton.disable();
		_clearButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				_cart.clearContentItems();
			}
		});
		contentsBB.add(_clearButton);

		_downloadButton = new Button("Download");
		_downloadButton.disable();
		_downloadButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				_cart.order();
				_win.close();
			}

		});
		contentsBB.add(_downloadButton);
		contentsVP.add(contentsBB);
		_tp.addTab(new Tab("Contents", contentsVP, false));

		//
		// Settings Tab
		//
		VerticalPanel settingsVP = new VerticalPanel();
		settingsVP.fitToParent();
		_settingsSP = new SimplePanel();
		_settingsSP.fitToParent();
		settingsVP.add(_settingsSP);
		ButtonBar settingsBB = new ButtonBar(ButtonBar.Position.BOTTOM,
				ButtonBar.Alignment.CENTER);
		settingsBB.setWidth100();
		settingsBB.setHeight(28);
		settingsBB.setMargin(15);
		settingsBB.setColourEnabled(false);
		settingsBB.setMargin(0);
		settingsBB.setBackgroundColour("#DDDDDD");
		_applyButton = new Button("Apply");
		_applyButton.disable();
		_applyButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				if (_applyButton.enabled()) {
					_settingsForm.validate();
					_applyButton.disable();
					_cart.commitChanges();
				}
			}
		});
		settingsBB.add(_applyButton);
		settingsVP.add(settingsBB);
		_tp.addTab(new Tab("Settings", settingsVP, false));
		/*
		 * Set data
		 */
		update();
		/*
		 * Window Properties
		 */
		_wp = new WindowProperties();
		_wp.setModal(false);
		_wp.setCanBeResized(true);
		_wp.setCanBeClosed(true);
		_wp.setCanBeMoved(true);
		_wp.setCenterInPage(true);
		_wp.setTitle("Shopping Cart");
		_wp.setSize(
				(int) (com.google.gwt.user.client.Window.getClientWidth() * 0.6),
				(int) (com.google.gwt.user.client.Window.getClientHeight() * 0.6));
		/*
		 * D&D
		 */
		_tp.makeDropTarget(new DropHandler() {

			@Override
			public DropCheck checkCanDrop(Object data) {

				if (data instanceof PSSDObjectRef) {
					return DropCheck.CAN;
				}
				return DropCheck.CANNOT;
			}

			@Override
			public void drop(BaseWidget target, final List<Object> data,
					final DropListener dl) {

				if (data == null) {
					return;
				}
				if (data.isEmpty()) {
					return;
				}
				final PSSDObjectRef o = (PSSDObjectRef) data.get(0);

				_cart.addContentItem(o, true);
				dl.dropped(DropCheck.CAN);
			}
		});
		/*
		 * subscribe
		 */
		ShoppingCartManager.instance().addListener(this);
	}

	private void showMe() {

		_win = Window.create(_wp);
		_win.setContent(_tp);
		_win.centerInPage();
		_win.show();
	}

	private void updateSettingsSP() {

		FormEditMode mode = _cart.status().value() == Status.Value.editable ? FormEditMode.UPDATE
				: FormEditMode.READ_ONLY;
		_settingsForm = new Form(mode);

		/*
		 * id
		 */
		if (mode != FormEditMode.CREATE) {
			Field<String> idField = new Field<String>(new FieldDefinition("id",
					ConstantType.DEFAULT, null, null, 1, 1));
			idField.setValue(_cart.id());
			_settingsForm.add(idField);
		}
		/*
		 * status
		 */
		if (mode != FormEditMode.CREATE) {
			Field<Status.Value> statusValueField = new Field<Status.Value>(
					new FieldDefinition("status", ConstantType.DEFAULT, null,
							null, 1, 1));
			statusValueField.setValue(_cart.status().value());
			_settingsForm.add(statusValueField);
			if (_cart.status().log() != null) {
				Field<String> statusLogField = new Field<String>(
						new FieldDefinition("log", ConstantType.DEFAULT, null,
								null, 1, 1));
				statusLogField.setValue(_cart.status().log());
				_settingsForm.add(statusLogField);
			}
		}
		/*
		 * order
		 */
		if (mode != FormEditMode.CREATE) {
			if (_cart.orderId() != null) {
				Field<String> orderField = new Field<String>(
						new FieldDefinition("order", ConstantType.DEFAULT,
								null, null, 1, 1));
				orderField.setValue(_cart.orderId());
				_settingsForm.add(orderField);
			}
		}
		/*
		 * name
		 */
		Field<String> nameField = new Field<String>(new FieldDefinition("name",
				StringType.DEFAULT, null, null, 0, 1));
		nameField.setValue(_cart.name());
		if (mode != FormEditMode.READ_ONLY) {
			nameField.addValueValidator(new FieldValueValidator<String>() {

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
			nameField.addListener(new FormItemListener<String>() {

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
		_settingsForm.add(nameField);
		/*
		 * destination
		 */
		final Field<Destination> destinationField = new Field<Destination>(
				new FieldDefinition(
						"destination",
						new EnumerationType<Destination>(DestinationEnum.get()),
						null, null, 1, 1));
		destinationField.setValue(_cart.destination());
		if (mode == FormEditMode.CREATE) {
			Destination
					.defaultDestination(new ObjectResolveHandler<Destination>() {

						@Override
						public void resolved(Destination d) {

							_cart.setDestination(d);
							destinationField.setValue(_cart.destination());
						}
					});
		}
		if (mode != FormEditMode.READ_ONLY) {
			destinationField.addListener(new FormItemListener<Destination>() {

				@Override
				public void itemValueChanged(FormItem<Destination> f) {

					Destination oldDest = _cart.destination();
					Destination newDest = f.value();
					_cart.setDestination(newDest);
					boolean needUpdate = false;
					if (oldDest == null) {
						needUpdate = true;
					} else {
						if (oldDest.type() != newDest.type()) {
							needUpdate = true;
						}
					}
					if (needUpdate) {
						updateSettingsSP();
					}
				}

				@Override
				public void itemPropertyChanged(FormItem<Destination> f,
						FormItem.Property p) {

				}
			});
		}
		_settingsForm.add(destinationField);
		/*
		 * archive
		 */
		if (_cart.destination().type() == Destination.Type.deposit) {
			_cart.setArchiveType(Archive.Type.none);
		}
		Field<Archive.Type> archiveTypeField = new Field<Archive.Type>(new FieldDefinition(
				"archive", Archive.Type.asEnumerationType(_cart.destination().type()), null,
				null, 1, 1));
		archiveTypeField.setValue(_cart.archive().type());
		if (mode != FormEditMode.READ_ONLY) {
			archiveTypeField.addListener(new FormItemListener<Archive.Type>() {

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
		_settingsForm.add(archiveTypeField);
		/*
		 * layout
		 */
		final Field<Layout> layoutField = new Field<Layout>(
				new FieldDefinition("layout", new EnumerationType<Layout>(
						LayoutEnum.get()), null, null, 1, 1));
		layoutField.setValue(_cart.layout());
		if (mode == FormEditMode.CREATE) {
			Layout.defaultLayout(new ObjectResolveHandler<Layout>() {

				@Override
				public void resolved(Layout l) {

					_cart.setLayout(l);
					layoutField.setValue(_cart.layout());
				}
			});
		}
		if (mode != FormEditMode.READ_ONLY) {
			layoutField.addListener(new FormItemListener<Layout>() {

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
		_settingsForm.add(layoutField);
		/*
		 * metadata-output
		 */
		Field<MetadataOutput> metadataOutputField = new Field<MetadataOutput>(
				new FieldDefinition("metadata-output",
						MetadataOutput.enumerationType(), null, null, 1, 1));
		metadataOutputField.setValue(_cart.metadataOutput());
		metadataOutputField.addListener(new FormItemListener<MetadataOutput>() {

			@Override
			public void itemValueChanged(FormItem<MetadataOutput> f) {

				_cart.setMetadataOutput(f.value());
			}

			@Override
			public void itemPropertyChanged(FormItem<MetadataOutput> f,
					FormItem.Property p) {

			}
		});
		_settingsForm.add(metadataOutputField);
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
			_settingsForm.add(contentFieldGroup);
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
			_settingsForm.add(transcodesFieldGroup);
		}
		_settingsForm.addListener(new FormListener() {

			@Override
			public void rendering(Form f) {

			}

			@Override
			public void rendered(Form f) {

			}

			@Override
			public void formValuesUpdated(Form f) {

				_applyButton.enable();
			}

			@Override
			public void formStateUpdated(Form f, FormItem.Property p) {

			}
		});
		_settingsForm.render();
		_settingsSP
				.setContent(new ScrollPanel(_settingsForm, ScrollPolicy.AUTO));
	}

	private void setData(ShoppingCartRef cart) {

		_cart = cart;
		/*
		 * Update contents tab
		 */
		_contentsGrid.setData(_cart);
		/*
		 * Update settings tab
		 */
		updateSettingsSP();
		/*
		 * Update button states
		 */
		_cart.contentItems(new ObjectResolveHandler<List<ContentItem>>() {

			@Override
			public void resolved(List<ContentItem> items) {

				if (items != null) {
					if (!items.isEmpty()) {
						// has contents
						_applyButton.disable();
						_removeButton.enable();
						_clearButton.enable();
						_downloadButton.enable();
					}
				}
			}
		}, false);

	}

	private void update() {

		ShoppingCartManager.instance().editableShoppingCart(
				new ObjectResolveHandler<ShoppingCartRef>() {

					@Override
					public void resolved(ShoppingCartRef cart) {

						setData(cart);
					}
				});
	}

	@Override
	public void created(ShoppingCartRef cart) {

	}

	@Override
	public void deleted(ShoppingCartRef cart) {

		if (_cart.equals(cart)) {
			update();
		}
	}

	@Override
	public void updated(ShoppingCartRef cart) {

		if (_cart.equals(cart)) {
			if (cart.status().value() != Status.Value.editable) {
				// status changed
				update();
			} else {
				setData(cart);
			}
		}
	}

	@Override
	public void contentChanged(ShoppingCartRef cart) {

		if (_cart.equals(cart)) {
			setData(cart);
		}
	}

	@Override
	public void dataReady(ShoppingCartRef cart) {

		// TODO Auto-generated method stub

	}

}
