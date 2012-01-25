package nig.mf.plugin.pssd.method;

import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlWriter;
import arc.xml.XmlDoc.Element;

public class MethodReferenceStep extends Step {
	private String _mid;
	
	public MethodReferenceStep(int id) {
		super(id);
	}

	public void restoreStepBody(Element se) throws Throwable {
		_mid = se.value("method/id");
	}

	public void saveStepBody(XmlWriter w) throws Throwable {
		w.push("method");
		w.add("id",_mid);
		w.pop();
	}

	public Step convertBranchesToSubSteps(String proute, ServiceExecutor executor) throws Throwable {
		Method m = Method.lookup(executor, new DistributedAsset(proute, _mid));
		Step ms = new MethodStep(id(),name(),description(),m);
		
		// Fully resolve this method..
		ms.convertBranchesToSubSteps(proute, executor);
		
		return ms;
	}

}
