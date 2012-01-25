package daris.client;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.FormItem.XmlType;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.window.WindowProperties;
import arc.mf.client.util.Action;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.StringType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;

import daris.client.ui.widget.ProgressDialog;
import daris.client.util.XmlUtil;

public class Test {

	public static void show() {

		WindowProperties wp = new WindowProperties();
		wp.setModal(true);
		wp.setCanBeResized(true);
		wp.setCanBeClosed(true);
		wp.setCanBeMoved(false);
		wp.setCentered(true);
		wp.setTitle("DaRIS");
		wp.setSize(800, 600);

		Window win = Window.create(wp);

		VerticalPanel vp = new VerticalPanel();
		vp.fitToParent();

		SimplePanel sp = new SimplePanel();
		sp.fitToParent();

		final Form f = new Form();
		FieldGroup fg1 = new FieldGroup(new FieldDefinition("mydoc",
				StringType.DEFAULT, null, null, 1, 1));
		Field<String> ef1 = new Field<String>(new FieldDefinition("elem1",
				StringType.DEFAULT, null, null, 1, 1));
		Field<String> af1 = new Field<String>(new FieldDefinition("attr1",
				StringType.DEFAULT, null, null, 1, 1));
		af1.setXmlType(XmlType.ATTRIBUTE);

		fg1.add(ef1);
		fg1.add(af1);

		f.add(fg1);
		f.render();
		sp.setContent(f);
		vp.add(sp);

		ButtonBar bb = new ButtonBar(ButtonBar.Position.BOTTOM,
				ButtonBar.Alignment.CENTER);
		bb.setHeight(30);
		Button btn = new Button("Show XML");
		btn.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				XmlStringWriter w = new XmlStringWriter();
				f.save(w);
				com.google.gwt.user.client.Window.alert(XmlUtil
						.makeSafeXmlString(w.document()));
			}
		});
		bb.add(btn);
		vp.add(bb);

		win.setContent(vp);
		win.show();

	}
	
	public static void show2(){
		final ProgressDialog pd = new ProgressDialog("Test", "Test progress dialog...", true, null);
		pd.show();
		final Timer timer = new Timer(){
			@Override
			public void run() {
				pd.setProgress(pd.progress()+0.1);
			}};
		timer.scheduleRepeating(100);
		pd.setCloseAction(new Action() {
			@Override
			public void execute() {

				timer.cancel();
			}
		});
	}

}
