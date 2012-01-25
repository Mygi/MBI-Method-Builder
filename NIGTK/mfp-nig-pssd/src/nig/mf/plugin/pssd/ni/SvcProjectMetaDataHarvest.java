package nig.mf.plugin.pssd.ni;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import nig.mf.plugin.util.DateUtil;
import nig.mf.plugin.util.DictionaryUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcProjectMetaDataHarvest extends PluginService {

	private Interface _defn;

	public SvcProjectMetaDataHarvest() {

		// matches DocType pssd-repository-description

		_defn = new Interface();
		_defn.add(new Element("id", CiteableIdType.DEFAULT, "The citeable asset id of the local Project to harvest.", 1, 1));
	}

	public String name() {

		return "nig.pssd.project.metadata.harvest";
	}

	public String description() {

		return "Harvest repository meta-data for ANDS registry. The Mediaflux XML is returned to the terminal. The service om.pssd.project.metadata.harvest (which uses this service) actually transforms to RIFCS.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_MODIFY;
	}



	public void execute(XmlDoc.Element args, Inputs inputs, Outputs outputs, XmlWriter w)
	throws Throwable {

		// Parse arguments
		String id = args.stringValue("id");

		// Fetch harvest meta-data for this Project.  This meta-data is defined by the nig-pssd package.
		XmlDocMaker meta = harvest (executor(), id);
		w.add(meta.root());
	}


	private XmlDocMaker harvest (ServiceExecutor executor, String id) throws Throwable {

		XmlDocMaker docOut = new XmlDocMaker("meta");

		// Get meta-data attached to project
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		XmlDoc.Element r = executor.execute("om.pssd.object.describe", dm.root());
		if (r==null) return null;
		//
		// Funding organization and ID. May be multiples. Writes element 'funding-id'
		Collection<XmlDoc.Element> funding = r.elements("object/meta/hfi.pssd.project/funding-id");
		String dict = "nig.funding.organization";
		if (funding != null) {
			// Dereference the type from nig.funding.organization
			for (XmlDoc.Element fund : funding) {
				replaceTypeWithDefinition(executor, fund, dict);
				docOut.add(fund);
			}
		}

		// Ethics organization and ID. May be multiples.  Writes element 'ethics-id'
		Collection<XmlDoc.Element> ethics = r.elements("object/meta/hfi.pssd.ethics/ethics-id");
		dict = "nig.ethics.organization";
		if (ethics != null) {
			for (XmlDoc.Element ethic : ethics) {
				// Dereference from nig.ethics.organization dictionary
				replaceTypeWithDefinition(executor, ethic, dict);
				docOut.add(ethic);
			}
		}

		// keyword may be multiples.  Writes element 'keyword'
		Collection<XmlDoc.Element> keywords = r.elements("object/meta/hfi.pssd.project/keyword");
		if (keywords != null) {
			for (XmlDoc.Element keyword : keywords) {
				docOut.add(keyword);
			}
		}

		// Add some more details about subjects
		addSubjectDetails (executor, id, docOut);

		// Ingest dates sit on specific meta-data
		// mf-dicom-study and hfi-bruker-study
		//
		// Find first and last studies. We are going to assume they
		// are in time order
		// even though they may not be. We'll bypass the number
		// ExMethods using this query
		Vector<XmlDoc.Element> studies = findChildren(executor, id, "study");
		if (studies != null) {
			String firstId = studies.firstElement().value();
			String lastId = studies.lastElement().value();

			// Get the ingest dates in ADNS format
			String ingestDate = getIngestDate(executor, firstId);
			if (ingestDate!=null) {
				String date = DateUtil.convertDateString(ingestDate, "dd-MMM-yyyy", "yyyy-MM-dd");
				if (date != null) docOut.add("first-ingest-date", date);
			}
			//
			ingestDate = getIngestDate(executor, lastId);
			if (ingestDate!=null) {
				String date = DateUtil.convertDateString(ingestDate, "dd-MMM-yyyy", "yyyy-MM-dd");
				if (date != null) docOut.add("last-ingest-date", date);
			}

			// Get acquisition dates
			String acqDate = getAcqDate(executor, firstId);
			if (acqDate!=null) {
				String date = DateUtil.convertDateString(acqDate, "dd-MMM-yyyy", "yyyy-MM-dd");
				if (date != null) docOut.add("first-acquisition-date", date);
			}
			//
			acqDate = getAcqDate(executor, lastId);
			if (acqDate!=null) {
				String date = DateUtil.convertDateString(acqDate, "dd-MMM-yyyy", "yyyy-MM-dd");
				if (date != null) docOut.add("last-acquisition-date", date);
			}
		}
		//
		return docOut;
	}


	// SUbject type summaries. This code is a bit cheap. It should really be  looking at
	// the Method and establishing what meta-data is located on the subject.
	// But in the end, I still need to know which elements
	// to look in so we by-pass the Method stuff.   This is rather reminiscent of the
	// framework that I use in the DICOM server. See nig.iio.metadata (COmmons)
	// These doc types are widely  used in the NIG projects.  If other projects use other document types
	// they would need to be added.
	private void addSubjectDetails (ServiceExecutor executor, String id, XmlDocMaker docOut) throws Throwable {
		Vector<XmlDoc.Element> subjects = findChildren(executor, id, "subject");
		HashMap<String,Integer> types = new HashMap<String,Integer>();
		HashMap<String,Integer> speciess = new HashMap<String,Integer>();
		HashMap<String,Integer> genders = new HashMap<String,Integer>();

		if (subjects!=null) {
			for (XmlDoc.Element subject : subjects) {
				String sid = subject.value();
				XmlDoc.Element meta = getSubjectMeta (executor, sid);
				//
				String type = meta.value("object/public/hfi.pssd.subject/type");
				String species = meta.value("object/public/hfi.pssd.animal.subject/species");
				String gender = meta.value("object/public/hfi.pssd.animal.subject/gender");

				// Add to the hash maps
				Integer val = 0;
				if (type!=null) {
					if (types.containsKey(type)) {
						val = types.get(type);
					}
					types.put(type,++val);
				}
				//
				val = 0;
				if (species!=null) {
					if (speciess.containsKey(species)) {
						val = speciess.get(species);
					}
					speciess.put(species,++val);
				}
				//
				val = 0;
				if (gender!=null) {
					if (genders.containsKey(gender)) {
						val = genders.get(gender);
					}
					genders.put(gender,++val);
				}	
			}

			// Populate output
			docOut.push("subject-details");
			if (types.size()>0) {
				docOut.push("type");
				Set<String> keys = types.keySet();
				for (String key : keys) {
					docOut.add(key, types.get(key));
				}
				docOut.pop();
			}
			if (speciess.size()>0) {
				docOut.push("species");
				Set<String> keys = speciess.keySet();
				for (String key : keys) {
					docOut.add(key, speciess.get(key));
				}
				docOut.pop();
			}
			if (genders.size()>0) {
				docOut.push("gender");
				Set<String> keys = genders.keySet();
				for (String key : keys) {
					docOut.add(key, genders.get(key));
				}
				docOut.pop();
			}
			docOut.pop();
		}
	}

	/**
	 * Find children of given type
	 * 
	 * @param executor
	 * @param id
	 *            parent CID
	 * @param type
	 *            type of object study, dataset
	 * @return
	 * @throws Throwable
	 */
	private Vector<XmlDoc.Element> findChildren(ServiceExecutor executor,
			String id, String type) throws Throwable {

		String query = "cid starts with '" + id
		+ "' and xpath(pssd-object/type)='" + type + "'";

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", query);
		dm.add("size", "infinity");
		dm.add("action", "get-cid");
		dm.add("pdist", 0);
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		if (r != null) {
			return r.elements("cid");
		} else {
			return null;
		}
	}

	private XmlDoc.Element getSubjectMeta (ServiceExecutor executor, String id) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		return executor.execute("om.pssd.object.describe", dm.root());

	}

	/**
	 * Get the ingest date. We rely on domain-specific meta-data for this to
	 * work
	 * 
	 * 
	 * @param executor
	 * @param id Study CID
	 * @return
	 * @throws Throwable
	 */
	private String getIngestDate(ServiceExecutor executor, String id) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		XmlDoc.Element r = executor.execute("om.pssd.object.describe", dm.root());

		// See if we can find a DICOM or Bruker date. The DICOM server adds mf-dicom-study
		// All this is generic, it could be configured to behave differently
		String date = r.stringValue("object/meta/mf-dicom-study/ingest/date");
		if (date == null) date = r.stringValue("object/meta/hfi-bruker-study/ingest/date");
		return date;
	}

	/**
	 * Get the acquisition date.
	 * 
	 * @param executor
	 * @param id
	 *            study CID
	 * @return
	 * @throws Throwable
	 */
	private String getAcqDate(ServiceExecutor executor, String id)
	throws Throwable {

		XmlDoc.Element r = describeObject(executor, id);

		// See if we can find a DICOM date
		String date = r.stringValue("object/meta/mf-dicom-study/sdate");

		// If no DICOM date, look for Bruker
		// The Bruker study has no date as there is no equivalent piece of
		// meta-data
		// So hunt it out from the first DataSet
		if (date == null) {
			Vector<XmlDoc.Element> dataSets = findChildren(executor, id, "dataset");
			if (dataSets!=null) {
				String firstId = dataSets.firstElement().value();
				if (firstId!=null) {
					r = describeObject(executor, firstId);
					date = r.stringValue("object/meta/hfi-bruker-series/acqTime");
				}
			}
		}
		return date;
	}

	private XmlDoc.Element describeObject(ServiceExecutor executor, String id)
	throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		return executor.execute("om.pssd.object.describe", dm.root());
	}


	private void replaceTypeWithDefinition(ServiceExecutor executor,
			XmlDoc.Element el, String dict) throws Throwable {

		XmlDoc.Attribute att = el.attribute("type");
		if (att!=null) {
			String type = att.value();
			String desc = DictionaryUtil.typeToDefinition(executor, type, dict);
			if (desc!=null) att.setValue(desc);
		}
	}
}
