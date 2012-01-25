package daris.client.ui.dicom;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.dicom.DicomElement;
import daris.client.model.dicom.messages.DicomMetadataGet;

public class DicomMetadataDataSource implements DataSource<DicomElement> {

	private String _assetId;
	private int _index = 0;
	private HashMap<Integer, List<DicomElement>> _mds;

	public DicomMetadataDataSource(String assetId, int index) {

		_assetId = assetId;
		_index = index;
		_mds = new HashMap<Integer, List<DicomElement>>();
	}

	public DicomMetadataDataSource(String assetId) {

		this(assetId, 0);
	}

	public int index() {

		return _index;
	}

	public void setIndex(int index) {

		_index = index;
	}

	@Override
	public boolean isRemote() {

		return true;
	}

	@Override
	public boolean supportCursor() {

		return false;
	}

	@Override
	public void load(final Filter f, final long start, final long end, final DataLoadHandler<DicomElement> lh) {

		List<DicomElement> md = _mds.get(_index);
		if (md != null) {
			doLoad(f, start, end, md, lh);
		} else {
			final int idx = _index;
			new DicomMetadataGet(_assetId, idx).send(new ObjectMessageResponse<List<DicomElement>>() {

				@Override
				public void responded(List<DicomElement> metadata) {
					
					_mds.put(idx, metadata);
					doLoad(f, start, end, metadata, lh);
				}
			});
		}

	}

	private void doLoad(Filter f, long start, long end, List<DicomElement> metadata, DataLoadHandler<DicomElement> lh) {

		if (metadata != null) {
			List<DicomElement> es = metadata;
			if (f != null) {
				List<DicomElement> fes = new Vector<DicomElement>();
				for (DicomElement de : metadata) {
					if (f.matches(de)) {
						fes.add(de);
					}
				}
				es = fes;
			}
			int total = es.size();
			int start1 = (int) start;
			int end1 = (int) end;
			if (start1 > 0 || end1 < es.size()) {
				if (start1 >= es.size()) {
					es = null;
				} else {
					if (end1 > es.size()) {
						end1 = es.size();
					}
					es = es.subList(start1, end1);
				}
			}
			lh.loaded(start1, end1, total, es, DataLoadAction.REPLACE);
		} else {
			lh.loaded(0, 0, 0, null, null);
		}
	}
}
