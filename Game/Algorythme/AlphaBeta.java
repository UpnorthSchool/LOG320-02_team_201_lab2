package Game.Algorythme;

import Game.Board;
import Game.CPUPlayer;
import Game.Mark;
import Game.Move;

public class AlphaBeta {

    // La marque appartenant au joueur IA (ne change jamais durant la partie)
    private Mark cpuMARK;

    public AlphaBeta(Mark cpuMark)
    {
        this.cpuMARK = cpuMark;
    }

    private Mark getOpponentMark(Mark mark)
    {
        return (mark == Mark.RED) ? Mark.BLACK : Mark.RED;
    }

    public int alphaBeta(Board board, Mark alphaBetaMark, CPUPlayer nbExploredNode, int alpha, int beta, int depth)
    {
        nbExploredNode.incrementNodeCounter();

        // --- Verification des conditions terminales en PREMIER ---
        // Identique au MinMax : on verifie la victoire avant toute evaluation.
        // Le score est ajuste par la profondeur pour privilegier les victoires rapides.
        if (board.verifierVictoire(cpuMARK))
        {
            return 30000 + depth;
        }
        if (board.verifierVictoire(getOpponentMark(cpuMARK)))
        {
            return -30000 - depth;
        }

        // --- Condition d'arret par profondeur ---
        if (depth == 0)
        {
            return board.evaluate(cpuMARK);
        }

        // --- Determination du joueur courant ---
        boolean isMax = (alphaBetaMark == cpuMARK);

        if (isMax)
        {
            int bestScore = Integer.MIN_VALUE;

            for (Move move : board.getMoveList(alphaBetaMark))
            {
                board.play(move, alphaBetaMark);
                int score = alphaBeta(board, getOpponentMark(alphaBetaMark), nbExploredNode, alpha, beta, depth - 1);
                board.undoMove(move, alphaBetaMark);

                bestScore = Math.max(bestScore, score);

                // Bug corrige 1 : mettre a jour alpha AVANT de verifier la coupure
                // Dans votre version, vous mettiez alpha = Math.max(bestScore, alpha) APRES le break,
                // ce qui signifie qu'alpha n'etait jamais mis a jour correctement.
                alpha = Math.max(alpha, bestScore);

                // Coupure beta : l'adversaire (minimiseur) ne choisira jamais cette branche
                // car il a deja une option meilleure (beta) ailleurs.
                if (bestScore >= beta)
                {
                    break;
                }
            }
            return bestScore;
        }
        else
        {
            int bestScore = Integer.MAX_VALUE;

            for (Move move : board.getMoveList(alphaBetaMark))
            {
                board.play(move, alphaBetaMark);
                int score = alphaBeta(board, getOpponentMark(alphaBetaMark), nbExploredNode, alpha, beta, depth - 1);
                board.undoMove(move, alphaBetaMark);

                bestScore = Math.min(bestScore, score);

                // Bug corrige 1 : mettre a jour beta AVANT de verifier la coupure
                beta = Math.min(beta, bestScore);

                // Coupure alpha : le maximiseur ne choisira jamais cette branche
                // car il a deja une option meilleure (alpha) ailleurs.
                if (bestScore <= alpha)
                {
                    break;
                }
            }
            return bestScore;
        }
    }
}