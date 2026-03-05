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


    /////
    /// poids des evaluations
    /// 
    int poidsMateriel = 2;
    int avancePion    = 1;
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



    ///////evaluation
    /// and heuristic here 
    /// 
    // retourne  30000 pour une victoire
    //          -30000 pour une défaite
    // Ne pas changer la signature de cette méthode
    public int evaluate(Mark mark){
        int scoreToReturn = 0;
        Mark adversaireXO = (mark == Mark.RED) ? Mark.BLACK : Mark.RED;
        int ourpiece      = (mark == Mark.RED) ? redPieceCounter : blackPieceCounter ;
        int ennemyPiece   = (mark == Mark.RED) ? blackPieceCounter : redPieceCounter;

        //verificationvictoire (a changer peutetre vers integer maxvalue et min value dependamenet nos scorings.)
        if(verifierVictoire(mark))
        {
            return 30000;
        }
        if(verifierVictoire(adversaireXO))
        {
            return -30000;
        }
        //materielle
        scoreToReturn     = (ourpiece - ennemyPiece) * poidsMateriel;
        //nombre de move possible a verifier si important
        //??

        //se rapproche de la victoire
        for(int y = 0; y < boardSize; y++)
        {
            for(int x = 0; x < boardSize; x++)
            {

                if(board[x][y] == mark){

                    // advancement bonus
                    if(mark == Mark.RED)
                        scoreToReturn += y;        // plus on avance vers victoire
                    else
                        scoreToReturn += (7 - y);  // lpus on est loin alors moin bon
                }

                else if(board[x][y] == adversaireXO){

                    if(adversaireXO == Mark.RED)
                        scoreToReturn -= y;
                    else
                        scoreToReturn -= (7 - y);
                }

                // heuristic test pour voir si on peux capturer ou on se fait capturer;
                scoreToReturn += (getMoveList(mark).size() - getMoveList(adversaireXO).size()) * 2;

                for(Move m : getMoveList(mark))
                    if(m.isCapture()) scoreToReturn += 3;

                for(Move m : getMoveList(adversaireXO))
                    if(m.isCapture()) scoreToReturn -= 3;

                scoreToReturn += (4 - Math.abs(x - 3));
            }
        }

        
       
        return scoreToReturn;
        
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
