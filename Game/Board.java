package Game;
import java.util.ArrayList;

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

    // Place la pièce 'mark' sur le plateau, à la
    // position spécifiée dans Move
    // Ne pas changer la signature de cette méthode
    public void play(Move m, Mark mark){
        //verifier ou land la piece 
        m.setCaptured(board[m.getTowardsCol()][m.getTowardsRow()]);
        //bouger notre piece
        board[m.getTowardsCol()][m.getTowardsRow()] = mark;
        //vider la piece dou on vient
        board[m.getFromCol()][m.getFromRow()] = Mark.EMPTY;
    }

    public void undoMove(Move moveUndo, Mark mark)
    {
        //ramenner la case a sa place originale
        board[moveUndo.getFromCol()][moveUndo.getFromRow()] = mark;

        //remettre ce qui a ete capturer, vide ou piece
        board[moveUndo.getTowardsCol()][moveUndo.getTowardsRow()] = moveUndo.getCaptured();
    }


    // retourne  30000 pour une victoire
    //          -30000 pour une défaite
    // Ne pas changer la signature de cette méthode
    public int evaluate(Mark mark){
        Mark adversaireXO = (mark == Mark.RED) ? Mark.BLACK : Mark.RED;

        //verificationvictoire 
        if(verifierVictoire(mark))
        {
            return 30000;
        }
        if(verifierVictoire(adversaireXO))
        {
            return -30000;
        }
      

       
        return Integer.MIN_VALUE;
        
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
    /// //@TODO modifier code pour evaluation pour victoire breakthrough - derniere ranger oubien plus dennemi
    public boolean verifierVictoire(Mark mark)
    {
        boolean victoire = false;
        // verification victoire si une piece est sur la derniere ligne ennemy ou si nbpiece noire a 0 
        if(mark == Mark.BLACK)
        {
            for(int i = 0 ; i < boardSize ; i ++)
            {
                victoire = (board[i][0] == Mark.BLACK);
            }
            if(getRedPieceCounter() == 0)
            {
                victoire = true;
            }
        }
        
        if(mark == Mark.RED)
        {
            // verification victoire si une piece est sur la derniere ligne ennemy ou si nbpiece noire a 0 
            for(int j = 0 ; j < boardSize ; j ++)
            {
                victoire = (board[j][7] == Mark.RED);
            }
            if(getBlackPiececounter() == 0)
            {
                victoire = true;
            }
            
        }
        return victoire;

    }


    //verification que le mouvement est sur le board 
    public boolean moveValide(int xColPosition, int yLignePosition)
    {
        //check on board and check if case empty
        if(xColPosition > -1 && xColPosition < boardSize && yLignePosition > -1 && yLignePosition < boardSize && board[xColPosition][yLignePosition] == Mark.EMPTY)
        {
            return true;
        }
        
        return false;
        
    }

    //listes des case disponbiles sur le board,comme demander dans l'enoncé
    public ArrayList<Move> getMoveList(Mark markSide)
    {
        ArrayList<Move> moveAvailableNow = new ArrayList<>();
        int direction                    = (markSide == Mark.RED) ? +1 : -1;
        for(int xCol = 0 ; xCol < getBoardSize() ; xCol++)
        {
            for(int yLig = 0; yLig < getBoardSize() ; yLig++)
            {
                //regarde les pieces de la meme couleur seulement, pas besoin de verifier les moves de lautre equipe
                if( getBoard()[xCol][yLig] == markSide)
                {
                    //avance ou recule dune ligne
                    int ligneAvancement     =  yLig + direction;
                    // si la ligne est sur le board
                    if(moveValide(xCol, ligneAvancement))
                    {
                        //verifier une case en avant, sans ennemy, alors bon move
                        if(getBoard()[xCol][ligneAvancement] == Mark.EMPTY)
                        {
                            moveAvailableNow.add(new Move(xCol,yLig,xCol,ligneAvancement));
                        }

                        //verification diagonal et case empty ou adversaire
                        if(moveValide(xCol - 1, ligneAvancement) && getBoard()[xCol - 1 ][ligneAvancement] != markSide)
                        {
                            moveAvailableNow.add(new Move(xCol, yLig, xCol - 1, ligneAvancement));
                        }
                        //autre diagonal
                        if(moveValide(xCol + 1, ligneAvancement) && getBoard()[xCol + 1 ][ligneAvancement] != markSide)
                        {
                            moveAvailableNow.add(new Move(xCol, yLig, xCol + 1, ligneAvancement));
                        }
                        
                    }
                }
            }
        }
        return moveAvailableNow;
    }

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
