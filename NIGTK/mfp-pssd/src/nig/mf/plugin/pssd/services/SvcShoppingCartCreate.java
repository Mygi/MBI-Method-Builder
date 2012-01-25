package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.sc.Archive;
import nig.mf.plugin.pssd.sc.DeliveryDestination;
import nig.mf.plugin.pssd.sc.DeliveryMethod;
import nig.mf.plugin.pssd.sc.MetadataOutput;
import nig.mf.plugin.pssd.sc.ShoppingCart;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcShoppingCartCreate extends PluginService {

	private Interface _defn;

	public SvcShoppingCartCreate() throws Throwable {

		_defn = new Interface();

		/*
		 * name
		 */
		Interface.Element nameElement = new Interface.Element("name", StringType.DEFAULT,
				"A human readable name (could be non-unique) for the shopping-cart.", 0, 1);
		_defn.add(nameElement);

		/*
		 * destination
		 */
		Interface.Element destinationElement = new Interface.Element(
				"destination",
				StringType.DEFAULT,
				"The check-out destination. Use service om.pssd.shoppingcart.destination.list to list the available destinations. Defaults to browser.",
				0, 1);
		_defn.add(destinationElement);

		/*
		 * archive
		 */
		Interface.Element archiveElement = new Interface.Element("archive", XmlDocType.DEFAULT,
				"The archive settings for the shopping-cart output.", 0, 1);
		archiveElement.add(new Interface.Element("type", new EnumType(Archive.Type.stringValues()),
				"type of the archive. Defaults to zip.", 1, 1));
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

		return "om.pssd.shoppingcart.create";
	}

	public String description() {

		return "Creates a shopping-cart based on default shopping-cart template.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		/*
		 * name
		 */
		String name = args.value("name");

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
		} else {
			dst = DeliveryDestination.BROWSER;
		}

		/*
		 * archive
		 */
		Archive arc = null;
		XmlDoc.Element arcElement = args.element("archive");
		if (arcElement != null) {
			arc = Archive.instantiate(arcElement);
		} else {
			arc = Archive.create(Archive.Type.zip);
		}

		if (dst != null && arc != null) {
			if (dst.method.equals(DeliveryMethod.deposit) && !arc.type().equals(Archive.Type.none)) {
				throw new Exception("If method is deposit, archive type must be none.");
			}
			if (dst.method.equals(DeliveryMethod.download) && arc.type().equals(Archive.Type.none)) {
				throw new Exception("If method is download, archive type must not beFs none.");
			}			
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
		 * create a shopping cart
		 */
		String scid = ShoppingCart.create(executor(), name, dst, arc, metadataOutput);
		/*
		 * output the id.
		 */
		w.add("scid", scid);
	}

}
