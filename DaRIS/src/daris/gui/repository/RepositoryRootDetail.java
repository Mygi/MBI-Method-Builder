package daris.gui.repository;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.mf.dtype.StringType;
import arc.mf.object.ObjectResolveHandler;
import daris.gui.object.ObjectDetail;
import daris.model.object.PSSDObject;
import daris.model.repository.RepositoryRoot;
import daris.model.repository.RepositoryRootRef;

public class RepositoryRootDetail extends ObjectDetail {

	public RepositoryRootDetail() {
		super(RepositoryRootRef.instance(), FormEditMode.READ_ONLY);
	}

	public RepositoryRootRef repositoryRoot() {
		return (RepositoryRootRef) object();
	}

	@Override
	protected void addToInterfaceForm(final Form interfaceForm) {
		repositoryRoot().resolve(new ObjectResolveHandler<PSSDObject>() {

			@Override
			public void resolved(PSSDObject o) {
				RepositoryRoot rro = (RepositoryRoot) o;
				Field<String> uuidField = new Field<String>(
						new FieldDefinition("uuid", StringType.DEFAULT,
								"Server UUID", null, 1, 1));
				uuidField.setValue(rro.uuid());
				interfaceForm.add(uuidField);
				Field<String> orgField = new Field<String>(new FieldDefinition(
						"organization", StringType.DEFAULT, "Organization",
						null, 0, 1));
				orgField.setValue(rro.organization());
				interfaceForm.add(orgField);
			}
		});

	}

}
