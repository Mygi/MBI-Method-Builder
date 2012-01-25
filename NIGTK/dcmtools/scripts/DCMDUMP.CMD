@echo off

if not exist %~dp0..\lib\dcmtools.jar echo "Error: could not find dcmtools.jar"

java -cp %~dp0..\lib\dcmtools.jar nig.dicom.util.DicomDump %*