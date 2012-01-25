package daris.client.ui.graph;

public interface GraphListener {
	void select(Node node);

	void deselect(Node node);

	void open(Node node);
}
