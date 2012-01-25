package nig.mf.plugin.pssd.method;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlDocWriter;
import arc.xml.XmlWriter;

/** 
 * Executing method. Instance of a method.
 * 
 * @author Jason Lohrey
 *
 */
public class ExMethod extends PSSDObject {
	public static final String TYPE = "ex-method";
	
	public static final int DEPTH = 2;
	
	public static final String MODEL = "om.pssd.ex-method";

	public static final String STATUS_INCOMPLETE = "incomplete";
	
	public static final String STATUS_WAITING = "waiting";
	
	public static final String STATUS_ABANDONED = "abandoned";
	
	public static final String STATUS_COMPLETE = "complete";
	
	/**
	 * Class level lock for all ExMethod objects.
	 */
	public static final Object LOCK = new Object();
	
	/**
	 * Returns true if the CID is for an ExMethod object
	 */
	public static boolean isObjectExMethod (ServiceExecutor executor, String cid) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id",cid);	
		XmlDoc.Element r = executor.execute("om.pssd.object.type",dm.root());	
		String type = r.value("type");
		if (type.equals(TYPE)) return true;
		return false;
	}
	
	
	private Method     _method;
	private String     _status;
	private List<ExMethodStepStatus> _steps;       // Only steps that have status information 
	
	/**
	 * Executing method - initial constructor for the given method.
	 * 
	 * @param method
	 */
	public ExMethod(Method m) {
		super(TYPE,m.name(),m.description());
		_status = STATUS_INCOMPLETE;
		_method = m;
		_steps = null;
	}
	
	/**
	 * Constructor for restoring an existing method.
	 */
	public ExMethod() {
		
	}
	
	public String status() {
		return _status;
	}
	
	public Method method() {
		return _method;
	}
	
	/**
	 * All steps for which there is information on the status. If not,
	 * the step should be deemed "incomplete".
	 * 
	 * @return
	 */
	public List<ExMethodStepStatus> steps() {
		return _steps;
	}
	
	/**
	 * Status of the given step, or null of there is no information.
	 * 
	 * @param path
	 * @return
	 */
	public ExMethodStepStatus statusForStep(String path) {
		if ( _steps == null ) {
			return null;
		}
		
		for ( int i=0; i < _steps.size(); i++ ) {
			ExMethodStepStatus mss = _steps.get(i);
			if ( mss.stepPath().equals(path) ) {
				return mss;
			}
		}
		
		return null;
	}
	
	
	/**
	 * Add, or update the existing step status.
	 * 
	 * @param step
	 */
	public void setStepStatus(ExMethodStepStatus step) {
		if ( _steps == null ) {
			_steps = new Vector<ExMethodStepStatus>(1);
		}
		
		for ( int i=0; i < _steps.size(); i++ ) {
			ExMethodStepStatus mss = _steps.get(i);
			if ( mss.stepPath().equals(step.stepPath()) ) {
				_steps.set(i, step);
				return;
			}
		}
		
		_steps.add(step);
	}
	
	/**
	 * Make ExMethod on local server
	 * 
	 * @param executor
	 * @param dSID Distributed citeable ID for parent Subject
	 * @param exMethodNumber
	 * @param dMID Distributed citeable ID for Method
	 * @param reUse
	 * @return
	 * @throws Throwable
	 */
	public static String create(ServiceExecutor executor, DistributedAsset dSID, long exMethodNumber, DistributedAsset dMID, boolean reUse) throws Throwable {
		
		
		Method m = Method.lookup(executor, dMID);
		
		// Take a copy, and fully instantiate the method to which we are referring.
		// The server route is passed down requiring all sub-Methods
		// to be managed on the same server
		m.convertBranchesToSubSteps(dMID.getServerRoute(), executor);

		ExMethod em = new ExMethod(m);

		// long maid = instantiateMethodTemplate(executor,proute,m);
		
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("namespace",PSSDUtils.namespace(executor, dSID));

		// Generate CID for new ExMethod, filling in allocator space if desired
		String cid = nig.mf.pssd.plugin.util.CiteableIdUtil.generateCiteableID(executor, dSID, exMethodNumber, reUse);
		dm.add("cid", cid);

		dm.add("model",MODEL);

		dm.push("meta");
		
		XmlDocWriter dw = new XmlDocWriter(dm);

		// Save the ExMethod
		em.saveAssetMeta(dw);
		
		dm.pop(); // meta

		// Add project ACLs
		String pcid = nig.mf.pssd.CiteableIdUtil.getParentId(dSID.getCiteableID());
		PSSDUtils.addExMethodACLs(dm, pcid);
		
		// Create		
		XmlDoc.Element r = executor.execute("asset.create",dm.root());
		if (cid!=null) {
			return cid;
		} else {
			return r.value("cid");
		}
	}

	
	
	/**
	 * Is the specified ExMethod in use by any Study?  If the Study is primary/replica then only primary/replica objects are checked
	 * 
	 * @param executor
	 * @param dMID  ExMethod ID
	 * @param pdist DIstation in federation. Null means implicit distribution, 0 means local, infinity means all
	 * enabled peers
	 * @return
	 * @throws Throwable
	 */
	public static boolean inUseByAnyStudy(ServiceExecutor executor, DistributedAsset dEMID, String pdist) throws Throwable {	
		return studyUseCount(executor, dEMID, pdist, "1") > 0;
	}
	
	/**
	 * The number of studies using this method.  If the Study is primary/replica then only primary/replica objects are checked.
	 * 
	 * @param executor
	 * @param dMID  Method ID
	 * @param pdist DIstation in federation. Null means implicit distribution, 0 means local, infinity means all
	 * enabled peers
	 * @param maxSize is the maximum number of items to count. Set to "infinity" for all.
	 * @return
	 * @throws Throwable
	 */
	public static long studyUseCount(ServiceExecutor executor,  DistributedAsset dEMID, String pdist, String maxSize) throws Throwable {
		
		// Prepare query
		String emid = dEMID.getCiteableID();
		
		// Primary projects have primary ExMethods.  Replica projects have replica ExMethods.
		// Note that the Study says which ExMEthod it uses, not which Method, despite the naming
		// of the 'method' element.  
		String query = null;
		if (dEMID.isReplica()) {
			query = "xpath(pssd-study/method)='" + emid + "' and rid has value";
		} else {
			query = "xpath(pssd-study/method)='" + emid + "' and rid hasno value";
		}

		// Query
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where",query);
		dm.add("size", maxSize);
		dm.add("action","count");
		if (pdist!=null) dm.add("pdist", pdist);
		
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		return r.longValue("value");
	}

	/**
	 * Creates an asset that contains a copy of the template used to execute this
	 * method.
	 * 
	 * @param executor
	 * @param mid
	 * @return
	 * @throws Throwable
	 */
	private static long instantiateMethodTemplate(ServiceExecutor executor,String proute,Method m) throws Throwable {
		
		// Fully instantiate the method as a related asset.
		m.convertBranchesToSubSteps(proute,executor);

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.push("meta");
		dm.push("pssd-method");
		
		XmlDocWriter dw = new XmlDocWriter(dm);
		
		m.saveSteps(dw);
		
		dm.pop();
		dm.pop();
		
		XmlDoc.Element r = executor.execute("asset.create",dm.root());
		return r.longValue("id");
	}
	
	/**
	 * Finds the ExMethod asset and instantiates the ExMethod object
	 * 
	 * @param executor
	 * @param id
	 * @return
	 * @throws Throwable
	 */
	public static ExMethod lookup(ServiceExecutor executor, DistributedAsset dID) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		
		dm.add("cid",dID.getCiteableID());
		dm.add("pdist",0);                 // Force local on whatever server it's executed		
		XmlDoc.Element r = executor.execute(dID.getServerRouteObject(), "asset.get",dm.root());
		
		ExMethod em = new ExMethod();
		em.parseAssetMeta(r.element("asset"));
		
		return em;
	}
	
	@Override
	public void parseAssetMeta(XmlDoc.Element ae) throws Throwable {
		super.parseAssetMeta(ae);
		
		if ( !type().equals(TYPE) ) {
			throw new Exception("Object " + id() + " is not an " + TYPE);
		}
		
		XmlDoc.Element me = ae.element("meta/pssd-ex-method");
		if ( me == null ) {
			throw new Exception("Object " + id() + " is missing method metadata");
		}
		
		_status = me.value("state");
		
		String id = me.value("method/id");
		String name = me.value("method/name");
		String description = me.value("method/description");
		
		// Since we are only interested in the Step, we don't need
		// the constructor that supplies the higher-level info (version/authors etc)
		_method = new Method(id,name,description);
		
		Collection<XmlDoc.Element> ses = me.elements("step");
		if ( ses != null ) {
			_steps = new Vector<ExMethodStepStatus>(ses.size());
			
			for (XmlDoc.Element se : ses) {
				_steps.add(restoreStep(se));
			}
		}		
		
		me = ae.element("meta/pssd-method");
		if ( ae != null ) {
			_method.restoreSteps(me);
		}
		

	}
	
	@Override
	public void saveAssetMeta(XmlWriter w) throws Throwable {
		super.saveAssetMeta(w);
		
		if ( method().numberOfSteps() > 0 ) {
			w.push("pssd-method");
			
			// SHould also save the name/version/author here ?
			method().saveSteps(w);
			w.pop();
		}
		
		
		w.push("pssd-ex-method");
		
		Method m = method();
		w.push("method");
		w.add("id",m.id());
		w.add("name",m.name());
		if ( m.description() != null ) {
			w.add("description",m.description());
		}
		w.pop();
		
		w.add("state",status());
		
		saveSteps(w);
		
		w.pop();
	}
	
	
	public void saveSteps(XmlWriter w) throws Throwable {
		if ( _steps != null ) {
			for ( int i=0; i < _steps.size(); i++ ) {
				saveStep(w,_steps.get(i));
			}
		}
	}
	
	private void saveStep(XmlWriter w,ExMethodStepStatus step) throws Throwable {
		w.push("step",new String[] { "path", step.stepPath() });
		w.add("state",step.status());
		if ( step.notes() != null ) {
			w.add("notes",step.notes());
		}
		w.pop();
	}
	
	private ExMethodStepStatus restoreStep(XmlDoc.Element se) throws Throwable {
		String path = se.value("@path");
		String state = se.value("state");
		
		ExMethodStepStatus s = new ExMethodStepStatus(path,state);
		s.setNotes(se.value("notes"));
		
		return s;
	}
	
	
	public static String metaNamespace(String mid,String stepPath) {
		return mid + "_" + stepPath;
	}
}
