package Game;

import Game.Algorythme.AlphaBeta;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CPUPlayer {

    // couleur du CPU et de l’adversaire
    private final Mark cpuMark;
    private final Mark opponentMark;

    // algo alpha-beta
    private final AlphaBeta ab;

    // stats : nombre de noeuds explorés
    private int numExploredNodes;

    // profondeur max de recherche
    private static final int MAX_DEPTH = 9;

    // limite de temps : un peu en dessous de 5s pour être safe
    private static final long TIME_LIMIT_MS = 4300;

    public CPUPlayer(Mark cpuMark) {
        this.cpuMark      = cpuMark;

        // déduit automatiquement l’adversaire
        this.opponentMark = (cpuMark == Mark.RED) ? Mark.BLACK : Mark.RED;

        // init alpha-beta
        this.ab = new AlphaBeta(cpuMark);
    }

    /**
     * iterative deepening + alpha-beta
     * on garde le meilleur résultat COMPLET avant timeout
     */
    public ArrayList<Move> getNextMove(Board board) {

        // reset compteur + timer
        numExploredNodes = 0;
        ab.resetTimer(TIME_LIMIT_MS);

        ArrayList<Move> bestMoves = new ArrayList<>();
        int bestScore = Integer.MIN_VALUE;
        
        // Pre-order root moves: captures first, then advancement
        ArrayList<Move> rootMoves = orderRootMoves(board, board.getMoveList(cpuMark));

        // on augmente la profondeur petit à petit
        for (int depth = 1; depth <= MAX_DEPTH; depth++) {

            ArrayList<Move> candidateMoves = new ArrayList<>();
            int candidateScore = Integer.MIN_VALUE;

            // on teste tous les moves possibles
            for (Move move : rootMoves) {

                // stop si timeout
                if (ab.timedOut) break;

                // on joue le move
                board.play(move, cpuMark);

                // on lance alpha-beta sur la position
                int score = ab.search(
                        board,
                        opponentMark,
                        depth - 1,
                        Integer.MIN_VALUE + 1,
                        Integer.MAX_VALUE,
                        this
                );

                // on annule le move (très important)
                board.undoMove(move, cpuMark);

                // si timeout pendant search
                if (ab.timedOut) break;

                // meilleur score trouvé
                if (score > candidateScore) {
                    candidateScore = score;
                    candidateMoves.clear();
                    candidateMoves.add(move);
                }
                // égalité : on garde plusieurs moves
                else if (score == candidateScore) {
                    candidateMoves.add(move);
                }
            }

            // si on a fini cette profondeur sans timeout
            if (!ab.timedOut) {

                bestMoves = candidateMoves;
                bestScore = candidateScore;

                System.out.printf(
                        "Depth %d done : score: %d, nodes: %d%n",
                        depth, bestScore, numExploredNodes
                );

                // si on trouve une position quasi gagnante : inutile d’aller plus loin
                // BUT : on continue même si on perd, pour trouver le meilleur move parmi les pertes
                if (bestScore >= 29000) break;
            }
            else {
                // timeout : on garde le dernier résultat complet
                System.out.printf(
                        "Timeout at depth %d : keep depth %d%n",
                        depth, depth - 1
                );
                break;
            }
        }

        // fallback : si jamais rien trouvé (rare)
        return bestMoves.isEmpty()
                ? board.getMoveList(cpuMark)
                : bestMoves;
    }

    // appelé par alpha-beta pour compter les noeuds
    public void incrementNodeCounter() {
        numExploredNodes++;
    }
    
    /**
     * Order moves at root level: captures first, then advancement
     * Ensures better pruning at deeper levels
     */
    private ArrayList<Move> orderRootMoves(Board board, ArrayList<Move> moves) {
        ArrayList<Move> ordered = new ArrayList<>(moves);
        
        ordered.sort((m1, m2) -> {
            // Captures are best
            boolean m1Captures = m1.getCaptured() != Mark.EMPTY;
            boolean m2Captures = m2.getCaptured() != Mark.EMPTY;
            
            if (m1Captures != m2Captures) {
                return m1Captures ? -1 : 1;
            }
            
            // Then by advancement
            int m1Advance = (cpuMark == Mark.RED) ? m1.getTowardsRow() : -m1.getTowardsRow();
            int m2Advance = (cpuMark == Mark.RED) ? m2.getTowardsRow() : -m2.getTowardsRow();
            
            return Integer.compare(m2Advance, m1Advance);
        });
        
        return ordered;
    }

    // getters utiles
    public int  getNumExploredNodes()  { return numExploredNodes; }
    public Mark getCpuMark()           { return cpuMark; }
    public Mark getOpponentMark()      { return opponentMark; }
}