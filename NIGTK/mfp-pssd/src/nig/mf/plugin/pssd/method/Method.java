package nig.mf.plugin.pssd.method;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import nig.mf.plugin.pssd.Metadata;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import nig.mf.pssd.plugin.util.DistributedQuery;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import arc.xml.XmlDoc.Element;

/**
 * A method is a set of activities to be executed - a research workflow.
 * 
 * @author Jason Lohrey
 *
 */
public class Method {
	public static final String TYPE = "method";

	public static final String MODEL = "om.pssd.method";

	public static class ExInUseByStudy extends Throwable {
		public ExInUseByStudy(String mid) {
			super("The Method " + mid + " cannot be destroyed. It is in use by at least one Study.");
		}
	}

	public static class ExInUseByMethod extends Throwable {
		public ExInUseByMethod(String mid) {
			super("The Method " + mid + " cannot be destroyed. It is in use by at least one other Method.");
		}
	}

	public static class ExInUseByExMethod extends Throwable {
		public ExInUseByExMethod(String mid) {
			super("The Method " + mid + " cannot be destroyed. It is in use by at least one ExMethod.");
		}
	}

	public static class ExNoSuchStep extends Throwable {
		public ExNoSuchStep(String mid,int step) {
			super("The Method " + mid + " does not have a step (id): " + step);
		}
		public ExNoSuchStep(String mid,int step,int branch) {
			super("The Method " + mid + " does not have a step (id): " + step + " with branch number " + branch);
		}
	}

	public static class ExLeafStep extends Throwable {
		public ExLeafStep(String mid,int step) {
			super("The step (id) " + step + " for Method " + mid + " cannot be expanded further.");
		}
	}

	public static class ExNotALeafStep extends Throwable {
		public ExNotALeafStep(String mid,int step) {
			super("The step (id) " + step + " for Method " + mid + " is not a leaf step - deference further.");
		}
	}

	/**
	 * Step visitor visits steps in the method!
	 *
	 */
	public interface StepVisitor {
		public void visit(Step step);
	}

	
	private String _id;
	
	// Stored in pssd-object
	private String _name;
	private String _description;
	
	// Stored in pssd-method
	private List<String> _authors;      // Not actually implemented in this class although is stored in object
	private String _version;            // Version of Method structure

	private Vector _subjectMetadata;
	private List<Step>  _steps;

	/**
	 * This constructor does not include the additional items version and authors
	 * which are stored in pssd-method at the top level.  It is appropriate to
	 * when working with a Method step only, which does not include the above
	 * components
	 * 
	 * @param id
	 * @param name
	 * @param description
	 */
	public Method(String id,String name,String description) {
		_id = id;
		_name = name;
		_description = description;
	}
	
	/**
	 * This constructor includes the Method structure version string, stored at the top level in
	 * pssd-method.
	 * 
	 * @param id
	 * @param name
	 * @param description
	 * @param version
	 */
	public Method(String id,String name,String description, String version) {
		_id = id;
		_name = name;
		_description = description;
		_version = version;
	}


	/**
	 * This constructor is never used, so it's not clear what type subjectMeta is !
	 * 
	 * @param id
	 * @param name
	 * @param description
	 * @param authors
	 * @param subjectMetadata
	 */
	public Method(String id,String name,String description,List<String> authors, String version, Vector subjectMetadata) {
		_id = id;
		_name = name;
		_description = description;
		_authors = authors;
		_version = version;
	}


	public String id() {
		return _id;
	}

	/**
	 * Name of this method.
	 * 
	 * @return
	 */
	public String name() {
		return _name;
	}

	/**
	 * Description, if any, of this method.
	 * 
	 * @return
	 */
	public String description() {
		return _description;
	}

	/**
	 * Identity of the author of this method.
	 * 
	 * @return
	 */
	public List<String> authors() {
		return _authors;
	}
	
	/**
	 * Version of Method meta-data layout.  1.0 was the original (Implied
	 * when the version is absent).
	 * 
	 * @return
	 */
	public String version () {
		return _version;
	}

	/**
	 * Causes all branches to other methods to be fully expanded as a sub-step
	 * within this method. This creates a complete copy of the method in a 
	 * single specification.
	 */
	public void convertBranchesToSubSteps(String proute, ServiceExecutor executor) throws Throwable {
		if ( _steps != null ) {
			for ( int i=0; i < _steps.size(); i++ ) {
				Step s = (Step)_steps.get(i);
				_steps.set(i, s.convertBranchesToSubSteps(proute, executor));
			}
		}
	}


	/**
	 * The number of steps in this method.
	 * 
	 * @return
	 */
	public int numberOfSteps() {
		if ( _steps == null ) {
			return 0;
		}

		return _steps.size();
	}

	/**
	 * Returns the idx'th step.
	 * 
	 * @param idx
	 * @return
	 */
	public Step step(int idx) {
		if ( _steps == null ) {
			throw new ArrayIndexOutOfBoundsException();
		}

		return _steps.get(idx);
	}

	/**
	 * Return a step by identifier.
	 * 
	 * @param id
	 * @return
	 */
	public Step stepById(int id) {
		if ( _steps == null ) {
			return null;
		}

		for ( int i=0; i < _steps.size(); i++ ) {
			Step s = _steps.get(i);
			if ( s.id() == id ) {
				return s;
			}
		}

		return null;
	}

	/**
	 * Returns the step given the dot-notated path.
	 * 
	 * @param path
	 * @return
	 */
	public ActionStep actionStepByPath(String path) throws Throwable {

		Method m = this;

		Step ls = null;

		int[] steps = steps(path);
		for ( int i=0; i < steps.length; i++ ) {
			int step = steps[i];

			Step s = m.stepById(step);
			if ( s == null ) {
				throw new Method.ExNoSuchStep(m.id(),step);
			}

			if ( s instanceof MethodStep ) {
				MethodStep ms = (MethodStep)s;
				m = ms.method();
			} else if ( s instanceof BranchMethodStep ) {
				BranchMethodStep bs = (BranchMethodStep)s;

				if ( i == steps.length-1 ) {
					throw new Method.ExNotALeafStep(m.id(),step);
				}

				step = steps[++i];

				if ( step > bs.numberOfBranches() ) {
					throw new Method.ExNoSuchStep(m.id(),step,step);
				}

				m = bs.method(step-1);
			} else {
				ls = s;
				if ( i < steps.length-1 ) {
					throw new Method.ExLeafStep(m.id(),step);
				}
			}
		}


		// Can only describe leaf steps..
		if ( ls == null || !(ls instanceof ActionStep ) ) {
			throw new Method.ExNotALeafStep(m.id(),steps[steps.length-1]);
		}

		return (ActionStep)ls;
	}


	/**
	 * Visit all (local) steps within this method.
	 * 
	 * @param v
	 */
	public void visitSteps(StepVisitor v) {
		if ( _steps == null ) {
			return;
		}

		for ( int i=0; i < _steps.size(); i++ ) {
			v.visit(_steps.get(i));
		}
	}

	public static class StudyAction {
		private String _path;
		private String _type;
		private XmlDoc.Element _dicom;    // DICOM element added in Method V 1.1

		public StudyAction(String path,String type, XmlDoc.Element dicom) {
			_path = path;
			_type = type;
			_dicom = dicom;
		}
		
		public StudyAction(String path,String type) {
			_path = path;
			_type = type;
			_dicom = null;
		}

		public String path() {
			return _path;
		}

		public String type() {
			return _type;
		}
		
		public XmlDoc.Element dicom () {
			return _dicom;
		}
	}

	public static class SubjectAction {
		private String _path;

		public SubjectAction(String path) {
			_path = path;
		}

		public String path() {
			return _path;
		}

	}


	/**
	 * Returns all of the Study Action steps (within the method) that generate studies
	 * of the given type (if set) or all studies if type not set.  This function
	 * is recursive so that steps that are themselves Methods are expanded.
	 * 
	 * @param type
	 * @return
	 */
	public List<StudyAction> studyActionStepPaths(String type) throws Throwable {
		if ( _steps == null ) {
			return null;
		}

		List<StudyAction> las = studyActionStepPaths(type,null,null);
		return las;
	}

	/**
	 * Returns all of the Subject Action steps (within the method) that specify
	 * Subject state meta-data.  This function
	 * is recursive so that steps that are themselves Methods are expanded.
	 * 
	 * @param type
	 * @return
	 */
	public List<SubjectAction> subjectActionStepPaths() throws Throwable {
		if ( _steps == null ) {
			return null;
		}

		List<SubjectAction> las = subjectActionStepPaths(null,null);
		return las;
	}

	/**
	 * See if Method is local and primary
	 * 
	 * @param executor
	 * @param id
	 * @return
	 * @throws Throwable
	 */
	public static boolean isMethodLocal (ServiceExecutor executor, String id) throws Throwable {
		return DistributedAssetUtil.assetExists(executor, null, null, id, DistributedQuery.ResultAssetType.primary, false, true, null);
	}


	private List<StudyAction> studyActionStepPaths(String type,List<StudyAction> las,String path) throws Throwable {

		for ( int i=0; i < _steps.size(); i++ ) {
			Step s = _steps.get(i);
			String sp = ( path == null )? String.valueOf(s.id()) : path + "." + String.valueOf(s.id());
			
			if ( s instanceof ActionStep ) {
				ActionStep as = (ActionStep)s;
				
				// Get the Study action steps
				List<XmlDoc.Element> sas = as.studyActions();
				if ( sas != null ) {
					boolean add = false;
					if ( type == null ) {

						// I don't understand this code. It makes a unique list of
						// types for this step path.  But a Study step  can only hold
						// one type...  I'll re-implement without, but leave
						// in case I am wrong [nebk]
						/*
						Set<String> stypes = new HashSet<String>();
						for ( int j=0; j < sas.size(); j++ ) {
							XmlDoc.Element sa = sas.get(j);
							System.out.println("StudyAction="+sa);
							String stype = sa.value("type");
							stypes.add(stype); 
						}

						for (String stype : stypes) {
							if ( las == null ) {
								las = new Vector<StudyAction>();
							}

							las.add(new StudyAction(sp,stype));
						}
						*/
						
						for ( int j=0; j < sas.size(); j++ ) {
							XmlDoc.Element sa = sas.get(j);
							String stype = sa.value("type");
							XmlDoc.Element dicom = sa.element("dicom");
							//
							if ( las == null ) {
								las = new Vector<StudyAction>();
							}

							las.add(new StudyAction(sp,stype,dicom));
						}					
					} else {
						for ( int j=0; j < sas.size(); j++ ) {
							XmlDoc.Element sa = sas.get(j);
							if ( sa.value("type").equals(type) ) {
								XmlDoc.Element dicom = sa.element("dicom");
								if ( las == null ) las = new Vector<StudyAction>();

								las.add(new StudyAction(sp,type,dicom));
								break;
							}
						}
					}

					if ( add ) {
						//TODO: JL forgot to implement...
					}
				}
			} else if ( s instanceof MethodStep ) {
				MethodStep ms = (MethodStep)s;
				las = ms.method().studyActionStepPaths(type, las,sp);
			} else if ( s instanceof BranchMethodStep ) {
				BranchMethodStep bms = (BranchMethodStep)s;
				for ( int k=0; k < bms.numberOfBranches(); k++ ) {
					// Branch methods push us down the stack. Each branch adds a child
					// starting at 1 (not 0), so k+1 not k...
					las = bms.method(k).studyActionStepPaths(type, las, sp + "." + String.valueOf(k+1));
				}
			}
		}

		return las;
	}


	private List<SubjectAction> subjectActionStepPaths(List<SubjectAction> las,String path) throws Throwable {

		for ( int i=0; i < _steps.size(); i++ ) {
			Step s = _steps.get(i);
			String sp = ( path == null )? String.valueOf(s.id()) : path + "." + String.valueOf(s.id());

			if ( s instanceof ActionStep ) {
				ActionStep as = (ActionStep)s;
				List<XmlDoc.Element> sas = as.subjectActions();
				if ( sas != null ) {
					boolean add = false;

					for ( int j=0; j < sas.size(); j++ ) {
						if ( las == null ) las = new Vector<SubjectAction>();
						las.add(new SubjectAction(sp));
					}

					if ( add ) {
						//TODO: JL forgot to implement...
					}
				}
			} else if ( s instanceof MethodStep ) {
				MethodStep ms = (MethodStep)s;
				las = ms.method().subjectActionStepPaths(las,sp);
			} else if ( s instanceof BranchMethodStep ) {
				BranchMethodStep bms = (BranchMethodStep)s;
				for ( int k=0; k < bms.numberOfBranches(); k++ ) {
					// Branch methods push us down the stack. Each branch adds a child
					// starting at 1 (not 0), so k+1 not k...
					las = bms.method(k).subjectActionStepPaths(las, sp + "." + String.valueOf(k+1));
				}
			}
		}

		return las;
	}

	/**
	 * Convert dot-notated step path into a vector of ints. E.g.
	 * 1.2.3 -> [1,2,3]
	 * 
	 * @param sid
	 * @return
	 */
	private static int[] steps(String sid) {
		StringTokenizer st = new StringTokenizer(sid,".");

		int[] steps = new int[st.countTokens()];
		for ( int i=0; i < steps.length; i++ ) {
			steps[i] = Integer.parseInt(st.nextToken());
		}

		return steps;
	}


	/**
	 * Looks up the method given the unique method identifier.
	 * 
	 * @param executor
	 * @param proute Route to server managing asset
	 * @param id  CID of Method asset
	 * @return
	 * @throws Throwable
	 */
	public static Method lookup(ServiceExecutor executor, DistributedAsset id) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid",id.getCiteableID());

		dm.add("pdist",0);                 // Force local on whatever server it's executed		
		XmlDoc.Element r = executor.execute(id.getServerRouteObject(), "asset.get",dm.root());

		XmlDoc.Element poe = r.element("asset/meta/pssd-object");
		String name = poe.value("name");
		String description = poe.value("description");

		XmlDoc.Element me = r.element("asset/meta/pssd-method");
		String version = me.value("version");

		Method m = new Method(id.getCiteableID(), name, description, version);
		m.restoreSteps(me);
		return m;
	}

	/**
	 * Definitions of the metadata required for a subject for the specified method.
	 * 
	 * @param executor
	 * @param proute Route to server managing asset
	 * @param id
	 * @return
	 * @throws Throwable
	 */
	public static Vector<SubjectMetadataDefinition>  subjectMetadataDefinitions(ServiceExecutor executor, DistributedAsset  id) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid",id.getCiteableID());

		dm.add("pdist",0);                 // Force local on whatever server it's executed		
		XmlDoc.Element r = executor.execute(id.getServerRouteObject(), "asset.get",dm.root());

		XmlDoc.Element me = r.element("asset/meta");

		return restoreSubjectMetadataDefinitions(executor,me);
	}

	/**
	 * Saves the method to the given writer.
	 * 
	 * @param w
	 * @throws Throwable
	 */
	public void saveSteps(XmlWriter w) throws Throwable {

		/*
		w.add("name",name());

		if ( description() != null ) {
			w.add("description",description());
		}

		if ( author() != null ) {
			w.add("author",author());
		}
		 */
		if ( _steps == null ) {
			return;
		}

		for ( int i=0; i < _steps.size(); i++ ) {
			Step s = _steps.get(i);
			s.save(w);
		}
	}

	/**
	 * Returns the metadata definition
	 * @param executor
	 * @param me
	 * @return
	 * @throws Throwable
	 */
	public static Vector<SubjectMetadataDefinition>  restoreSubjectMetadataDefinitions(ServiceExecutor executor,XmlDoc.Element me) throws Throwable {
		Vector<SubjectMetadataDefinition>  mds = null;

		if ( me == null ) {
			return null;
		}

		/*
		XmlDoc.Element se = me.element("subject");
		if ( se == null ) {
			return null;
		}
		 */

		XmlDoc.Element pm = me.element("pssd-method-subject/public");
		if ( pm != null ) {
			mds = restoreSubjectMetadataDefinitions(executor,SubjectMetadataDefinition.TYPE_PS_PUBLIC,pm,mds);
		}

		pm = me.element("pssd-method-subject/private");
		if ( pm != null ) {
			mds = restoreSubjectMetadataDefinitions(executor,SubjectMetadataDefinition.TYPE_PS_PRIVATE,pm,mds);
		}

		XmlDoc.Element im = me.element("pssd-method-rsubject/identity");
		if ( im != null ) {
			mds = restoreSubjectMetadataDefinitions(executor,SubjectMetadataDefinition.TYPE_RS_IDENTITY,im,mds);
		}

		pm = me.element("pssd-method-rsubject/public");
		if ( pm != null ) {
			mds = restoreSubjectMetadataDefinitions(executor,SubjectMetadataDefinition.TYPE_RS_PUBLIC,pm,mds);
		}

		pm = me.element("pssd-method-rsubject/private");
		if ( pm != null ) {
			mds = restoreSubjectMetadataDefinitions(executor,SubjectMetadataDefinition.TYPE_RS_PRIVATE,pm,mds);
		}

		return mds;
	}

	/**
	 * Restore specific metadata definition for a subject.
	 * 
	 * @param executor
	 * @param type
	 * @param sm
	 * @param mds
	 * @return
	 * @throws Throwable
	 */
	private static Vector<SubjectMetadataDefinition> restoreSubjectMetadataDefinitions(ServiceExecutor executor,int type,XmlDoc.Element sm,Vector<SubjectMetadataDefinition> mds) throws Throwable {
		Collection<XmlDoc.Element> mes = sm.elements("metadata");
		if ( mes != null ) {
			for (XmlDoc.Element  pme : mes) {
				XmlDoc.Element de = pme.element("definition");

				String mtype = de.value();
				int req = ( de.stringValue("@requirement","mandatory").equalsIgnoreCase("mandatory") )? SubjectMetadataDefinition.REQ_MANDATORY : SubjectMetadataDefinition.REQ_OPTIONAL;

				XmlDoc.Element ve = pme.element("value");

				// Resolve the (current) definition..
				XmlDoc.Element md = Metadata.lookup(executor,mtype,ve);
				if ( mds == null ) {
					mds = new Vector<SubjectMetadataDefinition>();
				}

				XmlDoc.Element mde = md.element("type");
				mds.add(new SubjectMetadataDefinition(type,mde,req));
			}
		}

		return mds;
	}

	/**
	 * Restore this method from the given element.
	 * 
	 * @param me
	 * @throws Throwable
	 */
	public void restoreSteps(XmlDoc.Element me) throws Throwable {
		if ( me == null ) {
			return;
		}

		Collection<XmlDoc.Element> ses = me.elements("step");
		if ( ses != null ) {
			_steps = new Vector<Step>(ses.size());

			int id = 1;

			for (XmlDoc.Element se : ses) {
				Step s = instantiateMethodStep(se,id++);
				s.restore(se);

				_steps.add(s);
			}
		}
	}

	public static Step instantiateMethodStep(XmlDoc.Element se,int id) throws Throwable {
		Step s;

		// OK -- we need to identify what type of step it is, based on
		// the elements contained therein..
		if ( se.element("subject") != null || se.element("study") != null ) {
			s = new ActionStep(id);
		} else if ( se.element("method") != null ) {
			XmlDoc.Element re = se.element("method/step");
			if ( re == null ) {
				s = new MethodReferenceStep(id);
			} else {
				s = new MethodStep(id);
			}
		} else if ( se.element("branch") != null ) {
			XmlDoc.Element re = se.element("branch/method/step");
			if ( re == null ) {
				s = new BranchReferenceStep(id);
			} else {
				s = new BranchMethodStep(id);
			}
		} else {
			throw new Exception("Cannot identify method step type");
		}

		return s;
	}

	/**
	 * Is the specified Method in use by any study?  If the Study is primary/replica then only primary/replica objects are checked
	 * 
	 * @param executor
	 * @param dMID  Method ID
	 * @param pdist DIstation in federation. Null means implicit distribution, 0 means local, infinity means all
	 * enabled peers
	 * @return
	 * @throws Throwable
	 */
	/*
	public static boolean inUseByAnyStudy(ServiceExecutor executor, DistributedAsset dMID, String pdist) throws Throwable {	
		return studyUseCount(executor, dMID, pdist, "1") > 0;
	}
	 */
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
	/*
	public static long studyUseCount(ServiceExecutor executor,  DistributedAsset dMID, String pdist, String maxSize) throws Throwable {

		// Prepare query
		String mid = dMID.getCiteableID();

		// Primary projects have primary Methods.  Replica projects have replica Methods.
		String query = null;
		if (dMID.isReplica()) {
			query = "xpath(pssd-study/method)='" + dMID.getCiteableID() + "' and rid has value";
		} else {
			query = "xpath(pssd-study/method)='" + dMID.getCiteableID() + "' and rid hasno value";
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
	 */

	/**
	 * Is the specified method in use by any Method.  If the Method is primary/replica then only primary/replica objects are checked
	 * 
	 * @param executor
	 * @param exMethod If true looks for ExMethods, if false looks for Methods
	 * @param dMID Method ID
	 * @param pdist DIstation in federation. Null means implicit distribution, 0 means local, infinity means all
	 * enabled peers
	 * @return
	 * @throws Throwable
	 */
	public static boolean inUseByAnyMethod(ServiceExecutor executor, boolean exMethod, DistributedAsset dMID, String pdist) throws Throwable {
		return methodUseCount(executor,exMethod,dMID,pdist,"1")>0;
	}

	/**
	 * The number of Methods or ExMethods using this Method.  If the Method is primary/replica then only primary/replica objects are checked
	 * 
	 * @param executor
	 * @param dMID Method ID
	 * @param pdist DIstation in federation. Null means implicit distribution, 0 means local, infinity means all
	 * enabled peers
	 * @param maxSize is the maximum number of items to count. Set to "infinity" for all.
	 * @return
	 * @throws Throwable
	 */
	public static long methodUseCount(ServiceExecutor executor, boolean exMethod, DistributedAsset dMID, String pdist, String maxSize) throws Throwable {

		// Prepare query
		String mid = dMID.getCiteableID();
		String query = "(xpath(pssd-method/step/method/id)='" + mid + "'";
		query += " or xpath(pssd-method/step/branch/method/id)='" + mid + "')";
		if (exMethod) {
			query += " and xpath(pssd-object/type)='ex-method'";
		} else {
			query += " and xpath(pssd-object/type)='method'";
		}

		// Primary Methods have primary sub-Methods.  Replica Methods have replica sub-Methods.
		if (dMID.isReplica()) {
			query += " and rid has value";
		} else {
			query += " and rid hasno value";
		}

		// DO it
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where",query);
		dm.add("action","count");
		if (pdist!=null) dm.add("pdist", pdist);
		dm.add("size", maxSize);

		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		return r.longValue("value");
	}

}
