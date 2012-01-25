package daris.client.ui.dti.file;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import arc.gui.file.FileFilter;
import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.format.WidgetFormatter;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.file.FileHandler;
import arc.mf.client.file.LocalFile;
import arc.mf.client.util.DateTime;
import arc.mf.client.util.Transformer;
import daris.client.Resource;
import daris.client.ui.DObjectGUIRegistry;
import daris.client.util.ByteUtil;

public class LocalFileGrid extends ListGrid<LocalFile> {

	public static final String FILE_ICON = Resource.INSTANCE.file16()
			.getSafeUri().asString();
	public static final String DIRECTORY_ICON = Resource.INSTANCE
			.folderViolet16().getSafeUri().asString();

	private static class LocalFileTransformer extends
			Transformer<LocalFile, ListGridEntry<LocalFile>> {

		public static final LocalFileTransformer INSTANCE = new LocalFileTransformer();

		private LocalFileTransformer() {

		}

		@Override
		protected ListGridEntry<LocalFile> doTransform(LocalFile f)
				throws Throwable {

			if (f == null) {
				return null;
			}
			ListGridEntry<LocalFile> e = new ListGridEntry<LocalFile>(f);
			e.set("name", f.name());
			e.set("length", f.length());
			e.set("lastModified", DateTime.SERVER_DATE_TIME_FORMAT
					.format(new Date(f.lastModified())));
			e.set("isDirectory", f.isDirectory());
			e.set("isFile", f.isFile());
			e.set("type", f.isFile() ? "File" : "Directory");
			e.set("path", f.path());
			return e;
		}
	}

	private static class LocalFileListTransformer extends
			Transformer<List<LocalFile>, List<ListGridEntry<LocalFile>>> {
		public static final LocalFileListTransformer INSTANCE = new LocalFileListTransformer();

		private LocalFileListTransformer() {

		}

		@Override
		protected List<ListGridEntry<LocalFile>> doTransform(List<LocalFile> fs)
				throws Throwable {

			if (fs == null) {
				return null;
			}
			if (fs.isEmpty()) {
				return null;
			}
			List<ListGridEntry<LocalFile>> es = new Vector<ListGridEntry<LocalFile>>(
					fs.size());
			for (LocalFile f : fs) {
				es.add(LocalFileTransformer.INSTANCE.transform(f));
			}
			return es;
		}

	}

	public static class LocalFileGridDataSource implements
			DataSource<ListGridEntry<LocalFile>> {

		private LocalFile _dir;
		private LocalFile.Filter _filter;
		private FileFilter _fileFilter;

		public LocalFileGridDataSource(LocalFile dir, LocalFile.Filter filter,
				FileFilter fileFilter) {

			_dir = dir;
			_filter = filter;
			_fileFilter = fileFilter;
		}

		public LocalFileGridDataSource(LocalFile dir) {

			this(dir, LocalFile.Filter.ANY, null);
		}

		protected LocalFile directory() {

			return _dir;
		}

		public void setDirectory(LocalFile dir) {

			_dir = dir;
		}

		public void setFilter(LocalFile.Filter filter) {

			_filter = filter;
		}

		public void setFileFilter(FileFilter fileFilter) {

			_fileFilter = fileFilter;
		}

		@Override
		public boolean isRemote() {

			return true;
		}

		@Override
		public boolean supportCursor() {

			return true;
		}

		private Filter mergeFilters(final Filter f, final FileFilter ff) {

			Filter rf;
			if (f == null) {
				if (ff == null) {
					rf = null;
				} else {
					rf = new Filter() {
						@Override
						public boolean matches(Object o) {

							return ff.accept((LocalFile) o);
						}
					};
				}
			} else {
				if (ff == null) {
					rf = f;
				} else {
					rf = new Filter() {
						@Override
						public boolean matches(Object o) {

							return f.matches(o) && ff.accept((LocalFile) o);
						}
					};
				}
			}
			return rf;
		}

		@Override
		public void load(final Filter f, final long start, final long end,
				final DataLoadHandler<ListGridEntry<LocalFile>> lh) {

			if (_dir == null) {
				// directory is not set, return an empty list.
				lh.loaded(0, 0, 0, null, null);
				return;
			}
			final Filter filter = mergeFilters(f, _fileFilter);
			_dir.files(_filter, start, end, new FileHandler() {
				@Override
				public void process(long start, long end, long total,
						List<LocalFile> files) {

					if (files != null) {
						List<LocalFile> rfiles = files;
						if (filter != null) {
							List<LocalFile> ffiles = new Vector<LocalFile>();
							for (LocalFile f : files) {
								if (filter.matches(f)) {
									ffiles.add(f);
								}
							}
							rfiles = ffiles;
						}
						int start1 = (int) start;
						int end1 = (int) end;
						if (start1 > 0 || end1 < rfiles.size() - 1) {
							if (start1 >= rfiles.size()) {
								rfiles = null;
							} else {
								if (end1 >= rfiles.size()) {
									end1 = rfiles.size() - 1;
								}
								rfiles = rfiles.subList(start1, end1 + 1);
							}
						}
						List<ListGridEntry<LocalFile>> entries;
						if (rfiles == null) {
							entries = null;
						} else {
							if (rfiles.isEmpty()) {
								entries = null;
							} else {
								entries = LocalFileListTransformer.INSTANCE
										.transform(rfiles);
							}
						}
						lh.loaded(start1, end1,
								entries == null ? 0 : entries.size(), entries,
								entries == null ? null : DataLoadAction.REPLACE);
					} else {
						lh.loaded(0, 0, 0, null, null);
					}
				}
			});
		}
	}

	private LocalFileGridDataSource _ds;

	public LocalFileGrid(LocalFile dir, LocalFile.Filter filter,
			FileFilter fileFilter, boolean multiSelect) {

		super(ScrollPolicy.AUTO);
		_ds = new LocalFileGridDataSource(dir, filter, fileFilter);
		setDataSource(_ds);

		setCursorSize(1000);
		addColumnDefn("name", "Name", "Name",
				new WidgetFormatter<LocalFile, String>() {

					@Override
					public BaseWidget format(LocalFile f, String name) {
						String icon = f.isFile() ? FILE_ICON : DIRECTORY_ICON;
						HTML html = new HTML(
								"<div><img src=\""
										+ icon
										+ "\" style=\"width:16px;height:16px;vertical-align:middle\"><span style=\"\">&nbsp;"
										+ name + "</span></div>");
						html.setFontSize(11);
						return html;
					}
				}).setWidth(180);
		addColumnDefn("lastModified", "Date Modified").setWidth(150);
		addColumnDefn("length", "Size", "File Size",
				new WidgetFormatter<LocalFile, Long>() {

					@Override
					public BaseWidget format(LocalFile f, Long length) {
						HTML html = new HTML(ByteUtil.humanReadableByteCount(
								length, true));
						html.setFontSize(11);
						return html;
					}
				});
		setEmptyMessage("");
		setMultiSelect(multiSelect);

		setObjectRegistry(DObjectGUIRegistry.get());
		enableRowDrag();
	}

	public void setDirectory(LocalFile dir) {

		_ds.setDirectory(dir);
		setDataSource(_ds);
	}

	public void setFilter(LocalFile.Filter filter) {

		_ds.setFilter(filter);
		setDataSource(_ds);
	}

	public void setFileFilter(FileFilter fileFilter) {

		_ds.setFileFilter(fileFilter);
		setDataSource(_ds);
	}

	public boolean hasSelections() {

		if (selections() == null) {
			return false;
		}
		if (selections().isEmpty()) {
			return false;
		}
		return true;
	}

}
