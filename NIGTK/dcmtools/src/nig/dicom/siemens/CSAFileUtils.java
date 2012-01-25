package nig.dicom.siemens;

import java.io.File;
import java.io.IOException;

import nig.iio.siemens.RDAFileHeader;
import nig.iio.siemens.RDAFileUtils;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomFileUtilities;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.TagFromName;

/**
 * A utility class to validate CSA DICOM file, convert it to Siemens RDA format file.
 * 
 * @see <a href="http://sourceforge.net/apps/mediawiki/gdcm/index.php?title=SIEMENS_CSA_Header">About Siemens CSA
 *      Header</a>
 * @author Wilson Liu
 * 
 */
public class CSAFileUtils {

	/**
	 * Prevent from being instantiated.
	 */
	private CSAFileUtils() {
	}

	/**
	 * Check if the file is an Siemens CSA DICOM file.
	 * 
	 * @param f
	 * @return
	 * @throws Throwable
	 */
	public static boolean isCSADicomFile(File f) throws Throwable {

		if (!DicomFileUtilities.isDicomOrAcrNemaFile(f)) {
			return false;
		}
		AttributeList list = new AttributeList();
		list.read(new DicomInputStream(f));
		if (!list.get(TagFromName.MediaStorageSOPClassUID).getSingleStringValueOrEmptyString().equals(
				CSAConstants.UID_CSANonImageStorage)) {
			return false;
		}
		if (!list.get(TagFromName.SOPClassUID).getSingleStringValueOrEmptyString().equals(
				CSAConstants.UID_CSANonImageStorage)) {
			return false;
		}
		return true;

	}

	/**
	 * The main method can be used to test the conversion from CSA DICOM file to Siemens RDA file.
	 * 
	 * <pre>
	 * * 
	 * Usage: java -cp <classpath> nig.image.dicom.siemens.CSAFileUtils  <csa-dicom-file> <rda-file>
	 * </pre>
	 * 
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {

		if (args.length != 2) {
			System.err.println("Error parsing arguments.");
			System.out.println("Usage: " + CSAFileUtils.class.getName() + " <csa-dicom-file> <rda-file>");
			System.exit(1);
		}
		convertToSiemensRDA(new File(args[0]), new File(args[1]));

	}

	/**
	 * Convert a Siemens CSA DICOM file to Siemens RDA file.
	 * 
	 * @param csaFile
	 * @param rdaFile
	 * @throws Throwable
	 */
	public static void convertToSiemensRDA(File csaFile, File rdaFile) throws Throwable {

		if (!CSAFileUtils.isCSADicomFile(csaFile)) {
			throw new IOException(csaFile.getAbsolutePath() + " is not a valid CSA DICOM file.");
		}

		RDAFileHeader rdaHeader = new RDAFileHeader();
		/*
		 * Retrieve some required information from DICOM header
		 */
		AttributeList al = new AttributeList();
		DicomInputStream dis = new DicomInputStream(csaFile);
		al.read(dis);
		dis.close();

		rdaHeader.PatientName = al.get(TagFromName.PatientName).getSingleStringValueOrEmptyString();
		rdaHeader.PatientID = al.get(TagFromName.PatientID).getSingleStringValueOrEmptyString();
		rdaHeader.PatientSex = al.get(TagFromName.PatientSex).getSingleStringValueOrEmptyString();
		rdaHeader.PatientBirthDate = al.get(TagFromName.PatientBirthDate).getSingleStringValueOrEmptyString();
		rdaHeader.StudyDate = al.get(TagFromName.StudyDate).getSingleStringValueOrEmptyString();
		rdaHeader.StudyTime = al.get(TagFromName.StudyTime).getSingleStringValueOrEmptyString();
		rdaHeader.StudyDescription = al.get(TagFromName.StudyDescription).getSingleStringValueOrEmptyString();
		rdaHeader.PatientAge = al.get(TagFromName.PatientAge).getSingleStringValueOrEmptyString();
		rdaHeader.PatientWeight = al.get(TagFromName.PatientWeight).getSingleStringValueOrEmptyString();
		rdaHeader.SeriesDate = al.get(TagFromName.SeriesDate).getSingleStringValueOrEmptyString();
		rdaHeader.SeriesTime = al.get(TagFromName.SeriesTime).getSingleStringValueOrEmptyString();
		rdaHeader.SeriesDescription = al.get(TagFromName.SeriesDescription).getSingleStringValueOrEmptyString();
		rdaHeader.ProtocolName = al.get(TagFromName.ProtocolName).getSingleStringValueOrEmptyString();
		rdaHeader.PatientPosition = al.get(TagFromName.PatientPosition).getSingleStringValueOrEmptyString();
		rdaHeader.SeriesNumber = al.get(TagFromName.SeriesNumber).getSingleStringValueOrEmptyString();
		rdaHeader.InstitutionName = al.get(TagFromName.InstitutionName).getSingleStringValueOrEmptyString();
		rdaHeader.StationName = al.get(TagFromName.StationName).getSingleStringValueOrEmptyString();
		rdaHeader.ModelName = al.get(TagFromName.ManufacturerModelName).getSingleStringValueOrEmptyString();
		rdaHeader.DeviceSerialNumber = al.get(TagFromName.DeviceSerialNumber).getSingleStringValueOrEmptyString();
		rdaHeader.SoftwareVersion = al.get(TagFromName.SoftwareVersions).getSingleStringValueOrEmptyString();
		rdaHeader.InstanceDate = al.get(TagFromName.InstanceCreationDate).getSingleStringValueOrEmptyString();
		rdaHeader.InstanceTime = al.get(TagFromName.InstanceCreationTime).getSingleStringValueOrEmptyString();
		rdaHeader.InstanceNumber = al.get(TagFromName.InstanceNumber).getSingleStringValueOrEmptyString();
		rdaHeader.InstanceComments = al.get(TagFromName.ImageComments).getSingleStringValueOrEmptyString();
		rdaHeader.AcquisitionNumber = al.get(TagFromName.AcquisitionNumber).getSingleStringValueOrEmptyString();

		// retrieve information from csa header
		CSAAttributeList csaal = new CSAAttributeList(csaFile);
		rdaHeader.SequenceName = csaal.getCSAAttribute("SequenceName").getData()[0];
		rdaHeader.SequenceDescription = csaal.getCSAAttribute("SequenceName").getData()[0];
		rdaHeader.TR = csaal.getCSAAttribute("RepetitionTime").getData()[0];
		rdaHeader.TE = csaal.getCSAAttribute("EchoTime").getData()[0];
		rdaHeader.TM = csaal.getCSAAttribute("MixingTime").getData()[0];
		rdaHeader.TI = csaal.getCSAAttribute("InversionTime").getData()[0];
		rdaHeader.DwellTime = csaal.getCSAAttribute("RealDwellTime").getData()[0];
		rdaHeader.EchoNumber = csaal.getCSAAttribute("EchoNumbers").getData()[0];
		rdaHeader.NumberOfAverages = csaal.getCSAAttribute("NumberOfAverages").getData()[0];
		rdaHeader.MRFrequency = csaal.getCSAAttribute("ImagingFrequency").getData()[0];
		rdaHeader.Nucleus = csaal.getCSAAttribute("ImagedNucleus").getData()[0];
		rdaHeader.MagneticFieldStrength = csaal.getCSAAttribute("MagneticFieldStrength").getData()[0];
		rdaHeader.NumOfPhaseEncodingSteps = csaal.getCSAAttribute("NumberOfPhaseEncodingSteps").getData()[0];
		rdaHeader.FlipAngle = csaal.getCSAAttribute("FlipAngle").getData()[0];
		rdaHeader.VectorSize = csaal.getCSAAttribute("SpectroscopyAcquisitionDataColumns").getData()[0];
		rdaHeader.CSIMatrixSize = new String[3];
		rdaHeader.CSIMatrixSize[0] = csaal.getCSAAttribute("Columns").getData()[0];
		rdaHeader.CSIMatrixSize[1] = csaal.getCSAAttribute("Rows").getData()[0];
		rdaHeader.CSIMatrixSize[2] = csaal.getCSAAttribute("NumberOfFrames").getData()[0];
		rdaHeader.CSIMatrixSizeOfScan = new String[3];
		rdaHeader.CSIMatrixSizeOfScan[0] = csaal.getCSAAttribute("SpectroscopyAcquisitionPhaseColumns").getData()[0];
		rdaHeader.CSIMatrixSizeOfScan[1] = csaal.getCSAAttribute("SpectroscopyAcquisitionPhaseRows").getData()[0];
		rdaHeader.CSIMatrixSizeOfScan[2] = csaal.getCSAAttribute("SpectroscopyAcquisitionOut-of-planePhaseSteps")
				.getData()[0];

		rdaHeader.CSIGridShift = new String[3];
		String[] CsiGridshiftVector = csaal.getCSAAttribute("CsiGridshiftVector").getData();
		if (CsiGridshiftVector.length == 3) {
			rdaHeader.CSIGridShift[0] = csaal.getCSAAttribute("CsiGridshiftVector").getData()[0];
			rdaHeader.CSIGridShift[1] = csaal.getCSAAttribute("CsiGridshiftVector").getData()[1];
			rdaHeader.CSIGridShift[2] = csaal.getCSAAttribute("CsiGridshiftVector").getData()[2];
		} else {
			rdaHeader.CSIGridShift[0] = "0";
			rdaHeader.CSIGridShift[1] = "0";
			rdaHeader.CSIGridShift[2] = "0";

		}
		String HammingFilterWidth = csaal.getCSAAttribute("HammingFilterWidth").getData()[0];
		if (Float.parseFloat(HammingFilterWidth) == 0.0) {
			rdaHeader.HammingFilter = "Off";
		} else {
			rdaHeader.HammingFilter = "On";
		}

		rdaHeader.FrequencyCorrection = csaal.getCSAAttribute("FrequencyCorrection").getData()[0];
		rdaHeader.TransmitCoil = csaal.getCSAAttribute("TransmittingCoil").getData()[0];
		rdaHeader.TransmitRefAmplitude = csaal.getCSAAttribute("TransmitterReferenceAmplitude").getData()[0];
		rdaHeader.SliceThickness = csaal.getCSAAttribute("SliceThickness").getData()[0];

		rdaHeader.PositionVector = new String[3];
		rdaHeader.PositionVector[0] = csaal.getCSAAttribute("ImagePositionPatient").getData()[0];
		rdaHeader.PositionVector[1] = csaal.getCSAAttribute("ImagePositionPatient").getData()[1];
		rdaHeader.PositionVector[2] = csaal.getCSAAttribute("ImagePositionPatient").getData()[2];

		rdaHeader.RowVector = new String[3];
		rdaHeader.RowVector[0] = csaal.getCSAAttribute("ImageOrientationPatient").getData()[0];
		rdaHeader.RowVector[1] = csaal.getCSAAttribute("ImageOrientationPatient").getData()[1];
		rdaHeader.RowVector[2] = csaal.getCSAAttribute("ImageOrientationPatient").getData()[2];

		rdaHeader.ColumnVector = new String[3];
		rdaHeader.ColumnVector[0] = csaal.getCSAAttribute("ImageOrientationPatient").getData()[3];
		rdaHeader.ColumnVector[1] = csaal.getCSAAttribute("ImageOrientationPatient").getData()[4];
		rdaHeader.ColumnVector[2] = csaal.getCSAAttribute("ImageOrientationPatient").getData()[5];

		rdaHeader.VOIPositionSag = csaal.getCSAAttribute("VoiPosition").getData()[0];
		rdaHeader.VOIPositionCor = csaal.getCSAAttribute("VoiPosition").getData()[1];
		rdaHeader.VOIPositionTra = csaal.getCSAAttribute("VoiPosition").getData()[2];

		rdaHeader.VOIThickness = csaal.getCSAAttribute("VoiThickness").getData()[0];
		rdaHeader.VOIPhaseFOV = csaal.getCSAAttribute("VoiPhaseFoV").getData()[0];
		rdaHeader.VOIReadoutFOV = csaal.getCSAAttribute("VoiReadoutFoV").getData()[0];

		rdaHeader.VOINormalSag = csaal.getCSAAttribute("VoiOrientation").getData()[0];
		rdaHeader.VOINormalCor = csaal.getCSAAttribute("VoiOrientation").getData()[1];
		rdaHeader.VOINormalTra = csaal.getCSAAttribute("VoiOrientation").getData()[2];

		rdaHeader.VOIRotationInPlane = csaal.getCSAAttribute("VoiInPlaneRotation").getData()[0];
		rdaHeader.FoVHeight = csaal.getCSAAttribute("VoiPhaseFoV").getData()[0];
		rdaHeader.FoVWidth = csaal.getCSAAttribute("VoiReadoutFoV").getData()[0];
		rdaHeader.FoV3D = csaal.getCSAAttribute("VoiThickness").getData()[0];
		rdaHeader.PercentOfRectFoV = csaal.getCSAAttribute("PercentPhaseFieldOfView").getData()[0];
		rdaHeader.NumberOfRows = csaal.getCSAAttribute("Rows").getData()[0];
		rdaHeader.NumberOfColumns = csaal.getCSAAttribute("Columns").getData()[0];
		rdaHeader.NumberOf3DParts = csaal.getCSAAttribute("NumberOfFrames").getData()[0];
		rdaHeader.PixelSpacingRow = csaal.getCSAAttribute("PixelSpacing").getData()[0];
		rdaHeader.PixelSpacingCol = csaal.getCSAAttribute("PixelSpacing").getData()[1];
		rdaHeader.PixelSpacing3D = csaal.getCSAAttribute("SliceThickness").getData()[0];

		float[] csaData = csaal.getCSAData();
		double[] rdaData = new double[csaData.length];
		for (int i = 0; i < csaData.length; i++) {
			rdaData[i] = (double) csaData[i];
		}
		RDAFileUtils.createRDAFile(rdaHeader, rdaData, rdaFile);

	}

}
