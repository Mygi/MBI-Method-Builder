package daris.client.model.sink;

import arc.mf.client.xml.XmlElement;

public class Sink {
	
	public static String TYPE_NAME = "sink";
	
	private String _name;
	private Destination _destination;
	
	public Sink(XmlElement se){
		_name = se.value("@name");
		XmlElement de = se.element("destination");
		if(de!=null){
			_destination = new Destination(de);
		}
	}
	
	public String name(){
		return _name;
	}

	public Destination  destination(){
		return _destination;
	}
}
