package nig.mf.plugin.pssd.sc;

import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class Access {

	public final boolean canModify;
	public final boolean canReEdit;
	public final boolean canWithdraw;
	public final boolean canReprocess;
	public final boolean canDestroy;

	public Access(XmlDoc.Element ae) throws Throwable {
		canModify = ae.booleanValue("can-modify", false);
		canReEdit = ae.booleanValue("can-re-edit", false);
		canWithdraw = ae.booleanValue("can-withdraw", false);
		canReprocess = ae.booleanValue("can-reprocess", false);
		canDestroy = ae.booleanValue("can-destroy", false);
	}

	public void describe(XmlWriter w) throws Throwable {
		w.push("access");
		w.add("can-modify", canModify);
		w.add("can-re-edit", canReEdit);
		w.add("can-withdraw", canWithdraw);
		w.add("can-reprocess", canReprocess);
		w.add("can-destory", canDestroy);
		w.pop();
	}

}
