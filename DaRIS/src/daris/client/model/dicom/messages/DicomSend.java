package daris.client.model.dicom.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlWriter;
import arc.mf.dtype.EnumerationType;
import arc.mf.object.BackgroundObjectMessage;

public class DicomSend extends BackgroundObjectMessage {

	public static enum PatientNameAction {
		unchanged, anonymize, use_subject_id;
		public String toString() {

			return super.toString().replace("_", "-");
		}

		public static EnumerationType<PatientNameAction> asEnumerationType() {

			List<EnumerationType.Value<PatientNameAction>> evs = new Vector<EnumerationType.Value<PatientNameAction>>(
					values().length);
			for (int i = 0; i < values().length; i++) {
				evs.add(new EnumerationType.Value<PatientNameAction>(
						values()[i].toString(), values()[i].toString(),
						values()[i]));
			}
			return new EnumerationType<PatientNameAction>(evs);
		}
	}

	private String _pid;
	private String _assetType;
	private String _aet;
	private String _remoteHost;
	private int _remotePort;
	private String _remoteAet;
	private PatientNameAction _patientNameAction;

	public DicomSend(String pid, String assetType, String aet,
			String remoteAet, String remoteHost, int remotePort,
			PatientNameAction patientNameAction) {

		_pid = pid;
		_assetType = assetType;
		_aet = aet;
		_remoteAet = remoteAet;
		_remoteHost = remoteHost;
		_remotePort = remotePort;
		_patientNameAction = patientNameAction;
	}

	public DicomSend(String pid, String aet, String remoteAet,
			String remoteHost, int remotePort,
			PatientNameAction patientNameAction) {

		this(pid, null, aet, remoteAet, remoteHost, remotePort,
				patientNameAction);
	}

	public DicomSend(String pid, String aet, String remoteAet,
			String remoteHost, int remotePort) {

		this(pid, null, aet, remoteAet, remoteHost, remotePort,
				PatientNameAction.unchanged);
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("pid", _pid);
		if (_assetType != null) {
			w.add("asset-type", _assetType);
		}
		w.add("aet", _aet);
		w.push("ae");
		w.add("host", _remoteHost);
		w.add("port", _remotePort);
		w.add("aet", _remoteAet);
		w.pop();
		if (_patientNameAction != null) {
			w.add("patient-name-action", _patientNameAction.toString());
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.dicom.send";
	}

	@Override
	protected String objectTypeName() {

		return null;
	}

	@Override
	protected String idToString() {

		return _pid;
	}

}
