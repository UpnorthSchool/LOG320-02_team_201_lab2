package Game.Algorythme;

import Game.Board;
import Game.CPUPlayer;
import Game.Mark;
import Game.Move;

public class AlphaBeta {

    private Mark cpuMARK;
    private int score;


    

    public int alphaBeta(Board board, Mark alphaBetaMark, CPUPlayer nbExploredNode, int alpha, int beta)
    {

        nbExploredNode.incrementNodeCounter();
       //premier tour est toujours evaluer comme les min (mentioner par le prof )
       //attention ! icidoit evaluer le score toujorus du point de vue du AI et non alterner
       int scoreSortie = board.evaluate(cpuMARK);
       //condition de sortie iciii
       if(scoreSortie == 100 || scoreSortie == -100 || scoreSortie == 0) return scoreSortie;

       // definition du joueur 
       boolean isMax = (alphaBetaMark == cpuMARK);

       int bestScore;
       if(isMax)
       { 
            bestScore = Integer.MIN_VALUE;
            for(Move move : board.getMoveList(alphaBetaMark))
            {
                board.play(move, alphaBetaMark);
                bestScore = Math.max(bestScore,  alphaBeta(board, getOpponentMark(alphaBetaMark), nbExploredNode, alpha, beta));
                board.undoMove(move, alphaBetaMark);
                if(bestScore >= beta)
                {
                    break;
                }
                alpha = Math.max(bestScore, alpha);
            }
            return bestScore;

            
       }else
       {
          bestScore = Integer.MAX_VALUE;
            for(Move move : board.getMoveList(alphaBetaMark))
            {
                board.play(move, alphaBetaMark);
                bestScore = Math.min(bestScore, alphaBeta(board, getOpponentMark(alphaBetaMark), nbExploredNode, alpha, beta));
                board.undoMove(move, alphaBetaMark);
                if(bestScore <= alpha)
                {
                    break;
                }
                beta = Math.min(bestScore, beta);
            }
            return bestScore;
       }

    }




    //permet de tracker quel joueur joue

    public AlphaBeta(Mark cpuMark)
    {
        this.cpuMARK = cpuMark;
    }
    private Mark getOpponentMark(Mark mark) {
        return (mark == Mark.RED) ? Mark.BLACK : Mark.RED;
    }

}


