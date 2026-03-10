package Game;
import java.util.ArrayList;
import java.util.List;

// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
// Vous pouvez par contre ajouter d'autres méthodes (ça devrait 
// être le cas)
public class Board
{
    //[row][column]
    private Mark[][] board;
    private int boardSize = 8;
    private int redPieceCounter         = 0 ; 
    private int blackPieceCounter       = 0 ;



    //ajout de couleur, pour le plaisir du display.
    // Source - https://stackoverflow.com/a/5762502
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";



    // Ne pas changer la signature de cette méthode
    public Board() {
        board = new Mark[boardSize][boardSize];
        for(int xCol = 0; xCol < boardSize; xCol++){
            for(int yLig = 0; yLig < boardSize; yLig++){
                board[xCol][yLig] = Mark.EMPTY;
            }
        }
    }

    public Mark[][] getBoard(){
        return board;
    }

    public void play(Move m, Mark mark)
    {
        // Toujours lire depuis le plateau, jamais depuis m.getCaptured()
        // qui pourrait contenir une valeur residuelle d'un appel precedent
        Mark contenuCase = board[m.getTowardsCol()][m.getTowardsRow()];
        m.setCaptured(contenuCase);

        if (contenuCase == Mark.RED)         redPieceCounter--;
        else if (contenuCase == Mark.BLACK)  blackPieceCounter--;

        board[m.getTowardsCol()][m.getTowardsRow()] = mark;
        board[m.getFromCol()][m.getFromRow()]        = Mark.EMPTY;
    }

    public void undoMove(Move moveUndo, Mark mark)
    {
        board[moveUndo.getFromCol()][moveUndo.getFromRow()]       = mark;
        board[moveUndo.getTowardsCol()][moveUndo.getTowardsRow()] = moveUndo.getCaptured();

        // Restaurer le compteur uniquement si une vraie piece avait ete capturee
        if (moveUndo.getCaptured() == Mark.RED)         redPieceCounter++;
        else if (moveUndo.getCaptured() == Mark.BLACK)  blackPieceCounter++;
    }

    //display des moves possibles des pieces avant minmax

    public void display(ArrayList<Move> displayMove){
        for (Move dMove : displayMove) {
            System.out.println("From : [" + dMove.getFromCol() +", " + dMove.getFromRow() + "] To : [" + dMove.getTowardsCol()+ ","+dMove.getTowardsRow() + "]") ;
            
        }
            
    }


    public boolean getVictoire()
    {
        return true;
    }


    //////// serie evaluation pour le jeu mieux diviser pour meilleur comprehension du minmax
    /// appri srecemment, possibiliter java de retourner un bool avec juste des verif,
    /// division des evaluations sur plusieurs ligne vue sur stack overflow
    public boolean verifierVictoire(Mark mark)
    {
        boolean victoire = false;
        // verification victoire si une piece est sur la derniere ligne ennemy ou si nbpiece noire a 0 
        if(mark == Mark.BLACK)
        {
            for(int i = 0 ; i < boardSize ; i ++)
            {
                if(board[i][0] == Mark.BLACK)
                {
                    return victoire = true;
                }
            }
            if(getRedPieceCounter() == 0)
            {
                return victoire = true;
            }
        }
        
        if(mark == Mark.RED)
        {
            // verification victoire si une piece est sur la derniere ligne ennemy ou si nbpiece noire a 0 
            for(int j = 0 ; j < boardSize ; j ++)
            {
                if(board[j][7] == Mark.RED)
                {
                    return victoire = true;
                }
            }
            if(getBlackPiececounter() == 0)
            {
                return victoire = true;
            }
            
        }
        return victoire;

    }
        // Verification des bornes uniquement — sans verifier le contenu de la case
    public boolean estSurLeBoard(int xColPosition, int yLignePosition)
    {
        return xColPosition >= 0 && xColPosition < boardSize
            && yLignePosition >= 0 && yLignePosition < boardSize;
    }

    // Verification bornes ET case vide — pour les moves en ligne droite uniquement
    public boolean moveValide(int xColPosition, int yLignePosition)
    {
        return estSurLeBoard(xColPosition, yLignePosition)
            && board[xColPosition][yLignePosition] == Mark.EMPTY;
    }



    public ArrayList<Move> getMoveList(Mark markSide)
    {
        ArrayList<Move> moveAvailableNow = new ArrayList<>();
        int direction   = (markSide == Mark.RED) ? +1 : -1;
        Mark adversaire = (markSide == Mark.RED) ? Mark.BLACK : Mark.RED;

        for (int xCol = 0; xCol < getBoardSize(); xCol++)
        {
            for (int yLig = 0; yLig < getBoardSize(); yLig++)
            {
                if (getBoard()[xCol][yLig] == markSide)
                {
                    int ligneAvancement = yLig + direction;

                    // Ligne droite : case doit etre vide (moveValide)
                    if (moveValide(xCol, ligneAvancement))
                    {
                        moveAvailableNow.add(new Move(xCol, yLig, xCol, ligneAvancement));
                    }

                    // Diagonal gauche : case doit etre sur le board (estSurLeBoard)
                    // et ne pas contenir un allie — peut etre vide OU ennemi
                    if (estSurLeBoard(xCol - 1, ligneAvancement)
                        && getBoard()[xCol - 1][ligneAvancement] != markSide)
                    {
                        Move m = new Move(xCol, yLig, xCol - 1, ligneAvancement);
                        if (getBoard()[xCol - 1][ligneAvancement] == adversaire)
                            m.setCaptured(adversaire);
                        moveAvailableNow.add(m);
                    }

                    // Diagonal droit : meme logique
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






    ///////evaluation
    /// and heuristic here 
    /// 
    /// 
    /// 
        /////
    public int evaluate(Mark mark)
    {
        Mark adversaire = (mark == Mark.RED) ? Mark.BLACK : Mark.RED;

        int score = 0;

        // --- Poids ---
        int poidsMateriel   = 15;
        int poidsCentre     =  1;
        int poidsProtection =  3;
        int poidsVulnerable = -5;

        // Table d'avancement exponentielle : plus on approche, plus c'est urgent
        int[] tableAvancement = {0, 1, 2, 4, 8, 16, 32, 100};

        // --- Avantage materiel ---
        // Les compteurs sont maintenus a jour par play() et undoMove()
        int nosPieces      = (mark == Mark.RED) ? redPieceCounter   : blackPieceCounter;
        int piecesEnnemies = (mark == Mark.RED) ? blackPieceCounter : redPieceCounter;
        score += (nosPieces - piecesEnnemies) * poidsMateriel;

        // --- Parcours unique du plateau ---
        for (int y = 0; y < boardSize; y++)
        {
            for (int x = 0; x < boardSize; x++)
            {
                if (board[x][y] == mark)
                {
                    // Avancement exponentiel vers la ligne de victoire
                    int rangee = (mark == Mark.RED) ? y : (boardSize - 1 - y);
                    score += tableAvancement[rangee];

                    // Position centrale
                    score += (4 - Math.abs(x - (boardSize / 2 - 1))) * poidsCentre;

                    // Protection : pion couvert par un allie derriere lui ?
                    int yDerriere = (mark == Mark.RED) ? y - 1 : y + 1;
                    boolean estProtege = false;
                    if (yDerriere >= 0 && yDerriere < boardSize)
                    {
                        if (x - 1 >= 0        && board[x - 1][yDerriere] == mark) estProtege = true;
                        if (x + 1 < boardSize && board[x + 1][yDerriere] == mark) estProtege = true;
                    }

                    if (estProtege)
                        score += poidsProtection * (1 + rangee / 2);
                    else if (rangee >= 3)
                        score += poidsVulnerable * (rangee - 2);
                }
                else if (board[x][y] == adversaire)
                {
                    int rangee = (adversaire == Mark.RED) ? y : (boardSize - 1 - y);
                    score -= tableAvancement[rangee];

                    score -= (4 - Math.abs(x - (boardSize / 2 - 1))) * poidsCentre;

                    int yDerriere = (adversaire == Mark.RED) ? y - 1 : y + 1;
                    boolean estProtege = false;
                    if (yDerriere >= 0 && yDerriere < boardSize)
                    {
                        if (x - 1 >= 0        && board[x - 1][yDerriere] == adversaire) estProtege = true;
                        if (x + 1 < boardSize && board[x + 1][yDerriere] == adversaire) estProtege = true;
                    }

                    if (estProtege)
                        score -= poidsProtection * (1 + rangee / 2);
                    else if (rangee >= 3)
                        score -= poidsVulnerable * (rangee - 2);
                }
            }
        }


    // Remplacer la detection de pion libre par cette version corrigee

    for (int x = 0; x < boardSize; x++)
    {
        for (int y = 0; y < boardSize; y++)
        {
            if (board[x][y] == adversaire)
            {
                int rangeeAdversaire = (adversaire == Mark.RED) ? y : (boardSize - 1 - y);

                if (rangeeAdversaire >= 5)
                {
                    boolean estBloque = false;

                    // Pour bloquer un pion ennemi, nos pions doivent se trouver
                    // SUR LA MEME COLONNE OU ADJACENTE, entre lui et sa ligne de victoire.
                    // Pour BLACK (avance vers y=0), ses pions sont bloques par nos pions
                    // qui se trouvent a des y INFERIEURS (entre lui et y=0).
                    // Pour RED (avance vers y=7), ses pions sont bloques par nos pions
                    // qui se trouvent a des y SUPERIEURS (entre lui et y=7).

                    int yVictoire = (adversaire == Mark.RED) ? boardSize - 1 : 0;
                    int direction = (yVictoire > y) ? 1 : -1;

                    for (int checkY = y + direction;
                        checkY >= 0 && checkY < boardSize;
                        checkY += direction)
                    {
                        if (board[x][checkY] == mark)                          { estBloque = true; break; }
                        if (x > 0           && board[x-1][checkY] == mark)     { estBloque = true; break; }
                        if (x < boardSize-1 && board[x+1][checkY] == mark)     { estBloque = true; break; }
                    }

                    if (!estBloque)
                        score -= 50 * (rangeeAdversaire - 4);
                }
            }
        }
    }

        return score;
    }
    //heuristic : https://www.comp.nus.edu.sg/~kanmy/courses/3243_2006/hw-breakthrough.html

    public int passedPawn()
    {return 0;}

    ///getter and setteer
    public int getBoardSize()
    {
        return boardSize;
    }



    //@TODO implementer verification pour MARK.rouge ou noir si atteint derniere ligne alors victoire.
    public boolean hasWon(int scoreKeeper)
    {
        
        return false;
    }



    public void setRedPieceCounter(int valeur)
    {
        redPieceCounter = valeur;
    }
    public void setBlackPieceCounter(int valeur)
    {
        blackPieceCounter = valeur;
    }
    public int getRedPieceCounter()
    {
        return redPieceCounter;
    }
    public int getBlackPiececounter()
    {
        return blackPieceCounter;
    }

}
