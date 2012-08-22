package sudoku;

public class Rule {
	
	public final int origin;
	public final int destination;
	public final boolean connected;
	
	public Rule(int origin, int destination, boolean connected) {
	  assert origin >= 0;
	  assert destination >= 0;
		this.origin = origin;
		this.destination = destination;
		this.connected = connected;
	}
}
