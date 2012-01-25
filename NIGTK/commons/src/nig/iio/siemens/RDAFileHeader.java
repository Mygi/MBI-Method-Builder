package nig.iio.siemens;

public class RDAFileHeader {

	static final String HEADER_BEGIN = ">>> Begin of header <<<";
	static final String HEADER_END = ">>> End of header <<<";

	public String PatientName = ""; // PAIN_DNIC_TT^HF43.5
	public String PatientID = ""; // R423081
	public String PatientSex = ""; // F
	public String PatientBirthDate = ""; // 19870402
	public String StudyDate = ""; // 20080826
	public String StudyTime = ""; // 143511.531000
	public String StudyDescription = ""; // Research Projects^Howard Florey
	public String PatientAge = ""; // 021Y
	public String PatientWeight = ""; // 55.000000
	public String SeriesDate = ""; // 20080826
	public String SeriesTime = ""; // 150744.312000
	public String SeriesDescription = ""; // svs_se_30_Cold_a
	public String ProtocolName = ""; // svs_se_30_Cold_a
	public String PatientPosition = ""; // HFS
	public String SeriesNumber = ""; // 9
	public String InstitutionName = ""; // Children's MRI Centre @ RCH
	public String StationName = ""; // MRC35113
	public String ModelName = ""; // TrioTim
	public String DeviceSerialNumber = ""; // 35113
	public String SoftwareVersion = ""; // syngo MR B15
	public String InstanceDate = ""; // 20080826
	public String InstanceTime = ""; // 150744.312000
	public String InstanceNumber = ""; // 1
	public String InstanceComments = ""; // _nc_4
	public String AcquisitionNumber = ""; // 1
	public String SequenceName = ""; // *svs_se
	public String SequenceDescription = ""; // *svs_se
	public String TR = ""; // 3000.000000
	public String TE = ""; // 30.000000
	public String TM = ""; // 0.000000
	public String TI = ""; // 0.000000
	public String DwellTime = ""; // 833
	public String EchoNumber = ""; // 0
	public String NumberOfAverages = ""; // 64.000000
	public String MRFrequency = ""; // 123.246181
	public String Nucleus = ""; // 1H
	public String MagneticFieldStrength = ""; // 3.000000
	public String NumOfPhaseEncodingSteps = ""; // 1
	public String FlipAngle = ""; // 90.000000
	public String VectorSize = ""; // 1024
	public String[] CSIMatrixSize; // 1
	public String[] CSIMatrixSizeOfScan; // 1
	public String[] CSIGridShift; // 0
	public String HammingFilter = ""; // Off
	public String FrequencyCorrection = ""; // NO
	public String TransmitCoil = ""; // Body
	public String TransmitRefAmplitude = ""; // 272.618000
	public String SliceThickness = ""; // 15.000000
	public String[] PositionVector; // -44.741978
	public String[] RowVector; // -0.000000
	public String[] ColumnVector; // 0.999849
	public String VOIPositionSag = ""; // -37.243119
	public String VOIPositionCor = ""; // -21.795376
	public String VOIPositionTra = ""; // 18.998312
	public String VOIThickness = ""; // 15.000000
	public String VOIPhaseFOV = ""; // 15.000000
	public String VOIReadoutFOV = ""; // 30.000000
	public String VOINormalSag = ""; // 0.017423
	public String VOINormalCor = ""; // -0.057566
	public String VOINormalTra = ""; // 0.998190
	public String VOIRotationInPlane = ""; // 1.570796
	public String FoVHeight = ""; // 15.000000
	public String FoVWidth = ""; // 30.000000
	public String FoV3D = ""; // 15.000000
	public String PercentOfRectFoV = ""; // 1.000000
	public String NumberOfRows = ""; // 1
	public String NumberOfColumns = ""; // 1
	public String NumberOf3DParts = ""; // 1
	public String PixelSpacingRow = ""; // 15.000000
	public String PixelSpacingCol = ""; // 30.000000
	public String PixelSpacing3D = ""; // 15.000000

	public String toString() {

		String lines = "";
		String line;

		// >>> Begin of header <<<
		line = HEADER_BEGIN + "\r\n";
		lines += line;

		// PatientName: PAIN_DNIC_TT^HF43.5
		line = "PatientName: " + PatientName + "\r\n";
		lines += line;

		// PatientID: R423081
		line = "PatientID: " + PatientID + "\r\n";
		lines += line;

		// PatientSex: F
		line = "PatientSex: " + PatientSex + "\r\n";
		lines += line;

		// PatientBirthDate: 19870402
		line = "PatientBirthDate: " + PatientBirthDate + "\r\n";
		lines += line;

		// StudyDate: 20080826
		line = "StudyDate: " + StudyDate + "\r\n";
		lines += line;

		// StudyTime: 143511.531000
		line = "StudyTime: " + StudyTime + "\r\n";
		lines += line;

		// StudyDescription: Research Projects^Howard Florey
		line = "StudyDescription: " + StudyDescription + "\r\n";
		lines += line;

		// PatientAge: 021Y
		line = "PatientAge: " + PatientAge + "\r\n";
		lines += line;

		// PatientWeight: 55.000000
		line = "PatientWeight: " + PatientWeight + "\r\n";
		lines += line;

		// SeriesDate: 20080826
		line = "SeriesDate: " + SeriesDate + "\r\n";
		lines += line;

		// SeriesTime: 150744.312000
		line = "SeriesTime: " + SeriesTime + "\r\n";
		lines += line;

		// SeriesDescription: svs_se_30_Cold_a
		line = "SeriesDescription: " + SeriesDescription + "\r\n";
		lines += line;

		// ProtocolName: svs_se_30_Cold_a
		line = "ProtocolName: " + ProtocolName + "\r\n";
		lines += line;

		// PatientPosition: HFS
		line = "PatientPosition: " + PatientPosition + "\r\n";
		lines += line;

		// SeriesNumber: 9
		line = "SeriesNumber: " + SeriesNumber + "\r\n";
		lines += line;

		// InstitutionName: Children's MRI Centre @ RCH
		line = "InstitutionName: " + InstitutionName + "\r\n";
		lines += line;

		// StationName: MRC35113
		line = "StationName: " + StationName + "\r\n";
		lines += line;

		// ModelName: TrioTim
		line = "ModelName: " + ModelName + "\r\n";
		lines += line;

		// DeviceSerialNumber: 35113
		line = "DeviceSerialNumber: " + DeviceSerialNumber + "\r\n";
		lines += line;

		// SoftwareVersion[0]: syngo MR B15
		line = "SoftwareVersion[0]: " + SoftwareVersion + "\r\n";
		lines += line;

		// InstanceDate: 20080826
		line = "InstanceDate: " + InstanceDate + "\r\n";
		lines += line;

		// InstanceTime: 150744.312000
		line = "InstanceTime: " + InstanceTime + "\r\n";
		lines += line;

		// InstanceNumber: 1
		line = "InstanceNumber: " + InstanceNumber + "\r\n";
		lines += line;

		// InstanceComments: _nc_4
		line = "InstanceComments: " + InstanceComments + "\r\n";
		lines += line;

		// AcquisitionNumber: 1
		line = "AcquisitionNumber: " + AcquisitionNumber + "\r\n";
		lines += line;

		// SequenceName: *svs_se
		line = "SequenceName: " + SequenceName + "\r\n";
		lines += line;

		// SequenceDescription: *svs_se
		line = "SequenceDescription: " + SequenceDescription + "\r\n";
		lines += line;

		// TR: 3000.000000
		line = "TR: " + TR + "\r\n";
		lines += line;

		// TE: 30.000000
		line = "TE: " + TE + "\r\n";
		lines += line;

		// TM: 0.000000
		line = "TM: " + TM + "\r\n";
		lines += line;

		// TI: 0.000000
		line = "TI: " + TI + "\r\n";
		lines += line;

		// DwellTime: 833
		line = "DwellTime: " + Integer.parseInt(DwellTime) / 1000 + "\r\n";
		lines += line;

		// EchoNumber: 0
		line = "EchoNumber: " + EchoNumber + "\r\n";
		lines += line;

		// NumberOfAverages: 64.000000
		line = "NumberOfAverages: " + NumberOfAverages + "\r\n";
		lines += line;

		// MRFrequency: 123.246181
		line = "MRFrequency: " + MRFrequency + "\r\n";
		lines += line;

		// Nucleus: 1H
		line = "Nucleus: " + Nucleus + "\r\n";
		lines += line;

		// MagneticFieldStrength: 3.000000
		line = "MagneticFieldStrength: " + MagneticFieldStrength + "\r\n";
		lines += line;

		// NumOfPhaseEncodingSteps: 1
		line = "NumOfPhaseEncodingSteps: " + NumOfPhaseEncodingSteps + "\r\n";
		lines += line;

		// FlipAngle: 90.000000
		line = "FlipAngle: " + FlipAngle + "\r\n";
		lines += line;

		// VectorSize: 1024
		line = "VectorSize: " + VectorSize + "\r\n";
		lines += line;

		// CSIMatrixSize[0]: 1
		line = "CSIMatrixSize[0]: " + CSIMatrixSize[0] + "\r\n";
		lines += line;

		// CSIMatrixSize[1]: 1
		line = "CSIMatrixSize[1]: " + CSIMatrixSize[1] + "\r\n";
		lines += line;

		// CSIMatrixSize[2]: 1
		line = "CSIMatrixSize[2]: " + CSIMatrixSize[2] + "\r\n";
		lines += line;

		// CSIMatrixSizeOfScan[0]: 1
		line = "CSIMatrixSizeOfScan[0]: " + CSIMatrixSizeOfScan[0] + "\r\n";
		lines += line;

		// CSIMatrixSizeOfScan[1]: 1
		line = "CSIMatrixSizeOfScan[1]: " + CSIMatrixSizeOfScan[1] + "\r\n";
		lines += line;

		// CSIMatrixSizeOfScan[2]: 1
		line = "CSIMatrixSizeOfScan[2]: " + CSIMatrixSizeOfScan[2] + "\r\n";
		lines += line;

		// CSIGridShift[0]: 0
		line = "CSIGridShift[0]: " + CSIGridShift[0] + "\r\n";
		lines += line;

		// CSIGridShift[1]: 0
		line = "CSIGridShift[1]: " + CSIGridShift[1] + "\r\n";
		lines += line;

		// CSIGridShift[2]: 0
		line = "CSIGridShift[2]: " + CSIGridShift[2] + "\r\n";
		lines += line;

		// HammingFilter: Off
		line = "HammingFilter: " + HammingFilter + "\r\n";
		lines += line;

		// FrequencyCorrection: NO
		line = "FrequencyCorrection: " + FrequencyCorrection + "\r\n";
		lines += line;

		// TransmitCoil: Body
		line = "TransmitCoil: " + TransmitCoil + "\r\n";
		lines += line;

		// TransmitRefAmplitude[1H]: 272.618000
		line = "TransmitRefAmplitude[1H]: " + TransmitRefAmplitude + "\r\n";
		lines += line;

		// SliceThickness: 15.000000
		line = "SliceThickness: " + SliceThickness + "\r\n";
		lines += line;

		// PositionVector[0]: -44.741978
		line = "PositionVector[0]: " + PositionVector[0] + "\r\n";
		lines += line;

		// PositionVector[1]: -36.778029
		line = "PositionVector[1]: " + PositionVector[1] + "\r\n";
		lines += line;

		// PositionVector[2]: 18.265146
		line = "PositionVector[2]: " + PositionVector[2] + "\r\n";
		lines += line;

		// RowVector[0]: -0.000000
		line = "RowVector[0]: " + RowVector[0] + "\r\n";
		lines += line;

		// RowVector[1]: 0.998342
		line = "RowVector[1]: " + RowVector[1] + "\r\n";
		lines += line;

		// RowVector[2]: 0.057575
		line = "RowVector[2]: " + RowVector[2] + "\r\n";
		lines += line;

		// ColumnVector[0]: 0.999849
		line = "ColumnVector[0]: " + ColumnVector[0] + "\r\n";
		lines += line;

		// ColumnVector[1]: 0.001003
		line = "ColumnVector[1]: " + ColumnVector[1] + "\r\n";
		lines += line;

		// ColumnVector[2]: -0.017394
		line = "ColumnVector[2]: " + ColumnVector[2] + "\r\n";
		lines += line;

		// VOIPositionSag: -37.243119
		line = "VOIPositionSag: " + VOIPositionSag + "\r\n";
		lines += line;

		// VOIPositionCor: -21.795376
		line = "VOIPositionCor: " + VOIPositionCor + "\r\n";
		lines += line;

		// VOIPositionTra: 18.998312
		line = "VOIPositionTra: " + VOIPositionTra + "\r\n";
		lines += line;

		// VOIThickness: 15.000000
		line = "VOIThickness: " + VOIThickness + "\r\n";
		lines += line;

		// VOIPhaseFOV: 15.000000
		line = "VOIPhaseFOV: " + VOIPhaseFOV + "\r\n";
		lines += line;

		// VOIReadoutFOV: 30.000000
		line = "VOIReadoutFOV: " + VOIReadoutFOV + "\r\n";
		lines += line;

		// VOINormalSag: 0.017423
		line = "VOINormalSag: " + VOINormalSag + "\r\n";
		lines += line;

		// VOINormalCor: -0.057566
		line = "VOINormalCor: " + VOINormalCor + "\r\n";
		lines += line;

		// VOINormalTra: 0.998190
		line = "VOINormalTra: " + VOINormalTra + "\r\n";
		lines += line;

		// VOIRotationInPlane: 1.570796
		line = "VOIRotationInPlane: " + VOIRotationInPlane + "\r\n";
		lines += line;

		// FoVHeight: 15.000000
		line = "FoVHeight: " + FoVHeight + "\r\n";
		lines += line;

		// FoVWidth: 30.000000
		line = "FoVWidth: " + FoVWidth + "\r\n";
		lines += line;

		// FoV3D: 15.000000
		line = "FoV3D: " + FoV3D + "\r\n";
		lines += line;

		// PercentOfRectFoV: 1.000000
		line = "PercentOfRectFoV: " + PercentOfRectFoV + "\r\n";
		lines += line;

		// NumberOfRows: 1
		line = "NumberOfRows: " + NumberOfRows + "\r\n";
		lines += line;

		// NumberOfColumns: 1
		line = "NumberOfColumns: " + NumberOfColumns + "\r\n";
		lines += line;

		// NumberOf3DParts: 1
		line = "NumberOf3DParts: " + NumberOf3DParts + "\r\n";
		lines += line;

		// PixelSpacingRow: 15.000000
		line = "PixelSpacingRow: " + PixelSpacingRow + "\r\n";
		lines += line;

		// PixelSpacingCol: 30.000000
		line = "PixelSpacingCol: " + PixelSpacingCol + "\r\n";
		lines += line;

		// PixelSpacing3D: 15.000000
		line = "PixelSpacing3D: " + PixelSpacing3D + "\r\n";
		lines += line;

		// >>> End of header <<<
		line = HEADER_END + "\r\n";
		lines += line;

		return lines;

	}

}
