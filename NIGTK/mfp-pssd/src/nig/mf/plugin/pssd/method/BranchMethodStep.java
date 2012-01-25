package nig.mf.plugin.pssd.method;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import arc.xml.XmlDoc;
import arc.xml.XmlWriter;
import arc.xml.XmlDoc.Element;

/**
 * A branching step that contains methods by value.
 * 
 * @author Jason Lohrey
 *
 */
public class BranchMethodStep extends Step {
	private int  _type;
	private List<MethodStep> _methods;
	
	public BranchMethodStep(int id) {
		super(id);
		_methods = null;
	}

	/**
	 * Branch to methods (by value).
	 * 
	 * @param id
	 * @param type
	 * @param methods
	 */
	public BranchMethodStep(int id,String name,String description,int type,List<MethodStep> methods) {
		super(id,name,description);
		_type = type;
		_methods = methods;
	}

	/**
	 * Number of branches available.
	 * 
	 * @return
	 */
	public int numberOfBranches() {
		return _methods.size();
	}
	
	/**
	 * Returns the method at the given 'idx'th branch point.
	 * 
	 * @param idx
	 * @return
	 */
	public Method method(int idx) {
		return _methods.get(idx).method();
	}
	
	public void restoreStepBody(Element se) throws Throwable {
		XmlDoc.Element be = se.element("branch");
		
		String ts = be.value("@type");
		if ( ts.equals("and") ) {
			_type = BranchReferenceStep.TYPE_AND;
		} else {
			_type = BranchReferenceStep.TYPE_OR;
		}
		
		Collection<XmlDoc.Element> mes = be.elements("method");
		if ( mes != null ) {
			_methods = new Vector<MethodStep>(mes.size());
			
			int i = 0;
			for (XmlDoc.Element me : mes) {
				MethodStep ms = new MethodStep(i++);
				ms.restoreStepBody(me);
				_methods.add(ms);
			}
		}
	}

	public void saveStepBody(XmlWriter w) throws Throwable {
		String ts = ( _type == BranchReferenceStep.TYPE_AND )? "and" : "or";
		
		w.push("branch",new String[] { "type", ts });
		
		if ( _methods != null ) {
			for ( int i=0; i < _methods.size(); i++ ) {
				MethodStep ms = _methods.get(i);
				ms.saveStepBody(w);
			}
		}
		
		w.pop();
	}
	
}
