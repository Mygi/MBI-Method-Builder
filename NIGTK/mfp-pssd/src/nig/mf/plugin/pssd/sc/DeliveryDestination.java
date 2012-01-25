package nig.mf.plugin.pssd.sc;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import nig.mf.plugin.pssd.Sink;
import arc.mf.plugin.ServiceExecutor;

public class DeliveryDestination {
	public static final DeliveryDestination BROWSER = new DeliveryDestination("browser", DeliveryMethod.download, null);

	public final String name;

	public final String url;

	public final DeliveryMethod method;

	public DeliveryDestination(String name, DeliveryMethod method, String url) {

		this.name = name;
		this.url = url;
		this.method = method;
	}

	public static List<DeliveryDestination> getDestinationsForCurrentUser(ServiceExecutor executor) throws Throwable {

		List<DeliveryDestination> dds = new Vector<DeliveryDestination>();
		dds.add(BROWSER);
		Map<String, String> sinkUrls = Sink.getSinkUrlsForCurrentUser(executor);
		if (sinkUrls != null) {
			for (String sink : sinkUrls.keySet()) {
				dds.add(new DeliveryDestination(sink, DeliveryMethod.deposit, sinkUrls.get(sink)));
			}
		}
		return dds;
	}

	public static DeliveryDestination getDestinationForCurrentUser(ServiceExecutor executor, String dstName)
			throws Throwable {

		List<DeliveryDestination> dsts = getDestinationsForCurrentUser(executor);
		if (dsts != null) {
			for (DeliveryDestination dst : dsts) {
				if (dst.name.equals(dstName)) {
					return dst;
				}
			}
		}
		return null;
	}

	public static DeliveryDestination getDestinationForCurrentUserByRootUrl(ServiceExecutor executor, String rootUrl)
			throws Throwable {

		List<DeliveryDestination> dsts = getDestinationsForCurrentUser(executor);
		if (dsts != null) {
			for (DeliveryDestination dst : dsts) {
				if (dst.method.equals(DeliveryMethod.download)) {
					continue;
				}
				if (dst.url != null) {
					if (dst.url.startsWith(rootUrl)) {
						return dst;
					}
				}
			}
		}
		return null;
	}
}
