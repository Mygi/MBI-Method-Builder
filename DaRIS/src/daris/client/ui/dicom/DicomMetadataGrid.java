package daris.client.ui.dicom;

import java.util.List;

import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridDataSource;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.Transformer;
import daris.client.model.dicom.DicomElement;

public class DicomMetadataGrid extends ListGrid<DicomElement> {
	private static class DicomElementTransformer extends Transformer<DicomElement, ListGridEntry<DicomElement>> {
		public static final DicomElementTransformer INSTANCE = new DicomElementTransformer();

		@Override
		protected ListGridEntry<DicomElement> doTransform(DicomElement de) throws Throwable {

			if (de == null) {
				return null;
			}
			ListGridEntry<DicomElement> e = new ListGridEntry<DicomElement>(de);
			e.set("group", de.group());
			e.set("element", de.element());
			e.set("offset", de.offset());
			e.set("length", de.length());
			e.set("vr", de.vr());
			e.set("definition", de.definition());
			e.set("value", de.valueAsString());
			return e;
		}
	}

	private ActionListener _al;
	private DicomMetadataDataSource _ds;

	public DicomMetadataGrid(String assetId) {

		super(ScrollPolicy.AUTO);

		addColumnDefn("group", "Group").setWidth(60);
		addColumnDefn("element", "Element").setWidth(60);
		addColumnDefn("definition", "Definition").setWidth(260);
		addColumnDefn("value", "Value").setWidth(300);
		setShowHeader(true);
		setHeight100();
		setWidth100();
		setFontSize(11);
		setCellSpacing(0);
		setCellPadding(2);
		setEmptyMessage("");
		setLoadingMessage("Loading DICOM metadata...");
		setCursorSize(10000);

		_ds = new DicomMetadataDataSource(assetId);
		setDataSource(new ListGridDataSource<DicomElement>(_ds, DicomElementTransformer.INSTANCE));
	}

	public void seek(int index, ActionListener al) {

		_ds.setIndex(index);
		_al = al;
		refresh();
	}

	protected void postLoad(long start, long end, long total, List<ListGridEntry<DicomElement>> entries) {

		if (_al != null) {
			_al.executed(true);
			_al = null;
		}
	}
}
