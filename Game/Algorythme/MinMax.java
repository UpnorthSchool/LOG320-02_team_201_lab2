package Game.Algorythme;

import Game.*;

public class MinMax {

    // La marque appartenant au joueur IA (ne change jamais durant la partie)
    private Mark cpuMARK;

    public MinMax(Mark cpuMark)
    {
        this.cpuMARK = cpuMark;
    }

    // Retourne la marque adverse
    private Mark getOpponentMark(Mark mark)
    {
        return (mark == Mark.RED) ? Mark.BLACK : Mark.RED;
    }

    public int minMax(Board board, Mark minMaxMark, CPUPlayer nbExploredNode, int depth)
    {
        nbExploredNode.incrementNodeCounter();

        // --- Verification des conditions terminales en PREMIER ---
        // On verifie la victoire avant toute evaluation heuristique.
        // Le score est ajuste par la profondeur restante :
        //   - une victoire rapide (depth eleve) vaut plus qu'une victoire lointaine
        //   - une defaite rapide (depth eleve) est pire qu'une defaite lointaine
        // Cela pousse l'IA a gagner le plus vite possible et a perdre le plus lentement possible.

        if (board.verifierVictoire(cpuMARK))
        {
            // L'IA a gagne : score maximum, bonifie par la profondeur restante
            return 30000 + depth;
        }

        if (board.verifierVictoire(getOpponentMark(cpuMARK)))
        {
            // L'adversaire a gagne : score minimum, penalise par la profondeur restante
            return -30000 - depth;
        }

        // --- Condition d'arret par profondeur ---
        // On atteint la feuille de l'arbre : on evalue la position heuristiquement.
        // L'evaluation est TOUJOURS du point de vue du cpuMARK (jamais du joueur courant).
        if (depth == 0)
        {
            return board.evaluate(cpuMARK);
        }

        // --- Determination du joueur courant ---
        // isMax = vrai si c'est le tour de l'IA (on cherche a maximiser)
        // isMax = faux si c'est le tour de l'adversaire (on cherche a minimiser)
        boolean isMax = (minMaxMark == cpuMARK);

        if (isMax)
        {
            // Tour de l'IA : on cherche le meilleur score possible
            int bestScore = Integer.MIN_VALUE;

            for (Move move : board.getMoveList(minMaxMark))
            {
                board.play(move, minMaxMark);
                int score = minMax(board, getOpponentMark(minMaxMark), nbExploredNode, depth - 1);
                board.undoMove(move, minMaxMark);

                bestScore = Math.max(bestScore, score);
            }

            return bestScore;
        }
        else
        {
            // Tour de l'adversaire : on suppose qu'il joue optimalement contre nous
            int bestScore = Integer.MAX_VALUE;

            for (Move move : board.getMoveList(minMaxMark))
            {
                board.play(move, minMaxMark);
                int score = minMax(board, getOpponentMark(minMaxMark), nbExploredNode, depth - 1);
                board.undoMove(move, minMaxMark);

                bestScore = Math.min(bestScore, score);
            }

            return bestScore;
        }
    }
}