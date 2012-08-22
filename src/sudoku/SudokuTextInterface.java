package sudoku;

public class SudokuTextInterface {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Type in the sudoku challenge. Type in one row at a time.");
		System.out.println("Type a zero for an unknown entry.");
		System.out.println("Every input character except 0-9 is ignored.");
	
		// challenge[column][row]
		int[][] challenge = new int[9][9];
		int index = 0;
		while (index < 81) {
			String s = TextInput.getString();
			for (int i=0; i<s.length(); i++) {
				char c = s.charAt(i);
				if ('0'<=c  &&  c<='9') {
					challenge[index%9][index/9] = c-'0';
					if (++index == 81) break;
				}
			}
		}

		System.out.println("Sudoku challenge received.");
		Sudoku sudoku;
		try {
			sudoku = new Sudoku(challenge);
			sudoku.print();
			System.out.println("Applying simple reasoning.");
			while (sudoku.simpleRound()) ;
			sudoku.print();
			if (!sudoku.solved()) {
				System.out.println("Applying advanced reasoning.");
				while (!sudoku.solved()  &&  sudoku.advancedSolve(false));
			}
			System.out.println("Final result:");
			sudoku.print();
		} catch (Exception e) {
			System.out.println(e);
			return;
		}
	}

}
