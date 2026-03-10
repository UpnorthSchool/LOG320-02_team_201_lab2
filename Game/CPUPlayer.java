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
    public AlphaBeta miniMax;
    //starting at 3 move ahead
    private int depth = 5;

    // Le constructeur reçoit en paramètre le
    // joueur MAX (rouge ou black)
    public CPUPlayer(Mark cpu){
        setCpuMark(cpu);
        setOpponentMark((cpu == Mark.RED) ? Mark.BLACK : Mark.RED);
        miniMax = new AlphaBeta(cpu);
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

        // depth=6 : l'IA voit 7 demi-coups en avant (son coup + 6 niveaux de recursion)
        // Cela permet de detecter qu'un pion ennemi en rangee 2 gagnera dans 3 coups
        // et de prioriser le blocage plutot qu'une avance personnelle

        int bestScore = Integer.MIN_VALUE;
        ArrayList<Move> bestMoves = new ArrayList<>();

        for (Move move : board.getMoveList(cpuMARK))
        {
            board.play(move, cpuMARK);
            // Premier appel recursif : c'est au tour de l'adversaire (MIN)
            int score = miniMax.alphaBeta(board, opponentMARK, this, Integer.MIN_VALUE, Integer.MIN_VALUE, depth - 1);
            board.undoMove(move, cpuMARK);

            if (score > bestScore)
            {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            }
            else if (score == bestScore)
            {
                bestMoves.add(move);
            }
        }

        System.out.println("nb node explored : " + getNumOfExploredNodes());
        System.out.println("Bestscore found = " + bestScore);
        return bestMoves;
    }
    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public ArrayList<Move> getNextMoveAB(Board board)
    {
        numExploredNodes = 0;
        ArrayList<Move> movesPossibles = board.getMoveList(cpuMARK);
        System.out.println("Nombre de moves racine : " + movesPossibles.size());

        // depth=8 est viable grace aux coupures alpha-beta.
        // Equivalent en qualite a un MinMax de profondeur ~14-16.

        int bestScore = Integer.MIN_VALUE;
        ArrayList<Move> bestMoves = new ArrayList<>();

        // Alpha et beta initiaux : les bornes les plus larges possibles.
        // Alpha = meilleur score garanti pour MAX (on commence au minimum)
        // Beta  = meilleur score garanti pour MIN (on commence au maximum)
        int alpha = Integer.MIN_VALUE;
        int beta  = Integer.MAX_VALUE;

        for (Move move : board.getMoveList(cpuMARK))
        {
            board.play(move, cpuMARK);
            // Premier appel recursif : c'est au tour de l'adversaire (MIN)
            int score = miniMax.alphaBeta(board, opponentMARK, this, alpha, beta, depth - 1);
            board.undoMove(move, cpuMARK);

            if (score > bestScore)
            {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            }
            else if (score == bestScore)
            {
                bestMoves.add(move);
            }

            // Mise a jour d'alpha au niveau racine
            alpha = Math.max(alpha, bestScore);
        }

        System.out.println("nb node explored : " + getNumOfExploredNodes());
        System.out.println("Bestscore found = " + bestScore);
        return bestMoves;
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
       this.numExploredNodes ++;
    }

}
