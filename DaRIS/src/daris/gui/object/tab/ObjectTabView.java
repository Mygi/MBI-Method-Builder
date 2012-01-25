package daris.gui.object.tab;

import com.google.gwt.user.client.ui.Widget;

import arc.gui.InterfaceComponent;
import arc.gui.form.FormEditMode;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.mf.client.util.Action;
import arc.mf.client.util.ActionListener;
import daris.client.ui.widget.CTabPanel;
import daris.gui.object.ObjectDetail;
import daris.model.Model;
import daris.model.Model.Event;
import daris.model.object.PSSDObjectRef;

public class ObjectTabView implements daris.model.Model.EventHandler,
		InterfaceComponent {

	private CTabPanel _tp;

	public ObjectTabView() {

		_tp = new CTabPanel();
		Model.subscribe(this);
	}

	public void addTab(final PSSDObjectRef ro) {

		if (_tp.tabOf(ro) == null) {
			ObjectDetail detail = ObjectDetail.detailFor(ro,
					FormEditMode.READ_ONLY);
			final CTabPanel.Tab tab = new CTabPanel.Tab(ro.referentTypeName()
					+ " - " + ro.id(), ro, detail);
			Menu menu = new Menu();
			menu.add(new ActionEntry("Reload Tab", new Action() {
				@Override
				public void execute() {

					reloadTab(ro);
				}
			}));
			menu.add(new ActionEntry("Close Tab", new Action() {

				@Override
				public void execute() {

					removeTab(ro);
				}
			}));
			menu.add(new ActionEntry("Close Other Tabs", new Action() {

				@Override
				public void execute() {
					int count = _tp.numTabs();
					if(count>1){
						Dialog.confirm(
								"Confirm Close",
								"You are about to close all the other tabs. Are you sure you want to continue?",
								new ActionListener() {

									@Override
									public void executed(boolean succeeded) {

										if (succeeded) {
											removeOtherTabs(ro);
										}
									}
								});
					}					
				}
			}));
			menu.add(new ActionEntry("Close All Tabs", new Action() {

				@Override
				public void execute() {

					Dialog.confirm(
							"Confirm Close",
							"You are about to close all the tabs. Are you sure you want to continue?",
							new ActionListener() {

								@Override
								public void executed(boolean succeeded) {

									if (succeeded) {
										removeAllTabs();
									}
								}
							});
				}
			}));
			tab.setContextMenu(menu);
			_tp.addTab(tab);
		}
	}

	public void reloadTab(final PSSDObjectRef ro) {

		final CTabPanel.Tab tab = _tp.tabOf(ro);
		if (tab != null) {
			ObjectDetail detail = ObjectDetail.detailFor(ro,
					FormEditMode.READ_ONLY);
			tab.setContent(detail);
		}
	}

	public void removeTab(PSSDObjectRef ro) {

		_tp.removeTab(_tp.tabOf(ro));
	}

	public void selectTab(PSSDObjectRef ro) {

		CTabPanel.Tab tab = _tp.tabOf(ro);
		if (tab == null) {
			addTab(ro);
		}
		_tp.selectTab(_tp.tabOf(ro));
	}

	public void removeAllTabs() {

		_tp.removeAllTabs();
	}

	public void removeOtherTabs(PSSDObjectRef ro) {

		_tp.removeAllTabs();
		addTab(ro);
	}

	@Override
	public void handleEvent(Event e) {

		switch (e.type()) {
		case Model.Event.OBJECT_CREATED:
			selectTab(e.object());
			break;
		case Model.Event.OBJECT_DELETED:
			removeTab(e.object());
			break;
		case Model.Event.OBJECT_SELECTED:
			selectTab(e.object());
			break;
		case Model.Event.OBJECT_UPDATED:
			reloadTab(e.object());
			break;
		}
	}

	@Override
	public Widget gui() {

		return _tp;
	}

	public void destroy() {

		Model.unsubscribe(this);
	}
}
