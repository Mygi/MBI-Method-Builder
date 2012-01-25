package daris.client.util;

import java.util.List;

import arc.mf.client.util.ThrowableUtil;
import arc.mf.client.xml.XmlAttribute;
import arc.mf.client.xml.XmlDoc;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;

import com.google.gwt.core.client.JavaScriptObject;

public class XmlUtil {
	
	/**
	 * Transform a XML element to a HTML fragment.
	 * 
	 * @param e
	 *            The XML element
	 * @param indent
	 * @param step
	 * @return
	 */
	public static String toHtml(XmlElement e, int indent, int step) {

		String html = "";
		html += "<b style=\"margin-left:" + indent
				+ "px;font-family:arial;font-size:14px;letter-spacing:2px;line-height:1.5\">";
		html += ":" + e.name();
		List<XmlAttribute> attrs = e.attributes();
		if (attrs != null) {
			for (int i = 0; i < attrs.size(); i++) {
				XmlAttribute attr = attrs.get(i);
				html += " -" + attr.name() + " \"" + attr.value() + "\"";
			}
		}
		if (e.value() != null) {
			html += " \"" + e.value() + "\"";
		}
		html += "</b>";
		html += "<br/>";
		List<XmlElement> children = e.elements();
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				html += toHtml(children.get(i), indent + step, step);
			}
		}
		return html;
	}

	public static native String makeSafeXmlString(String xmlString)/*-{
		var s = xmlString;
		s = s.replace(/[\&]/g, "&amp;"); 
		s = s.replace(/[\"]/g, "&quot;"); 
		s = s.replace(/[\<]/g, "&lt;"); 
		s = s.replace(/[\>]/g, "&gt;"); 
		return s;
	}-*/;

	public static native String serialize(JavaScriptObject node) /*-{
		if(node.xml) {
			// Internet Explorer
			return node.xml;
		} else if($wnd.XMLSerializer) {
			// Firefox and Other W3C browsers
			return (new $wnd.XMLSerializer()).serializeToString(node);
		} else {
			throw new Error('Cannot convert/serialize XML node: ' + node + '.');
		}
	}-*/;
	
	public static String toXmlString(XmlElement xe){
		XmlStringWriter w =  new XmlStringWriter();w.add(xe);
		return w.document();
	}
	
	public static String toSafeXmlString(XmlElement xe){
		return makeSafeXmlString(toXmlString(xe));
	}
	
	public static void importElements(XmlStringWriter from, XmlStringWriter to, boolean replace){
		
		if(from == null || to == null){
			return;
		}
		
		try {

			XmlElement e = XmlDoc.parse("<args>" + from.document() + "</args>");
			if (e.elements() != null) {
				if(replace){
					to.clear();
				}
				to.add(e, false);
			}

		} catch ( Throwable t ) {
			ThrowableUtil.rethrowAsUnchecked(t);
		}


	}

}
