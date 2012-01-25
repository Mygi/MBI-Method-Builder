package daris.client.ui.repository;

import java.util.Date;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.mf.client.util.DateTime;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.DateType;
import arc.mf.dtype.StringType;
import arc.mf.dtype.TextType;
import daris.client.model.repository.Repository;
import daris.client.ui.object.DObjectDetails;

public class RepositoryDetails extends DObjectDetails {

	public RepositoryDetails(Repository o, FormEditMode mode) {

		super(null, o, mode);
	}

	@Override
	protected void addInterfaceFields(Form interfaceForm) {

		super.addInterfaceFields(interfaceForm);
		final Repository ro = (Repository) object();

		/*
		 * Custodian
		 */
		if (ro.custodian() != null || !mode().equals(FormEditMode.READ_ONLY)) {
			addCustodianFields(interfaceForm, ro.custodian(), mode());
		}

		/*
		 * Location
		 */
		if (ro.location() != null || !mode().equals(FormEditMode.READ_ONLY)) {
			addLocationFields(interfaceForm, ro.location(), mode());
		}

		/*
		 * Data Holdings
		 */
		if (ro.dataHoldings() != null || !mode().equals(FormEditMode.READ_ONLY)) {
			addDataHoldingsFields(interfaceForm, ro.dataHoldings(), mode());
		}

		/*
		 * Rights
		 */
		Field<String> rightsField = new Field<String>(new FieldDefinition("Rights", TextType.DEFAULT, "Rights", null,
				0, 1));
		rightsField.setValue(ro.rights());
		if (!mode().equals(FormEditMode.READ_ONLY)) {
			rightsField.addListener(new FormItemListener<String>() {

				@Override
				public void itemValueChanged(FormItem<String> f) {

					ro.setRights(f.value());
				}

				@Override
				public void itemPropertyChanged(FormItem<String> f, Property property) {

				}
			});
		}

		/*
		 * Server and database information
		 */
		if (!mode().equals(FormEditMode.CREATE)) {
			Repository.Server server = ro.server();
			if (server != null) {
				addServerFields(interfaceForm, server, mode());
			}
			Repository.Database database = ro.database();
			if (database != null) {
				addDatabaseFields(interfaceForm, database, mode());
			}
		}
	}

	private void addCustodianFields(Form interfaceForm, final Repository.Custodian custodian, FormEditMode mode) {

		final Repository ro = (Repository) object();
		FieldGroup fg = new FieldGroup(new FieldDefinition("Custodian", ConstantType.DEFAULT, "Custodian", null, 0, 1));
		final Field<String> prefixField = new Field<String>(new FieldDefinition("Prefix", StringType.DEFAULT, "Prefix",
				null, 0, 1));
		fg.add(prefixField);
		final Field<String> firstNameField = new Field<String>(new FieldDefinition("First Name", StringType.DEFAULT,
				"First Name", null, 0, 1));
		fg.add(firstNameField);
		final Field<String> middleNameField = new Field<String>(new FieldDefinition("Middle Name", StringType.DEFAULT,
				"Middle Name", null, 0, 1));
		fg.add(middleNameField);
		final Field<String> lastNameField = new Field<String>(new FieldDefinition("Last Name", StringType.DEFAULT,
				"Last Name", null, 0, 1));
		fg.add(lastNameField);
		final Field<String> emailField = new Field<String>(new FieldDefinition("E-Mail", StringType.DEFAULT, "E-Mail",
				null, 1, 1));
		fg.add(emailField);
		final Field<String> addressField = new Field<String>(new FieldDefinition("Address", TextType.DEFAULT,
				"Address", null, 0, 1));
		fg.add(addressField);
		if (custodian != null) {
			prefixField.setValue(custodian.prefix());
			firstNameField.setValue(custodian.firstName());
			middleNameField.setValue(custodian.middleName());
			lastNameField.setValue(custodian.lastName());
			emailField.setValue(custodian.email());
			addressField.setValue(custodian.address());
		}
		if (!mode.equals(FormEditMode.READ_ONLY)) {
			FormItemListener<String> fl = new FormItemListener<String>() {

				@Override
				public void itemValueChanged(FormItem<String> f) {

					String email = emailField.value();
					if (email != null) {
						ro.setCustodian(prefixField.value(), firstNameField.value(), middleNameField.value(),
								lastNameField.value(), addressField.value(), email);
					}
				}

				@Override
				public void itemPropertyChanged(FormItem<String> f, Property property) {

				}
			};
			prefixField.addListener(fl);
			firstNameField.addListener(fl);
			middleNameField.addListener(fl);
			lastNameField.addListener(fl);
			emailField.addListener(fl);
			addressField.addListener(fl);
		}
		interfaceForm.add(fg);
	}

	private void addLocationFields(Form interfaceForm, final Repository.Location location, FormEditMode mode) {

		final Repository ro = (Repository) object();
		FieldGroup fg = new FieldGroup(new FieldDefinition("Location", ConstantType.DEFAULT, "Location", null, 0, 1));
		final Field<String> buildingField = new Field<String>(new FieldDefinition("Building", StringType.DEFAULT,
				"Building", null, 0, 1));
		fg.add(buildingField);
		final Field<String> departmentField = new Field<String>(new FieldDefinition("Department", StringType.DEFAULT,
				"Department", null, 0, 1));
		fg.add(departmentField);
		final Field<String> institutionField = new Field<String>(new FieldDefinition("Institution", StringType.DEFAULT,
				"Institution", null, 0, 1));
		fg.add(institutionField);
		final Field<String> precinctField = new Field<String>(new FieldDefinition("Precinct", StringType.DEFAULT,
				"Precinct", null, 0, 1));
		fg.add(precinctField);
		if (location != null) {
			buildingField.setValue(location.building());
			departmentField.setValue(location.department());
			institutionField.setValue(location.institution());
			precinctField.setValue(location.precinct());
		}
		if (!mode.equals(FormEditMode.READ_ONLY)) {
			FormItemListener<String> fl = new FormItemListener<String>() {

				@Override
				public void itemValueChanged(FormItem<String> f) {

					String institution = institutionField.value();
					if (institution != null) {
						ro.setLocation(buildingField.value(), departmentField.value(), institution,
								precinctField.value());
					}
				}

				@Override
				public void itemPropertyChanged(FormItem<String> f, Property property) {

				}
			};
			buildingField.addListener(fl);
			departmentField.addListener(fl);
			institutionField.addListener(fl);
			precinctField.addListener(fl);
		}
		interfaceForm.add(fg);
	}

	private void addDataHoldingsFields(Form interfaceForm, final Repository.DataHoldings dataHoldings, FormEditMode mode) {

		final Repository ro = (Repository) object();
		FieldGroup fg = new FieldGroup(new FieldDefinition("Data Holdings", ConstantType.DEFAULT, "Data Holdings",
				null, 0, 1));
		final Field<String> descriptionField = new Field<String>(new FieldDefinition("Description", StringType.DEFAULT,
				"Description", null, 0, 1));
		fg.add(descriptionField);
		final Field<Date> startDateField = new Field<Date>(new FieldDefinition("Start Date", DateType.DEFAULT,
				"Start Date", null, 0, 1));
		fg.add(startDateField);
		if (dataHoldings != null) {
			descriptionField.setValue(dataHoldings.description());
			startDateField.setValue(dataHoldings.startDate());
		}
		if (!mode.equals(FormEditMode.READ_ONLY)) {
			descriptionField.addListener(new FormItemListener<String>() {

				@Override
				public void itemValueChanged(FormItem<String> f) {

					if (f.value() != null) {
						ro.setDataHoldings(startDateField.value(), f.value());
					}
				}

				@Override
				public void itemPropertyChanged(FormItem<String> f, Property property) {

				}
			});
			startDateField.addListener(new FormItemListener<Date>() {

				@Override
				public void itemValueChanged(FormItem<Date> f) {

					if (descriptionField.value() != null) {
						ro.setDataHoldings(f.value(), descriptionField.value());
					}
				}

				@Override
				public void itemPropertyChanged(FormItem<Date> f, Property property) {

				}
			});
		}
		interfaceForm.add(fg);
	}

	private void addServerFields(Form interfaceForm, Repository.Server server, FormEditMode mode) {

		FieldGroup fg = new FieldGroup(new FieldDefinition("Server", ConstantType.DEFAULT, "Mediaflux server identity",
				null, 1, 1));
		Field<String> f = new Field<String>(new FieldDefinition("PRoute", ConstantType.DEFAULT, "Server route", null,
				1, 1));
		f.setValue(server.proute());
		fg.add(f);
		f = new Field<String>(new FieldDefinition("UUID", ConstantType.DEFAULT, "Server UUID", null, 1, 1));
		f.setValue(server.uuid());
		fg.add(f);
		f = new Field<String>(new FieldDefinition("Name", ConstantType.DEFAULT, "Server Name", null, 1, 1));
		f.setValue(server.name());
		fg.add(f);
		f = new Field<String>(new FieldDefinition("Organization", ConstantType.DEFAULT, "Organization", null, 1, 1));
		f.setValue(server.organization());
		fg.add(f);
		interfaceForm.add(fg);
	}

	private void addDatabaseFields(Form interfaceForm, Repository.Database database, FormEditMode mode) {

		FieldGroup fg = new FieldGroup(new FieldDefinition("Database", ConstantType.DEFAULT, "Database description",
				null, 1, 1));
		Field<String> f = new Field<String>(new FieldDefinition("Type", ConstantType.DEFAULT, "Type", null, 1, 1));
		f.setValue(database.type());
		fg.add(f);
		f = new Field<String>(new FieldDefinition("Version", ConstantType.DEFAULT, "Version", null, 1, 1));
		f.setValue(database.version());
		fg.add(f);
		f = new Field<String>(new FieldDefinition("Vendor", ConstantType.DEFAULT, "Vendor", null, 1, 1));
		f.setValue(database.vendor());
		fg.add(f);
		f = new Field<String>(new FieldDefinition("Description", ConstantType.DEFAULT, "Description", null, 1, 1));
		f.setValue(database.description());
		fg.add(f);
		f = new Field<String>(new FieldDefinition("Creation Time", ConstantType.DEFAULT, "ctime", null, 1, 1));
		f.setValue(DateTime.dateTimeAsClientString(database.ctime()));
		fg.add(f);
		interfaceForm.add(fg);
	}
}
