package nig.mf.plugin.pssd.method;

import arc.xml.*;
import java.util.*;

/**
 * Action step does something -- an activity generating some form of data
 * Action steps may generate Studies or Subject state meta-data or 
 * 
 * @author Jason Lohrey
 *
 */
public class ActionStep extends Step {
	private Vector<XmlDoc.Element> _subjectActions;
	private Vector<XmlDoc.Element> _studyActions;
	
	public ActionStep(int id) {
		super(id);
		
		_subjectActions = null;
		_studyActions = null;
	}

	public List<XmlDoc.Element> subjectActions() {
		return _subjectActions;
	}
	
	public List<XmlDoc.Element> studyActions() {
		return _studyActions;
	}
	
	public void restoreStepBody(XmlDoc.Element se) throws Throwable {
		_subjectActions = se.elements("subject");
		_studyActions = se.elements("study");
	}

	public void saveStepBody(XmlWriter w) throws Throwable {
		if ( _subjectActions != null ) {
			for ( int i=0; i < _subjectActions.size(); i++ ) {
				w.add(_subjectActions.get(i));
			}
		}
		
		if ( _studyActions != null ) {
			for ( int i=0; i < _studyActions.size(); i++ ) {
				w.add(_studyActions.get(i));
			}
		}
	}

}
