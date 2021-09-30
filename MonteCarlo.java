import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;


class MonteCarlo {
	public static Map<Long,long[]> hashToStats = new HashMap<Long,long[]>(); //{hash: [wins, plays],...}
	public int games;
	public int maxMoves;
	public double C;
	
	MonteCarlo(int games, int maxMoves, double C){
		//getting the options
		this.games = games;
		this.maxMoves = maxMoves;
		this.C = C;
	}
	
	public byte getMove(UltimateTictactoe board){
		byte[] legalMoves = board.legalMoves();
		byte numOfMoves = (byte) legalMoves.length;
		
		//dealing with easy choices
		if(legalMoves.length == 0) {
			return -1;
		}
		if(legalMoves.length == 1) {
			return legalMoves[0];
		}
		
		for(int i = 0; i < games; i++) {
			run(board);
		}
		
		//sets up an map of all the possible next states to their respective move to get there
		long[] hashes = new long[legalMoves.length];
		for(byte index = 0; index < numOfMoves; index++) {
			byte move = legalMoves[index];
			hashes[index] = new UltimateTictactoe(board,move).hash();
		}
		
		//getting the highest winrate move
		double highest = 0;
		int highIndex = -1;
		for(byte index = 0; index < numOfMoves; index++) {
			long hash = hashes[index];
			long[] winPlay = hashToStats.get(hash);
			if(winPlay == null) {continue;}
			long win = winPlay[0];
			long play = winPlay[1] == 0?1:winPlay[1];
			double winPercent = (double)win / play;
			if(winPercent > highest) {
				highest = winPercent;
				highIndex = index;
			}
		}
		
		if(highIndex == -1) {
			//default move is a moving through all the choices until there is one that doesnt give them access to the whole board
			Square[] state = board.state;
			highIndex = 0;
			for(byte index = 1; index < legalMoves.length; index++) {
				byte move = legalMoves[index];
				byte row = (byte)(move / 9);
				byte col = (byte)(move % 9);
				byte bigIndex = (byte)(3*(row/3) + (col/3));
				UltimateTictactoe next = new UltimateTictactoe(board,legalMoves[index]);
				if(next.state[bigIndex] == board.currentPlayer) {
					highIndex = index;
					break;
				}
				if(state[bigIndex] == Square.E && hashToStats.get(hashes[index]) == null) {
					highIndex = index;
				}
			}
		}
		
		//returning the highest winrate move
		byte highMove = legalMoves[highIndex];
		return highMove;
	}
	
	public void run(UltimateTictactoe b){
		Set<UltimateTictactoe> visited = new HashSet<UltimateTictactoe>(); 
		Stack<UltimateTictactoe> stack = new Stack<UltimateTictactoe>();
		stack.add(b);
		Random rand = new Random();
		Square winner = Square.E;
		
		for(int m = 0; m < maxMoves; m++){
			UltimateTictactoe board = stack.pop();
			stack.add(board);
			byte[] legalMoves = board.legalMoves();
			byte numOfMoves = (byte)legalMoves.length;
			boolean included = true;
			
			//sets up a map of all the possible next states to their respective move to get there and checking if its happened before
			long[] hashes = new long[numOfMoves];
			UltimateTictactoe[] boards = new UltimateTictactoe[numOfMoves];
			int logSum = 0; //also getting the log sum for UCB1
			for(byte index = 0; index < numOfMoves; index++){
				byte move = legalMoves[index];
				UltimateTictactoe newBoard = new UltimateTictactoe(board,move);
				boards[index] = newBoard;
				long hash = newBoard.hash();
				hashes[index] = hash;
				//checking if this state has happened before
				if(hashToStats.get(hash) == null) {
						included = false;
				}else if(included){
					logSum += hashToStats.get(hash)[1];
				}
			}
			
			//UCB1 decides move
			byte highIndex = -1;
			if(included) {
				//finding the highest move
				double logTotal = Math.log(logSum);
				double highest = 0;
				for(byte index = 0; index < numOfMoves; index++){
					long hash = hashes[index];
					long[] stats = hashToStats.get(hash);
					long win = stats[0];
					long play = stats[0] == 0?1:stats[1];
					double value = (double)win / play + C * Math.sqrt(logTotal / play);
					if(value > highest) {
						highest = value;
						highIndex = index;
					}
				}
			}
			if(highIndex == -1) {
				//default move is a moving through all the choices until there is one that doesnt give them access to the whole board
				Square[] state = board.state;
				highIndex = 0;
				for(byte index = 0; index < legalMoves.length; index++) {
					byte move = legalMoves[index];
					byte row = (byte)(move / 9);
					byte col = (byte)(move % 9);
					byte bigIndex = (byte)(3*(row/3) + (col/3));
					if(boards[index].state[bigIndex] == board.currentPlayer) {
						highIndex = index;
						break;
					}
					if(state[bigIndex] == Square.E && hashToStats.get(hashes[index]) == null) {
						highIndex = index;
					}
				}
			}
			
			//getting the new board
			board = boards[highIndex];
			
			visited.add(board);
			
			winner = board.winner();
			if(winner != Square.E) {break;}
			stack.add(board);
		}
		
		for(UltimateTictactoe board: visited){
			long key = board.hash();
			long[] stats = hashToStats.get(key);
			if(stats == null) {
				stats = new long[]{0,0};
				hashToStats.put(key, stats);
			}
			stats[1]+=2; //play + 2 so that draw can be +1 and it can be long instead of double
			if(winner == Square.D) {
				stats[0]+=1;
			}else if(winner == board.lastPlayer) {
				stats[0]+=2;
			}
		}
	}
	
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		Scanner in = new Scanner(System.in);
		UltimateTictactoe board = new UltimateTictactoe();
		MonteCarlo mc = new MonteCarlo(10000,50000,1);
		long turns = 1;
		long time = System.currentTimeMillis();
		while(board.winner() == Square.E) {
			byte move = mc.getMove(board);
			board = board.turn(move);
			System.out.println(board.toString());
			System.out.println(move/9 + " " + move%9 + "\n");
			turns++;
		}
		long elapsed = System.currentTimeMillis() - time;
		System.out.println(elapsed / turns);
		
		//cycling through the map and writing it to a file
		Set<Long> keys = hashToStats.keySet();
		BufferedWriter out = new BufferedWriter(new FileWriter(new File("mapString.txt")));
		for(long key :keys) {
			long[] val = hashToStats.get(key);
			out.append(String.format("%d:%d:%s\n",val[0],val[1],key));
		}
		out.close();
	}
	
	/*test speed / training
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		Scanner in = new Scanner(System.in);
		UltimateTictactoe board = new UltimateTictactoe();
		MonteCarlo mc = new MonteCarlo(1000,50000,.01);
		long turns = 1;
		long time = System.currentTimeMillis();
		while(board.winner() == Square.E) {
			byte move = mc.getMove(board);
			board = board.turn(move);
			System.out.println(board.toString());
			System.out.println(move/9 + " " + move%9 + "\n");
			turns++;
		}
		long elapsed = System.currentTimeMillis() - time;
		System.out.println(elapsed / turns);
		
		//cycling through the map and writing it to a file
		Set<String> keys = hashToStats.keySet();
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(path)));
		for(String key :keys) {
			long[] val = hashToStats.get(key);
			out.append(String.format("%d:%d:%s\n",val[0],val[1],key));
		}
		out.close();
	}
	*/
	
	/* play against it
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		Scanner in = new Scanner(System.in);
		UltimateTictactoe board = new UltimateTictactoe();
        MonteCarlo mc = new MonteCarlo(500,50000,1);
		int turn = 1;
		System.out.println(board.toString() + "\n");
		while(board.winner() == Square.E) {
			if(turn++ % 2 == 1) {
				byte myMove = (byte) (in.nextByte() * 9 + in.nextByte());;
				board = board.turn(myMove);
				System.out.println(myMove / 9 + " " + myMove % 9);
			}else {
				byte move = mc.getMove(board);
				board = board.turn(move);
				System.out.println(move / 9 + " " + move % 9);
			}
			System.out.println("\n" + board.toString());
		}
	}
	*/
	
}
