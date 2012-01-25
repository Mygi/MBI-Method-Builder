package daris.client.ui.object.action;

import java.util.List;
import java.util.Vector;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.ActionInterface;
import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.object.action.precondition.ActionPreconditionListener;
import arc.gui.object.action.precondition.ActionPreconditionOutcome;
import arc.gui.object.action.precondition.EvaluatePrecondition;
import arc.gui.window.Window;
import arc.mf.client.file.LocalFile;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.fcp.DicomIngestFCP;
import daris.client.model.fcp.FileCompilationProfile;
import daris.client.model.fcp.messages.FCPList;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.ui.util.WindowUtil;

public class DicomIngestAction extends ActionInterface<DObject> {

	private DicomIngestFCP _fcp;
	private List<LocalFile> _files;

	public DicomIngestAction(List<LocalFile> files, DObjectRef po, Window owner) {

		this(files, po, owner, WindowUtil.windowWidth(owner, 0.5), WindowUtil
				.windowHeight(owner, 0.5));
	}

	public DicomIngestAction(List<LocalFile> files, DObjectRef po,
			Window owner, int width, int height) {

		super(po.referentTypeName(), po, new Vector<ActionPrecondition>(),
				owner, width, height);
		_files = files;
		preconditions().add(new ActionPrecondition() {

			@Override
			public EvaluatePrecondition evaluate() {
				return EvaluatePrecondition.BEFORE_INTERACTION;
			}

			@Override
			public String description() {
				return "Load file compilation profile for DICOM ingest";
			}

			@Override
			public void execute(final ActionPreconditionListener l) {
				new FCPList()
						.send(new ObjectMessageResponse<List<FileCompilationProfile>>() {

							@Override
							public void responded(
									List<FileCompilationProfile> fcps) {
								if (fcps != null) {
									for (FileCompilationProfile fcp : fcps) {
										if (fcp instanceof DicomIngestFCP) {
											_fcp = (DicomIngestFCP) fcp;
											l.executed(
													ActionPreconditionOutcome.PASS,
													"File compilation profile for DICOM ingest found");
											return;
										}
									}
								}
								l.executed(ActionPreconditionOutcome.PASS,
										"File compilation profile for DICOM ingest not found");
							}
						});
			}
		});

	}

	@Override
	public void createInterface(InterfaceCreateHandler ch) {
		ch.created(new DicomIngestForm(_files, (DObjectRef) object(), _fcp));
	}

	@Override
	public String actionName() {
		return "DICOM Ingest";
	}
	
	@Override
	public String actionButtonName(){
		return "Ingest";
	}

	@Override
	public String title() {
		DObjectRef o = (DObjectRef) object();
		return "Ingest DICOM datasets for " + o.referentTypeName() + " "
				+ o.id();
	}
}
