public enum Square{
	E(2), //empty square
	X(5), //player1 mark
	O(11),  //player2 mark
	D(17); //draw mark
	
	public final int value;
	public final String upper;
	
	Square(int val){
		value = val;
		switch(val) {
		case 2:
			upper = "E";
			break;
		case 5:
			upper = "X";
			break;
		case 11:
			upper = "O";
			break;
		case 17:
			upper = "D";
			break;
		default:
			upper = "?";
			break;
		}
	}
	
	public char toChar() {
		return upper.charAt(0);
	}
	
	@Override
	public String toString() {
		return upper;
	}
}