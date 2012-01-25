package nig.mf.plugin;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlWriter;


public class SvcIPAddressResolve extends PluginService {
	
	public static final int IPV_4 = 4;
	public static final int IPV_6 = 6;
	
	private Interface _defn;

	public SvcIPAddressResolve() {

		_defn = new Interface();
		_defn.add(new Element("version", new EnumType(new String[]{"IPV4", "IPV6"}), "IPV4 or IPV6. defaults to IPV4", 0, 1));
		_defn.add(new Element("host", StringType.DEFAULT, "The host address.", 1, 1));

	}

	public String name() {
		return "nig.ip.resolve";
	}

	public String description() {
		return "Resolve the ip address of the specified host.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	@Override
	public void execute(arc.xml.XmlDoc.Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
		boolean ipv4 = true;
		String version = args.value("version");
		if(version!=null){
			if(version.equals("IPV6")){
				ipv4 = false;
			}
		}
		String host = args.value("host");
		InetAddress addr =null;
		if(ipv4){
			addr = resolveHostAddress(IPV_4, host);
		} else {
			addr = resolveHostAddress(IPV_6, host);
		}
		if(addr != null){
			w.add("ip", new String[]{"host", host}, addr.getHostAddress());
		}
	}
	
	private static InetAddress resolveHostAddress(int ipv, String host) throws Throwable {
		try {
			InetAddress[] ias = InetAddress.getAllByName(host); 
			if (ipv == IPV_4) {
				for (int i = 0; i < ias.length; i++) {
					if (ias[i] instanceof Inet4Address) {
						return ias[i];
					}
				}
			} else if (ipv == IPV_6) {
				for (int i = 0; i < ias.length; i++) {
					if (ias[i] instanceof Inet6Address) {
						return ias[i];
					}
				}
			} else {
				return ias[0];
			}
		} catch (java.net.UnknownHostException uhe) {
			throw new Exception("fail to resolve IP address of " + host);
		}
		throw new Exception("fail to resolve IP address of " + host);
	}

}