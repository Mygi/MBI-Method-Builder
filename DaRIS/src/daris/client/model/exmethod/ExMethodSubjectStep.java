package daris.client.model.exmethod;

import java.util.List;


import arc.mf.client.xml.XmlDoc;
import arc.mf.client.xml.XmlElement;

public class ExMethodSubjectStep extends ExMethodStep {
	private List<XmlElement> _psMeta;
	private List<XmlElement> _rsMeta;

	public ExMethodSubjectStep(String mid, String step, String name, State state, String notes,
			List<XmlElement> psMeta, List<XmlElement> rsMeta, boolean editable) {

		super(mid, step, name, state, notes, editable);

		_psMeta = psMeta;
		_rsMeta = rsMeta;
	}

	/**
	 * Project subject metadata.
	 * 
	 * @return
	 */
	public List<XmlElement> psPublicMetadata() {

		return _psMeta;
	}

	/**
	 * R-Subject metadata.
	 * 
	 * @return
	 */
	public List<XmlElement> rsPublicMetadata() {

		return _rsMeta;
	}

	public void setPSPublicMetadata(String psMeta) {

		try {
			XmlElement psme = XmlDoc.parse(psMeta);
			if (psme != null) {
				_psMeta = psme.elements();
			}
		} catch (Throwable e) {

		}
	}

	public void setRSPublicMetadata(String rsMeta) {

		try {
			XmlElement rsme = XmlDoc.parse(rsMeta);
			if (rsme != null) {
				_rsMeta = rsme.elements();
			}
		} catch (Throwable e) {

		}
	}
}