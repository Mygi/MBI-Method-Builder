package nig.mf.plugin.pssd;

import nig.mf.plugin.pssd.method.ExMethod;


/**
 * The PSSD Object model.
 * 
 * @author Jason Lohrey
 *
 */
public class Model {
	
	private static String[] ROOT    = { Project.TYPE };
	private static String[] PROJECT = { Subject.TYPE };
	private static String[] SUBJECT = { ExMethod.TYPE};
	private static String[] EXMETHOD = { Study.TYPE};
    private static String[] STUDY   = { DataSet.TYPE };
	private static String[] DATASET = { DataSet.TYPE, DataObject.TYPE };
	
	/**
	 * The valid collection members for the specified type.
	 * 
	 * @param type
	 * @return
	 */
	public static String[] memberTypesFor(String type) {
		if ( type == null ) {
			return ROOT;
		}
		
		if ( type.equals(Project.TYPE) ) {
			return PROJECT;
		}
		
		if ( type.equals(Subject.TYPE) ) {
			return SUBJECT;
		}
		
		if ( type.equals(ExMethod.TYPE) ) {
			return EXMETHOD;
		}

		if ( type.equals(Study.TYPE) ) {
			return STUDY;
		}
		
		if ( type.equals(DataSet.TYPE) ) {
			return DATASET;
		}
		
		return null;
	}
	
}
