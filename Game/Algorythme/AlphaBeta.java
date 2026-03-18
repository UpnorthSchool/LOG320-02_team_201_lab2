package Game.Algorythme;

import Game.Board;
import Game.CPUPlayer;
import Game.Mark;
import Game.Move;

public class AlphaBeta {

    private Mark cpuMARK;
    private long startTime;
    private long timeLimit;
    public boolean timeOut;

    public AlphaBeta(Mark cpuMark)
    {
        this.cpuMARK = cpuMark;
    }

    // Appele par CPUPlayer avant chaque recherche
    public void resetTimer(long startTime, long timeLimit)
    {
        this.startTime = startTime;
        this.timeLimit = timeLimit;
        this.timeOut   = false;
    }

    private Mark getOpponentMark(Mark mark)
    {
        return (mark == Mark.RED) ? Mark.BLACK : Mark.RED;
    }

    public int alphaBeta(Board board, Mark alphaBetaMark, CPUPlayer nbExploredNode, int alpha, int beta, int depth)
    {
        // Verifier le temps a CHAQUE noeud
        if (System.currentTimeMillis() - startTime > timeLimit)
        {
            timeOut = true;
            return 0; // valeur neutre, sera ignoree par CPUPlayer
        }

        nbExploredNode.incrementNodeCounter();

        if (board.verifierVictoire(cpuMARK))
        {
            return 30000 + depth;
        }
        if (board.verifierVictoire(getOpponentMark(cpuMARK)))
        {
            return -30000 - depth;
        }

        if (depth == 0)
        {
            return board.evaluate(cpuMARK);
        }

        boolean isMax = (alphaBetaMark == cpuMARK);

        if (isMax)
        {
            int bestScore = Integer.MIN_VALUE;

            for (Move move : board.getMoveListOrdered(alphaBetaMark))
            {
                if (timeOut) break; // propagation immediate

                board.play(move, alphaBetaMark);
                int score = alphaBeta(board, getOpponentMark(alphaBetaMark), nbExploredNode, alpha, beta, depth - 1);
                board.undoMove(move, alphaBetaMark);

                if (timeOut) break;

                bestScore = Math.max(bestScore, score);
                alpha     = Math.max(alpha, bestScore);

                if (bestScore >= beta) break;
            }
            return bestScore;
        }
        else
        {
            int bestScore = Integer.MAX_VALUE;

            for (Move move : board.getMoveListOrdered(alphaBetaMark))
            {
                if (timeOut) break; // propagation immediate

                board.play(move, alphaBetaMark);
                int score = alphaBeta(board, getOpponentMark(alphaBetaMark), nbExploredNode, alpha, beta, depth - 1);
                board.undoMove(move, alphaBetaMark);

                if (timeOut) break;

                bestScore = Math.min(bestScore, score);
                beta      = Math.min(beta, bestScore);

                if (bestScore <= alpha) break;
            }
            return bestScore;
        }
    }
}