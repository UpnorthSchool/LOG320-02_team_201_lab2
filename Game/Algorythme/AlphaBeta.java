package Game.Algorythme;

import Game.Board;
import Game.CPUPlayer;
import Game.Mark;
import Game.Move;

public class AlphaBeta {

    private final Mark cpuMark;
    private long startTime;
    private long timeLimit;
    public boolean timedOut;

    public AlphaBeta(Mark cpuMark) {
        this.cpuMark = cpuMark;
    }

    public void resetTimer(long timeLimitMs) {
        this.startTime = System.currentTimeMillis();
        this.timeLimit = timeLimitMs;
        this.timedOut  = false;
    }

    private boolean isTimeUp() {
        if (System.currentTimeMillis() - startTime > timeLimit) {
            timedOut = true;
        }
        return timedOut;
    }

    private Mark opponent(Mark mark) {
        return (mark == Mark.RED) ? Mark.BLACK : Mark.RED;
    }

    /**
     * Alpha-beta search.
     * Score is always from cpuMark's perspective.
     * MAX node when mark == cpuMark, MIN node otherwise.
     */
    public int search(Board board, Mark mark, int depth, int alpha, int beta, CPUPlayer counter) {
        if (isTimeUp()) return 0;

        counter.incrementNodeCounter();

        if (board.verifierVictoire(cpuMark))           return  30000 - depth; // prefer faster wins
        if (board.verifierVictoire(opponent(cpuMark))) return -30000 + depth; // prefer delayed losses

        if (depth == 0) return board.evaluate(cpuMark);

        boolean isMax = (mark == cpuMark);

        if (isMax) {
            int best = Integer.MIN_VALUE + 1;
            for (Move move : board.getMoveList(mark)) {
                if (timedOut) break;
                board.play(move, mark);
                int score = search(board, opponent(mark), depth - 1, alpha, beta, counter);
                board.undoMove(move, mark);
                if (score > best)  best  = score;
                if (best  > alpha) alpha = best;
                if (best  >= beta) break; // cut-off
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (Move move : board.getMoveList(mark)) {
                if (timedOut) break;
                board.play(move, mark);
                int score = search(board, opponent(mark), depth - 1, alpha, beta, counter);
                board.undoMove(move, mark);
                if (score < best)  best = score;
                if (best  < beta)  beta = best;
                if (best  <= alpha) break; // cut-off
            }
            return best;
        }
    }
}