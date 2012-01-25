package daris.client.model.dicom.messages;

import java.util.List;

import arc.gui.gwt.widget.dialog.Dialog;
import arc.mf.client.Output;
import arc.mf.client.RemoteServer;
import arc.mf.client.ResponseHandler;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.dicom.DicomImage;

public class DicomImageGet extends ObjectMessage<DicomImage> {
	private String _assetId;
	private int _index;
	private boolean _lossless = false;
	private double _quality = 0.8;
	private double _size = 1.0;

	public DicomImageGet(String assetId, int index, boolean lossless, double quality, double size) {

		_assetId = assetId;
		_index = index;
		_lossless = lossless;
		_quality = quality;
		_size = size;
	}

	public DicomImageGet(String assetId, int index, boolean lossless) {

		this(assetId, index, lossless, 0.8, 1.0);
	}

	public DicomImageGet(String assetId, int index) {

		this(assetId, index, false, (float) 0.8, (float) 1.0);
	}

	public DicomImageGet(String assetId) {

		this(assetId, 0, false, (float) 0.8, (float) 1.0);
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		String idx = "" + _index;
		w.add("id", new String[] { "idx", idx }, _assetId);
		w.add("lossless", _lossless);
		w.add("quality", _quality);
		w.add("size", _size);

	}

	@Override
	protected String messageServiceName() {

		return "dicom.image.get";
	}

	@Override
	protected DicomImage instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			return new DicomImage(_assetId, _index, null, _lossless);
		}
		return null;
	}

	@Override
	protected void process(DicomImage di, List<Output> outputs) throws Throwable {

		if (di != null) {
			if (outputs != null) {
				di.setUrl(outputs.get(0).url());
			}
		}
	}

	@Override
	protected String objectTypeName() {

		return "Dicom Image";
	}

	@Override
	protected String idToString() {

		return _assetId + "_" + _index;
	}

	@Override
	protected int numberOfOutputs() {

		return 1;
	}

	// TODO: ask Jason to improve the dicom compatibilities
	@Override
	public void send(final ObjectMessageResponse<DicomImage> rh) {

		XmlStringWriter w = new XmlStringWriter();
		messageServiceArgs(w);
		RemoteServer.execute(messageServiceName(), w.document(), numberOfOutputs(), new ResponseHandler() {
			@Override
			public void processResponse(XmlElement xe, List<Output> outputs) {

				if (rh == null) {
					return;
				}
				if (numberOfOutputs() > 0) {
					rh.responded(new DicomImage(_assetId, _index, outputs.get(0).url(), _lossless));
				} else {
					rh.responded(null);
				}
			}

			@Override
			public void processError(Throwable se) {

				if (!se.getMessage().equals("Expected output data, but no data generated.")) {
					Dialog.inform(se.getMessage());
				}
				if (rh != null) {
					rh.responded(null);
				}
			}
		});
	}

	public void setLostless(boolean lossless) {

		_lossless = lossless;
	}

	public void setQuality(double quality) {

		_quality = quality;
	}

	public void setSize(double size) {

		_size = size;
	}

}
