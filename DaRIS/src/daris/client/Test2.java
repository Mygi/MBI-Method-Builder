package daris.client;

import arc.gui.form.FormEditMode;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tree.TreeGUI;
import arc.gui.gwt.widget.window.Window;
import arc.gui.menu.ActionEntry;
import arc.gui.window.WindowProperties;
import arc.mf.client.util.Action;
import arc.mf.model.asset.namespace.NamespaceTree;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.project.Project;
import daris.client.ui.project.ProjectDetails;
import daris.client.ui.sc.SCMD;
import daris.client.ui.widget.SwitchButton;

public class Test2 {

	public static void show() {

		WindowProperties wp = new WindowProperties();
		wp.setModal(true);
		wp.setCanBeResized(true);
		wp.setCanBeClosed(true);
		wp.setCanBeMoved(false);
		wp.setCentered(true);
		wp.setTitle("DaRIS");
		wp.setSize(1280, 800);

		Window win = Window.create(wp);
		HorizontalSplitPanel hsp = new HorizontalSplitPanel();
		hsp.fitToParent();
		TreeGUI treeGUI = new TreeGUI(new NamespaceTree(), ScrollPolicy.AUTO);
		treeGUI.setHeight100();
		treeGUI.setPreferredWidth(0.5);
		hsp.add(treeGUI);
		SimplePanel sp = new SimplePanel();
		sp.fitToParent();
		hsp.add(sp);

		win.setContent(hsp);
		win.show();
	}

	public static void show2() {

		WindowProperties wp = new WindowProperties();
		wp.setModal(true);
		wp.setCanBeResized(true);
		wp.setCanBeClosed(true);
		wp.setCanBeMoved(false);
		wp.setCentered(true);
		wp.setTitle("DaRIS");
		wp.setSize(com.google.gwt.user.client.Window.getClientWidth(),
				com.google.gwt.user.client.Window.getClientHeight());

		final Window win = Window.create(wp);

		DObjectRef r = new DObjectRef("1005.2.1");
		r.setForEdit(true);
		r.resolve(new ObjectResolveHandler<DObject>() {

			@Override
			public void resolved(DObject o) {

				ProjectDetails pd = new ProjectDetails((Project) o,
						FormEditMode.UPDATE);
				win.setContent((BaseWidget) (pd.gui()));
				win.show();
			}
		});

	}

	
}
