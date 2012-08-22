package sudoku;
import java.util.ArrayList;

/**
 * There are 3*9 subsets of positions each of which must contain the numbers 1 to 9.
 * Each such subset of positions is referred to as a group in the implementation below.
 *
 * @author Jesper Kristensen
 */
public class Sudoku {

  /**
   * b[index][number] is true iff number is a current candidate value for the given position
   */
  private boolean b[][];

  /**
   * bCount[index] is the current number of possible values for the given position.
   */
  private int bCount[];

  /**
   * counters[groupIndex][number] is the current number of possible placements of the number
   * within the given group.
   */
  private int counters[][];

  /**
   * counterIsSolved[groupIndex][number] is set to true when we have
   * exploited the fact that counters[groupIndex][number] has dropped to 1.
   */
  private boolean counterIsSolved[][];

  /**
   * solvedPos[index] is true when the number in the given position has been found.
   */
  private boolean solvedPos[];

  /**
   * The number of remaining unknown positions.
   */
  private int numberPositionsUnsolved;

  /**
   * ref[index] is an array containing the indexes of the three groups of which it's a member.
   */
  private int ref[][];
  
  /**
   * backRef[groupIndex] is an array containing the nine positions in the group.
   */
  private int backRef[][];

	/**
	 * @param challenge indexed [column][row] with dimension 9x9
	 * @throws Exception if challenge has an invalid size
	 */
	public Sudoku(int[][] challenge) throws Exception {
		// Verify input
		if (challenge.length != 9)
			throw new Exception("Sudoku dimension error!");
		for (int i=0; i<9; i++)
			if (challenge[i].length != 9)
				throw new Exception("Sudoku dimension error!");
		
		// Initialize
		b = new boolean[81][9];
		bCount = new int[81];
		solvedPos = new boolean[81];
		numberPositionsUnsolved = 81;
		for (int i=0; i<81; i++) {
			for (int j=0; j<9; j++)
				b[i][j] = true;
			bCount[i] = 9;
			solvedPos[i] = false;
		}
		counters = new int[27][9];
		counterIsSolved = new boolean[27][9];
		for (int i=0; i<27; i++)
			for (int j=0; j<9; j++) {
				counters[i][j] = 9;
				counterIsSolved[i][j] = false;
			}
		
		ref = new int[81][3];
		backRef = new int[27][9];
		int[] backRefCounters = new int[27];
		for (int i=0; i<27; i++)
			backRefCounters[i] = 0;
		for (int j=0; j<81; j++) {
			int tmp;
			// Horizontal line
			tmp = ref[j][0] = j/9;
			backRef[tmp][backRefCounters[tmp]++] = j;
			// Vertical line
			tmp = ref[j][1] = 9+j%9;
			backRef[tmp][backRefCounters[tmp]++] = j;
			// Square
			tmp = ref[j][2] = 18 + 3*(j/27) + (j%9)/3;
			backRef[tmp][backRefCounters[tmp]++] = j;
		}
		
		for (int column=0; column<9; column++)
			for (int row=0; row<9; row++) {
				if (challenge[column][row] != 0) {
					//print();
					setBox(9*row+column, challenge[column][row]-1);
				}
				if (row == 30)
					return;
			}
	}

	public boolean simpleRound() {
		boolean progress = false;
		
		for (int i=0; i<81; i++)
			if (!solvedPos[i]  &&  bCount[i] == 1) {
				setBox(i, leastNumber(i));
				progress = true;
			}
		
		for (int i=0; i<27; i++)
			for (int j=0; j<9; j++)
				if (!counterIsSolved[i][j]  &&  counters[i][j] == 1) {
					for (int k=0; k<9; k++) {
						int index = backRef[i][k]; 
						if (b[index][j]) {
							setBox(index, j);
							progress = true;
						}
					}
				}
		
		return progress;
	}
	
	/**
	 * TODO: Document!
	 */
	public boolean advancedSolve(Boolean printDebug) throws Exception {
		int count = 0;
		int mapping[][] = new int[81][9];
		for (int i=0; i<81; i++)
			for (int j=0; j<9; j++)
				mapping[i][j] = -2;
		for (int i=0; i<81; i++)
			if (bCount[i] > 1) {
				for (int j=0; j<9; j++)
					if (b[i][j]) {
						mapping[i][j] = count;
						count += 2;
					}
			}

		// Create rules
		ArrayList<Rule> rules = new ArrayList<Rule>();
		for (int square=0; square<81; square++) {
			if (bCount[square] > 1) {
				for (int number=0; number<9; number++)
					if (b[square][number]) {
						
						{ // Add rules  (And_{i \ne number} not b[square][i]) -> b[square][number]
							int i = -1;
							// (bCount[square]-2)+1 elements on the left side.
							int countDown = bCount[square]-1;
							while (--countDown > 0) {
								while (!b[square][++i]  ||  i==number) ;
								rules.add(new Rule(mapping[square][i]+1, mapping[square][number], true));
							}
							while (!b[square][++i]  ||  i==number) ;
							rules.add(new Rule(mapping[square][i]+1, mapping[square][number], false));
						}
						
						
						// Add rules  b[square][number] -> not b[index][number]
						for (int setClass=0; setClass<3; setClass++) {
							int setIndex = ref[square][setClass];
							for (int i=0; i<9; i++) {
								int index = backRef[setIndex][i];
								if (bCount[index] > 1  &&  b[index][number]  &&  index != square) {
									// b[square][number] -> not b[index][number]
									rules.add(new Rule(mapping[square][number], mapping[index][number] + 1, false));
								}
							}
						}
					}
			}
		}
		
		{ // Print rules
			for (int i=0; i<rules.size(); i++) {
				Rule rule = rules.get(i);
				
				String right = "";
				{
					for (int pos=0; pos<81; pos++)
						for (int num=0; num<9; num++) {
							if (mapping[pos][num] == rule.destination) {
								right = "(c " +(pos%9)+", r "+(pos/9)+") = "+(num+1);
								break;
							}
							if (mapping[pos][num]+1 == rule.destination) {
								right = "(c " +(pos%9)+", r "+(pos/9)+") != "+(num+1);
								break;
							}
						}
				}
				
				
				String left = "";
				{
					for (int pos=0; pos<81; pos++)
						for (int num=0; num<9; num++) {
							if (mapping[pos][num] == rule.origin) {
								left = "(c " +(pos%9)+", r "+(pos/9)+") = "+(num+1);
								break;
							}
							if (mapping[pos][num]+1 == rule.origin) {
								left = "(c " +(pos%9)+", r "+(pos/9)+") != "+(num+1);
								break;
							}
						}
				}
				if (printDebug) System.out.print(left);
				
				while (rule.connected) {
					rule = rules.get(++i);
					
					left = "";
					{
						for (int pos=0; pos<81; pos++)
							for (int num=0; num<9; num++) {
								if (mapping[pos][num] == rule.origin) {
									left = "(c " +(pos%9)+", r "+(pos/9)+") = "+(num+1);
									break;
								}
								if (mapping[pos][num]+1 == rule.origin) {
									left = "(c " +(pos%9)+", r "+(pos/9)+") != "+(num+1);
									break;
								}
							}
					}
					if (printDebug) System.out.print("  ^  " + left);
				}
				
				if (printDebug) System.out.println("  ->  " + right);
			}
		}
			
		// Create initial bit vectors
		BitSet[] bitSet = new BitSet[count];
		for (int i=0; i<count; i++) {
			bitSet[i] = new BitSet(count);
			bitSet[i].set(i);
			//bitSet[i].print(count);
		}
		
		// Iterate the process until no more improvement can be made
		boolean progress = true;
		while (progress) {
			progress = false;
			for (int i=0; i<rules.size(); i++) {
				Rule rule = rules.get(i);
				int destination = rule.destination;
				BitSet andExpression = bitSet[rule.origin].clone();
				while (rule.connected) {
					rule = rules.get(++i);
					if (destination != rule.destination)
						throw new Exception("destination != rule.getDestination()");
					andExpression.applyAnd(bitSet[rule.origin]);
				}
				
				if (!andExpression.isSubsetOf(bitSet[destination])) {
					//System.out.println("Hej ho!");
					progress = true;
					bitSet[destination].applyOr(andExpression);
				}
			}
		}
		
		//for (int i=0; i<count; i++)	bitSet[i].print(count);
		
		// Find all b[square][number] -> not b[square][number]
		progress = false;
		for (int i=0; i<81; i++)
			if (bCount[i] > 1) {
				for (int j=0; j<9; j++)
					if (b[i][j]) {
						int index = mapping[i][j];
						if (bitSet[index+1].index(index) != 0) {
							if (printDebug) System.out.println("(Row "+(i/9)+", Column "+(i%9)+") : Contains "+(j+1)+
									" implies that it doesn't contain "+(j+1)+".");
							erasePossibility(i, j);
							progress = true;
						}
						if (bitSet[index].index(index+1) != 0) {
							if (bitSet[index+1].index(index) != 0)
								throw new Exception("(a -> !a) ^ (!a -> a)");
							if (printDebug) System.out.println("(Row "+(i/9)+", Column "+(i%9)+") : Doesn't contains "+(j+1)+
									" implies that it contains "+(j+1)+".");
							setBox(i, j);
							progress = true;
						}
					}
			}
		
		return progress | simpleRound();
	}

	// 0<=number<=8
	private void setBox(int index, int number) {
		
		for (int i=0; i<3; i++) {
			int setIndex = ref[index][i];

			for (int j=0; j<9; j++) {
				int tmp = backRef[setIndex][j];
				if (tmp != index  &&  b[tmp][number]) {
					for (int k=0; k<3; k++)	
						counters[ref[tmp][k]][number]--;
					b[tmp][number] = false;
					--bCount[tmp];
				}
			}

			counterIsSolved[setIndex][number] = true;
		}
		if (!solvedPos[index]) {
			solvedPos[index] = true;
			numberPositionsUnsolved--;
		}
		
		for (int i=0; i<9; i++)
			if (b[index][i]  &&  i!=number) {
				for (int j=0; j<3; j++)
					--counters[ref[index][j]][i];
				b[index][i] = false;
				--bCount[index];
			}
	}
	
	public boolean solved() {
		return numberPositionsUnsolved == 0;
	}
	
	private void erasePossibility(int index, int number) throws Exception {
		if (bCount[index] < 2)
			throw new Exception("bCount must be larger than 1.");
		if (bCount[index] == 2) {
			for (int i=0; i<9; i++)
				if (b[index][i]  &&  i!=number) {
					setBox(index, i);
					break;
				}
		} else {
			for (int j=0; j<3; j++)
				--counters[ref[index][j]][number];
			b[index][number] = false;
			--bCount[index];
		}
	}
	
	private int leastNumber(int index) {
		for (int i=0; ; i++)
			if (b[index][i]) return i;
	}
	
	private int zZ(int c, int r) {
		return leastNumber(9*r+c)+1;
	}
	
	public void print() {
		if (numberPositionsUnsolved == 0) {
			System.out.println("----  SOLVED SUDOKU  ----");
			System.out.println("#########################");
			for (int i=0; i<9; i++) {
				if (i==3  ||  i==6)
					System.out.println("#-------+-------+-------#");
				System.out.println("# "+zZ(0,i)+" "+zZ(1,i)+" "+zZ(2,i)+" | "+zZ(3,i)+" "+
						zZ(4,i)+" "+zZ(5,i)+" | "+zZ(6,i)+" "+zZ(7,i)+" "+zZ(8,i)+" #");	
			}
			System.out.println("#########################");
			return;
		}
		
		for (int i=0; ; i++) {
			if (i%9 == 0) {
				System.out.println("###########################################");
			} else if (i%3 == 0) {
				System.out.println("#-------------#-------------#-------------#");
			}
			if (i == 27)
				break;
			
			for (int j=0; j<3; j++) {
				System.out.print("# ");
				for (int k=0; k<3; k++) {
					if (k != 0)
						System.out.print("|");
					for (int n=0; n<3; n++) {
						int number = 3*(i%3)+n;
						int index = 9*(i/3) + (3*j+k);
						if (solvedPos[index]) {
							System.out.print((number == 4 ? (char)(leastNumber(index)+'1') : ((number&1)==0 ? '¤' : ' ')));
						} else {
							System.out.print(b[index][number] ? (char)(number+'1') : ' ');
						}
					}
				}
				System.out.print(" ");
			}
			System.out.println("#");
		}
		
		// counters
		for (int i=0; i<27; i++) {
			if (9*(i/9) == i) {
				switch (i/9) {
				case 0:
					System.out.println("Horizontal:");
					break;
				case 1:
					System.out.println("Vertical:");
					break;
				case 2:
					System.out.println("Squares:");
					break;
				}
			}
				
			System.out.print("" + i + " : ");
			for (int j=0; j<9; j++)
				System.out.print("" + (j==0 ? "(" : ",  ") + (j+1) + " : " + counters[i][j]);
			System.out.println(")");
		}
	}
}
