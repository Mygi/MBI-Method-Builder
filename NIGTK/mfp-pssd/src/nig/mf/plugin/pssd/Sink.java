package nig.mf.plugin.pssd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;

public class Sink {

	public static String FILE_SYSTEM_SINK = "file-system";

	private static String evaluateDirectoryPatternForCurrentUser(
			ServiceExecutor executor, String pattern) throws Throwable {

		StringBuffer sb = new StringBuffer();
		int i = pattern.indexOf("{");
		while (i != -1) {
			sb.append(pattern.substring(0, i));
			pattern = pattern.substring(i + 1);
			int j = pattern.indexOf("}");
			String xpath = pattern.substring(0, j);
			XmlDoc.Element r = executor.execute("user.self.describe");
			String value = r.value(xpath);
			sb.append(value);
			pattern = pattern.substring(j + 1);
			i = pattern.indexOf("{");
		}
		sb.append(pattern);
		return sb.toString();
	}

	public static Map<String, String> getSinkUrlsForCurrentUser(
			ServiceExecutor executor) throws Throwable {

		XmlDoc.Element r = executor.execute("sink.describe");
		List<XmlDoc.Element> ses = r.elements("sink");
		if (ses != null) {
			Map<String, String> urls = new HashMap<String, String>(ses.size());
			for (XmlDoc.Element se : ses) {
				String name = se.value("@name");
				String url = null;
				String type = se.value("destination/type");
				if (type != null) {
					if (type.equalsIgnoreCase(Sink.FILE_SYSTEM_SINK)) {
						String directoryPattern = se
								.value("destination/arg[@name='directory']");
						if (directoryPattern != null) {
							url = "file:"
									+ Sink.evaluateDirectoryPatternForCurrentUser(
											executor, directoryPattern);
						}
					}
				}
				if (name != null && url != null) {
					urls.put(name, url);
				}
			}
			if (urls.size() > 0) {
				return urls;
			}
		}
		return null;
	}

	public static List<String> getSinkRootUrls(ServiceExecutor executor)
			throws Throwable {
		XmlDoc.Element r = executor.execute("sink.describe");
		List<XmlDoc.Element> ses = r.elements("sink");
		if (ses != null) {
			List<String> rootUrls = new Vector<String>();
			for (XmlDoc.Element se : ses) {
				String type = se.value("destination/type");
				if (type != null) {
					if (type.equalsIgnoreCase(Sink.FILE_SYSTEM_SINK)) {
						String rootDir = se
								.value("destination/arg[@name='directory']");
						int idx = rootDir.indexOf("{");
						if (idx != -1) {
							rootDir = rootDir.substring(0, idx - 1);
						}
						rootUrls.add("file:" + rootDir);
					}
				}
			}
			if (!rootUrls.isEmpty()) {
				return rootUrls;
			}
		}
		return null;
	}
}
