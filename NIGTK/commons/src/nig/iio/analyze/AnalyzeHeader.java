package nig.iio.analyze;

import java.io.BufferedOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * 
 * 
 * @see <a href="http://eeg.sourceforge.net/ANALYZE75.pdf">ANALYZE 7.5 Header File format</a>
 * @see <a href="http://www.grahamwideman.com/gw/brain/analyze/formatdoc.htm">Mayo/SPM "Analyze" Format Spec
 *      Compilation</a>
 * @see <a href="http://rsbweb.nih.gov/ij/plugins/analyze.html">ImageJ Analyze Plugin source code</a>
 * 
 * 
 */

public class AnalyzeHeader {

	public static class DataType {

		public static final int DT_NONE = 0;
		public static final int DT_UNKNOWN = 0;
		public static final int DT_BINARY = 1;
		public static final int DT_UNSIGNED_CHAR = 2;
		public static final int DT_SIGNED_SHORT = 4;
		public static final int DT_SIGNED_INT = 8;
		public static final int DT_FLOAT = 16;
		public static final int DT_COMPLEX = 32;
		public static final int DT_DOUBLE = 64;
		public static final int DT_RGB = 128;
		public static final int DT_ALL = 255;

	}

	public static class Complex {

		public final float real;
		public final float imag;

		public Complex(float real, float imag) {
			this.real = real;
			this.imag = imag;
		}

	}

	/**
	 * Header Key total: 40 bytes
	 */
	/*
	 * sizeof_hdr: Must indicate the byte size of the header file.
	 */
	public int sizeof_hdr; /* 0 + 4 */
	public byte[] data_type = new byte[10]; /* 4 + 10 */
	public byte[] db_name = new byte[18]; /* 14 + 18 */

	/*
	 * extents: Should be 16384, the image file is created as contiguous with a minimum extent size.
	 */
	public int extents; /* 32 + 4 */
	public short session_error; /* 36 + 2 */

	/*
	 * regular: Must be `r' to indicate that all images and volumes are the same size.
	 */
	public byte regular; /* 38 + 1 */
	public byte hkey_un0; /* 39 + 1 */

	/**
	 * Image Dimension total: 108 bytes The image dimension fields describe the organisation and size of the images.
	 * These elements enable the database to reference images by volume and slice number.
	 */
	/*
	 * dim: array of the image dimensions.
	 */
	// dim[0] Number of dimensions in database; usually 4
	// dim[1] Image X dimension; number of pixels in an image row
	// dim[2] Image Y dimension; number of pixel rows in slice
	// dim[3] Volume Z dimension; number of slices in a volume
	// dim[4] Time points, number of volumes in database.
	public short[] dim = new short[8]; /* 0 + 16 */
	/*
	 * vox_units: spatial units of measure for a voxel
	 */
	public byte[] vox_units = new byte[4]; /* 16 + 4 */
	/*
	 * cal_units: specifies the name of the calibration unit
	 */
	public byte[] cal_units = new byte[8]; /* 20 + 8 */
	public short unused1; /* 28 + 2 */
	/*
	 * datatype: datatype for this image set
	 */
	public short datatype; /* 30 + 2 */
	/*
	 * bitpix: number of bits per pixel; 1, 8, 16, 32, or 64.
	 */
	public short bitpix; /* 32 + 2 */
	public short dim_un0; /* 34 + 2 */
	/*
	 * pixdim: Parallel array to dim[], giving real world measurements in mm. and ms.
	 */
	// pixdim[1]; voxel width in mm.
	// pixdim[2]; voxel height in mm.
	// pixdim[3]; slice thickness in mm.
	public float[] pixdim = new float[8]; /* 36 + 32 */
	/*
	 * vox_offset: byte offset in the .img file at which voxels start. This value can negative to specify that the
	 * absolute value is applied for every image in the file.
	 */
	public float vox_offset; /* 68 + 4 */
	public float roi_scale; //funused1  /* 72 + 4 */
	public float funused1;  //funused2 /* 76 + 4 */
	public float funused2;  //funused3 /* 80 + 4 */
	/*
	 * calibrated: Max, Min specify the range of calibration values
	 */
	public float cal_max; /* 84 + 4 */
	public float cal_min; /* 88 + 4 */
	public float compressed; /* 92 + 4 */
	public float verified; /* 96 + 4 */
	/*
	 * glmax, glmin: The maximum and minimum pixel values for the entire database.
	 */
	public int glmax; /* 100 + 4 */
	public int glmin; /* 104 + 4 */

	/**
	 * Data History total: 200 bytes
	 */
	public byte[] descrip = new byte[80]; /* 0 + 80 */
	public byte[] aux_file = new byte[24]; /* 80 + 24 */
	/*
	 * orient: slice orientation for this dataset.
	 */
	// 0 transverse unflipped
	// 1 coronal unflipped
	// 2 sagittal unflipped
	// 3 transverse flipped
	// 4 coronal flipped
	// 5 sagittal flipped
	public byte orient; /* 104 + 1 */
	public byte[] originator = new byte[10]; /* 105 + 10 */
	public byte[] generated = new byte[10]; /* 115 + 10 */
	public byte[] scannum = new byte[10]; /* 125 + 10 */
	public byte[] patient_id = new byte[10]; /* 135 + 10 */
	public byte[] exp_date = new byte[10]; /* 145 + 10 */
	public byte[] exp_time = new byte[10]; /* 155 + 10 */
	public byte[] hist_un0 = new byte[3]; /* 165 + 3 */
	public int views; /* 168 + 4 */
	public int vols_added; /* 172 + 4 */
	public int start_field; /* 176 + 4 */
	public int field_skip; /* 180 + 4 */
	public int omax, omin; /* 184 + 8 */
	public int smax, smin; /* 192 + 8 */

	public AnalyzeHeader() {
		
		this.sizeof_hdr = 348;
		Arrays.fill(this.data_type, (byte) 0);
		Arrays.fill(this.db_name, (byte) 0);
        this.extents=0;
        this.session_error= 0;
        this.regular=(byte)'r';
        this.hkey_un0 = 0;
        Arrays.fill(this.dim, (short)0);
        this.dim[0] = 4;
        Arrays.fill(this.vox_units, (byte)0);
        Arrays.fill(this.cal_units, (byte)0);
        this.unused1 = 0;
        this.datatype = DataType.DT_NONE;
        this.bitpix = 0;
        this.dim_un0 = 0;
        Arrays.fill(this.pixdim, (float)0.0);
        this.vox_offset = (float)0.0;
        this.roi_scale = (float)0.00392157;
        this.funused1= (float)0.0;
        this.funused2= (float)0.0;
        this.cal_max= (float)0.0;
        this.cal_min= (float)0.0;
        this.compressed=0;
        this.verified= 0;
        this.glmax= 0;
        this.glmin= 0;
        Arrays.fill(this.descrip, (byte)0);
        Arrays.fill(this.aux_file, (byte)0);
        this.orient = 0;
        Arrays.fill(this.originator, (byte)0);
        Arrays.fill(this.generated, (byte)0);
        Arrays.fill(this.scannum, (byte)0);
        Arrays.fill(this.patient_id, (byte)0);
        Arrays.fill(this.exp_date, (byte)0);
        Arrays.fill(this.exp_time, (byte)0);
        Arrays.fill(this.hist_un0, (byte)0);
        this.views = 0;
        this.vols_added = 0;
        this.start_field = 0;
        this.field_skip = 0;
        this.omax = 0;
        this.omin = 0;                         
        this.smax = 0;
        this.smin = 0;

	}
	
	public void write(File file, boolean bigEndian) throws Throwable {
		
		DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		try {
			writeInt(os, bigEndian, sizeof_hdr);
			os.write(data_type);
			os.write(db_name);
			writeInt(os, bigEndian, extents);
			writeShort(os, bigEndian, session_error);
			os.write(regular);
			os.write(hkey_un0);
			writeShort(os, bigEndian, dim);
			os.write(vox_units);
			os.write(cal_units);
			writeShort(os, bigEndian, unused1);
			writeShort(os, bigEndian, datatype);
			writeShort(os, bigEndian, bitpix);
			writeShort(os, bigEndian, dim_un0);
			writeFloat(os, bigEndian, pixdim);
			writeFloat(os, bigEndian, vox_offset);
			writeFloat(os, bigEndian, roi_scale);
			writeFloat(os, bigEndian, funused1);
			writeFloat(os, bigEndian, funused2);
			writeFloat(os, bigEndian, cal_max);
			writeFloat(os, bigEndian, cal_min);
			writeFloat(os, bigEndian, compressed);
			writeFloat(os, bigEndian, verified);
			writeInt(os, bigEndian, glmax);
			writeInt(os, bigEndian, glmin);
			os.write(descrip);
			os.write(aux_file);
			os.write(orient);
			os.write(originator);
			os.write(generated);
			os.write(scannum);
			os.write(patient_id);
			os.write(exp_date);
			os.write(exp_time);
			os.write(hist_un0);
			writeInt(os, bigEndian, views);
			writeInt(os, bigEndian, vols_added);
			writeInt(os, bigEndian, start_field);
			writeInt(os, bigEndian, field_skip);
			writeInt(os, bigEndian, omax);
			writeInt(os, bigEndian, omin);
			writeInt(os, bigEndian, smax);
			writeInt(os, bigEndian, smin);
		} finally {
			os.close();
		}
		
	}
	
	private void writeShort(DataOutput o, boolean bigEndian, short v) throws Throwable {
		
		// Java always use big endian;
		if(bigEndian){
			o.writeShort(v);
		} else {
			o.writeShort(Short.reverseBytes(v));
		}
		
	}
	
	private void writeShort(DataOutput o, boolean bigEndian, short[] vs) throws Throwable {
		
		for(int i=0;i<vs.length;i++){
			writeShort(o, bigEndian, vs[i]);
		}
		
	}
	
	private void writeInt(DataOutput o, boolean bigEndian, int v) throws Throwable {
		
		// Java always use big endian;
		if(bigEndian){
			o.writeInt(v);
		} else {
			o.writeInt(Integer.reverseBytes(v));
		}
				
	}
	
	private void writeFloat(DataOutput o, boolean bigEndian, float v) throws Throwable {
		
		// Java always use big endian;
		if(bigEndian){
			o.writeFloat(v);
		} else {
			o.writeInt(Integer.reverseBytes(Float.floatToIntBits(v)));
		}
				
	}
	
	private void writeFloat(DataOutput o, boolean bigEndian, float[] vs) throws Throwable {
		
		for(int i=0;i<vs.length;i++){
			writeFloat(o, bigEndian, vs[i]);
		}
				
	}


}
