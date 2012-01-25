package daris.gui.exmethod;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.menu.MenuToolBar;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.StringType;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;

import daris.client.Resource;
import daris.client.ui.graph.Graph;
import daris.client.ui.graph.GraphListener;
import daris.client.ui.graph.GraphWidget;
import daris.client.ui.graph.Node;
import daris.gui.object.ObjectDetail;
import daris.model.exmethod.ExMethod;
import daris.model.exmethod.ExMethodRef;
import daris.model.exmethod.ExMethodStep;
import daris.model.exmethod.ExMethodStepRef;
import daris.model.method.Method;
import daris.model.object.PSSDObject;

public class ExMethodDetail extends ObjectDetail {

	public static ImageResource ZOOM_IN_ICON = Resource.INSTANCE.zoomIn16();
	public static ImageResource ZOOM_OUT_ICON = Resource.INSTANCE.zoomOut16();

	public static final String WORKFLOW_TAB_NAME = "workflow";

	public ExMethodDetail(ExMethodRef exmethod, FormEditMode mode) {

		super(exmethod, mode);

		/*
		 * workflow tab
		 */
		updateWorkflowTab();
	}

	public ExMethodRef exmethod() {

		return (ExMethodRef) object();
	}

	private void updateWorkflowTab() {

		exmethod().resolve(new ObjectResolveHandler<PSSDObject>() {

			@Override
			public void resolved(PSSDObject o) {

				if (o != null) {
					ExMethod em = (ExMethod) o;
					Graph graph = ExMethodGraph.graphFor(em);
					final GraphWidget gw = new GraphWidget(graph);
					// TODO: temp fix for zoom in/out.
					gw.setWidth(320);
					gw.setHeight(240);
					// gw.fitToParent();

					gw.addGraphListener(new GraphListener() {

						@Override
						public void select(Node node) {

						}

						@Override
						public void deselect(Node node) {

						}

						@Override
						public void open(Node node) {

							if (node.isAtomic()) {
								final MethodAndStep mas = (MethodAndStep) node
										.object();
								ExMethodStepRef emsr = new ExMethodStepRef(mas
										.rootMethodId(), mas.stepPath());
								emsr.resolve(new ObjectResolveHandler<ExMethodStep>() {

									@Override
									public void resolved(ExMethodStep ems) {

										if (ems != null) {
											StepEditorDialog dlg = new StepEditorDialog(
													mas, ems);
											dlg.show();
										}
									}
								});
							}
						}
					});
					// gw.layoutGraph();

					VerticalPanel vp = new VerticalPanel();
					vp.fitToParent();

					MenuToolBar toolBar = new MenuToolBar();
					toolBar.setHeight(25);
					toolBar.setWidth100();
					toolBar.setFontSize(11);
					toolBar.setBackgroundColour("#F0F0F0");
					toolBar.setPaddingTop(2);
					toolBar.setPaddingBottom(2);

					Button zoomInButton = new Button(
							"<div style=\"cursor: pointer\"><img width=\""
									+ ZOOM_IN_ICON.getWidth()
									+ "px\" height=\""
									+ ZOOM_IN_ICON.getHeight()
									+ "px\" src=\""
									+ ZOOM_IN_ICON.getURL()
									+ "\" style=\"vertical-align: top; padding-top: 0px;\"></img>&nbsp;Zoom in</div>",
							false);
					zoomInButton.setWidth(90);
					zoomInButton.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {

							gw.setWidth((int) (gw.width() * 1.1));
							gw.setHeight((int) (gw.height() * 1.1));
						}
					});
					toolBar.add(zoomInButton);

					Button zoomOutButton = new Button(
							"<div style=\"cursor: pointer\"><img width=\""
									+ ZOOM_OUT_ICON.getWidth()
									+ "px\" height=\""
									+ ZOOM_OUT_ICON.getHeight()
									+ "px\" src=\""
									+ ZOOM_OUT_ICON.getURL()
									+ "\" style=\"vertical-align: top; padding-top: 0px;\"></img>&nbsp;Zoom out</div>",
							false);
					zoomOutButton.setWidth(90);
					zoomOutButton.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {

							gw.setWidth((int) (gw.width() / 1.1));
							gw.setHeight((int) (gw.height() / 1.1));
						}
					});
					toolBar.add(zoomOutButton);

					vp.add(toolBar);

					vp.add(new ScrollPanel(gw, ScrollPolicy.AUTO));
					putTab(WORKFLOW_TAB_NAME, vp, false);
				}
			}
		});
	}

	@Override
	protected void addToInterfaceForm(Form interfaceForm) {

		if (mode() == FormEditMode.READ_ONLY) {
			FieldGroup fg = new FieldGroup(new FieldDefinition("method",
					ConstantType.DEFAULT, "Method", null, 1, 1));
			final Field<String> idField = new Field<String>(
					new FieldDefinition("id", StringType.DEFAULT, null, null,
							1, 1));

			fg.add(idField);
			final Field<String> nameField = new Field<String>(
					new FieldDefinition("name", StringType.DEFAULT, null, null,
							1, 1));

			fg.add(nameField);
			final Field<String> descriptionField = new Field<String>(
					new FieldDefinition("description", StringType.DEFAULT,
							null, null, 1, 1));

			fg.add(descriptionField);
			exmethod().resolve(new ObjectResolveHandler<PSSDObject>() {

				@Override
				public void resolved(PSSDObject o) {

					if (o != null) {
						Method m = ((ExMethod) o).method();
						idField.setValue(m.id());
						nameField.setValue(m.name());
						descriptionField.setValue(m.description());
					}
				}
			});
			interfaceForm.add(fg);
		}
	}

}
