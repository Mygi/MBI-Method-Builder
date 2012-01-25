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
import daris.client.model.fcp.DerivationDataSetCreateFCP;
import daris.client.model.fcp.FileCompilationProfile;
import daris.client.model.fcp.messages.FCPList;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.ui.util.WindowUtil;

public class DerivationDataSetImportAction extends ActionInterface<DObject> {

	private DerivationDataSetCreateFCP _fcp;
	private List<LocalFile> _files;

	public DerivationDataSetImportAction(List<LocalFile> files, DObjectRef po,
			Window owner) {

		this(files, po, owner, WindowUtil.windowWidth(owner, 0.5), WindowUtil
				.windowHeight(owner, 0.5));
	}

	public DerivationDataSetImportAction(List<LocalFile> files, DObjectRef po,
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
				return "Load file compilation profile for object attachment";
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
										if (fcp instanceof DerivationDataSetCreateFCP) {
											_fcp = (DerivationDataSetCreateFCP) fcp;
											l.executed(
													ActionPreconditionOutcome.PASS,
													"File compilation profile for object attachment is found");
											return;
										}
									}
								}
								l.executed(ActionPreconditionOutcome.PASS,
										"File compilation profile for object attachment is  not found");
							}
						});
			}
		});

	}

	@Override
	public void createInterface(InterfaceCreateHandler ch) {
		ch.created(new DerivationDataSetImportForm(_files,
				(DObjectRef) object(), _fcp));
	}

	@Override
	public String actionName() {
		return "Import Derived Dataset";
	}

	@Override
	public String actionButtonName() {
		return "Import";
	}

	@Override
	public String title() {
		DObjectRef o = (DObjectRef) object();
		return "Import Derviation Dataset for " + o.referentTypeName() + " "
				+ o.id();
	}
}