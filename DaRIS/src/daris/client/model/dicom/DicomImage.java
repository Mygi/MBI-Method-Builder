package daris.client.model.dicom;

import arc.mf.client.util.ActionListener;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.dicom.messages.DicomImageGet;

public class DicomImage {

	private String _assetId;
	private String _url;
	private boolean _lossless;
	private int _index;

	public DicomImage(String assetId, int index, String url, boolean lossless) {

		_assetId = assetId;
		_index = index;
		_url = url;
		_lossless = lossless;
	}

	public DicomImage(String assetId, int index, boolean lossless) {

		this(assetId, index, null, lossless);
	}

	public String url() {

		return _url;
	}

	public void setUrl(String url) {

		_url = url;
	}

	public boolean lostless() {

		return _lossless;
	}

	public String extension() {

		if (_lossless) {
			return "png";
		} else {
			return "jpg";
		}
	}

	public int index() {

		return _index;
	}

	public void load(final ActionListener al) {

		new DicomImageGet(_assetId, _index, _lossless).send(new ObjectMessageResponse<DicomImage>() {

			@Override
			public void responded(DicomImage di) {

				if (di != null) {
					_url = di.url();
					if (al != null) {
						al.executed(true);
					}
				} else {
					al.executed(false);
				}
			}
		});
	}

}
