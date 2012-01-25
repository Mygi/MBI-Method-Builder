package daris.client.ui.sc.action;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.ActionInterface;
import arc.gui.window.Window;
import daris.client.model.sc.ShoppingCart;

public class ShoppingCartClearAction extends ActionInterface<ShoppingCart>{

	public ShoppingCartClearAction(Window owner) {
		super(ShoppingCart.TYPE_NAME, null, owner, 390, 250);
	}

	@Override
	public void createInterface(InterfaceCreateHandler ch) {
		ch.created(new ShoppingCartClearForm());
	}

	@Override
	public String actionName() {
		return "Remove finished shopping carts";
	}
	
	@Override
	public String actionButtonName(){
		return "Remove";
	}

}
