/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.*;

/**
 * <p>
 * A concrete class specializing {@link com.pixelmed.dicom.Attribute Attribute} for Unknown (UN) attributes.
 * </p>
 * 
 * <p>
 * Though an instance of this class may be created using its constructors, there is also a factory class,
 * {@link com.pixelmed.dicom.AttributeFactory AttributeFactory}.
 * </p>
 * 
 * @see com.pixelmed.dicom.Attribute
 * @see com.pixelmed.dicom.AttributeFactory
 * @see com.pixelmed.dicom.AttributeList
 * 
 * @author dclunie
 */
public class UnknownAttribute extends Attribute {

	private static final String identString = "@(#) $Header: /data/bigmac/services/cvs/NIGTK/dcmtools/src/com/pixelmed/dicom/UnknownAttribute.java,v 1.1 2010-06-11 05:52:31 wilson Exp $";

	protected byte[] originalLittleEndianByteValues;

	/**
	 * <p>
	 * Construct an (empty) attribute.
	 * </p>
	 * 
	 * @param t
	 *            the tag of the attribute
	 */
	public UnknownAttribute(AttributeTag t) {
		super(t);
	}

	/**
	 * <p>
	 * Read an attribute from an input stream.
	 * </p>
	 * 
	 * @param t
	 *            the tag of the attribute
	 * @param vl
	 *            the value length of the attribute
	 * @param i
	 *            the input stream
	 * @exception IOException
	 * @exception DicomException
	 */
	public UnknownAttribute(AttributeTag t, long vl, DicomInputStream i) throws IOException, DicomException {
		super(t);
		doCommonConstructorStuff(vl, i);
	}

	/**
	 * <p>
	 * Read an attribute from an input stream.
	 * </p>
	 * 
	 * @param t
	 *            the tag of the attribute
	 * @param vl
	 *            the value length of the attribute
	 * @param i
	 *            the input stream
	 * @exception IOException
	 * @exception DicomException
	 */
	public UnknownAttribute(AttributeTag t, Long vl, DicomInputStream i) throws IOException, DicomException {
		super(t);
		doCommonConstructorStuff(vl.longValue(), i);
	}

	/**
	 * @param vl
	 * @param i
	 * @exception IOException
	 * @exception DicomException
	 */
	private void doCommonConstructorStuff(long vl, DicomInputStream i) throws IOException, DicomException {
		valueLength = vl;
		valueMultiplicity = 1;
		try {
			// i.skipInsistently(vl);
			originalLittleEndianByteValues = new byte[(int) vl];
			i.readInsistently(originalLittleEndianByteValues, 0, (int) vl);
		} catch (IOException e) {
			// throw new DicomException("Failed to skip value (length "+vl+" dec) in UN attribute "+getTag());
			throw new DicomException("Failed to read value (length " + vl + " dec) in UN attribute " + getTag());
		}
	}

	/**
	 * @param o
	 * @exception IOException
	 * @exception DicomException
	 */
	public void write(DicomOutputStream o) throws DicomException, IOException {
		// writeBase(o);
		/*
		 * below is doing the same thing as OtherByteAttribute
		 */
		//== TODO: added by Wilson Liu
		writeBase(o);
		if (originalLittleEndianByteValues != null && originalLittleEndianByteValues.length > 0) {
			o.write(originalLittleEndianByteValues);
			if (getVL() != originalLittleEndianByteValues.length) {
				throw new DicomException("Internal error - byte array length ("+originalLittleEndianByteValues.length+") not equal to expected VL("+getVL()+")");
			}
			long npad = getPaddedVL() - originalLittleEndianByteValues.length;
			while (npad-- > 0) o.write(0x00);
		}
		//==
	}

	/***/
	public String toString(DicomDictionary dictionary) {
		return super.toString(dictionary) + " " + getVR() + " ";
	}

	/**
	 * @exception DicomException
	 */
	public void removeValues() {
		valueMultiplicity = 0;
		valueLength = 0;
	}

	/**
	 * <p>
	 * Get the value representation of this attribute (UN).
	 * </p>
	 * 
	 * @return 'U','U' in ASCII as a two byte array; see {@link com.pixelmed.dicom.ValueRepresentation
	 *         ValueRepresentation}
	 */
	public byte[] getVR() {
		return ValueRepresentation.UN;
	}

	/**
	 * <p>
	 * Get the values of this attribute as a byte array.
	 * </p>
	 * 
	 * <p>
	 * Always to be interpreted as little endian, per the DICOM definition of UN, regardless of the received transfer
	 * syntax.
	 * </p>
	 * 
	 * @return the values as an array of bytes
	 * @exception DicomException
	 *                thrown if values are not available (such as not supported for this concrete attribute class)
	 */
	public byte[] getByteValues() {
		return originalLittleEndianByteValues;
	}
	
	/***/
	//== TODO: added by Wilson Liu
	public long getPaddedVL() {
		long vl = getVL();
		if (vl%2 != 0) ++vl;
		return vl;
	}
	//==
}
