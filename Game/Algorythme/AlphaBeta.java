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

    private Move[][] killerMoves = new Move[9][2];

    public AlphaBeta(Mark cpuMark)
    {
        this.cpuMARK = cpuMark;
    }

    public void resetTimer(long startTime, long timeLimit)
    {
        this.startTime = startTime;
        this.timeLimit = timeLimit;
        this.timeOut   = false;
        killerMoves    = new Move[9][2];
    }

    private Mark getOpponentMark(Mark mark)
    {
        return (mark == Mark.RED) ? Mark.BLACK : Mark.RED;
    }

    private void storeKiller(int depth, Move move)
    {
        if (move == null) return;
        if (depth >= killerMoves.length) return;
        if (move.getCaptured() != null && move.getCaptured() != Mark.EMPTY) return;
        if (move.equals(killerMoves[depth][0])) return;
        killerMoves[depth][1] = killerMoves[depth][0];
        killerMoves[depth][0] = move;
    }

    public boolean isTimeOut()
    {
        return timeOut;
    }

    // Quiescence search — continue uniquement sur les captures
    // pour eviter l'horizon effect sur les positions tactiques instables
    private int quiescence(Board board, Mark mark, CPUPlayer nbExploredNode, int alpha, int beta)
    {
        if (System.currentTimeMillis() - startTime > timeLimit)
        {
            timeOut = true;
            return 0;
        }

        nbExploredNode.incrementNodeCounter();

        // Score de la position sans jouer — si c'est deja bon, on coupe
        int standPat = board.evaluate(cpuMARK);

        if (standPat >= beta) return beta;
        if (standPat > alpha) alpha = standPat;

        // Chercher uniquement les captures
        for (Move move : board.getMoveListOrdered(mark, null, null))
        {
            if (move.getCaptured() == null || move.getCaptured() == Mark.EMPTY) continue;

            board.play(move, mark);
            int score = quiescence(board, getOpponentMark(mark), nbExploredNode, alpha, beta);
            board.undoMove(move, mark);

            if (timeOut) return 0;

            if (score >= beta) return beta;
            if (score > alpha) alpha = score;
        }

        return alpha;
    }

    public int alphaBeta(Board board, Mark alphaBetaMark, CPUPlayer nbExploredNode, int alpha, int beta, int depth)
    {
        if (System.currentTimeMillis() - startTime > timeLimit)
        {
            timeOut = true;
            return 0;
        }

        nbExploredNode.incrementNodeCounter();

        if (board.verifierVictoire(cpuMARK))
            return 30000 + depth;

        if (board.verifierVictoire(getOpponentMark(cpuMARK)))
            return -30000 - depth;

        // Remplace evaluate() direct par quiescence aux feuilles
        if (depth == 0)
            return quiescence(board, alphaBetaMark, nbExploredNode, alpha, beta);

        Move k1 = (depth < killerMoves.length) ? killerMoves[depth][0] : null;
        Move k2 = (depth < killerMoves.length) ? killerMoves[depth][1] : null;

        boolean isMax = (alphaBetaMark == cpuMARK);

        if (isMax)
        {
            int  bestScore = Integer.MIN_VALUE;
            Move bestMove  = null;

            for (Move move : board.getMoveListOrdered(alphaBetaMark, k1, k2))
            {
                if (timeOut) break;

                board.play(move, alphaBetaMark);
                int score = alphaBeta(board, getOpponentMark(alphaBetaMark), nbExploredNode, alpha, beta, depth - 1);
                board.undoMove(move, alphaBetaMark);

                if (timeOut) break;

                if (score > bestScore)
                {
                    bestScore = score;
                    bestMove  = move;
                }

                alpha = Math.max(alpha, bestScore);

                if (bestScore >= beta)
                {
                    storeKiller(depth, move);
                    break;
                }
            }
            return bestScore;
        }
        else
        {
            int  bestScore = Integer.MAX_VALUE;
            Move bestMove  = null;

            for (Move move : board.getMoveListOrdered(alphaBetaMark, k1, k2))
            {
                if (timeOut) break;

                board.play(move, alphaBetaMark);
                int score = alphaBeta(board, getOpponentMark(alphaBetaMark), nbExploredNode, alpha, beta, depth - 1);
                board.undoMove(move, alphaBetaMark);

                if (timeOut) break;

                if (score < bestScore)
                {
                    bestScore = score;
                    bestMove  = move;
                }

                beta = Math.min(beta, bestScore);

                if (bestScore <= alpha)
                {
                    storeKiller(depth, move);
                    break;
                }
            }
            return bestScore;
        }
    }
}