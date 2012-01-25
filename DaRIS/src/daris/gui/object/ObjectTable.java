package daris.gui.object;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.theme.ThemeRegistry;
import arc.gui.gwt.widget.input.CheckBox;
import arc.gui.gwt.widget.table.Table;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.VerticalAlign;

import daris.model.object.PSSDObjectRef;
import daris.model.object.PSSDObjectRefSet;

public class ObjectTable extends Table {
	private List<PSSDObjectRef> _selections;
	
	private CheckBox _cbSelectAll;

	public ObjectTable(PSSDObjectRefSet os) {

		setMargin(10);
		setCellPadding(5);
		setCellSpacing(0);

		addHeader();
		for (PSSDObjectRef o : os) {
			addRow(o);
		}
		_selections = new Vector<PSSDObjectRef>();
	}

	private void select(int i) {

		if (i > 0) {
			_selections.add((PSSDObjectRef) row(i).data());
			ThemeRegistry.current().listSelect().applyTo(row(i));
		}
	}

	private void deselect(int i) {

		if (i > 0) {
			_selections.remove((PSSDObjectRef) row(i).data());
			ThemeRegistry.current().listSelect().revokeFrom(row(i));
		}
	}
	
	private void addHeader() {

		Row r = addRow();
		_cbSelectAll = new CheckBox(false);
		_cbSelectAll.addChangeListener(new CheckBox.Listener (){
			@Override
			public void changed(CheckBox cb) {
			
				for (int i = 1; i < rowCount(); i++) {
					CheckBox cbx = (CheckBox) row(i).cell(0).widget();
					cbx.setChecked(_cbSelectAll.checked());
				}			
				
			}});
		r.addCell(_cbSelectAll);		
		r.addCell("<span style=\"font:bold 12px serif;\">type</span>");
		r.addCell("<span style=\"font:bold 12px serif;\">id</span>");
		r.addCell("<span style=\"font:bold 12px serif;\">name</span>");
		r.addCell("<span style=\"font:bold 12px serif;\">description</span>");
		r.setBackgroundColour("#ccc");
		r.setVerticalAlignment(VerticalAlign.TOP);
		for(int i=0;i<r.cellCount();i++){
			r.cell(i).setBorderBottom(1,BorderStyle.DOTTED,"#ddd");
			r.cell(i).setBorderRight(2, BorderStyle.SOLID, "#eee");
		}
	}

	private void addRow(final PSSDObjectRef o) {

		final int i = rowCount();
		Row r = addRow();
		r.setData(o);
		CheckBox cb = new CheckBox(false);
//		cb.addClickHandler(new ClickHandler(){
//			@Override
//			public void onClick(ClickEvent event) {
//				_cbSelectAll.setChecked(false);
//			}});
		cb.addChangeListener(new CheckBox.Listener() {
			@Override
			public void changed(CheckBox cb) {
				if (cb.checked()) {
					select(i);
				} else {
					deselect(i);
				}
			}
		});
		r.addCell(cb);
		r.addCell(o.referentTypeName());
		r.addCell(o.id());
		r.addCell(o.name());
		r.addCell(o.description());
		for(int j=0;j<r.cellCount();j++){
			r.cell(j).setBorderBottom(1,BorderStyle.DOTTED,"#ddd");
		}
	}

	public List<PSSDObjectRef> selections() {

		return _selections;
	}
}
