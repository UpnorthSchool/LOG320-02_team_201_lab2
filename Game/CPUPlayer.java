package Game;

import Game.Algorythme.AlphaBeta;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class CPUPlayer {

    private final Mark cpuMark;
    private final Mark opponentMark;
    private final AlphaBeta ab;

    private int  numExploredNodes;
    private static final int MAX_DEPTH    = 8;
    private static final long TIME_LIMIT_MS = 4200; // safe buffer under 5 s

    public CPUPlayer(Mark cpuMark) {
        this.cpuMark      = cpuMark;
        this.opponentMark = (cpuMark == Mark.RED) ? Mark.BLACK : Mark.RED;
        this.ab           = new AlphaBeta(cpuMark);
    }

    /**
     * Iterative-deepening alpha-beta.
     * Commits the result of the deepest fully-searched depth before timeout.
     */
    public ArrayList<Move> getNextMove(Board board) {
        numExploredNodes = 0;
        ab.resetTimer(TIME_LIMIT_MS);

        ArrayList<Move> bestMoves = new ArrayList<>();
        int        bestScore = Integer.MIN_VALUE;

        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            ArrayList<Move> candidateMoves = new ArrayList<>();
            int        candidateScore = Integer.MIN_VALUE;

            for (Move move : board.getMoveList(cpuMark)) {
                if (ab.timedOut) break;

                board.play(move, cpuMark);
                int score = ab.search(board, opponentMark, depth - 1,
                                      Integer.MIN_VALUE + 1, Integer.MAX_VALUE,
                                      this);
                board.undoMove(move, cpuMark);

                if (ab.timedOut) break;

                if (score > candidateScore) {
                    candidateScore = score;
                    candidateMoves.clear();
                    candidateMoves.add(move);
                } else if (score == candidateScore) {
                    candidateMoves.add(move);
                }
            }

            if (!ab.timedOut) {
                bestMoves = candidateMoves;
                bestScore = candidateScore;
                System.out.printf("Depth %d done — score: %d, nodes: %d%n",
                                  depth, bestScore, numExploredNodes);

                // No point searching deeper if a forced result is found
                if (Math.abs(bestScore) >= 29000) break;
            } else {
                System.out.printf("Timeout at depth %d — keeping depth %d result%n",
                                  depth, depth - 1);
                break;
            }
        }

        return bestMoves.isEmpty() ? board.getMoveList(cpuMark) : bestMoves;
    }

    public void incrementNodeCounter() { numExploredNodes++; }
    public int  getNumExploredNodes()  { return numExploredNodes; }
    public Mark getCpuMark()           { return cpuMark; }
    public Mark getOpponentMark()      { return opponentMark; }
}