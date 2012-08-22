package sudoku;

public class BitSet {
	
	private int[] data;
	private int size;
	
	public BitSet(int size) {
		this.size = (size+31) >> 5;
		data = new int[size];
		for (int i=0; i<size; i++)
			data[i] = 0;
	}

	/**
	 * @param i
	 * @return nonzero iff the bit is set
	 */
	public int index(int index) {
		return data[index >> 5] & (1 << (index & 31));
	}
	
	public void set(int index) {
		data[index >> 5] |= 1 << (index & 31);
	}
	
	public BitSet clone() {
		BitSet result = new BitSet(size << 5);
		for (int i=0; i<size; i++)
			result.data[i] = data[i];
		return result;
	}
	
	public BitSet and(BitSet bs) {
		BitSet result = bs.clone();
		for (int i=0; i<size; i++)
			result.data[i] &= data[i];
		return result;
	}
	
	public void applyAnd(BitSet bs) {
		for (int i=0; i<size; i++)
			data[i] &= bs.data[i];
	}
	
	public BitSet or(BitSet bs) {
		BitSet result = bs.clone();
		for (int i=0; i<size; i++)
			result.data[i] |= data[i];
		return result;
	}
	
	public void applyOr(BitSet bs) {
		for (int i=0; i<size; i++)
			data[i] |= bs.data[i];
	}
	
	public boolean equal(BitSet bs) {
		for (int i=0; i<size; i++)
			if (data[i] != bs.data[i])
				return false;
		return true;
	}
	
	public boolean isSubsetOf(BitSet bs) {
		for (int i=0; i<size; i++)
			if ((data[i] & bs.data[i]) != data[i])
				return false;
		return true;
	}
	
	public void print(int length) {
		char[] s = new char[length];
		for (int i=0; i<length; i++)
			s[i] = index(i)==0 ? '0' : '1';
		System.out.println(new String(s));
	}
}
