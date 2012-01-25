package daris.model.object;

import arc.mf.client.xml.XmlElement;
import daris.client.model.IDUtil;
import daris.model.dataobject.DataObject;
import daris.model.dataobject.DataObjectRef;
import daris.model.dataset.DataSet;
import daris.model.dataset.DerivationDataSetRef;
import daris.model.dataset.PrimaryDataSetRef;
import daris.model.exmethod.ExMethod;
import daris.model.exmethod.ExMethodRef;
import daris.model.project.Project;
import daris.model.project.ProjectRef;
import daris.model.study.Study;
import daris.model.study.StudyRef;
import daris.model.subject.Subject;
import daris.model.subject.SubjectRef;

public class PSSDObjectRefFactory {

	private PSSDObjectRefFactory() {

	}

	public static PSSDObjectRef instantiate(XmlElement oe) {

		if (oe != null) {

			if (oe.name().equals("object")) {

				String type = oe.value("@type");

				if (type.equals(Project.TYPE_NAME)) {

					return new ProjectRef(oe);

				} else if (type.equals(Subject.TYPE_NAME)) {

					return new SubjectRef(oe);
				} else if (type.equals(ExMethod.TYPE_NAME)) {

					return new ExMethodRef(oe);

				} else if (type.equals(Study.TYPE_NAME)) {

					return new StudyRef(oe);

				} else if (type.equals(DataSet.TYPE_NAME)) {

					String sourceType = oe.value("source/type");
					if (sourceType != null) {
						if (sourceType.equals("primary")) {
							return new PrimaryDataSetRef(oe);
						} else if (sourceType.equals("derivation")) {
							return new DerivationDataSetRef(oe);
						} else {
							// throw new
							// Exception("Failed to instantiate dataset. Invalid type: "
							// + sourceType);
						}
					}

				} else if (type.equals(DataObject.TYPE_NAME)) {

					return new DataObjectRef(oe);

				}
			}

		}

		return null;

	}

	public static PSSDObjectRef instantiate(String proute, String id,
			String name, String description, boolean isleaf, boolean primaryds) {

		if (IDUtil.isProjectId(id)) {
			return new ProjectRef(proute, id, name, description, isleaf);
		}
		if (IDUtil.isSubjectId(id)) {
			return new SubjectRef(proute, id, name, description, isleaf);
		}
		if (IDUtil.isExMethodId(id)) {
			return new ExMethodRef(proute, id, name, description, isleaf);
		}
		if (IDUtil.isStudyId(id)) {
			return new StudyRef(proute, id, name, description, isleaf);
		}
		if (IDUtil.isDataSetId(id)) {
			if (primaryds) {
				return new PrimaryDataSetRef(proute, id, name, description,
						isleaf);
			} else {
				return new DerivationDataSetRef(proute, id, name, description,
						isleaf);
			}
		}
		if (IDUtil.isDataObjectId(id)) {
			return new DataObjectRef(proute, id, name, description, isleaf);
		}
		return null;

	}

}
