package Game.Algorythme;

import Game.*;

public class MinMax {

    private Mark cpuMARK;


    

    public int minMax(Board board, Mark minMaxMark, CPUPlayer nbExploredNode)
    {

        nbExploredNode.incrementNodeCounter();
       //premier tour est toujours evaluer comme les min (mentioner par le prof )
       //attention ! icidoit evaluer le score toujorus du point de vue du AI et non alterner
       int scoreSortie = board.evaluate(cpuMARK);
       //condition de sortie iciii
       if(scoreSortie == 100 || scoreSortie == -100 || scoreSortie == 0) return scoreSortie;

       // definition du joueur 
       boolean isMax = (minMaxMark == cpuMARK);

       int bestScore;
       if(isMax)
       { 
            bestScore = Integer.MIN_VALUE;
            for(Move move : board.getMoveList(minMaxMark))
            {
                board.play(move, minMaxMark);
                int score = minMax(board, getOpponentMark(minMaxMark), nbExploredNode);
                board.undoMove(move, minMaxMark);
                bestScore = Math.max(bestScore, score);
            }
            return bestScore;

            
       }else
       {
          bestScore = Integer.MAX_VALUE;
            for(Move move : board.getMoveList(minMaxMark))
            {
                board.play(move, minMaxMark);
                int score = minMax(board, getOpponentMark(minMaxMark), nbExploredNode);
                board.undoMove(move, minMaxMark);
                bestScore = Math.min(bestScore, score);
            }
            return bestScore;
       }

    }




    //permet de tracker quel joueur joue

    public MinMax(Mark cpuMark)
    {
        this.cpuMARK = cpuMark;
    }
    private Mark getOpponentMark(Mark mark) {
        return (mark == Mark.RED) ? Mark.BLACK : Mark.RED;
    }

}
