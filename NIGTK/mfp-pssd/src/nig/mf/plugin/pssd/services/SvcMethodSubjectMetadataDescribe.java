package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.plugin.pssd.method.*;

import nig.mf.plugin.pssd.*;
import nig.mf.pssd.plugin.util.DistributedAsset;

import java.util.*;

public class SvcMethodSubjectMetadataDescribe extends PluginService {
	private Interface _defn;

	public SvcMethodSubjectMetadataDescribe() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the Method.", 1, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.", 0));
		_defn.add(me);
	}

	public String name() {
		return "om.pssd.method.subject.metadata.describe";
	}

	public String description() {
		return "Describes the metadata required for the subject of a Method.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		// We don't care if its primary or a replica for this service
		DistributedAsset dID = new DistributedAsset(args.element("id"));
		
		Vector sm = Method.subjectMetadataDefinitions(executor(), dID);
		if ( sm != null ) {
			w.push("method",new String[] { "id", dID.getCiteableID() });
			describePSubjectMetadata(w,sm);
			describeRSubjectMetadata(w,sm);
			w.pop();
		}
	}
	
	private boolean hasSubjectMetadata(int type,Vector<SubjectMetadataDefinition> sm) {
		for ( int i=0; i < sm.size(); i++ ) {
			SubjectMetadataDefinition md = sm.get(i);
			if ( md.subjectType() == type ) {
				return true;
			}
		}
		
		return false;
	}
	
	private void describePSubjectMetadata(XmlWriter w,Vector sm) throws Throwable {
		
		boolean hasPublic = hasSubjectMetadata(SubjectMetadataDefinition.TYPE_PS_PUBLIC,sm);
		boolean hasPrivate = hasSubjectMetadata(SubjectMetadataDefinition.TYPE_PS_PRIVATE,sm);
		
		if ( hasPublic || hasPrivate  ) {
			w.push("subject");

			if ( hasPublic ) {
				w.push("public");
				describe(w,SubjectMetadataDefinition.TYPE_PS_PUBLIC,sm);
				w.pop();
			}
			
			if ( hasPrivate ) {
				w.push("private");
				describe(w,SubjectMetadataDefinition.TYPE_PS_PRIVATE,sm);
				w.pop();
			}

			w.pop();
		}
	}
	
	private void describeRSubjectMetadata(XmlWriter w,Vector sm) throws Throwable {

		boolean hasIdentity = hasSubjectMetadata(SubjectMetadataDefinition.TYPE_RS_IDENTITY,sm);
		boolean hasPublic   = hasSubjectMetadata(SubjectMetadataDefinition.TYPE_RS_PUBLIC,sm);
		boolean hasPrivate  = hasSubjectMetadata(SubjectMetadataDefinition.TYPE_RS_PRIVATE,sm);

		if ( hasIdentity || hasPublic || hasPrivate ) {
			w.push("rsubject");
			
			if ( hasIdentity ) {
				w.push("identity");
				describe(w,SubjectMetadataDefinition.TYPE_RS_IDENTITY,sm);
				w.pop();
			}

			if ( hasPublic ) {
				w.push("public");
				describe(w,SubjectMetadataDefinition.TYPE_RS_PUBLIC,sm);
				w.pop();
			}
			
			if ( hasPrivate ) {
				w.push("private");
				describe(w,SubjectMetadataDefinition.TYPE_PS_PRIVATE,sm);
				w.pop();
			}

			w.pop();
		}
	}
	
	private void describe(XmlWriter w,int type,Vector sm) throws Throwable {
		Vector ms = filter(type,sm);
		if ( ms == null ) {
			return;
		}

		for ( int i=0; i < ms.size(); i++ ) {
			SubjectMetadataDefinition md = (SubjectMetadataDefinition)ms.get(i);
			describe(w,md);
		}
		
	}
	
	private Vector filter(int type,Vector sm) {
		Vector fm = null;
		
		for ( int i=0; i < sm.size(); i++ ) {
			SubjectMetadataDefinition md = (SubjectMetadataDefinition)sm.get(i);
			if ( md.subjectType() == type ) {
				if ( fm == null ) {
					fm = new Vector();
				}
				
				fm.add(md);
			}
		}
		
		return fm;
	}
	
	private void describe(XmlWriter w,SubjectMetadataDefinition m) throws Throwable {
		String req = ( m.requirement() == SubjectMetadataDefinition.REQ_MANDATORY )? "mandatory" : "optional";
		Metadata.describeTypeDefn(executor(), w, m.metadata(), null, req);
	}
}
