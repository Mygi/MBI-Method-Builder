package daris.model.object;

import arc.mf.client.xml.XmlElement;
import daris.model.dataobject.DataObject;
import daris.model.dataset.DataSet;
import daris.model.dataset.DerivationDataSet;
import daris.model.dataset.DicomDerivationDataSet;
import daris.model.dataset.PrimaryDataSet;
import daris.model.exmethod.ExMethod;
import daris.model.method.Method;
import daris.model.project.Project;
import daris.model.rsubject.RSubject;
import daris.model.study.Study;
import daris.model.subject.Subject;

public class PSSDObjectFactory {

	private PSSDObjectFactory() {
	}

	public static PSSDObject instantiate(XmlElement oe) throws Throwable {

		String type = oe.value("@type");
		if (type.equals(Project.TYPE_NAME)) {
			return new Project(oe);
		} else if (type.equalsIgnoreCase(Subject.TYPE_NAME)) {
			return new Subject(oe);
		} else if (type.equalsIgnoreCase(ExMethod.TYPE_NAME)) {
			return new ExMethod(oe);
		} else if (type.equalsIgnoreCase(Study.TYPE_NAME)) {
			return new Study(oe);
		} else if (type.equalsIgnoreCase(DataSet.TYPE_NAME)) {
			String sourceType = oe.value("source/type");
			if (sourceType == null) {
				throw new Exception(
						"Failed parsing DataSet. source/type is null.");
			}
			if (sourceType.equals("derivation")) {
				String mimeType = oe.value("type");
				if (mimeType.equals("dicom/series")) {
					return new DicomDerivationDataSet(oe);
				} else {
					return new DerivationDataSet(oe);
				}
			} else if (sourceType.equals("primary")) {
				return new PrimaryDataSet(oe);
			} else {
				throw new Exception(
						"Failed parsing DataSet. Invalid source/type:"
								+ sourceType);
			}
		} else if (type.equalsIgnoreCase(DataObject.TYPE_NAME)) {
			return new DataObject(oe);
		} else if (type.equalsIgnoreCase(Method.TYPE_NAME)) {
			return new Method(oe);
		} else if (type.equalsIgnoreCase(RSubject.TYPE_NAME)) {
			return new RSubject(oe);
		} else {
			throw new Exception("Failed parsing XML to PSSDObject. \n"
					+ oe.toString());
		}

	}

}
