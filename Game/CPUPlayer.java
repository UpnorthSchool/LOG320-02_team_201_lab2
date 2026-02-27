package Game;
import java.util.ArrayList;

import Game.Algorythme.AlphaBeta;
import Game.Algorythme.MinMax;

// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
// Vous pouvez par contre ajouter d'autres méthodes (ça devrait 
// être le cas)
public class CPUPlayer
{

    // Contient le nombre de noeuds visités (le nombre
    // d'appel à la fonction MinMax ou Alpha Beta)
    // Normalement, la variable devrait être incrémentée
    // au début de votre MinMax ou Alpha Beta.
    private int numExploredNodes;
    private Mark cpuMARK;
    private Mark opponentMARK;
    private ArrayList<Move> movePossibleUncheck = new ArrayList<>();
    public MinMax miniMax;
    //starting at 3 move ahead
    private int depth = 1;

    // Le constructeur reçoit en paramètre le
    // joueur MAX (rouge ou black)
    public CPUPlayer(Mark cpu){
        setCpuMark(cpu);
        setOpponentMark((cpu == Mark.RED) ? Mark.BLACK : Mark.RED);
        miniMax = new MinMax(cpu);
    }

    // Ne pas changer cette méthode
    public int  getNumOfExploredNodes(){
        return numExploredNodes;
    }

    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
public ArrayList<Move> getNextMoveMinMax(Board board)
{
    numExploredNodes = 0;
    int bestScore = Integer.MIN_VALUE;
    ArrayList<Move> bestMoves = new ArrayList<>(); // Changed from single Move

    for(Move move : getMovePossibleUncheck(board))
    {
        board.play(move, cpuMARK);
        //alphabeta
        // int score = miniMax.alphaBeta(board, getOpponentMark(), this, Integer.MIN_VALUE, Integer.MAX_VALUE);
        //minmax
        int score = miniMax.minMax(board, getOpponentMark(), this, depth);
        board.undoMove(move, cpuMARK);
        
        if(score > bestScore) {
            // trouver un meilleur move alors noter 
            bestScore = score;
            bestMoves.clear();
            bestMoves.add(move);
        } 
        else if(score == bestScore) {
            // trouver un move egal, ajouter 
            bestMoves.add(move);
        }
        // si pas de meilleur move,prendre le seul bon move"
    }

    return bestMoves;
}
    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public ArrayList<Move> getNextMoveAB(Board board){
        numExploredNodes = 0;

        //todo
        return new ArrayList<>();
    }


    //check for available move on board for cpu
    public ArrayList<Move> getMovePossibleUncheck(Board board)
    {
        movePossibleUncheck = board.getMoveList(cpuMARK);
        return movePossibleUncheck;
    }








    //////getter setter
    /// 
    /// 
    public void setCpuMark(Mark cpu)
    {
        cpuMARK = cpu;
    }

    public Mark getCpuMark()
    {
        return cpuMARK;
    }
     public void setOpponentMark(Mark opponent)
    {
        opponentMARK = opponent;
    }

    public Mark getOpponentMark()
    {
        return opponentMARK;
    }

    public void incrementNodeCounter()
    {
        this.numExploredNodes++;
    }
}
