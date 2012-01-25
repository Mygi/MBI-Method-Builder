package nig.iio.nifti;

import java.util.Arrays;
import java.util.Vector;

/**
 * 
 * @see http://nifti.nimh.nih.gov/pub/dist/src/niftilib/nifti1.h
 * @see <a href="http://rsbweb.nih.gov/ij/plugins/nifti.html">Sample 1</a>
 * 
 */

public class NiftiHeader {

	/**
	 * Constant values for datatype
	 */
	public static class DataType {

		public static final short DT_NONE = 0;
		public static final short DT_UNKNOWN = 0;
		public static final short DT_BINARY = 1;
		public static final short DT_UNSIGNED_CHAR = 2;
		public static final short DT_SIGNED_SHORT = 4;
		public static final short DT_SIGNED_INT = 8;
		public static final short DT_FLOAT = 16;
		public static final short DT_COMPLEX = 32;
		public static final short DT_DOUBLE = 64;
		public static final short DT_RGB = 128;
		public static final short DT_ALL = 255;
		public static final short DT_UINT8 = 2;
		public static final short DT_INT16 = 4;
		public static final short DT_INT32 = 8;
		public static final short DT_FLOAT32 = 16;
		public static final short DT_COMPLEX64 = 32;
		public static final short DT_FLOAT64 = 64;
		public static final short DT_RGB24 = 128;
		public static final short DT_INT8 = 256;
		public static final short DT_UINT16 = 512;
		public static final short DT_UINT32 = 768;
		public static final short DT_INT64 = 1024;
		public static final short DT_UINT64 = 1280;
		public static final short DT_FLOAT128 = 1536;
		public static final short DT_COMPLEX128 = 1792;
		public static final short DT_COMPLEX256 = 2048;
		public static final short DT_RGBA32 = 2304;

	}

	/**
	 * 
	 * Constant values for intent
	 *
	 */
	public static class Intent {

		public static final short NIFTI_INTENT_NONE = 0;
		public static final short NIFTI_INTENT_CORREL = 2;
		public static final short NIFTI_INTENT_TTEST = 3;
		public static final short NIFTI_INTENT_FTEST = 4;
		public static final short NIFTI_INTENT_ZSCORE = 5;
		public static final short NIFTI_INTENT_CHISQ = 6;
		public static final short NIFTI_INTENT_BETA = 7;
		public static final short NIFTI_INTENT_BINOM = 8;
		public static final short NIFTI_INTENT_GAMMA = 9;
		public static final short NIFTI_INTENT_POISSON = 10;
		public static final short NIFTI_INTENT_NORMAL = 11;
		public static final short NIFTI_INTENT_FTEST_NONC = 12;
		public static final short NIFTI_INTENT_CHISQ_NONC = 13;
		public static final short NIFTI_INTENT_LOGISTIC = 14;
		public static final short NIFTI_INTENT_LAPLACE = 15;
		public static final short NIFTI_INTENT_UNIFORM = 16;
		public static final short NIFTI_INTENT_TTEST_NONC = 17;
		public static final short NIFTI_INTENT_WEIBULL = 18;
		public static final short NIFTI_INTENT_CHI = 19;
		public static final short NIFTI_INTENT_INVGAUSS = 20;
		public static final short NIFTI_INTENT_EXTVAL = 21;
		public static final short NIFTI_INTENT_PVAL = 22;
		public static final short NIFTI_INTENT_LOGPVAL = 23;
		public static final short NIFTI_INTENT_LOG10PVAL = 24;
		public static final short NIFTI_FIRST_STATCODE = 2;
		public static final short NIFTI_LAST_STATCODE = 24;
		public static final short NIFTI_INTENT_ESTIMATE = 1001;
		public static final short NIFTI_INTENT_LABEL = 1002;
		public static final short NIFTI_INTENT_NEURONAME = 1003;
		public static final short NIFTI_INTENT_GENMATRIX = 1004;
		public static final short NIFTI_INTENT_SYMMATRIX = 1005;
		public static final short NIFTI_INTENT_DISPVECT = 1006;
		public static final short NIFTI_INTENT_VECTOR = 1007;
		public static final short NIFTI_INTENT_POINTSET = 1008;
		public static final short NIFTI_INTENT_TRIANGLE = 1009;
		public static final short NIFTI_INTENT_QUATERNION = 1010;
		public static final short NIFTI_INTENT_DIMLESS = 1011;
		public static final short NIFTI_INTENT_TIME_SERIES = 2001;
		public static final short NIFTI_INTENT_NODE_INDEX = 2002;
		public static final short NIFTI_INTENT_RGB_VECTOR = 2003;
		public static final short NIFTI_INTENT_RGBA_VECTOR = 2004;
		public static final short NIFTI_INTENT_SHAPE = 2005;

	}

	public static class XForm {

		public static final short NIFTI_XFORM_UNKNOWN = 0;
		public static final short NIFTI_XFORM_SCANNER_ANAT = 1;
		public static final short NIFTI_XFORM_ALIGNED_ANAT = 2;
		public static final short NIFTI_XFORM_TALAIRACH = 3;
		public static final short NIFTI_XFORM_MNI_152 = 4;

	}

	public static class Magic {

		public static final byte[] NIFTI_MAGIC = new byte[] { 'n', 'i', '1', '\0' };
		public static final byte[] NIFTI_PAIR_MAGIC = new byte[] { 'n', '+', '1', '\0' };

	}

	public static class Units {

		public static final short NIFTI_UNITS_UNKNOWN = 0;
		public static final short NIFTI_UNITS_METER = 1;
		public static final short NIFTI_UNITS_MM = 2;
		public static final short NIFTI_UNITS_MICRON = 3;
		public static final short NIFTI_UNITS_SEC = 8;
		public static final short NIFTI_UNITS_MSEC = 16;
		public static final short NIFTI_UNITS_USEC = 24;
		public static final short NIFTI_UNITS_HZ = 32;
		public static final short NIFTI_UNITS_PPM = 40;
		public static final short NIFTI_UNITS_RADS = 48;

	}

	public static class SliceSEQ {

		public static final short NIFTI_SLICE_UNKNOWN = 0;
		public static final short NIFTI_SLICE_SEQ_INC = 1;
		public static final short NIFTI_SLICE_SEQ_DEC = 2;
		public static final short NIFTI_SLICE_ALT_INC = 3;
		public static final short NIFTI_SLICE_ALT_DEC = 4;
		public static final short NIFTI_SLICE_ALT_INC2 = 5;
		public static final short NIFTI_SLICE_ALT_DEC2 = 6;

	}

	/**
	 * Header Key
	 */
	public int sizeof_hdr; /* !< MUST be 348 */
	public byte[] data_type = new byte[10]; /* !< ++UNUSED++ */
	public byte[] db_name = new byte[18]; /* !< ++UNUSED++ */
	public int extents; /* !< ++UNUSED++ */
	public short session_error; /* !< ++UNUSED++ */
	public byte regular; /* !< ++UNUSED++ */
	public byte dim_info; /* !< MRI slice ordering. */

	/**
	 * Image Dimension
	 */
	public short[] dim = new short[8]; /* !< Data array dimensions. */
	public float intent_p1; /* !< 1st intent parameter. */
	public float intent_p2; /* !< 2nd intent parameter. */
	public float intent_p3; /* !< 3rd intent parameter. */
	public short intent_code; /* !< NIFTI_INTENT_* code. */
	public short datatype; /* !< Defines data type! */
	public short bitpix; /* !< Number bits/voxel. */
	public short slice_start; /* !< First slice index. */
	public float[] pixdim = new float[8]; /* !< Grid spacings. */
	public float vox_offset; /* !< Offset into .nii file */
	public float scl_slope; /* !< Data scaling: slope. */
	public float scl_inter; /* !< Data scaling: offset. */
	public short slice_end; /* !< Last slice index. */
	public byte slice_code; /* !< Slice timing order. */
	public byte xyzt_units; /* !< Units of pixdim[1..4] */
	public float cal_max; /* !< Max display intensity */
	public float cal_min; /* !< Min display intensity */
	public float slice_duration;/* !< Time for 1 slice. */
	public float toffset; /* !< Time axis shift. */
	public int glmax; /* !< ++UNUSED++ */
	public int glmin; /* !< ++UNUSED++ */

	/**
	 * Data History
	 */
	public byte[] descrip = new byte[80]; /* !< any text you like. */
	public byte[] aux_file = new byte[24]; /* !< auxiliary filename. */
	public short qform_code; /* !< NIFTI_XFORM_* code. */
	public short sform_code; /* !< NIFTI_XFORM_* code. */
	public float quatern_b; /* !< Quaternion b param. */
	public float quatern_c; /* !< Quaternion c param. */
	public float quatern_d; /* !< Quaternion d param. */
	public float qoffset_x; /* !< Quaternion x shift. */
	public float qoffset_y; /* !< Quaternion y shift. */
	public float qoffset_z; /* !< Quaternion z shift. */
	public float[] srow_x = new float[4]; /* !< 1st row affine transform. */
	public float[] srow_y = new float[4]; /* !< 2nd row affine transform. */
	public float[] srow_z = new float[4]; /* !< 3rd row affine transform. */
	public byte[] intent_name = new byte[16];/* !< 'name' or meaning of data. */
	public byte[] magic = new byte[4]; /* !< MUST be "ni1\0" or "n+1\0". */

	/* 348 byte in total */

	/**
	 * Extension
	 */
	public static class Extension {

		public final int esize;
		public final int ecode;
		public final byte[] eblob;

		public Extension(int esize, int ecode, byte[] eblob) {
			this.esize = esize;
			this.ecode = ecode;
			this.eblob = eblob;
		}

	}

	public byte[] extension = new byte[4];
	public Vector<Extension> extensions;

	public NiftiHeader() {

		/*
		 * Initialize NiftiHeader Object
		 */
		sizeof_hdr = 348;
		Arrays.fill(data_type, (byte) 0);
		Arrays.fill(db_name, (byte) 0);
		extents = 0;
		session_error = 0;
		regular = 0;
		dim_info = 0;
		Arrays.fill(dim, (short) 0);
		intent_p1 = (float) 0.0;
		intent_p2 = (float) 0.0;
		intent_p3 = (float) 0.0;
		intent_code = Intent.NIFTI_INTENT_NONE;
		datatype = DataType.DT_NONE;
		bitpix = 0;
		slice_start = 0;
		Arrays.fill(pixdim, (float) 0.0);
		pixdim[0] = 1;
		vox_offset = (float) 0.0;
		scl_slope = (float) 0.0;
		scl_inter = (float) 0.0;
		slice_end = (short) 0;
		slice_code = (byte) 0;
		xyzt_units = (byte) 0;
		cal_max = (float) 0.0;
		cal_min = (float) 0.0;
		slice_duration = (float) 0.0;
		toffset = (float) 0.0;
		glmax = 0;
		glmin = 0;
		Arrays.fill(descrip, (byte) 0);
		Arrays.fill(aux_file, (byte) 0);
		qform_code = XForm.NIFTI_XFORM_UNKNOWN;
		sform_code = XForm.NIFTI_XFORM_UNKNOWN;
		quatern_b = (float) 0.0;
		quatern_c = (float) 0.0;
		quatern_d = (float) 0.0;
		qoffset_x = (float) 0.0;
		qoffset_y = (float) 0.0;
		qoffset_z = (float) 0.0;
		Arrays.fill(srow_x, (float) 0.0);
		Arrays.fill(srow_y, (float) 0.0);
		Arrays.fill(srow_z, (float) 0.0);
		Arrays.fill(intent_name, (byte) 0);
		System.arraycopy(Magic.NIFTI_MAGIC, 0, magic, 0, magic.length);
		/* 348 byte in total */
		/*
		 * extension
		 */
		Arrays.fill(extension, (byte) 0);
		extensions = null;

	}

}
