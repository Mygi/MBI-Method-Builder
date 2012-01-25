package daris.client.ui.form;

import java.util.List;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.FieldSet;
import arc.gui.form.Form;
import arc.gui.form.Form.BooleanAs;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem.XmlType;
import arc.gui.form.xml.XmlForm;
import arc.mf.client.util.UnhandledException;
import arc.mf.client.xml.XmlAttribute;
import arc.mf.client.xml.XmlElement;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.DocType;
import arc.mf.dtype.StringType;

public class XmlMetaForm {

	private static void addToForm(Form form, FormEditMode mode,
			List<XmlElement> mes) {

		if (mode != FormEditMode.READ_ONLY) {
			for (XmlElement e : mes) {
				assert e.name().equals("metadata");
				arc.mf.xml.defn.Element de = new arc.mf.xml.defn.Element(
						e.value("@type"));
				de.setMinOccurs(e.value("@requirement").equals("optional") ? 0
						: 1);
				de.setMaxOccurs(1);
				de.setDescription(e.value("description"));
				de.setDataType(DocType.DEFAULT);
				List<XmlElement> xdes = e.elements("definition/element");
				if (xdes != null) {
					for (XmlElement xde : xdes) {
						try {
							arc.mf.xml.defn.Element sde = new arc.mf.xml.defn.Element(
									de, xde);
							de.add(sde, false);
						} catch (Throwable t) {
							UnhandledException.report(null, t);
						}
					}
				}
				XmlForm.addToForm(form, de);
			}
		} else {
			for (XmlElement e : mes) {
				FieldGroup fg = new FieldGroup(new FieldDefinition(e.name(),
						StringType.DEFAULT, null, null, 1, 1));
				List<XmlElement> ces = e.elements();
				if (ces != null) {
					for (XmlElement ce : ces) {
						addXmlElement(fg, ce);
					}
				}
				form.add(fg);
			}
		}
	}

	private static void addToForm(Form form, FormEditMode mode, XmlElement xe) {

		if (xe.element("metadata") != null) {
			// XML docs with defintion
			List<XmlElement> mes = xe.elements("metadata");
			addToForm(form, mode, mes);
		} else {
			// XML docs without definition
			assert mode == FormEditMode.READ_ONLY;
			List<XmlElement> mes = xe.elements();
			addToForm(form, mode, mes);
		}

	}

	public static Form formFor(XmlElement xe, FormEditMode mode) {

		assert xe.name().equals("meta") || xe.name().equals("public")
				|| xe.name().equals("private") || xe.name().equals("method");
		Form form = new Form(mode);
		addToForm(form, mode, xe);
		form.setBooleanAs(BooleanAs.TRUE_FALSE);
		form.render();
		return form;
	}

	public static Form formFor(List<XmlElement> mes, FormEditMode mode) {

		Form form = new Form(mode);
		addToForm(form, mode, mes);
		form.setBooleanAs(BooleanAs.TRUE_FALSE);
		form.render();
		return form;
	}

	private static void addXmlElement(FieldSet fs, XmlElement e) {

		FieldDefinition fd = new FieldDefinition(e.name(),
				ConstantType.DEFAULT, null, null, 1, 1);
		if (!e.hasAttributes() && !e.hasElements()) {
			// no sub-nodes
			Field<String> f = new Field<String>(fd);
			f.setXmlType(XmlType.ELEMENT);
			if (e.value() != null) {
				f.setValue(e.value());
			}
			f.setXmlType(XmlType.ELEMENT);
			fs.add(f);
			return;
		}
		FieldGroup fg = new FieldGroup(fd);
		if (e.hasAttributes()) {
			// has attributes
			for (XmlAttribute a : e.attributes()) {
				Field<String> af = new Field<String>(new FieldDefinition(
						a.name(), ConstantType.DEFAULT, null, null, 1, 1));
				af.setXmlType(XmlType.ATTRIBUTE);
				if (a.value() != null) {
					af.setValue(a.value());
				}
				fg.add(af);
			}
		}
		if (e.value() != null) {
			// has value and (has attributes/sub-elements)
			Field<String> f = new Field<String>(new FieldDefinition(null,
					ConstantType.DEFAULT, null, null, 1, 1));
			f.setValue(e.value());
			fg.add(f);
		}
		List<XmlElement> ces = e.elements();
		if (e.hasElements()) {
			// has sub-elements
			for (XmlElement ce : ces) {
				addXmlElement(fg, ce);
			}
		}
		fs.add(fg);

	}

}
