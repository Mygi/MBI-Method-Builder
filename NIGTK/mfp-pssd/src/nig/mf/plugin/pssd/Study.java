package nig.mf.plugin.pssd;

import java.util.List;
import java.util.Vector;

import nig.mf.plugin.pssd.method.ActionStep;
import nig.mf.plugin.pssd.method.ExMethod;
import nig.mf.plugin.pssd.util.*;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class Study {
	public static final String TYPE = "study";

	public static final int DEPTH = 3;

	public static final String MODEL = "om.pssd.study";

	public static final String TYPE_DICTIONARY = "pssd.study.types";

	private String _type;
	private String _mid;          // ExMethod cid
	private String _mname;
	private String _step;
	private String _sname;

	/**
	 * Constructor 
	 * 
	 * @param type type of Study object
	 * @param mid ExMethod CID
	 * @param mname Name of Method
	 * @param step STep in Method
	 * @param sname Name of STep in ExMethod
	 */
	public Study(String type, String mid, String mname, String step, String sname) {
		_type = type;
		_mid = mid;
		_mname = mname;
		_step = step;
		_sname = sname;
	}

	public String type() {
		return _type;
	}

	public String methodId() {
		return _mid;
	}

	public String methodName() {
		return _mname;
	}

	public String step() {
		return _step;
	}

	public String stepName() {
		return _sname;
	}

	/**
	 * Create a Study on the local server
	 * 
	 * @param executor
	 * @param dExMethod Distributed citeable asset of the parent ExMethod 
	 * @param studyNumber
	 * @param type
	 * @param name
	 * @param description
	 * @param step
	 * @param meta
	 * @param dProject
	 * @param fillIn
	 * @return
	 * @throws Throwable
	 */
	public static String create(ServiceExecutor executor, DistributedAsset dExMethod,
			long studyNumber, String type, String name, String description,
			 String step, XmlDoc.Element meta, DistributedAsset dProject, boolean fillIn)
			throws Throwable {
		

		XmlDocMaker dm = new XmlDocMaker("args");

		// Generate CID, filling in allocator space if desired
		String cid = nig.mf.pssd.plugin.util.CiteableIdUtil.generateCiteableID(executor, dExMethod, studyNumber, fillIn);
		dm.add("cid", cid);
	
		dm.add("namespace", PSSDUtils.namespace(executor, dProject));
		dm.add("model", Study.MODEL);

		if (dExMethod != null && step != null) {
			String etype = addMethodStudyTemplates(executor, dm, dExMethod, step);
			if (type == null) {
				type = etype;
			}
		}

		dm.push("meta");
		PSSDUtils.setObjectMeta(dm, Study.TYPE, name, description);
		if (meta != null) {

			Vector<XmlDoc.Element> es = meta.elements();
			if (es != null) {
				// Filter out method specific metadata.
				String mns = ExMethod.metaNamespace(dExMethod.getCiteableID(), step);

				XmlDoc.Element om = new XmlDoc.Element("meta");
				for (int i = 0; i < es.size(); i++) {
					XmlDoc.Element me = es.get(i);

					String ns = me.value("@ns");
					if (ns == null || !ns.equalsIgnoreCase(mns)) {
						om.add(me);
					} else {
						dm.add(me);
					}
				}

				// If there is anything left, then that is optional metadata.
				PSSDUtils.setObjectOptionalMeta(dm, om, "om.pssd.study");
			}
		}

		dm.push("pssd-study");
		dm.add("type", type);
		if (dExMethod != null && step != null) {
			dm.add("method", new String[] { "step", step }, dExMethod.getCiteableID());
		}

		dm.pop();

		dm.pop();

		PSSDUtils.addStudyACLs(dm, dProject.getCiteableID());

		XmlDoc.Element r = executor.execute("asset.create", dm.root());
		if (cid == null) {
			return r.value("cid");
		} else {
			return cid;
		}
	}

	/**
	 * Returns true if the CID is for a Study object
	 */
	public static boolean isObjectStudy(ServiceExecutor executor, String cid)
			throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", cid);
		XmlDoc.Element r = executor.execute("om.pssd.object.type", dm.root());
		String type = r.value("type");
		if (type.equals(TYPE))
			return true;
		return false;
	}

	/**
	 * Adds templates for a study. This will specify meta-data for Study Action steps
	 * that require meta-data attached to a Study. Returns the expected type of study.
	 * 
	 * @param executor
	 * @param dm
	 * @param dEID  Distributed exMethod CID (checked to be compliant with federation policy)
	 * @param stepPath
	 * @return
	 * @throws Throwable
	 */
	private static String addMethodStudyTemplates(ServiceExecutor executor,
			XmlDocMaker dm, DistributedAsset dEID, String stepPath) throws Throwable {
		
		// Reinstantiate ExMethod from its asset
		ExMethod em = ExMethod.lookup(executor, dEID);
		
		// Find the action step for this path
		ActionStep as = em.method().actionStepByPath(stepPath);

		// See if there are any Study actions (could also be Subject actions)
		List<XmlDoc.Element> sas = as.studyActions();
		if (sas == null) {
			throw new Exception("Not a study step: method=" + dEID.getCiteableID() + ", step="
					+ stepPath);
		}

		String mns = ExMethod.metaNamespace(dEID.getCiteableID(), stepPath);

		String type = null;

		boolean pushed = false;
		// Iterate over the Study actions
		for (int i = 0; i < sas.size(); i++) {
			XmlDoc.Element se = sas.get(i);

			if (type == null) {
				type = se.value("type");
			}

			// If the Study action prescribes that some meta-data are required
			// then include that in the template
			List<XmlDoc.Element> mes = se.elements("metadata");
			if (mes != null) {
				for (int j = 0; j < mes.size(); j++) {
					if (!pushed) {
						dm.push("template", new String[] { "ns", mns });
						pushed = true;
					}

					dm.add(mes.get(j));
				}
			}
		}

		if (pushed) {
			dm.pop();
		}

		return type;
	}

	/**
	 * Update local Study object
	 * 
	 * @param executor
	 * @param id  Citeable ID of Study object
	 * @param name
	 * @param description
	 * @param exMethod is the CID of the ExMethod that we want to update the Study with. We don't need to fetch its meta-data
	 *              so we don't care where it is located in a federation
	 * @param step 
	 * @param meta
	 * @return
	 * @throws Throwable
	 */
	public static String update(ServiceExecutor executor, String id, String type, String name, String description, String exMethod, String step,  XmlDoc.Element meta)
			throws Throwable {
		
		// See if we can safely update the exmethod/step (only if no template information)
		if ( (exMethod!=null || step!=null) && PSSDUtils.objectHasTemplate(executor, id)) {
			throw new Exception ("Cannot update Study ExMethod/Step because this Study has template information that cannot currently be updated");
		}

		// Get existing meta from local Study object
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", id);
		dm.add("pdist",0);                 // Force local		
		XmlDoc.Element oldAsset = executor.execute("asset.get", dm.root());
		
		// Build new meta
		dm = new XmlDocMaker("args");
		dm.add("cid", id);		
		dm.push("meta", new String[] { "action", "merge" });
		PSSDUtils.setObjectMeta(dm, Study.TYPE, name, description);
		dm.pop();
		
		// Get the existing pssd-study meta		
		if(exMethod!=null || step!=null || type!=null){
			XmlDoc.Element oldEl = oldAsset.element("asset/meta/pssd-study");
			
			// Remove old (only way to update the step)
			dm.push("meta", new String[] {"action", "remove"});
			dm.push("pssd-study");
			dm.pop();
			dm.pop();
			
			// Now add new
			dm.push("meta", new String[] { "action", "merge" });  
			dm.push("pssd-study"); 
			if (type==null) {
				dm.add("type", oldEl.value("type"));
			} else {
				dm.add("type", type); 
			}
			
			// Set new Method/step. SOme or all of old/current may be null.
			String oldStep = oldEl.value("method/@step");
            String newStep = step;
            if (newStep==null) newStep = oldStep;
            //
            String oldExMethod = oldEl.value("method");
            String newExMethod = exMethod;
            if (newExMethod==null) newExMethod = oldExMethod;
            
            // MF requires attributes so can't have naked Method.
			if (newStep!=null && newExMethod!=null) {
				dm.add("method", new String[] {"step", newStep}, newExMethod);
			}
			dm.pop();
			dm.pop();
		}
		
		// Extract the action for handling the meta
		// Defaults to replace
		if (meta != null) {
			XmlDoc.Attribute actionAttr = meta.attribute("action");
			String action = "replace";
			if (actionAttr != null) action = actionAttr.value();
			dm.push("meta", new String[] { "action", action });
			PSSDUtils.setObjectOptionalMeta(dm, meta, "om.pssd.study");
			dm.pop();
		}
		
		// Do it
		XmlDoc.Element r = executor.execute("asset.set", dm.root());
		return r.value("cid");
	}

}
