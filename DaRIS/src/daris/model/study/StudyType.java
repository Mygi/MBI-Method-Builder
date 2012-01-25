package daris.model.study;


public class StudyType {

	private String _name;

	private String _description;
	
	public StudyType(String name, String description){
		_name = name;
		_description = description;
	}
	
	public StudyType(String name){
		this(name, null);
	}

	public String name() {

		return _name;
	}

	public String description() {

		return _description;
	}

	public String toString() {

		return _name + ": " + _description;
	}

	public boolean equals(Object o) {

		if (o != null) {
			if (o instanceof StudyType) {
				if (((StudyType) o).name().equals(_name)) {
					return true;
				}
			}
		}
		return false;
	}

}
