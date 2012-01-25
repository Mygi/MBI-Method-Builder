package daris.gui.exmethod;

import java.util.List;
import java.util.Vector;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItemListener;
import arc.gui.form.FormListener;
import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.button.ButtonBar.Alignment;
import arc.gui.gwt.widget.button.ButtonBar.Position;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridDataSource;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.window.Window;
import arc.gui.window.WindowProperties;
import arc.mf.client.util.Action;
import arc.mf.client.util.Transformer;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.TextType;
import arc.mf.object.ObjectMessage;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import daris.client.ui.form.XmlMetaForm;
import daris.client.ui.widget.SimpleTabPanel;
import daris.gui.ObjectDialog;
import daris.model.exmethod.ExMethodRef;
import daris.model.exmethod.ExMethodStep;
import daris.model.exmethod.ExMethodStudyStep;
import daris.model.exmethod.ExMethodSubjectStep;
import daris.model.exmethod.State;
import daris.model.exmethod.messages.ExMethodStepStudyFind;
import daris.model.exmethod.messages.ExMethodStepUpdate;
import daris.model.exmethod.messages.ExMethodSubjectStepUpdate;
import daris.model.study.Study;
import daris.model.study.StudyRef;

public class StepEditorDialog {

	private ExMethodStep _step;

	private MethodAndStep _mas;

	private Window _win;

	private Field<State> _stateField;

	private Field<String> _notesField;

	private ListGrid<Study> _studyGrid;

	private Button _saveButton;

	private SimpleTabPanel _subjectTabPanel;

	private Form _psForm;

	private Form _rsForm;

	public StepEditorDialog(MethodAndStep mas, ExMethodStep step) {

		_step = step;
		_mas = mas;
		WindowProperties wp = new WindowProperties();
		wp.setModal(true);
		wp.setCanBeResized(true);
		wp.setCanBeClosed(true);
		wp.setCanBeMoved(true);
		wp.setCenterInPage(true);
		wp.setTitle("ex-method " + _step.exMethodId() + ", step "
				+ _step.stepPath());
		wp.setSize(
				(int) (com.google.gwt.user.client.Window.getClientWidth() * 0.6),
				(int) (com.google.gwt.user.client.Window.getClientHeight() * 0.6));
		_win = Window.create(wp);

		VerticalPanel vp = new VerticalPanel();
		vp.fitToParent();

		/*
		 * step status
		 */
		Form stepForm = new Form(FormEditMode.UPDATE);
		stepForm.setWidth100();
		stepForm.setHeight(125);
		Field<String> stepNameField = new Field<String>(new FieldDefinition(
				"Step", ConstantType.DEFAULT, "Step name", null, 1, 1));
		stepNameField.setValue(_step.name());
		stepForm.add(stepNameField);
		_stateField = new Field<State>(
				new FieldDefinition("State", new EnumerationType<State>(
						State.values()), "State", null, 1, 1));
		_stateField.setValue(_step.state());
		_stateField.addListener(new FormItemListener<State>() {

			@Override
			public void itemValueChanged(FormItem<State> f) {

				_step.setState(f.value());
			}

			@Override
			public void itemPropertyChanged(FormItem<State> f,
					FormItem.Property property) {

			}
		});
		stepForm.add(_stateField);
		_notesField = new Field<String>(new FieldDefinition("Notes",
				TextType.DEFAULT, "Notes", null, 0, 1));
		_notesField.setValue(_step.notes());
		_notesField.addListener(new FormItemListener<String>() {

			@Override
			public void itemValueChanged(FormItem<String> f) {

				_step.setNotes(f.value());
			}

			@Override
			public void itemPropertyChanged(FormItem<String> f,
					FormItem.Property p) {

			}

		});
		stepForm.add(_notesField);
		stepForm.addListener(new FormListener() {

			@Override
			public void rendering(Form f) {

			}

			@Override
			public void rendered(Form f) {

			}

			@Override
			public void formValuesUpdated(Form f) {

				_saveButton.enable();
			}

			@Override
			public void formStateUpdated(Form f, FormItem.Property p) {

			}
		});
		stepForm.render();
		vp.add(stepForm);

		if (_step instanceof ExMethodStudyStep) {
			vp.add(createStudiesPanel());
		} else if (_step instanceof ExMethodSubjectStep) {
			vp.add(createSubjectPanel((ExMethodSubjectStep) _step));
		}

		ButtonBar bb = new ButtonBar(Position.BOTTOM, Alignment.RIGHT);
		bb.setHeight(26);
		bb.setWidth100();
		bb.setPadding(6);
		bb.setButtonSpacing(10);
		bb.setColourEnabled(false);
		bb.setBackgroundColour("#cccccc");
		_saveButton = bb.addButton("Save");
		_saveButton.disable();
		_saveButton.setMarginRight(30);
		_saveButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				ObjectMessage<Boolean> msg;
				if (_step instanceof ExMethodSubjectStep) {
					ExMethodSubjectStep subjectStep = (ExMethodSubjectStep) _step;
					if (_psForm != null) {
						_psForm.validate();
						Validity valid = _psForm.valid();
						if (valid.valid()) {
							XmlStringWriter w = new XmlStringWriter();
							w.push("ps-meta");
							_psForm.save(w);
							w.pop();
							subjectStep.setPSPublicMetadata(w.document());
						} else {
							_subjectTabPanel.selectTab("Subject");
							Dialog.inform("Error", "Subject form invalid: "
									+ valid.reasonForIssue());
							return;
						}
					}
					if (_rsForm != null) {
						_rsForm.validate();
						Validity valid = _rsForm.valid();
						if (valid.valid()) {
							XmlStringWriter w = new XmlStringWriter();
							w.push("rs-meta");
							_rsForm.save(w);
							w.pop();
							subjectStep.setRSPublicMetadata(w.document());
						} else {
							_subjectTabPanel.selectTab("R-subject");
							Dialog.inform(
									"Error",
									"R-subject form is invalid: "
											+ valid.reasonForIssue());
							return;
						}
					}
					msg = new ExMethodSubjectStepUpdate(subjectStep);
				} else {
					msg = new ExMethodStepUpdate(_step);
				}
				msg.send(new ObjectMessageResponse<Boolean>() {

					@Override
					public void responded(Boolean r) {

						if (r) {
							_win.close();
						}
					}
				});
			}
		});

		vp.add(bb);
		_win.setContent(vp);
	}

	private Widget createSubjectPanel(ExMethodSubjectStep step) {

		_subjectTabPanel = new SimpleTabPanel();
		_subjectTabPanel.fitToParent();
		FormListener fl = new FormListener() {

			@Override
			public void rendering(Form f) {

			}

			@Override
			public void rendered(Form f) {

			}

			@Override
			public void formValuesUpdated(Form f) {

				_saveButton.enable();
			}

			@Override
			public void formStateUpdated(Form f, FormItem.Property p) {

			}
		};
		List<XmlElement> psmeta = step.psPublicMetadata();
		if (psmeta != null) {
			if (psmeta.size() > 0) {
				_psForm = XmlMetaForm.formFor(psmeta, FormEditMode.UPDATE);
				_psForm.fitToParent();
				_psForm.render();
				_psForm.addListener(fl);
				_subjectTabPanel.putTab("Subject", new ScrollPanel(_psForm,
						ScrollPolicy.AUTO));
			}
		}
		List<XmlElement> rsmeta = step.rsPublicMetadata();
		if (rsmeta != null) {
			if (rsmeta.size() > 0) {
				_rsForm = XmlMetaForm.formFor(rsmeta, FormEditMode.UPDATE);
				_rsForm.fitToParent();
				_rsForm.render();
				_rsForm.addListener(fl);
				_subjectTabPanel.putTab("R-subject", new ScrollPanel(_rsForm,
						ScrollPolicy.AUTO));
			}
		}
		return _subjectTabPanel;
	}

	private Widget createStudiesPanel() {

		VerticalPanel vp = new VerticalPanel();
		vp.setBorder(1, "#979797");
		vp.setBorderRadiusTopLeft(3);
		vp.setBorderRadiusTopRight(3);
		vp.fitToParent();
		Label label = new Label("Studies");
		label.setHeight(20);
		label.setWidth100();
		label.setFontSize(12);
		label.setBorderRadiusTopLeft(3);
		label.setBorderRadiusTopRight(3);
		label.setPaddingLeft(5);
		label.setBackgroundImage(new LinearGradient(
				LinearGradient.Orientation.TOP_TO_BOTTOM, "#cccccc", "#999999"));
		vp.add(label);

		ButtonBar bb = new ButtonBar(Position.TOP, Alignment.LEFT);
		bb.setHeight(26);
		Button btn = bb.addButton("Add Study...");
		btn.setFontSize(12);
		btn.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				StudyRef sr = new StudyRef(_mas.method().proute(), _step
						.exMethodId() + ".0");
				sr.setMethodId(_step.exMethodId());
				sr.setMethodStep(_step.stepPath());
				sr.setStudyType(((ExMethodStudyStep) _step).studyType());
				ExMethodRef emr = new ExMethodRef(_mas.method().proute(), _step
						.exMethodId(), null, null, false);
				ObjectDialog dlg = ObjectDialog.dialogFor(emr, sr,
						FormEditMode.CREATE);
				dlg.show(new Action() {

					@Override
					public void execute() {

						_studyGrid.refresh();
					}
				});
			}
		});
		vp.add(bb);

		_studyGrid = new ListGrid<Study>(ScrollPolicy.AUTO);
		_studyGrid.setDataSource(new ListGridDataSource<Study>(
				new DataSource<Study>() {

					@Override
					public boolean isRemote() {

						return true;
					}

					@Override
					public boolean supportCursor() {

						return false;
					}

					@Override
					public void load(final Filter filter, final long start,
							final long end, final DataLoadHandler<Study> lh) {

						ObjectMessage<List<Study>> msg = new ExMethodStepStudyFind(
								_step.exMethodId(), _mas.stepPath());
						msg.send(new ObjectMessageResponse<List<Study>>() {

							@Override
							public void responded(List<Study> studies) {

								if (studies != null) {
									List<Study> rstudies = studies;
									if (filter != null) {
										List<Study> fstudies = new Vector<Study>();
										for (Study a : studies) {
											if (filter.matches(a)) {
												fstudies.add(a);
											}
										}
										rstudies = fstudies;
									}
									long total = rstudies.size();
									int start1 = (int) start;
									int end1 = (int) end;
									if (start1 > 0 || end1 < rstudies.size()) {
										if (start1 >= rstudies.size()) {
											rstudies = null;
										} else {
											if (end1 > rstudies.size()) {
												end1 = rstudies.size();
											}
											rstudies = rstudies.subList(start1,
													end1);
										}
									}
									if (rstudies != null) {
										if (rstudies.isEmpty()) {
											rstudies = null;
										}
									}
									lh.loaded(start1, end1, total, rstudies,
											rstudies == null ? null
													: DataLoadAction.REPLACE);
								} else {
									lh.loaded(0, 0, 0, null, null);
								}
							}
						});
					}
				}, new Transformer<Study, ListGridEntry<Study>>() {

					@Override
					protected ListGridEntry<Study> doTransform(Study study)
							throws Throwable {

						ListGridEntry<Study> entry = new ListGridEntry<Study>(
								study);
						entry.set("id", study.id());
						entry.set("name", study.name());
						entry.set("path", study.methodStep());
						return entry;
					}
				}));
		_studyGrid.setCursorSize(500);
		_studyGrid.addColumnDefn("id", "id");
		_studyGrid.addColumnDefn("name", "name");
		_studyGrid.setEmptyMessage("");
		_studyGrid.fitToParent();
		vp.add(_studyGrid);
		return vp;
	}

	public void show() {

		_win.show();
	}

	public void hide() {

		_win.hide();
	}

}
