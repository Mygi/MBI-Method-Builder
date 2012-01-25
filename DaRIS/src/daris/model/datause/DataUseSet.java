package daris.model.datause;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;

@SuppressWarnings("serial")
public class DataUseSet extends Vector<String> {

	public DataUseSet(XmlElement xe) {

		List<String> dataUses = xe.values("data-use");
		if (dataUses != null) {
			addAll(dataUses);
		}

	}

}
