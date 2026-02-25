package Game;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import Game.Decoder.stringToInt;

class Client {
	public static void main(String[] args) {
		Socket MyClient;
		BufferedInputStream input;
		BufferedOutputStream output;
		int[][] board = new int[8][8];
		//from tictactoe :
		Board board8x8 = new Board();
		stringToInt decoderEntrer = new stringToInt();
		System.out.println("Svp entrer les cases que vous voulez jouer comme suit (x,y), exemple : A1,b2,c3)");
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
							board8x8.setBlackPieceCounter(board8x8.getBlackPiececounter() + 1);
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
					board8x8.display(board8x8.getMoveList(Mark.RED));
					System.out.println("Nouvelle partie! Vous jouer blanc, entrez votre premier coup : ");
					//code pour algorythme ici humain ! a remplacer par ai par la suite
					String move = null;
					move = console.readLine();
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
						x++;
						if (x == 8) {
							x = 0;
							y++;
						}
					}
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
					System.out.println("Entrez votre coup : ");
					String move = null;
					move = console.readLine();
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
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		}

	}


	public static void cpuMovePlay(Board board, CPUPlayer aiMachine)
	{
		ArrayList<Move> aiPossibleNextMove= aiMachine.getNextMoveMinMax(board);
		System.out.println("Move ayant meme score : ");
		aiPossibleNextMove.forEach( (movePossible) -> { System.out.print(movePossible.toString() +" ; ");});
		System.out.println();
		System.out.println("Nombre de node explorer : " + aiMachine.getNumOfExploredNodes());
		//joue un move random de la liste 
		int random = ThreadLocalRandom.current().nextInt(0, aiPossibleNextMove.size());
		board.play(aiPossibleNextMove.get(random), aiMachine.getCpuMark());
	}


	public void populateBoard()
	{
		
	}

}
