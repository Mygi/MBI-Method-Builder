package daris.model.object;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;

@SuppressWarnings("serial")
public class PSSDObjectRefSet extends Vector<PSSDObjectRef> {

	public PSSDObjectRefSet(XmlElement xe) {

		if (xe.elements("object") != null) {
			// unfederated session:
			List<XmlElement> oes = xe.elements("object");
			if (oes != null) {
				for (XmlElement oe : oes) {
					add(PSSDObjectRefFactory.instantiate(oe));
				}
			}
		} else if (xe.elements("proute") != null) {
			// federated session: (layout of the output of service
			// om.pssd.collection.members changed when session is federated.)
			List<XmlElement> pes = xe.elements("proute");
			if (pes != null) {
				for (XmlElement pe : pes) {
					List<XmlElement> oes = pe.elements("object");
					if (oes != null) {
						for (XmlElement oe : oes)
							add(PSSDObjectRefFactory.instantiate(oe));
					}
				}
			}
		}

	}

}
