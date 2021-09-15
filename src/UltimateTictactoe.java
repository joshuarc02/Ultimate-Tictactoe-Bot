import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

class UltimateTictactoe {	
	public static final char[] base = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
	public static final Tictactoe blank = new Tictactoe();
	public Tictactoe[] board; //storing the board and marks
	public boolean[] active;
	public Square[] state;
	public Square lastPlayer;
	public Square currentPlayer;
	public int turnsLeft;
	public Long hash;
	public byte[] legalMoves;
	
	UltimateTictactoe(){
		Tictactoe[] board = new Tictactoe[9];
		boolean[] active = new boolean[9];
		Square[] state = new Square[9];
		for(byte index = 0; index < 9; index++) {
			board[index] = blank;
			active[index] = true;
			state[index] = Square.E;
		}
		this.board = board;
		this.active = active;
		this.state = state;
		this.currentPlayer = Square.X;
		this.turnsLeft = 81;
		hash();
	}
	
	UltimateTictactoe(UltimateTictactoe old){
		Tictactoe[] oldBoard = old.board;
		boolean[] oldActive = old.active;
		Square[] oldState = old.state;
		Tictactoe[] board = new Tictactoe[9];
		boolean[] active = new boolean[9];
		Square[] state = new Square[9];
		for(byte index = 0; index < 9; index++) {
			board[index] = oldBoard[index];
			active[index] = oldActive[index];
			state[index] = oldState[index];
		}
		this.board = board;
		this.active = active;
		this.state = state;
		this.lastPlayer = old.lastPlayer;
		this.currentPlayer = old.currentPlayer;
		this.turnsLeft = old.turnsLeft;
		this.legalMoves = old.legalMoves;
		this.hash = old.hash;
	}
	UltimateTictactoe(UltimateTictactoe old,byte move){
		Tictactoe[] oldBoard = old.board;
		boolean[] oldActive = old.active;
		Square[] oldState = old.state;
		Tictactoe[] board = new Tictactoe[9];
		boolean[] active = new boolean[9];
		Square[] state = new Square[9];
		for(byte index = 0; index < 9; index++) {
			board[index] = oldBoard[index];
			active[index] = oldActive[index];
			state[index] = oldState[index];
		}
		this.board = board;
		this.active = active;
		this.state = state;
		this.turnsLeft = old.turnsLeft - 1;
		this.lastPlayer = old.currentPlayer; //dont need for codinGame
		this.currentPlayer = old.currentPlayer == Square.X? Square.O : Square.X;
		byte row = (byte)(move / 9);
		byte col = (byte)(move % 9);
		byte bigIndex = (byte)(3*(row/3) + (col/3));
		byte smlRow = (byte)(row % 3);
		byte smlCol = (byte)(move % 3);
		byte smlIndex = (byte)(3*smlRow + smlCol);
		//updating the sml board with the move
		Tictactoe smlBoard = board[bigIndex].turn(smlIndex, currentPlayer);
		board[bigIndex] = smlBoard;
		state[bigIndex] = smlBoard.winner();
		//updating which boards are active
		if(state[smlIndex] == Square.E) {
			for(int index = 0; index < 9; index++) {
				//setting all of them to false
				active[index] = false;
			}
			//activating one board
			active[smlIndex] = true;
		}else {
			for(int index = 0; index < 9; index++) {
				//setting a board to active if its not completed yet
				active[index] = state[index] == Square.E;
			}
		}

	}
	
	public byte[] legalMoves(){
		if(legalMoves != null) {return legalMoves;}
		Tictactoe[] board = this.board; //referncing local is faster than instance
		byte[] legalMoves = new byte[turnsLeft];
		byte pointer = 0;
		for(byte index = 0; index < 9; index++) {
			if(active[index]) {
				byte indexRow = (byte) (index / 3);
				byte indexCol = (byte) (index % 3);
				byte[] bMoves = board[index].legalMoves();
				for(byte move: bMoves) {
					byte row = (byte) ((3 * indexRow + move / 3));
					byte col = (byte) ((3 * indexCol + move % 3));
					legalMoves[pointer++] = (byte) (9*row + col);
				}
			}
		}
		legalMoves = Arrays.copyOfRange(legalMoves,0,pointer);
		this.legalMoves = legalMoves;
		return legalMoves;
	}
	
	public UltimateTictactoe turn(byte move){
		//creating the new board
		UltimateTictactoe next = new UltimateTictactoe(this,move);
		return next;
	}
	
	public Square winner() {
		Square[] state = this.state;
		//checking top diagonal
		Square[] tDia = new Square[]{state[0],state[4],state[8]};
		if(tDia[0] != Square.E && tDia[0] == tDia[1] && tDia[1] == tDia[2]) {
			return tDia[0];
		}
		//checking low diagonal
		Square[] lDia = new Square[]{state[2],state[4],state[6]};
		if(lDia[0] != Square.E && lDia[0] == lDia[1] && lDia[1] == lDia[2]) {
			return lDia[0];
		}
		
		for(int i = 0; i < 3; i++) {
			//checking rows
			Square[] row = new Square[]{state[3*i],state[3*i+1],state[3*i+2]};
			if(row[0] != Square.E && row[0] == row[1] && row[1] == row[2]) {
				return row[0];
			}
			//checking columns
			Square[] col = new Square[]{state[i],state[3+i],state[6+i]};
			if(col[0] != Square.E && col[0] == col[1] && col[1] == col[2]) {
				return col[0];
			}
		}
		
		//checking for filled board
		boolean filled = true;
		int x = 0;
		int o = 0;
		for(byte index = 0; index < 9; index++) {
			if(state[index] == Square.E) {
				filled=false;
				break;
			}else if(state[index] == Square.X) {
				x++;
			}else if(state[index] == Square.O) {
				o++;
			}
		}
		
		if(filled){
			if(x == o) {
				return Square.D;
			}else if(x > o){
				return Square.X;
			}else {
				return Square.O;
			}
		}else {
			return Square.E;
		}
	}
	
	public long hash(){
		if(hash != null) {return hash;}
		Tictactoe[] board = this.board; //referncing local is faster than instance
		boolean[] active = this.active;
		Square[] state = this.state;
		long hash = (long) currentPlayer.value + 7;
		for(byte index = 0; index < 9; index++) {
			Tictactoe smlBoard = board[index];
			long val = state[index].value + (active[index]?1:0);
			hash = hash * (val + index + 101) + smlBoard.hash;
		}
//		int baseNum = base.length;
//		StringBuilder hashString = new StringBuilder();
//		if(hash < 0) {
//			hashString.append("~");
//			hash*=-1;
//		}
//		while(hash!=0) {
//			hashString.append(base[(int) (hash%baseNum)]);
//			hash/=baseNum;
//		}
//		return this.hashString = hashString.toString();
		return hash;
	}
	
	@Override
	public String toString() {
		Tictactoe[] board = this.board; //referncing local is faster than instance
		boolean[] active = this.active;
		Square[] state = this.state;
		StringBuilder big = new StringBuilder();
		StringBuilder sml = new StringBuilder();
		StringBuilder[] lines = new StringBuilder[3];
		//getting the huge board
		for(int r = 0; r < 3; r++) {
			for(int i = 0; i < 3; i++) {
				lines[i] = new StringBuilder();
			}
			for(int c = 0; c < 3; c++) {
				Tictactoe smlBoard = board[3*r+c];
				Square[] smlSquares = smlBoard.board;
				for(int r2 = 0; r2 < 3; r2++) {
					for(int c2 = 0; c2 < 3; c2++) {
						lines[r2].append(smlSquares[3*r2 + c2].toString());
					}
					if(c != 2) {
						lines[r2].append(" | ");
					}
				}
				
				//small board with capilization for active
				char square = state[3*r+c].toChar();
				if(!active[3*r+c]) {
					square = (char) (square+32);
					
				}
				sml.append(square);
				if(c != 2) {
					sml.append("|");
				}
			}
			//big line stuff
			for(StringBuilder line: lines) {
				big.append(line + "\n");
			}
			if(r != 2) {
				big.append("----------------\n");
			}
			//sml line stuff
			sml.append("\n");
			if(r != 2) {
				sml.append("-----\n");
			}
		}
		return "Last Player: " + lastPlayer + "\n" + big + "\n" + sml;
	}
	
	public static void main(String[] args){
		Random rand = new Random();
		int runs = 1;
		int games = 100000;
		int hashes = Integer.MAX_VALUE;
		int valsSum = 0;
		int hashSum = 0;
		for(int run = 1; run <= runs; run++) {
			Map<Long, String> hash = new HashMap<>();
			Set<String> vals = new HashSet<String>();
			UltimateTictactoe start = new UltimateTictactoe();
			hash.put(start.hash(),start.toString());
			vals.add(start.toString());
			int game = 0;
			while(vals.size() < hashes && game++ < games) {
				UltimateTictactoe ttt = start;
				while(vals.size() < hashes && ttt.winner() == Square.E) {
					byte[] moves = ttt.legalMoves();
					ttt = ttt.turn(moves[rand.nextInt(moves.length)]);
					String s = ttt.toString();
					long h = ttt.hash();
					if(vals.add(s) && hash.containsKey(h) && !hash.get(h).equals(s)){
						s = "\n" + h + "\n" + hash.get(h) + "\n" + s;
						System.out.println(s);
					}else {
						hash.put(h,s);
					}
				}
//				if(game % 10000 == 0) {
//					System.out.println("Game " + game + " completed!\nvals size: " + vals.size() + "\nhash size: " + hash.size());
//				}
				
			}
			valsSum+=vals.size();
			hashSum+=hash.size();
			System.out.println("Run " + run + " done! Difference: " + (vals.size() - hash.size()));
		}
		
		int valsAvg = valsSum / runs;
		int hashAvg = hashSum / runs;
		
		System.out.println("Number of hashes there should be: " + valsAvg + "\n"
				+ "How many different hashes there are: " + hashAvg + "\n"
				+ "How many less hashes:" + (valsAvg - hashAvg) + "\n"
				+ "Percent of total hashes: " + ((valsAvg - hashAvg) / (double)valsAvg * 100));
	}
	
	/* test speed
	public static void main(String[] args){
		Random rand = new Random();
		long time = System.nanoTime();
		int games = 200000;
		int turns = 0;
		for(double i = 0; i < games; i++) {
			UltimateTictactoe ttt = new UltimateTictactoe();
			byte[] moves = ttt.legalMoves();
			while(ttt.winner() == Square.E) {
				turns++;
				ttt = ttt.turn(moves[rand.nextInt(moves.length)]);
				moves = ttt.legalMoves();
			}
		}
		System.out.println((System.nanoTime() - time)/turns);
	}
	 */
	
	
	/* test hash
	public static void main(String[] args){
		Random rand = new Random();
		int runs = 1;
		int games = 10000;
		int hashes = Integer.MAX_VALUE;
		int valsSum = 0;
		int hashSum = 0;
		for(int run = 1; run <= runs; run++) {
			Map<String, String> hash = new HashMap<>();
			Set<String> vals = new HashSet<String>();
			UltimateTictactoe ttt = new UltimateTictactoe();
			hash.put(ttt.hash(),ttt.toString());
			vals.add(ttt.toString());
			int game = 0;
			while(vals.size() < hashes && game++ < games) {
				ttt = new UltimateTictactoe();
				while(vals.size() < hashes && ttt.winner() == Square.E) {
					byte[] moves = ttt.legalMoves();
					ttt = ttt.turn(moves[rand.nextInt(moves.length)]);
					String s = ttt.toString();
					String h = ttt.hash();
					vals.add(s);
					if(hash.containsKey(h) && !hash.get(h).equals(s)){
						s = "\n" + h + "\n" + hash.get(h) + "\n" + s;
						System.out.println(s);
					}else {
						hash.put(h,s);
					}
				}
//				if(game % 10000 == 0) {
//					System.out.println("Game " + game + " completed!\nvals size: " + vals.size() + "\nhash size: " + hash.size());
//				}
				
			}
			valsSum+=vals.size();
			hashSum+=hash.size();
			System.out.println("Run " + run + " done! Difference: " + (vals.size() - hash.size()));
		}
		
		int valsAvg = valsSum / runs;
		int hashAvg = hashSum / runs;
		
		System.out.println("Number of hashes there should be: " + valsAvg + "\n"
				+ "How many different hashes there are: " + hashAvg + "\n"
				+ "How many less hashes:" + (valsAvg - hashAvg) + "\n"
				+ "Percent of total hashes: " + ((valsAvg - hashAvg) / (double)valsAvg * 100));
	}
	*/
	
	/*test long to base
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		Set<String> visited = new HashSet<String>();
		for(long v = 0; v < 500000; v++) {
			long val = v;
			String base = "!#$%&'()*+,-./0123456789:;<=>@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`abcdefghijklmnopqrstuvwxyz{|}~ ¡¢£¤¥§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏĞÑÒÓÔÕÖ×ØÙÚÛÜİŞßàáâãäåæçèéêëìíîïğñòóôõö÷øùúûüışÿŒœŠšŸƒˆ˜–—‘’‚“”„†‡•…‰‹›€™";
			int baseNum = base.length();
			StringBuilder hashString = new StringBuilder();
			if(val < 0) {
				hashString.append("¦");
				val*=-1;
			}
			while(val!=0) {
				hashString.append(base.charAt((int) (val%baseNum)));
				val/=baseNum;
			}
			String hs = hashString.toString();
			if(!visited.add(hs)) {
				System.out.println(hs + " " + v + " :(");
			}
		}
	}
	 */
	
	/* getting all the possible characters to be used in the base
	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder();
		for(int i = 32; i<10000; i++) {
			sb.append((char)i);
		}
		System.out.println(sb.toString());
	}
	*/
	
	/*test hash with paths
	public static void main(String[] args){
		Random rand = new Random();
		Map<String, String> hash = new HashMap<>();
		UltimateTictactoe ttt = new UltimateTictactoe();
		Queue<UltimateTictactoe> queue = new LinkedList<UltimateTictactoe>();
		hash.put(ttt.hash(),ttt.toString());
		queue.add(ttt);
		int hashes = 1;
		while(!queue.isEmpty()) {
			ttt = queue.remove();
			//randomizing it so it explores some more
			while(rand.nextInt(2) == 0) {
				queue.add(ttt);
				ttt = queue.remove();
					
			}
			//adding all the moves to the bfs
			byte[] moves = ttt.legalMoves();
			for(byte move: moves) {
				UltimateTictactoe next = ttt.turn(move);
				if(next.winner()==Square.E) {
					queue.add(next);
				}
				//getting the info about ttt
				String s = next.toString();
				String h = next.hash();
				//checking if the hash exists with a different board
				if(hash.containsKey(h) && !hash.get(h).equals(s)){
					s = "\n" + h + "\n" + hash.get(h) + "\n" + s;
					System.out.println(s);
					break;
				}else 
				//adding the unique hash to the map
				if(!hash.containsKey(h)){
					hash.put(h,s);
					hashes++;
				}
				if(hashes % 1000 == 0) {
					System.out.println("How many hashes there are: " + hash.size() + "\n" +
							"How many hashes there should be: " + hashes + "\n"+
							"Difference: " + (hashes - hash.size()) + "\n");
				}
			}
			
		}
		

		System.out.println("How many hashes there are: " + hash.size() + "\n" +
							"How many hashes there should be: " + hashes + "\n"+
							"Difference: " + (hashes - hash.size()));
	}
	*/
	
	/* step search
	public static void main(String[] args){
		long time = System.currentTimeMillis();
		Random rand = new Random();
		UltimateTictactoe ttt = new UltimateTictactoe();
		Queue<UltimateTictactoe> search = new LinkedList<UltimateTictactoe>();
		search.add(ttt);
		double turns = 1;
		while(!search.isEmpty()) {
			ttt = search.remove();
			ttt.hash();
			//adding all the moves to the bfs
			byte[] moves = ttt.legalMoves();
			Square[] state = ttt.state;
			for(byte move: moves) {
				turns++;
				byte row = (byte)(move / 9);
				byte col = (byte)(move % 9);
				byte bigIndex = (byte)(3*(row/3) + (col/3));
				if(state[bigIndex] != Square.E) {continue;}
				UltimateTictactoe next = ttt.turn(move);
				if(next.winner()==Square.E) {
					search.add(next);
				}
			}
			if(turns % 10000 == 0) {
				System.out.println("How long it's taken to search " + turns + " turns: "+ (System.currentTimeMillis() - time));
			}
		}
	}
	*/
}
