package nig.mf.plugin.pssd.services;

import java.util.List;

import nig.mf.plugin.pssd.sc.Archive;
import nig.mf.plugin.pssd.sc.DeliveryDestination;
import nig.mf.plugin.pssd.sc.MetadataOutput;
import nig.mf.plugin.pssd.sc.ShoppingCart;
import nig.mf.plugin.pssd.sc.Transcode;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.LongType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcShoppingCartUpdate extends PluginService {

	private Interface _defn;

	public SvcShoppingCartUpdate() throws Throwable {

		_defn = new Interface();
		/*
		 * id
		 */
		_defn.add(new Interface.Element("scid", LongType.DEFAULT, "The shopping cart id.", 1, 1));

		/*
		 * name
		 */
		Interface.Element nameElement = new Interface.Element("name", StringType.DEFAULT,
				"A human readable name (but non-unique) for the shopping-cart.", 0, 1);
		_defn.add(nameElement);

		/*
		 * transcode
		 */
		Interface.Element transcodeElement = new Interface.Element("transcode", XmlDocType.DEFAULT,
				"transcoding for the specified (from) MIME type.", 0, Integer.MAX_VALUE);
		transcodeElement.add(new Interface.Element("from", StringType.DEFAULT, "The MIME type transcode from.", 1, 1));
		transcodeElement.add(new Interface.Element("to", StringType.DEFAULT, "The MIME type transcode to.", 1, 1));
		_defn.add(transcodeElement);

		/*
		 * destination
		 */
		Interface.Element destinationElement = new Interface.Element("destination", StringType.DEFAULT,
				"The check-out destination. Run service " + SvcShoppingCartDestinationList.SERVICE_NAME
						+ " to see the available destinations. Defaults to browser.", 0, 1);
		_defn.add(destinationElement);

		/*
		 * archive
		 */
		Interface.Element archiveElement = new Interface.Element("archive", XmlDocType.DEFAULT,
				"The archive settings for the shopping-cart output.", 0, 1);
		archiveElement.add(new Interface.Element("type", new EnumType(Archive.Type.stringValues()),
				"type of the archive.", 1, 1));
		Interface.Element parameterElement = new Interface.Element("parameter", StringType.DEFAULT,
				" Additional parameter for the archive type.", 0, Integer.MAX_VALUE);
		parameterElement.add(new Interface.Attribute("name", StringType.DEFAULT, "The parameter name.", 1));
		archiveElement.add(parameterElement);
		_defn.add(archiveElement);

		/*
		 * metadata-output
		 */
		Interface.Element metadataOutputElement = new Interface.Element("metadata-output", new EnumType(
				MetadataOutput.stringValues()), "The metadata output method. Defaults to none.", 0, 1);
		_defn.add(metadataOutputElement);

	}

	public String name() {

		return "om.pssd.shoppingcart.update";
	}

	public String description() {

		return "Modify/Update the settings of the specified shopping cart.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		/*
		 * id
		 */
		String scid = args.value("scid");

		/*
		 * name
		 */
		String name = args.value("name");

		/*
		 * transcode
		 */
		List<XmlDoc.Element> tes = args.elements("transcode");
		List<Transcode> transcodes = null;
		if (tes != null) {
			transcodes = Transcode.instantiate(tes);
		}

		/*
		 * destination
		 */
		String dstName = args.value("destination");
		DeliveryDestination dst = null;
		if (dstName != null) {
			dst = DeliveryDestination.getDestinationForCurrentUser(executor(), dstName);
			if (dst == null) {
				throw new Exception("Invalid destination: " + dstName + ". Please run "
						+ SvcShoppingCartDestinationList.SERVICE_NAME + " service to check the available destinations.");

			}
		}

		/*
		 * archive
		 */
		Archive arc = null;
		XmlDoc.Element arcElement = args.element("archive");
		if (arcElement != null) {
			arc = Archive.instantiate(arcElement);
		}

		/*
		 * metadata output
		 */
		MetadataOutput metadataOutput = null;
		String mo = args.value("metadata-output");
		if (mo != null) {
			metadataOutput = MetadataOutput.instantiate(mo);
		}

		/*
		 * do update
		 */
		ShoppingCart.update(executor(), scid, name, transcodes, dst, arc, metadataOutput, true);

	}
}
