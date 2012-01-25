package nig.mf.plugin.pssd.services;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

//import com.sun.tools.javac.util.List;

import arc.mf.plugin.PluginService;
//import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import au.com.bytecode.opencsv.CSVWriter;

public class SvcObjectCSVExport extends PluginService {
	private Interface _defn;

	public SvcObjectCSVExport() {

		_defn = new Interface();
		//add "id" as input
		_defn.add(new Interface.Element("id", 
				CiteableIdType.DEFAULT,
				"Input the identity of root PSSD object.", 
				1, 1));
		//add "xpath" as input
		_defn.add(new Interface.Element(
				"xpath",
				StringType.DEFAULT,
				"Input the xpath to query element.",
				1, Integer.MAX_VALUE));
		//add "xpathoutput" option
		Interface.Element xpathoutput = new Interface.Element(
				"xpathoutput",
				new EnumType(new String[] {"true", "false"}),
				"Select if output Xpath as the header of csv table",
				0,1);
		xpathoutput.add(new Interface.Attribute(
				"value", 
				StringType.DEFAULT, 
				"Choose if need to output xpath",
				0));
		_defn.add(xpathoutput);
		//add "delimiter" as input
		Interface.Element e = new Interface.Element(
				"delimiter",
				new EnumType(new String[] { "tab", "comma", "semicolon",
						"space", "other" }),
				"Input the delimiter for csv file: tab, semicolon, comma, space, other. Defaults to comma. " +
				"If sets to other, the value attribute must be specified.",
				0, 1);
		e.add(new Interface.Attribute("value", 
				StringType.DEFAULT,
				"Input the delimiter string value if it is set to other.",
				0));
		_defn.add(e);
		//add data "type" as input
		Interface.Element selectObj = new Interface.Element(
				"type",
				new EnumType(new String[] {"project", "subject", "ex-method", "method","dataset"}),
				"Select the object.",0, Integer.MAX_VALUE);
		selectObj.add(new Interface.Attribute("value", 
				StringType.DEFAULT,
				"The selected data type user want to output", 
				0));
		_defn.add(selectObj);
	}

	@Override
	public Access access() {

		return ACCESS_ACCESS;
	}

	@Override
	public Interface definition() {

		return _defn;
	}

	public String description() {

		return "This services require user specify object id, at least one xpath to the element which value " +
				"is queried, type of data, and delimiter type which is optional. Object citable id is a default output." +
				"The queried results will be output into a csv file which name is specified by user";
	}
	
	@Override
	public void execute(Element args, Inputs inputs, Outputs outputs,
			XmlWriter w) throws Throwable {
		String id = GetId(args);
		char delimiter = GetDelimiterType(args);
		Collection<String> type = GetSelectDataType(args);
		boolean xpathOutput = GetXpathOutput(args);
		Map<String,XmlDocMaker> queries = GetQueriesXml(args, id, type, xpathOutput); //first string is data type, second one is query
		Map<String, Element> xmlElements = GetXmlElements(args, queries);
		WriteToCSVFile(xmlElements, delimiter, outputs, xpathOutput);
		w.add(args);
	}
	
	//return the input id
	public String GetId(Element args) throws Throwable
	{
		return args.value("id");
	}
	
	//return delimiter type, default is ','
	public char GetDelimiterType(Element args) throws Throwable
	{
		char delimiter = ',';
		String delimiterType = args.value("delimiter");
		if (delimiterType != null)
		{
			if (delimiterType.equals("tab")) {
				delimiter = '	';
			} else if (delimiterType.equals("semicolon")) {
				delimiter = ';';
			} else if (delimiterType.equals("comma")) {
				delimiter = ',';
			} else if (delimiterType.equals("space")) {
				delimiter = ' ';
			} else if (delimiterType.equals("other")) {
				String dv = args.value("delimiter/@value");
				if (dv == null) {
					throw new Exception("delimiter value attribute is not set.");
				}
				if (dv.length() != 1) {
					throw new Exception("invalid delimiter: " + dv
							+ " Expected a single character.");
				}
				delimiter = dv.charAt(0);
			}
		}
		return delimiter;
	}
	
	//return output data type as collection
	public Collection<String> GetSelectDataType (Element args) throws Throwable
	{
		return args.values("type");
	}
	
	public boolean GetXpathOutput (Element args) throws Throwable
	{
		boolean option = true;
		String xpathOption = args.value("xpathoutput");
		if (xpathOption != null)
		{
			if (xpathOption.equals("false"))
				option = false;
			else if (xpathOption.equals("true"))
				option = true;
			else
				throw new Exception("Expected input \"true\" or \"false\"");
		}
		return option;
	}
	
	//return queries as Map data structure
	public Map<String, XmlDocMaker> GetQueriesXml
	(Element args, String id, Collection<String> selectObjectType, boolean xpathOutput) 
			throws Throwable
	{
		Map<String, XmlDocMaker> queries = new HashMap<String, XmlDocMaker>(); //use Map data structure to combine data type and data query
		StringBuilder query = new StringBuilder();
		Collection<String> xpaths = args.values("xpath");
		XmlDocMaker xmlDocMaker = new XmlDocMaker("args");
		//query the same part for difference queries.
		xmlDocMaker.add("action","get-value");
		//query id by default
		String [] attribute = new String[] {"ename", "cid"};
		xmlDocMaker.add("xpath",attribute, "cid");
		if (selectObjectType == null) //if selectDataType is null, assign default to key
		{
			query.append("cid='" + id 
					+ "' or cid starts with '" + id + "'");
			xmlDocMaker.add("where", query.toString());
			for (String xpath : xpaths)
			{
				if (!xpathOutput)
				{
					attribute = new String [] {"ename", xpath.split("/")[xpath.split("/").length -1 ]};
				}
				else
				{
					attribute = new String [] {"ename", xpath.replace("/", ".")};
				}
				xmlDocMaker.add("xpath", attribute ,xpath);
			}
			queries.put("default", xmlDocMaker);
		}
		else 
		{
			for (String type : selectObjectType)
			{
				XmlDocMaker tempXmlDocMaker = new XmlDocMaker("args");
				for (Element element : xmlDocMaker.root().elements())
					tempXmlDocMaker.add(element);
				query = new StringBuilder();
				query.append("( cid='" + id 
						+"' or cid starts with '" + id + "')" 
						+ " and model = 'om.pssd." + type + "'");
				tempXmlDocMaker.add("where", query.toString());
				for(String xpath : xpaths)
				{
					if (!xpathOutput)
					{
						attribute = new String [] {"ename", xpath.split("/")[xpath.split("/").length - 1]};
					}
					else
					{
						attribute = new String [] {"ename", xpath.replace("/",".")};
					}
					tempXmlDocMaker.add("xpath", attribute, xpath);
				}
				queries.put(type, tempXmlDocMaker);
			}
		}
		return queries;
	}

	//return xml elements as Map data structure, Map.key is the type of object and Map.value is queried Element
	public Map<String, Element> GetXmlElements(Element args, Map<String,XmlDocMaker> queries) throws Throwable
	{
		Map<String, Element> xmlElements = new HashMap<String, Element>();
		Set<Entry<String, XmlDocMaker>> setQueries = queries.entrySet();
		Iterator<Entry<String, XmlDocMaker>> iteratorQueries = setQueries.iterator();
		while (iteratorQueries.hasNext())
		{
			Map.Entry<String, XmlDocMaker> queryEntry = (Map.Entry<String, XmlDocMaker>)iteratorQueries.next();
			XmlDoc.Element xmlDocElement = executor().execute("asset.query", queryEntry.getValue().root());
			// Vector<Element> vectorElements = xmlDocElement.elements();
			xmlElements.put(queryEntry.getKey(), xmlDocElement);
		}
		return xmlElements;
	}
	
	public void WriteToCSVFile (Map<String, Element> xmlElements, char delimiter, Outputs outputs, boolean xpathOutput) throws Throwable
	{
		//setup the output string
		PluginService.Output output = outputs.output(0);
		File of = PluginService.createTemporaryFile();
		CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(
				new BufferedOutputStream(new FileOutputStream(of, true))), delimiter, '"');
		//setup the iterator
		Set<Entry<String, Element>> setXmlElements = xmlElements.entrySet();
		Iterator<Entry<String, Element>> iteratorXmlElements = setXmlElements.iterator();
		while (iteratorXmlElements.hasNext())
		{
			Map.Entry<String, Element> mapElement = (Map.Entry<String, XmlDoc.Element>)iteratorXmlElements.next();
			Vector<XmlDoc.Element> elements = mapElement.getValue().elements("asset");			
			//write the type of csv file
			String [] setStrings = new String[] {mapElement.getKey()};
			csvWriter.writeNext(setStrings);
			//write the header of csv file
			Vector<String> header = new Vector<String>();
			Vector<Element> headElement = elements.elementAt(0).elements();
			if (headElement != null)
			{
				for (Element csvHeader : headElement)
				{
					if (xpathOutput)
					{
						header.add(csvHeader.name().replace(".", "/"));
					}
					else
						header.add(csvHeader.name());
				}
			}
			String [] stringHeader = new String [header.size()];
			for (int i = 0; i != header.size(); i ++)
			{
				stringHeader[i] = header.elementAt(i);
			}
			csvWriter.writeNext(stringHeader);
			
			//write csv content
			for (XmlDoc.Element element : elements)
			{
				Vector<Element> subElement = element.elements();
				if (subElement != null)
				{
					Vector<String> stringVector = new Vector<String>();
					for (Element nodeElement : subElement)
					{	
						//stringVector.add(nodeElement.name());
						//if (nodeElement.value() != null)
						//{
							stringVector.add(nodeElement.value());
						//}	
						//else
						//{
							//stringVector.add("N/A");  // if the value of element is null then write N/A (not available) to output
						//}
						//String [] stringValue = (String[]) stringVector.toArray();
					}
					String [] stringValue = new String [stringVector.size()];
					for (int i = 0; i != stringVector.size(); i++)
					{
						stringValue[i] = stringVector.elementAt(i);
					}
					csvWriter.writeNext(stringValue);
				}
			}
			
		}
		csvWriter.close();
		output.setData(new TempFileInputStream(of), of.length(), "plain/text");
	}
	
	@Override
	public String name() {

		return "om.pssd.object.csv.export";
	}

	@Override
	public int maxNumberOfOutputs() {

		return 1;
	}

	@Override
	public int minNumberOfOutputs() {

		return 1;
	}

	public static class TempFileInputStream extends FileInputStream {

		private File _file;

		public TempFileInputStream(File file) throws FileNotFoundException {

			super(file);
			_file = file;

		}

		public void close() throws IOException {

			super.close();
			_file.delete();

		}
	}
}
