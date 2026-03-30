
package Game;
import java.util.ArrayList;
import java.util.List;

// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
public class Board
{
    //[row][column]
    private Mark[][] board;
    private int boardSize = 8;

    // compteurs de pièces pour chaque joueur
    private int redPieceCounter   = 0;
    private int blackPieceCounter = 0;

    // couleurs pour affichage console
    public static final String ANSI_RESET  = "\u001B[0m";
    public static final String ANSI_RED    = "\u001B[31m";
    public static final String ANSI_GREEN  = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    public Board() {
        // init du board vide
        board = new Mark[boardSize][boardSize];

        for(int xCol = 0; xCol < boardSize; xCol++){
            for(int yLig = 0; yLig < boardSize; yLig++){
                board[xCol][yLig] = Mark.EMPTY;
            }
        }
    }

    public Mark[][] getBoard(){ return board; }

    public void play(Move m, Mark mark)
    {
        // on regarde ce qu’il y avait sur la case d’arrivée
        Mark contenuCase = board[m.getTowardsCol()][m.getTowardsRow()];
        m.setCaptured(contenuCase);

        // si on capture une pièce : on update le compteur
        if (contenuCase == Mark.RED)         redPieceCounter--;
        else if (contenuCase == Mark.BLACK)  blackPieceCounter--;

        // on déplace la pièce
        board[m.getTowardsCol()][m.getTowardsRow()] = mark;
        board[m.getFromCol()][m.getFromRow()]        = Mark.EMPTY;
    }

    public void undoMove(Move moveUndo, Mark mark)
    {
        // on remet la pièce à sa position initiale
        board[moveUndo.getFromCol()][moveUndo.getFromRow()] = mark;

        // on restaure ce qui avait été capturé
        board[moveUndo.getTowardsCol()][moveUndo.getTowardsRow()] = moveUndo.getCaptured();

        // on remet les compteurs
        if (moveUndo.getCaptured() == Mark.RED)         redPieceCounter++;
        else if (moveUndo.getCaptured() == Mark.BLACK)  blackPieceCounter++;
    }

    public void display(ArrayList<Move> displayMove){
        // affiche tous les moves possibles
        for (Move dMove : displayMove) {
            System.out.println("From : [" + dMove.getFromCol() +", " + dMove.getFromRow() + "] To : [" + dMove.getTowardsCol()+ ","+dMove.getTowardsRow() + "]");
        }
    }

    public boolean getVictoire() { return true; }

    public boolean verifierVictoire(Mark mark)
    {
        // check victoire selon la couleur
        boolean victoire = false;

        if(mark == Mark.BLACK)
        {
            // noir gagne s’il atteint la première ligne
            for(int i = 0 ; i < boardSize ; i++)
                if(board[i][0] == Mark.BLACK) return true;

            // ou si rouge n’a plus de pièces
            if(getRedPieceCounter() == 0) return true;
        }

        if(mark == Mark.RED)
        {
            // rouge gagne s’il atteint la dernière ligne
            for(int j = 0 ; j < boardSize ; j++)
                if(board[j][7] == Mark.RED) return true;

            // ou si noir n’a plus de pièces
            if(getBlackPieceCounter() == 0) return true;
        }

        return victoire;
    }

    public boolean estSurLeBoard(int xColPosition, int yLignePosition)
    {
        // check si position valide dans le board
        return xColPosition >= 0 && xColPosition < boardSize
            && yLignePosition >= 0 && yLignePosition < boardSize;
    }

    public boolean moveValide(int xColPosition, int yLignePosition)
    {
        // move valide si dans le board et case vide
        return estSurLeBoard(xColPosition, yLignePosition)
            && board[xColPosition][yLignePosition] == Mark.EMPTY;
    }

    public ArrayList<Move> getMoveList(Mark markSide)
    {
        ArrayList<Move> moveAvailableNow = new ArrayList<>();

        // direction dépend de la couleur
        int direction   = (markSide == Mark.RED) ? +1 : -1;
        Mark adversaire = (markSide == Mark.RED) ? Mark.BLACK : Mark.RED;

        // scan complet du board
        for (int xCol = 0; xCol < getBoardSize(); xCol++)
        {
            for (int yLig = 0; yLig < getBoardSize(); yLig++)
            {
                if (getBoard()[xCol][yLig] == markSide)
                {
                    int ligneAvancement = yLig + direction;

                    // move tout droit
                    if (moveValide(xCol, ligneAvancement))
                        moveAvailableNow.add(new Move(xCol, yLig, xCol, ligneAvancement));

                    // diagonale gauche
                    if (estSurLeBoard(xCol - 1, ligneAvancement)
                        && getBoard()[xCol - 1][ligneAvancement] != markSide)
                    {
                        Move m = new Move(xCol, yLig, xCol - 1, ligneAvancement);

                        // capture si ennemi
                        if (getBoard()[xCol - 1][ligneAvancement] == adversaire)
                            m.setCaptured(adversaire);

                        moveAvailableNow.add(m);
                    }

                    // diagonale droite
                    if (estSurLeBoard(xCol + 1, ligneAvancement)
                        && getBoard()[xCol + 1][ligneAvancement] != markSide)
                    {
                        Move m = new Move(xCol, yLig, xCol + 1, ligneAvancement);

                        if (getBoard()[xCol + 1][ligneAvancement] == adversaire)
                            m.setCaptured(adversaire);

                        moveAvailableNow.add(m);
                    }
                }
            }
        }
        return moveAvailableNow;
    }

    // =========================================================================
    // evaluate : score du board du point de vue du cpu
    // 3 idées : matériel, progression, passers
    // https://en.wikipedia.org/wiki/Evaluation_function#Example
    // https://trmph.com/bin/Basic_Introduction_to_Breakthrough.pdf
    // =========================================================================
    public int evaluate(Mark cpuMark)
    {
        // états finaux : win / lose
        if (verifierVictoire(cpuMark))           return  30000;
        if (verifierVictoire(opponent(cpuMark))) return -30000;

        int score = 0;

        // 1 : matériel : nombre de pièces
        int cpuPieces = (cpuMark == Mark.RED) ? redPieceCounter : blackPieceCounter;
        int oppPieces = (cpuMark == Mark.RED) ? blackPieceCounter : redPieceCounter;
        score += (cpuPieces - oppPieces) * 100;

        // 2 : progression : plus t’avances plus ça vaut cher
        int[] advBonus = { 0, 5, 15, 30, 55, 90, 140, 200 };
        Mark  opp      = opponent(cpuMark);

        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                if (board[x][y] == cpuMark) {
                    int rank = (cpuMark == Mark.RED) ? y : (boardSize - 1 - y);
                    score += advBonus[rank];
                } 
                else if (board[x][y] == opp) {
                    int rank = (opp == Mark.RED) ? y : (boardSize - 1 - y);
                    score -= advBonus[rank];
                }
            }
        }

        // 3 : passers : pièces impossibles à intercepter
        score += (countPassers(cpuMark, opp) - countPassers(opp, cpuMark)) * 500;

        return score;
    }

    // =========================================================================
    // countPassers : check si une pièce peut être bloquée ou non
    // =========================================================================
    private int countPassers(Mark mark, Mark adversaire)
    {
        int goalRow = (mark == Mark.RED) ? boardSize - 1 : 0;
        int passers = 0;

        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {

                if (board[x][y] != mark) continue;

                int distToGoal = Math.abs(goalRow - y);

                // déjà arrivé = passer direct
                if (distToGoal == 0) {
                    passers++;
                    continue;
                }

                boolean interceptable = false;

                outer:
                for (int ex = 0; ex < boardSize; ex++) {
                    for (int ey = 0; ey < boardSize; ey++) {

                        if (board[ex][ey] != adversaire) continue;

                        boolean enemyAhead = (mark == Mark.RED) ? (ey > y) : (ey < y);
                        int lateralDist   = Math.abs(ex - x);

                        // ennemi peut intercepter
                        if (enemyAhead && lateralDist <= Math.abs(ey - y)) {
                            interceptable = true;
                            break outer;
                        }
                    }
                }

                if (!interceptable) passers++;
            }
        }
        return passers;
    }

    // helper pour éviter de répéter la logique
    private Mark opponent(Mark mark) {
        return (mark == Mark.RED) ? Mark.BLACK : Mark.RED;
    }

    public int passedPawn() { return 0; }

    public int getBoardSize() { return boardSize; }

    public boolean hasWon(int scoreKeeper) { return false; }

    // setters / getters des compteurs
    public void setRedPieceCounter(int valeur)   { redPieceCounter = valeur; }
    public void setBlackPieceCounter(int valeur) { blackPieceCounter = valeur; }
    public int getRedPieceCounter()  { return redPieceCounter; }
    public int getBlackPieceCounter() { return blackPieceCounter; }
}