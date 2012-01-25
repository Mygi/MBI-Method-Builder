package nig.encrypt;

/**
 * Class to hold an enum defining the types of reversible encryption that are available.
 * 
 * @author nebk
 *
 */
public class EncryptionTypes {

	private EncryptionTypes() {
	};
	
	public enum EncryptionType { BASE_64, ROT13 };
	public static final String ENCRYPTED_ATTR = "pssd-encryption";   // Name of Document attribute
	public static final String ENCRYPTED_VALUE = "encrypted";   // Value of attributed when document encrypted
}