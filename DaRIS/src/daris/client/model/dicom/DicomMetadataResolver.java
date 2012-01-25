package daris.client.model.dicom;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import arc.mf.object.ObjectMessageResponse;
import daris.client.model.dicom.messages.DicomMetadataGet;

public class DicomMetadataResolver {

	public static interface MetadataResolveListener {

		void metadataResolved(int index, List<DicomElement> metadata);
	}

	private String _assetId;
	private int _index;
	private Map<Integer, List<DicomElement>> _mds;
	private List<MetadataResolveListener> _listeners;

	public DicomMetadataResolver(String assetId) {

		_assetId = assetId;
		_mds = new java.util.HashMap<Integer, List<DicomElement>>();
		_index = 0;
	}

	public void resolve(int index) {

		_index = index;
		List<DicomElement> md = _mds.get(_index);
		if (md != null) {
			notifyOfMetadataResolve(_index, md);
		} else {
			new DicomMetadataGet(_assetId, _index).send(new ObjectMessageResponse<List<DicomElement>>() {
				@Override
				public void responded(List<DicomElement> metadata) {

					_mds.put(_index, metadata);
					notifyOfMetadataResolve(_index, metadata);
				}
			});
		}
	}

	private void notifyOfMetadataResolve(int index, List<DicomElement> metadata) {

		if (_listeners != null) {
			for (MetadataResolveListener l : _listeners) {
				l.metadataResolved(index, metadata);
			}
		}
	}

	public void addResolveListener(MetadataResolveListener l) {

		if (l == null) {
			return;
		}
		if (_listeners == null) {
			_listeners = new Vector<MetadataResolveListener>();
		}
		_listeners.add(l);
	}

	public void removeResolveListener(MetadataResolveListener l) {

		if (_listeners != null) {
			_listeners.remove(l);
		}
	}
}
