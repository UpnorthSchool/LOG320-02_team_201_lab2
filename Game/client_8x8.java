package Game;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import Game.Decoder.intToString;
import Game.Decoder.stringToInt;

class Client {


	static Mark adversaire;
	public static void main(String[] args) {
		String host = "localhost";
		int port = 8888;
		if (args.length >= 1 && args[0] != null && !args[0].isBlank()) {
			host = args[0];
		}
		if (args.length >= 2) {
			try {
				port = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				System.out.println("Port invalide, utilisation du port par défaut 8888.");
			}
		}
		System.out.println("Connexion à " + host + ":" + port);
		Socket MyClient;
		BufferedInputStream input;
		BufferedOutputStream output;
		int[][] board = new int[8][8];
		//from tictactoe :
		Board board8x8 = new Board();
		//decodeur et encoder de move
		stringToInt decoderEntrer = new stringToInt();
		System.out.println("Svp entrer les cases que vous voulez jouer comme suit xy-x2y2, exemple : A1-b2, c2-d3,d3-c2)");
		Scanner myObj = new Scanner(System.in);
		CPUPlayer aiMachine = new CPUPlayer(null);

		Move newMoveHuman = new Move();
		//tant que min value game is ongoing;
		int scoreKeeper = Integer.MIN_VALUE;

		try {
			MyClient = new Socket("localhost", 8888);

			input = new BufferedInputStream(MyClient.getInputStream());
			output = new BufferedOutputStream(MyClient.getOutputStream());
			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
			while (1 == 1) {
				char cmd = 0;

				cmd = (char) input.read();
				System.out.println(cmd);
				// Debut de la partie en joueur rouge
				if (cmd == '1') {
					byte[] aBuffer = new byte[1024];
					
					int size = input.available();
					// System.out.println("size " + size);
					input.read(aBuffer, 0, size);
					String s = new String(aBuffer).trim();
					System.out.println(s);
					String[] boardValues;
					boardValues = s.split(" ");
					int x = 0, y = 0;
					for (int i = 0; i < boardValues.length; i++) {
						board[x][y] = Integer.parseInt(boardValues[i]);
						if(Integer.parseInt(boardValues[i]) == 2)
						{
							board8x8.getBoard()[7-x][7-y] = Mark.BLACK;
							board8x8.setBlackPieceCounter(board8x8.getBlackPieceCounter() + 1);
						}

						if(Integer.parseInt(boardValues[i]) == 4)
						{
							board8x8.getBoard()[7-x][7-y] = Mark.RED;
							board8x8.setRedPieceCounter(board8x8.getRedPieceCounter() + 1);
						}
						x++;
						if (x == 8) {
							x = 0;
							y++;
						}
					}

					// AI algorythme
					//test to see move list here !! 
					//board8x8.display(board8x8.getMoveList(Mark.RED));

					//marking the ai team color
					aiMachine 	= new CPUPlayer(Mark.RED);
					adversaire	= Mark.BLACK;
					//
					System.out.println("Nouvelle partie! Vous jouer blanc, entrez votre premier coup : ");
					//code pour algorythme ici 
					//if human : 
					//String move = null;
					//move = console.readLine();

					//cpu:
					String move = cpuMovePlay(board8x8, aiMachine);
					output.write(move.getBytes(), 0, move.length());
					output.flush();


				}
				// Debut de la partie en joueur Noir
				if (cmd == '2') {
					System.out.println("Nouvelle partie! Vous jouer noir, attendez le coup des blancs");
					byte[] aBuffer = new byte[1024];

					int size = input.available();
					// System.out.println("size " + size);
					input.read(aBuffer, 0, size);
					String s = new String(aBuffer).trim();
					System.out.println(s);
					String[] boardValues;
					boardValues = s.split(" ");
					int x = 0, y = 0;
					for (int i = 0; i < boardValues.length; i++) {
						board[x][y] = Integer.parseInt(boardValues[i]);
						if(Integer.parseInt(boardValues[i]) == 2)
						{
							board8x8.getBoard()[7-x][7-y] = Mark.BLACK;
							board8x8.setBlackPieceCounter(board8x8.getBlackPieceCounter() + 1);
						}

						if(Integer.parseInt(boardValues[i]) == 4)
						{
							board8x8.getBoard()[7-x][7-y] = Mark.RED;
							board8x8.setRedPieceCounter(board8x8.getRedPieceCounter() + 1);
						}
						x++;
						if (x == 8) {
							x = 0;
							y++;
						}
					}
					aiMachine = new CPUPlayer(Mark.BLACK);
					adversaire = Mark.RED;
				}
				// Le serveur demande le prochain coup
				// Le message contient aussi le dernier coup joue.
				if (cmd == '3') {
					byte[] aBuffer = new byte[16];

					int size = input.available();
					System.out.println("size :" + size);
					input.read(aBuffer, 0, size);

					String s = new String(aBuffer);
					System.out.println("Dernier coup :" + s);
					//
					//trim space and play the move on our board to track
					board8x8.play(decoderEntrer.decode(s), adversaire);
					System.out.println("Entrez votre coup : ");
					String move = cpuMovePlay(board8x8, aiMachine);
					System.out.println("My ai playing the move : " + move);
					//move = console.readLine();
					output.write(move.getBytes(), 0, move.length());
					output.flush();
				}
				// Le dernier coup est invalide
				if (cmd == '4') {
					System.out.println("Coup invalide, entrez un nouveau coup : ");
					String move = null;
					move = console.readLine();
					output.write(move.getBytes(), 0, move.length());
					output.flush();
				}
				// La partie est terminée
				if (cmd == '5') {
					byte[] aBuffer = new byte[16];
					int size = input.available();
					input.read(aBuffer, 0, size);
					String s = new String(aBuffer);
					System.out.println("Partie Terminé. Le dernier coup joué est: " + s);
					String move = null;
					move = console.readLine();
					output.write(move.getBytes(), 0, move.length());
					output.flush();

					// Automatically relaunch the server for a new game
					try {
						ProcessBuilder pb = new ProcessBuilder("C:\\Users\\Kevin-pc\\Desktop\\Ecole\\Hiver2026\\LOG320\\equipe_lab\\LOG320-02_team201_lab2\\breakThrough_win11\\boardgame.exe");
						pb.start();
						System.out.println("Relaunching boardgame.exe for new game...");
						Thread.sleep(2000); // Wait 2 seconds for server to start
					} catch (Exception e) {
						System.out.println("Error relaunching server: " + e.getMessage());
					}
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		}

	}


	public static String cpuMovePlay(Board board, CPUPlayer aiMachine)
	{
		intToString encoderMoveAi = new intToString();

		ArrayList<Move> aiPossibleNextMove = aiMachine.getNextMove(board); // was getNextMoveAB

		System.out.println("Nombre de move ayant meme score : " + aiPossibleNextMove.size());
		//aiPossibleNextMove.forEach((movePossible) -> { System.out.print(movePossible.toString() + " ; "); });
		System.out.println();
		Move movePlayed = selectBestMoveFromTies(aiPossibleNextMove, board, aiMachine.getCpuMark());
		board.play(movePlayed, aiMachine.getCpuMark());
		return encoderMoveAi.encode(movePlayed);
	}

	/**
	 * Select the best move from a list of moves with equal scores.
	 * 
	 * The moves are already ordered optimally by getMoveListOrdered() in CPUPlayer
	 * 
	 * We simply return the first move, trusting this pre-ordering.
	 */
	private static Move selectBestMoveFromTies(ArrayList<Move> moves, Board board, Mark playerMark)
	{
		if (moves.isEmpty()) return null;
		return moves.get(0);  // Trust getMoveListOrdered's ordering
	}


	public void populateBoard()
	{
		
	}

}
