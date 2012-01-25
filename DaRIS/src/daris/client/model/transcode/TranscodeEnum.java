package daris.client.model.transcode;

import java.util.List;
import java.util.Vector;


import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectResolveHandler;

public class TranscodeEnum implements DynamicEnumerationDataSource<Transcode>{

	private TranscodeListRef _transcodes;

	public TranscodeEnum(String from) {

		this(from, null);
	}

	public TranscodeEnum(String from, String to) {

		_transcodes = new TranscodeListRef(from, to);
	}

	@Override
	public boolean supportPrefix() {
		return false;
	}

	@Override
	public void exists(final String to, final DynamicEnumerationExistsHandler handler) {

		_transcodes.resolve(new ObjectResolveHandler<List<Transcode>>() {

			@Override
			public void resolved(List<Transcode> ts) {

				if (ts != null) {
					for (Transcode t : ts) {
						if (t.to().equals(to)) {
							handler.exists(to, true);
						}
					}
				} else {
					handler.exists(to, false);
				}
			}
		});
	}

	@Override
	public void retrieve(String prefix, final long start, final long end,
			final DynamicEnumerationDataHandler<Transcode> handler) {
		_transcodes.resolve(new ObjectResolveHandler<List<Transcode>>() {

			@Override
			public void resolved(List<Transcode> ts) {

				Transcode none = new Transcode(_transcodes.from());
				Value<Transcode> noneValue = new Value<Transcode>(none.to(), none.to(), none);
				List<Value<Transcode>> rvs = null;

				if (ts != null) {
					List<Value<Transcode>> values = new Vector<Value<Transcode>>(ts.size());
					for (Transcode t : ts) {
						values.add(new Value<Transcode>(t.to(), t.toDescription(), t));
					}
					int start1 = (int) start;
					int end1 = (int) end;

					int total = values.size();
					if (start1 >= total) {
						rvs = new Vector<Value<Transcode>>();
					} else {
						if (end1 > total) {
							end1 = total;
						}
						rvs = values.subList(start1, end1);
					}
					rvs.add(noneValue);
					handler.process(start1, end1+1, total+1, rvs);
				} else {
					rvs = new Vector<Value<Transcode>>();
					rvs.add(noneValue);
					handler.process(0, 1, 1, rvs);
				}
			}
		});
		
	}

}
