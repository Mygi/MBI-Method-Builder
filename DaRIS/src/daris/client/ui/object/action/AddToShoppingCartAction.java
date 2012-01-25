package daris.client.ui.object.action;

import java.util.Vector;

import arc.gui.InterfaceCreateHandler;
import arc.gui.ValidatedInterfaceComponent;
import arc.gui.gwt.widget.HTML;
import arc.gui.object.action.ActionInterface;
import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.object.action.precondition.ActionPreconditionListener;
import arc.gui.object.action.precondition.ActionPreconditionOutcome;
import arc.gui.object.action.precondition.EvaluatePrecondition;
import arc.gui.window.Window;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.user.client.ui.Widget;

import daris.client.Resource;
import daris.client.model.dataset.messages.DataSetCount;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.sc.ShoppingCartManager;
import daris.client.model.sc.ShoppingCartRef;

public class AddToShoppingCartAction extends ActionInterface<DObject> {

	private static final int WIDTH = 320;
	private static final int HEIGHT = 200;
	private static String ICON_QUESTION = Resource.INSTANCE.question32()
			.getSafeUri().asString();

	private int _nbDatasets = 0;
	private ShoppingCartRef _asc = null;

	public AddToShoppingCartAction(DObjectRef o, Window owner) {

		this(o, owner, WIDTH, HEIGHT);
	}

	public AddToShoppingCartAction(DObjectRef o, Window owner, int width,
			int height) {

		super(o.referentTypeName(), o, new Vector<ActionPrecondition>(), owner,
				width, height);
		preconditions().add(new ActionPrecondition() {

			@Override
			public EvaluatePrecondition evaluate() {
				return EvaluatePrecondition.BEFORE_INTERACTION;
			}

			@Override
			public String description() {
				return "Checking if the active shopping cart is ready.";
			}

			@Override
			public void execute(ActionPreconditionListener l) {
				_asc = ShoppingCartManager.activeShoppingCart();
				if (_asc == null) {
					l.executed(ActionPreconditionOutcome.FAIL,
							"Active shopping cart is not ready. See shopping cart manager for details.");
				} else {
					l.executed(ActionPreconditionOutcome.PASS,
							"Active shopping cart is ready.");
				}
			}
		});
		preconditions().add(new ActionPrecondition() {

			@Override
			public EvaluatePrecondition evaluate() {
				return EvaluatePrecondition.BEFORE_INTERACTION;
			}

			@Override
			public String description() {
				return "Check if the object contains any descendant datasets";
			}

			@Override
			public void execute(final ActionPreconditionListener l) {
				final DObjectRef ro = (DObjectRef) object();
				new DataSetCount(ro).send(new ObjectMessageResponse<Integer>() {

					@Override
					public void responded(Integer count) {

						if (count > 0) {
							_nbDatasets = count;
							l.executed(
									ActionPreconditionOutcome.PASS,
									"The object "
											+ (ro.id() == null ? "" : ro.id())
											+ " contains " + count
											+ " datasets.");
						} else {
							_nbDatasets = 0;
							l.executed(
									ActionPreconditionOutcome.FAIL,
									"The object "
											+ (ro.id() == null ? "" : ro.id())
											+ " contains no datasets.");
						}
					}
				});
			}
		});
	}

	@Override
	public void createInterface(InterfaceCreateHandler ch) {
		ch.created(new Interface((DObjectRef) object(), _nbDatasets, _asc));

	}

	@Override
	public String actionName() {
		return "Add to shopping cart";
	}

	@Override
	public String title() {
		DObjectRef o = ((DObjectRef) object());
		return "Add " + o.referentTypeName() + " " + o.id()
				+ " to shopping cart";
	}

	@Override
	public String actionButtonName() {
		return "Add";
	}

	private static class Interface extends ValidatedInterfaceComponent
			implements AsynchronousAction {

		private DObjectRef _o;
		private int _nbDatasets;
		private ShoppingCartRef _sc;
		private HTML _html;

		public Interface(DObjectRef o, int nbDatasets, ShoppingCartRef sc) {
			_o = o;
			_nbDatasets = nbDatasets;
			_sc = sc;
			String text = "Add " + _o.referentTypeName() + " "
					+ _o.idToString() + "(contains " + _nbDatasets
					+ " datasets) to shopping cart?";
			String html = "<div><img src=\""
					+ ICON_QUESTION
					+ "\" style=\"width:32px;height:32px;vertical-align:middle\"><span style=\"\">&nbsp;"
					+ text + "</span></div>";
			_html = new HTML(html);
			_html.setFontSize(12);
			_html.setMargin(25);
		}

		@Override
		public void execute(ActionListener l) {
			ShoppingCartManager.addContent(_sc, _o);
			l.executed(true);
		}

		@Override
		public Widget gui() {
			return _html;
		}

	}

}
