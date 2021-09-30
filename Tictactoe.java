import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

class Tictactoe {
	public Square[] board; //storing the board and marks
	public int turnsLeft;
	public Integer hash = null;
	public Tictactoe[] savedMoves;
	public byte[] legalMoves;
	
	Tictactoe(){
		Square[] board = new Square[9];
		for(byte index = 0; index < 9; index++) {
			board[index] = Square.E;
		}
		this.board = board;
		this.turnsLeft = 9;
		this.savedMoves = new Tictactoe[9 * 2]; //deal with both players possible moves
		this.hash();
	}
	
	Tictactoe(Tictactoe old){
		Square[] oldBoard = old.board;
		Square[] board = new Square[9];
		for(byte index = 0; index < 9; index++) {
			board[index] = oldBoard[index];
		}
		this.board = board;
		this.turnsLeft = old.turnsLeft;
		this.savedMoves = old.savedMoves;
		this.legalMoves = old.legalMoves;
		this.hash = old.hash;
	}
	
	public byte[] legalMoves(){
		if(legalMoves != null) {return legalMoves;}
		Square[] board = this.board; //referncing local is faster than instance
		byte[] legalMoves = new byte[turnsLeft];
		int pointer = 0;
		for(byte index = 0; index < 9; index++) {
			if(board[index] == Square.E) {
				legalMoves[pointer++] = index;
			}
		}
		this.legalMoves = legalMoves;
		return legalMoves;
	}
	
	public Tictactoe turn(byte move, Square currentPlayer){
		int player = currentPlayer == Square.X?0:1;
		if(savedMoves[move + 9*player]!=null) {return savedMoves[move + 9*player];}
		Tictactoe next = new Tictactoe(this);
		next.board[move] = currentPlayer;
		next.turnsLeft = turnsLeft - 1;
		next.savedMoves = new Tictactoe[9 * 2];
		next.legalMoves = null;
		next.hash = null;
		next.hash();
		savedMoves[move + 9*player] = next;
		return next;
	}
	
	public Square winner() {
		Square[] board = this.board;
		
		//checking top diagonal
		Square[] tDia = new Square[]{board[0],board[4],board[8]};
		if(tDia[0] != Square.E && tDia[0] == tDia[1] && tDia[1] == tDia[2]) {
			return tDia[0];
		}
		//checking low diagonal
		Square[] lDia = new Square[]{board[2],board[4],board[6]};
		if(lDia[0] != Square.E && lDia[0] == lDia[1] && lDia[1] == lDia[2]) {
			return lDia[0];
		}
		
		for(int i = 0; i < 3; i++) {
			//checking rows
			Square[] row = new Square[]{board[3*i],board[3*i+1],board[3*i+2]};
			if(row[0] != Square.E && row[0] == row[1] && row[1] == row[2]) {
				return row[0];
			}
			//checking columns
			Square[] col = new Square[]{board[i],board[3 + i],board[6 + i]};
			if(col[0] != Square.E && col[0] == col[1] && col[1] == col[2]) {
				return col[0];
			}
		}
		
		//checking draw
		if(turnsLeft == 0) {
			return Square.D;
		}
		
		return Square.E;
	}
	
	public int hash() {
		Square[] board = this.board; //referncing local is faster than instance
		int hash = 0;
		for(byte index = 0; index < 9; index++) {
			int val = board[index].value; //the squares value
			hash = hash * 13 + val;
		}
		return this.hash = hash;
	}
	
	@Override
	public String toString() {
		Square[] board = this.board; //referncing local is faster than instance
		String out = "";
		for(int r = 0; r < 3; r++) {
			for(int c = 0; c < 3; c++) {
				out+=board[3*r+c];
				if(c != 2) {
					out+="|";
				}
			}
			out +="\n";
			if(r != 2) {
				out+="-----\n";
			}
		}
		return out;
	}
	
	public static void main(String[] args){
		Random rand = new Random();
		int N = 100000;
		Map<Integer, String> hash = new HashMap<>();
		Set<String> vals = new HashSet<String>();
		int bad = 0;
		Tictactoe empty = new Tictactoe();
		for(double i = 0; i < N; i++) {
			Tictactoe ttt = empty;
			hash.put(ttt.hash(),ttt.toString());
			vals.add(ttt.toString());
			while(ttt.winner() == Square.E) {
				byte[] moves = ttt.legalMoves();
				ttt = ttt.turn(moves[rand.nextInt(moves.length)],ttt.turnsLeft%2==0?Square.X:Square.O);
				vals.add(ttt.toString());
				int h = ttt.hash();
				if(hash.containsKey(h) && !hash.get(h).equals(ttt.toString())){
					bad++;
					System.out.println("Bad: \n" + hash.get(h) + "\n" + ttt.toString());
				}else {
					hash.put(h,ttt.toString());
				}
				if(i % 1000 == 0) {
					System.out.println(hash.get(h) + "\n" + h);
				}
			}
		}
		System.out.println("Number of hashes there should be: " + vals.size() + "\n"
						+ "How many different hashes there are: " + hash.size() + "\n"
						+ "How many conflicts there were: " + bad);
	}
	
	/* test speed
	public static void main(String[] args){
		Random rand = new Random();
		long time = System.nanoTime();
		int games = 10000000;
		int turns = 0;
		for(double i = 0; i < games; i++) {
			Tictactoe ttt = new Tictactoe();
			Square player = Square.X;
			while(ttt.winner() == Square.E) {
				byte[] moves = ttt.legalMoves();
				turns++;
				ttt = ttt.turn(moves[rand.nextInt(moves.length)],player=(player==Square.X?Square.O:Square.X));
				ttt.hash();
			}
			
		}
		System.out.println((System.nanoTime() - time)/turns);
	}
	 */
	
	
	/* test hash
	public static void main(String[] args){
		Random rand = new Random();
		int N = 100000;
		Map<Integer, String> hash = new HashMap<>();
		Set<String> vals = new HashSet<String>();
		int bad = 0;
		Tictactoe empty = new Tictactoe();
		for(double i = 0; i < N; i++) {
			Tictactoe ttt = empty;
			hash.put(ttt.hash(),ttt.toString());
			vals.add(ttt.toString());
			while(ttt.winner() == Square.E) {
				byte[] moves = ttt.legalMoves();
				ttt = ttt.turn(moves[rand.nextInt(moves.length)],ttt.turnsLeft%2==0?Square.X:Square.O);
				vals.add(ttt.toString());
				int h = ttt.hash();
				if(hash.containsKey(h) && !hash.get(h).equals(ttt.toString())){
					bad++;
					System.out.println("Bad: \n" + hash.get(h) + "\n" + ttt.toString());
				}else {
					hash.put(h,ttt.toString());
				}
				if(i % 1000 == 0) {
					System.out.println(hash.get(h) + "\n" + h);
				}
			}
		}
		System.out.println("Number of hashes there should be: " + vals.size() + "\n"
						+ "How many different hashes there are: " + hash.size() + "\n"
						+ "How many conflicts there were: " + bad);
	}
	*/
}
