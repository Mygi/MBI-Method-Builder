package daris.gui;

import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.window.WindowProperties;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

public class AboutDialog {

	public static final String name = "Distributed and Reflective Informatics System";

	public static final String version = "0.1";

	public static final String[][] attribution = {
			{ "Wilson Liu", "Centre for Neuroscience, University of Melbourne" },
			{ "Neil Killeen", "Centre for Neuroscience, University of Melbourne" },
			{ "Jason Lohrey", "Arcitecta Pty. Ltd." } };

	public static final String[] contact = { "Neil Killeen",
			"nkilleen@unimelb.edu.au" };

	private static String createHtml() {

		String html = "<center>";
		html += "<b style=\"font-size:1em;color:#d64203\">" + name
				+ "</b><br/><br/>";
		html += "<b style=\"font-size:1em;color:#909090\">Version:&nbsp;"
				+ version + "</b><br/><br/>";
		html += "<b style=\"font-size:1em\">Attribution:</b><br/>";
		for (int i = 0; i < attribution.length; i++) {
			html += "<font style=\"font-size:9pt;\">";
			for (int j = 0; j < attribution[i].length; j++) {
				if (j < attribution[i].length - 1) {
					html += attribution[i][j] + ", &nbsp;";
				} else {
					html += attribution[i][j];
				}
			}
			html += "</font><br/>";
		}
		html += "<br/><b style=\"font-size:1em\">Contact:</b><br/>";
		html += "<a href=\"mailto:" + contact[1]
				+ "\" style=\"font-size:9pt;color:#648bcb\">" + contact[0]
				+ ", &nbsp;" + contact[1] + "</a>";
		html += "</center>";
		return html;
	}

	public static void show() {

		WindowProperties wp = new WindowProperties();
		wp.setModal(true);
		wp.setCanBeResized(false);
		wp.setCanBeClosed(false);
		wp.setCanBeMoved(false);
		wp.setCentered(true);
		wp.setTitle("About DaRIS");
		wp.setSize(480, 320);
		final Window win = Window.create(wp);

		VerticalPanel vp = new VerticalPanel();
		vp.fitToParent();

		HTML about = new HTML(createHtml());
		about.setWidth100();
		about.setPadding(20);
		about.fitToParent();
		vp.add(about);

		ButtonBar bb = new ButtonBar(ButtonBar.Alignment.CENTER);
		bb.setWidth100();
		bb.setHeight(30);
		bb.setMargin(15);
		bb.setColourEnabled(true);

		Button okButton = new Button("OK");
		okButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				win.close();
			}
		});
		bb.add(okButton);

		vp.add(bb);
		win.setContent(vp);
		win.show();
	}
}
