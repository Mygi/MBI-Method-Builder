package daris.client.model.sc;

import daris.client.Resource;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.EnumerationType.Value;

public enum Status {
	editable, await_processing, assigned, processing, data_ready, fulfilled, rejected, error, withdrawn, aborted;
	@Override
	public String toString() {

		return super.toString().replace('_', ' ');
	}

	public String icon() {
		switch (this) {
		case editable:
			return ICON_EDITABLE;
		case await_processing:
			return ICON_AWAIT_PROCESSING;
		case assigned:
			return ICON_ASSIGNED;
		case processing:
			return ICON_PROCESSING;
		case data_ready:
			return ICON_DATA_READY;
		case fulfilled:
			return ICON_FULFILLED;
		case rejected:
			return ICON_REJECTED;
		case error:
			return ICON_ERROR;
		case withdrawn:
			return ICON_WITHDRAWN;
		case aborted:
			return ICON_ABORTED;
		}
		return null;
	}

	public static final String ICON_EDITABLE = Resource.INSTANCE.editable24()
			.getSafeUri().asString();
	public static final String ICON_AWAIT_PROCESSING = Resource.INSTANCE
			.awaitProcessing24().getSafeUri().asString();
	public static final String ICON_ASSIGNED = Resource.INSTANCE.assigned24()
			.getSafeUri().asString();
	public static final String ICON_PROCESSING = Resource.INSTANCE
			.processing24().getSafeUri().asString();
	public static final String ICON_DATA_READY = Resource.INSTANCE.download24()
			.getSafeUri().asString();
	public static final String ICON_FULFILLED = Resource.INSTANCE.fulfilled24()
			.getSafeUri().asString();
	public static final String ICON_REJECTED = Resource.INSTANCE.rejected24()
			.getSafeUri().asString();
	public static final String ICON_ERROR = Resource.INSTANCE.error24()
			.getSafeUri().asString();
	public static final String ICON_WITHDRAWN = Resource.INSTANCE.withdrawn24()
			.getSafeUri().asString();
	public static final String ICON_ABORTED = Resource.INSTANCE.abort24()
			.getSafeUri().asString();

	public static Status instantiate(String status) {

		if (status != null) {
			if (status.equalsIgnoreCase(editable.toString())) {
				return editable;
			}
			if (status.equalsIgnoreCase(await_processing.toString())) {
				return await_processing;
			}
			if (status.equalsIgnoreCase(assigned.toString())) {
				return assigned;
			}
			if (status.equalsIgnoreCase(processing.toString())) {
				return processing;
			}
			if (status.equalsIgnoreCase(data_ready.toString())) {
				return data_ready;
			}
			if (status.equalsIgnoreCase(fulfilled.toString())) {
				return fulfilled;
			}
			if (status.equalsIgnoreCase(rejected.toString())) {
				return rejected;
			}
			if (status.equalsIgnoreCase(error.toString())) {
				return error;
			}
			if (status.equalsIgnoreCase(withdrawn.toString())) {
				return withdrawn;
			}
			if (status.equalsIgnoreCase(aborted.toString())) {
				return aborted;
			}
		}
		return null;
	}

	public static EnumerationType<Status> asEnumerationType() {

		Status[] values = values();
		@SuppressWarnings("unchecked")
		Value<Status>[] vs = new Value[values.length];
		for (int i = 0; i < values.length; i++) {
			vs[i] = new Value<Status>(values[i].toString(),
					values[i].toString(), values[i]);
		}
		return new EnumerationType<Status>(vs);
	}
}
