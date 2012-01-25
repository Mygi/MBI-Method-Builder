package daris.model.object;

import arc.mf.client.xml.XmlElement;
import daris.model.dataobject.DataObject;
import daris.model.dataset.DataSet;
import daris.model.dataset.DataSetMeta;
import daris.model.exmethod.ExMethod;
import daris.model.method.Method;
import daris.model.project.Project;
import daris.model.project.ProjectMeta;
import daris.model.rsubject.RSubject;
import daris.model.study.Study;
import daris.model.study.StudyMeta;
import daris.model.subject.Subject;
import daris.model.subject.SubjectMeta;

public class PSSDObjectMetaFactory {

	private PSSDObjectMetaFactory() {

	}

	public static PSSDObjectMeta instantiate(XmlElement oe) {

		String type = oe.value("@type");
		if (type.equals(Project.TYPE_NAME)) {
			return new ProjectMeta(oe);
		} else if (type.equalsIgnoreCase(Subject.TYPE_NAME)) {
			return new SubjectMeta(oe);
		} else if (type.equalsIgnoreCase(ExMethod.TYPE_NAME)) {
			// return new ExMethodMeta(oe);
		} else if (type.equalsIgnoreCase(Study.TYPE_NAME)) {
			return new StudyMeta(oe);
		} else if (type.equalsIgnoreCase(DataSet.TYPE_NAME)) {
			return new DataSetMeta(oe);
		} else if (type.equalsIgnoreCase(DataObject.TYPE_NAME)) {
			// return new DataObject(oe);
		} else if (type.equalsIgnoreCase(Method.TYPE_NAME)) {
			// return new MethodMeta(oe);
		} else if (type.equalsIgnoreCase(RSubject.TYPE_NAME)) {
			// return new RSubjectMeta(oe);
		}
		return null;

	}

}
