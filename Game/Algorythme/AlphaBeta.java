package Game.Algorythme;

import Game.Board;
import Game.CPUPlayer;
import Game.Mark;
import Game.Move;
import java.util.ArrayList;

public class AlphaBeta {

    private final Mark cpuMark;
    private long startTime;
    private long timeLimit;
    public boolean timedOut;
    
    // Killer moves: track moves that caused cutoffs at each depth
    private Move[][] killerMoves;
    private static final int MAX_KILLER_DEPTH = 9;

    public AlphaBeta(Mark cpuMark) {
        this.cpuMark = cpuMark;
        this.killerMoves = new Move[MAX_KILLER_DEPTH][2];
    }

    public void resetTimer(long timeLimitMs) {
        this.startTime = System.currentTimeMillis();
        this.timeLimit = timeLimitMs;
        this.timedOut  = false;
        this.killerMoves = new Move[MAX_KILLER_DEPTH][2]; // reset killer moves
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
     * Order moves: prioritize captures, then killer moves, then advancement
     * This dramatically improves alpha-beta pruning
     */
    private ArrayList<Move> orderMoves(Board board, ArrayList<Move> moves, Mark mark, int depth) {
        ArrayList<Move> ordered = new ArrayList<>(moves);
        
        // Sort by: captures first, then killer moves, then advancement bonus
        ordered.sort((m1, m2) -> {
            int score1 = getMoveScore(m1, mark, depth);
            int score2 = getMoveScore(m2, mark, depth);
            return Integer.compare(score2, score1); // descending order
        });
        
        return ordered;
    }
    
    /**
     * Score a move for ordering purposes - higher score = evaluate first
     */
    private int getMoveScore(Move move, Mark mark, int depth) {
        int score = 0;
        
        // Captures are best: +1000
        if (move.getCaptured() != Mark.EMPTY) {
            score += 1000;
        }
        
        // Killer moves at this depth: +100
        if (depth < MAX_KILLER_DEPTH && killerMoves[depth] != null) {
            if ((killerMoves[depth][0] != null && movesEqual(killerMoves[depth][0], move)) ||
                (killerMoves[depth][1] != null && movesEqual(killerMoves[depth][1], move))) {
                score += 100;
            }
        }
        
        // Advancement: prefer moving forward
        if (mark == Mark.RED && move.getTowardsRow() > move.getFromRow()) {
            score += 50 * (move.getTowardsRow() - move.getFromRow());
        } else if (mark == Mark.BLACK && move.getTowardsRow() < move.getFromRow()) {
            score += 50 * (move.getFromRow() - move.getTowardsRow());
        }
        
        return score;
    }
    
    /**
     * Check if two moves are the same
     */
    private boolean movesEqual(Move m1, Move m2) {
        if (m1 == null || m2 == null) return false;
        return m1.getFromCol() == m2.getFromCol() &&
               m1.getFromRow() == m2.getFromRow() &&
               m1.getTowardsCol() == m2.getTowardsCol() &&
               m1.getTowardsRow() == m2.getTowardsRow();
    }
    
    /**
     * Update killer moves: store moves that caused cutoffs
     */
    private void updateKillerMove(Move move, int depth) {
        if (depth >= MAX_KILLER_DEPTH) return;
        
        if (killerMoves[depth] == null) {
            killerMoves[depth] = new Move[2];
        }
        
        if (!movesEqual(killerMoves[depth][0], move)) {
            killerMoves[depth][1] = killerMoves[depth][0];
            killerMoves[depth][0] = move;
        }
    }

    /**
     * Alpha-beta search with move ordering and killer moves.
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
        
        // Order moves for better pruning
        int currentDepth = MAX_KILLER_DEPTH - depth;
        ArrayList<Move> orderedMoves = orderMoves(board, board.getMoveList(mark), mark, currentDepth);

        if (isMax) {
            int best = Integer.MIN_VALUE + 1;
            for (Move move : orderedMoves) {
                if (timedOut) break;
                board.play(move, mark);
                int score = search(board, opponent(mark), depth - 1, alpha, beta, counter);
                board.undoMove(move, mark);
                
                if (score > best) {
                    best = score;
                    if (best >= beta && currentDepth < MAX_KILLER_DEPTH) {
                        updateKillerMove(move, currentDepth);
                    }
                }
                if (best > alpha) alpha = best;
                if (best >= beta) break; // cut-off
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (Move move : orderedMoves) {
                if (timedOut) break;
                board.play(move, mark);
                int score = search(board, opponent(mark), depth - 1, alpha, beta, counter);
                board.undoMove(move, mark);
                
                if (score < best) {
                    best = score;
                    if (best <= alpha && currentDepth < MAX_KILLER_DEPTH) {
                        updateKillerMove(move, currentDepth);
                    }
                }
                if (best < beta) beta = best;
                if (best <= alpha) break; // cut-off
            }
            return best;
        }
    }
}