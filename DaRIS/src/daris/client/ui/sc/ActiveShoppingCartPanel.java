package daris.client.ui.sc;

import arc.mf.client.util.ObjectUtil;
import daris.client.model.sc.ShoppingCartManager;
import daris.client.model.sc.ShoppingCartRef;

public class ActiveShoppingCartPanel extends ShoppingCartPanel implements
		ShoppingCartManager.Listener {

	public ActiveShoppingCartPanel() {
		super(ShoppingCartManager.activeShoppingCart());
		ShoppingCartManager.addListener(this);
	}

	@Override
	public void activated(ShoppingCartRef sc) {
		if (!ObjectUtil.equals(cart(), sc)) {
			setCart(sc);
		}
	}

	@Override
	public void deactivated(ShoppingCartRef sc) {
		if (ObjectUtil.equals(cart(), sc)) {
			setCart(null);
		}
	}

	@Override
	public void created(ShoppingCartRef sc) {

	}

	@Override
	public void destroyed(ShoppingCartRef sc) {

	}

	@Override
	public void modified(ShoppingCartRef sc) {
		if (ObjectUtil.equals(cart(), sc)) {
			refresh();
		}
	}

	@Override
	public void contentModified(ShoppingCartRef sc) {
		if (ObjectUtil.equals(cart(), sc)) {
			refresh();
		}
	}

	@Override
	public void collectionModified() {

	}

	public void discard() {
		ShoppingCartManager.removeListener(this);
	}

}
