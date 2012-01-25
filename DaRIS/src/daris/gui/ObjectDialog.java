package daris.gui;

import arc.gui.form.FormEditMode;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.gwt.widget.window.WindowCloseListener;
import arc.gui.window.WindowProperties;
import arc.mf.client.util.Action;
import arc.mf.client.util.ActionListener;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;

import daris.client.ui.widget.ProgressDialog;
import daris.gui.object.ObjectDetail;
import daris.model.exmethod.ExMethodRef;
import daris.model.object.PSSDObjectRef;
import daris.model.project.ProjectRef;
import daris.model.repository.RepositoryRootRef;
import daris.model.study.StudyRef;
import daris.model.subject.SubjectRef;

public class ObjectDialog {

	private Window _win;

	private ObjectDetail _detail;

	private Button _commitButton;

	// private Button _cancelButton;

	private PSSDObjectRef _o;

	private PSSDObjectRef _po;

	private FormEditMode _mode;

	private ObjectDialog(PSSDObjectRef parent, PSSDObjectRef object, FormEditMode mode) {

		_o = object;
		_po = parent;
		if (_po != null) {
			_o.setProute(_po.proute());
		}
		if (_po != null && _o.id() == null) {
			if (!(_po instanceof RepositoryRootRef)) {
				_o.setId(_po.id() + ".0");
			}
		}
		_mode = mode;

		WindowProperties wp = new WindowProperties();
		wp.setModal(true);
		wp.setCanBeResized(true);
		wp.setCanBeClosed(true);
		wp.setCanBeMoved(true);
		wp.setCenterInPage(true);
		String title;
		if (mode == FormEditMode.CREATE) {
			title = "Create " + object.referentTypeName();
		} else if (mode == FormEditMode.UPDATE) {
			title = "Edit " + object.referentTypeName() + " - " + object.id();
		} else {
			title = object.referentTypeName() + " - " + object.id();
		}
		wp.setTitle(title);
		wp.setSize((int) (com.google.gwt.user.client.Window.getClientWidth() * 0.8),
				(int) (com.google.gwt.user.client.Window.getClientHeight() * 0.8));
		_win = Window.create(wp);

		VerticalPanel vp = new VerticalPanel();
		vp.fitToParent();

		_detail = ObjectDetail.detailFor(object, mode);
		vp.add(_detail);

		if (mode != FormEditMode.READ_ONLY) {
			ButtonBar bb = new ButtonBar(ButtonBar.Position.BOTTOM, ButtonBar.Alignment.RIGHT);
			bb.setHeight(28);
			bb.setWidth100();
			bb.setPadding(6);
			bb.setColourEnabled(false);
			bb.setBackgroundColour("#DDDDDD");

			_commitButton = new Button(mode == FormEditMode.UPDATE ? "Save" : "Create");
			_commitButton.setMarginRight(15);
			_commitButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {

					if (_mode == FormEditMode.UPDATE) {
						doUpdate();
					} else if (_mode == FormEditMode.CREATE) {
						doCreate();
					}
				}
			});
			bb.add(_commitButton);
			// _cancelButton = new Button("Cancel");
			// _cancelButton.setMarginRight(15);
			// _cancelButton.addClickHandler(new ClickHandler() {
			//
			// @Override
			// public void onClick(ClickEvent event) {
			//
			// _win.close();
			// }
			// });
			// bb.add(_cancelButton);
			vp.add(bb);
		}
		_win.setContent(vp);
		_win.centerInPage();
	}

	private ProgressDialog showProgressDialog(String title, String message) {

		final ProgressDialog pd = new ProgressDialog(title, message);

		final Timer timer = new Timer() {
			@Override
			public void run() {

				double progress = pd.progress() + 0.3;
				if (progress > 1) {
					progress = progress - 1;
				}
				pd.setProgress(progress);
			}
		};
		timer.scheduleRepeating(300);
		pd.setCloseAction(new Action() {
			@Override
			public void execute() {

				timer.cancel();
			}
		});
		pd.show();
		return pd;
	}

	private void doCreate() {

		_detail.validate();
		if (!_detail.valid()) {
			return;
		}

		final ProgressDialog pd = showProgressDialog("Creating...", "Communicating with server...");
		_po.createChild(_o, new ObjectMessageResponse<PSSDObjectRef>() {

			@Override
			public void responded(final PSSDObjectRef r) {

				pd.close();
				if (r != null) {
					_win.close();
					Dialog.inform("Response", r.referentTypeName() + " " + r.id() + " has been created.");
				}

			}
		});

	}

	private void doUpdate() {

		_detail.validate();
		if (!_detail.valid()) {
			return;
		}

		final ProgressDialog pd = showProgressDialog("Updating...", "Communicating with server...");
		_o.update(new ActionListener() {

			@Override
			public void executed(boolean succeeded) {

				pd.close();
				if (succeeded) {
					_win.close();
					Dialog.inform("Info", _o.referentTypeName() + " " + _o.id() + " has been updated.");
				}
			}
		});

	}

	public void show(final Action onClose) {

		if (onClose != null) {
			_win.addCloseListener(new WindowCloseListener() {

				@Override
				public void closed(Window w) {

					onClose.execute();
				}
			});
		}
		_win.show();
	}

	public void show() {

		show(null);
	}

	public void close() {

		_win.close();
	}

	public static ObjectDialog dialogFor(PSSDObjectRef o, FormEditMode mode) {

		if (FormEditMode.CREATE == mode) {
			// o is the parent object
			// currently we only support project, subject and study creations.
			PSSDObjectRef co = null;
			if (o instanceof RepositoryRootRef) {
				co = new ProjectRef();
			} else if (o instanceof ProjectRef) {
				co = new SubjectRef((ProjectRef) o);
			} else if (o instanceof ExMethodRef) {
				co = new StudyRef((ExMethodRef) o);
			}
			if (co == null) {
				throw new AssertionError();
			}
			return new ObjectDialog(o, co, mode);
		} else {
			return new ObjectDialog(null, o, mode);
		}
	}

	public static ObjectDialog dialogFor(PSSDObjectRef po, PSSDObjectRef o, FormEditMode mode) {

		return new ObjectDialog(po, o, mode);
	}

}
