package nig.mf.plugin.pssd.method;

import java.util.*;

import nig.mf.pssd.plugin.util.DistributedAsset;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.*;

/**
 * Branch using method references.
 * 
 * @author Jason Lohrey
 *
 */
public class BranchReferenceStep extends Step {
	public static final int TYPE_AND = 1;
	public static final int TYPE_OR  = 2;
	
	private int  _type;
	private List<String> _methods;
	
	public BranchReferenceStep(int id) {
		super(id);
		_type = TYPE_AND;
	}

	public BranchReferenceStep(int id,List<String> methods) {
		super(id);
		
		_methods = methods;
	}
	
	/**
	 * Returns the branch type.
	 * 
	 * @return
	 */
	public int type() {
		return _type;
	}

	/**
	 * Returns a list of method references.
	 * 
	 * @return
	 */
	public List<String> methods() {
		return _methods;
	}
	
	public Step convertBranchesToSubSteps(String proute, ServiceExecutor executor) throws Throwable { 
		if ( _methods == null ) {
			return this;
		}
		
		List<MethodStep> mss = new Vector<MethodStep>(_methods.size());
		for ( int i=0; i < _methods.size(); i++ ) {
			String mid = _methods.get(i);
			Method m = Method.lookup(executor, new DistributedAsset(proute, mid));
			
			MethodStep ms = new MethodStep(i+1,null,null,m);
			
			// Fully resolve this method..
			ms.convertBranchesToSubSteps(proute, executor);
			
			mss.add(ms);
		}
		
		BranchMethodStep mbs = new BranchMethodStep(id(),name(),description(),type(),mss);
		return mbs;
	}

	public void restoreStepBody(XmlDoc.Element se) throws Throwable {
		XmlDoc.Element be = se.element("branch");
		if ( be == null ) {
			throw new Exception("Node is not a branch step!");
		}
		
		String ts = be.value("@type");
		if ( ts.equals("and") ) {
			_type = BranchReferenceStep.TYPE_AND;
		} else {
			_type = BranchReferenceStep.TYPE_OR;
		}
		
		Collection<String> ms = be.values("method/id");
		if ( ms != null ) {
			_methods = new Vector<String>(ms);
		}
	}

	public void saveStepBody(XmlWriter w) throws Throwable {
		
		String ts = ( type() == BranchReferenceStep.TYPE_AND )? "and" : "or";
		
		w.push("branch",new String[] { "type", ts });
		
		Collection<String> ms = methods();
		if ( ms != null ) {
			for (String mid : ms) {
				w.add("method",mid);
			}
		}
		
		w.pop();
	}
}
