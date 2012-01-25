package daris.model.sc;

public interface ShoppingCartListener {
	
	void created(ShoppingCartRef cart);
	void deleted(ShoppingCartRef cart);
	void updated(ShoppingCartRef cart);
	void contentChanged(ShoppingCartRef cart);
	void dataReady(ShoppingCartRef cart);
	
}
