package nig.iio.bruker;


/**
 * Simple container class for the Bruker meta-data parsed from the SUbject
 * identifier string by the Bruker client utilised at the Neuroimaging group. This is totally
 * not portable.
 * 
 * @author nebk
 *
 */
public class NIGBrukerIdentifierMetaData {
	
	private String _projectDescription = null;
	private String _coil = null;
	private String _animalID = null;
	private String _gender = null;
	private String _expGroup = null;
	private String _vivo = null;
	private String _date = null;          // TODO make it a Date

	
	/**
	 * Create the meta-data from the array (project description, coil, animalID, gender, expGroup, vivo, date)
	 * 
	 * @param dem
	 * @return
	 * @throws Throwable
	 */
	public NIGBrukerIdentifierMetaData (String[] parts) throws Throwable {
		if (parts.length!=7) {
			String errMsg = "The subject identifier was not of the correct form to extract the NIG meta-data";
			throw new Exception(errMsg);
		}	

		restore(parts);
	}
	
	public String projectDescription () {
		return _projectDescription;
	}
	
	public String coil () {
		return _coil;
	}
	
	public String animalID () {
		return _animalID;
	}
	
	public String gender () {
		return _gender;
	}

	public String experimentalGroup () {
		return _expGroup;
	}
	
	
	public String vivo () {
		return _vivo;
	}
	
	public String date () {
		return _date;
	}
	//
	private void restore (String[] parts) throws Throwable {
		
		_projectDescription = parts[0];
		_coil = parts[1];
		_animalID = parts[2];
		_gender = parts[3];
		_expGroup = parts[4];
		_vivo = parts[5];
		_date = parts[6];
	}

}
