package nig.mf.plugin.pssd.method;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;
import arc.xml.XmlDoc.Element;

/**
 * A step that contains the fully populated method.
 * 
 * @author Jason Lohrey
 *
 */
public class MethodStep extends Step {
	private Method _method;
	
	public MethodStep(int id) {
		super(id);
		_method = null;
	}

	public MethodStep(int id,String name,String description,Method m) {
		super(id,name,description);
		_method = m;
	}

	public Method method() {
		return _method;
	}
	
	public void restore(XmlDoc.Element se) throws Throwable {
		super.restoreIdAndName(se);
		
		XmlDoc.Element me = se.element("method");
		restoreStepBody(me);
	}
	
	public void restoreStepBody(Element me) throws Throwable {
		/*
		XmlDoc.Element me = se.element("method");
		if ( me == null ) {
			return;
		}
		*/
		
		String id = me.value("id");
		String name = me.value("name");
		String description = me.value("description");
		
		// Since we are only interested in the Step, we don't need
		// the constructor that supplies the higher-level info (version/authors etc)
		_method = new Method(id,name,description);
		_method.restoreSteps(me);
	}

	public void saveStepBody(XmlWriter w) throws Throwable {
		if ( _method == null ) {
			return;
		}
		
		w.push("method");
		
		w.add("id",_method.id());
		w.add("name",_method.name());
		if ( _method.description() != null ) {
			w.add("description",_method.description());
		}
		
		_method.saveSteps(w);
		w.pop();
	}
	
	public Step convertBranchesToSubSteps(String proute, ServiceExecutor executor) throws Throwable {
		method().convertBranchesToSubSteps(proute, executor);
		return this;
	}
}
