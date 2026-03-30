package Game;
import java.util.ArrayList;
import java.util.List;

// IMPORTANT: Il ne faut pas changer la signature des méthodes
// de cette classe, ni le nom de la classe.
public class Board
{
    //[row][column]
    private Mark[][] board;
    private int boardSize = 8;
    private int redPieceCounter         = 0 ;
    private int blackPieceCounter       = 0 ;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    public Board() {
        board = new Mark[boardSize][boardSize];
        for(int xCol = 0; xCol < boardSize; xCol++){
            for(int yLig = 0; yLig < boardSize; yLig++){
                board[xCol][yLig] = Mark.EMPTY;
            }
        }
    }

    public Mark[][] getBoard(){ return board; }

    public void play(Move m, Mark mark)
    {
        Mark contenuCase = board[m.getTowardsCol()][m.getTowardsRow()];
        m.setCaptured(contenuCase);

        if (contenuCase == Mark.RED)         redPieceCounter--;
        else if (contenuCase == Mark.BLACK)  blackPieceCounter--;

        board[m.getTowardsCol()][m.getTowardsRow()] = mark;
        board[m.getFromCol()][m.getFromRow()]        = Mark.EMPTY;
    }

    public void undoMove(Move moveUndo, Mark mark)
    {
        board[moveUndo.getFromCol()][moveUndo.getFromRow()]       = mark;
        board[moveUndo.getTowardsCol()][moveUndo.getTowardsRow()] = moveUndo.getCaptured();

        if (moveUndo.getCaptured() == Mark.RED)         redPieceCounter++;
        else if (moveUndo.getCaptured() == Mark.BLACK)  blackPieceCounter++;
    }

    public void display(ArrayList<Move> displayMove){
        for (Move dMove : displayMove) {
            System.out.println("From : [" + dMove.getFromCol() +", " + dMove.getFromRow() + "] To : [" + dMove.getTowardsCol()+ ","+dMove.getTowardsRow() + "]") ;
        }
    }

    public boolean getVictoire() { return true; }

    public boolean verifierVictoire(Mark mark)
    {
        boolean victoire = false;
        if(mark == Mark.BLACK)
        {
            for(int i = 0 ; i < boardSize ; i ++)
                if(board[i][0] == Mark.BLACK) return true;
            if(getRedPieceCounter() == 0) return true;
        }
        if(mark == Mark.RED)
        {
            for(int j = 0 ; j < boardSize ; j ++)
                if(board[j][7] == Mark.RED) return true;
            if(getBlackPieceCounter() == 0) return true;
        }
        return victoire;
    }

    public boolean estSurLeBoard(int xColPosition, int yLignePosition)
    {
        return xColPosition >= 0 && xColPosition < boardSize
            && yLignePosition >= 0 && yLignePosition < boardSize;
    }

    public boolean moveValide(int xColPosition, int yLignePosition)
    {
        return estSurLeBoard(xColPosition, yLignePosition)
            && board[xColPosition][yLignePosition] == Mark.EMPTY;
    }

    public ArrayList<Move> getMoveList(Mark markSide)
    {
        ArrayList<Move> moveAvailableNow = new ArrayList<>();
        int direction   = (markSide == Mark.RED) ? +1 : -1;
        Mark adversaire = (markSide == Mark.RED) ? Mark.BLACK : Mark.RED;

        for (int xCol = 0; xCol < getBoardSize(); xCol++)
        {
            for (int yLig = 0; yLig < getBoardSize(); yLig++)
            {
                if (getBoard()[xCol][yLig] == markSide)
                {
                    int ligneAvancement = yLig + direction;

                    if (moveValide(xCol, ligneAvancement))
                        moveAvailableNow.add(new Move(xCol, yLig, xCol, ligneAvancement));

                    if (estSurLeBoard(xCol - 1, ligneAvancement)
                        && getBoard()[xCol - 1][ligneAvancement] != markSide)
                    {
                        Move m = new Move(xCol, yLig, xCol - 1, ligneAvancement);
                        if (getBoard()[xCol - 1][ligneAvancement] == adversaire)
                            m.setCaptured(adversaire);
                        moveAvailableNow.add(m);
                    }

                    if (estSurLeBoard(xCol + 1, ligneAvancement)
                        && getBoard()[xCol + 1][ligneAvancement] != markSide)
                    {
                        Move m = new Move(xCol, yLig, xCol + 1, ligneAvancement);
                        if (getBoard()[xCol + 1][ligneAvancement] == adversaire)
                            m.setCaptured(adversaire);
                        moveAvailableNow.add(m);
                    }
                }
            }
        }
        return moveAvailableNow;
    }


    // =========================================================================
    //  FIXED evaluate — now requires knowing whose turn it is.
    //
    //  CHANGE REQUIRED IN YOUR MINMAX/ALPHABETA:
    //    At the root (AI turn) and every even ply: isMyTurn = true
    //    At every odd ply (opponent's turn):        isMyTurn = false
    //
    //  Simplest way: pass (depth % 2 == 0) as isMyTurn if root is depth 0.
    // =========================================================================
    public int evaluate(Mark mark, boolean isMyTurn)
    {
        Mark adversaire = (mark == Mark.RED) ? Mark.BLACK : Mark.RED;

        // ── Already-won states (should normally be caught by the minmax loop,
        //    but kept as safety net) ───────────────────────────────────────────
        if (verifierVictoire(mark))      return  50000;
        if (verifierVictoire(adversaire)) return -50000;

        // ── Tempo: minimum moves to reach goal ──────────────────────────────
        int nosTempoMin    = minMovesToWin(mark);
        int ennemyTempoMin = minMovesToWin(adversaire);

        if (nosTempoMin    == 0) return  50000;
        if (ennemyTempoMin == 0) return -50000;

        // ── BUG FIX #2: Turn-aware terminal detection ───────────────────────
        //
        //  OLD (broken): always checked both tempos the same way,
        //                ignoring whose turn it actually is.
        //
        //  NEW: if it's MY turn and I can reach the goal NOW → I win.
        //       If it's THEIR turn and they can reach the goal NOW → I lose.
        //       If both can reach in 1 and it's my turn → I win (I go first).
        //       If both can reach in 1 and it's their turn → I lose (they go first).
        //
        if (isMyTurn)
        {
            if (nosTempoMin == 1 && canReachGoalNow(mark))       return  50000;
            if (ennemyTempoMin == 1 && canReachGoalNow(adversaire)) return -49999; // They reply next move
        }
        else
        {
            if (ennemyTempoMin == 1 && canReachGoalNow(adversaire)) return -50000;
            if (nosTempoMin == 1 && canReachGoalNow(mark))           return  49999; // We reply next move
        }

        // ── BUG FIX #3: Passer detection (MOST IMPORTANT POSITIONAL CONCEPT) ─
        //
        //  A "passer" is a piece that NO enemy can intercept before it reaches
        //  the goal. In Breakthrough this is almost always a winning advantage.
        //
        int nosPassers    = countPassers(mark, adversaire);
        int ennemyPassers = countPassers(adversaire, mark);

        // A single unstoppable passer = decisive advantage
        if (nosPassers > 0 && ennemyPassers == 0) {
            // We have a passer, they don't — we win the race
            int distBonus = 5000 + (8 - nosTempoMin) * 500;
            return distBonus;
        }
        if (ennemyPassers > 0 && nosPassers == 0) {
            int distPenalty = 5000 + (8 - ennemyTempoMin) * 500;
            return -distPenalty;
        }
        if (nosPassers > ennemyPassers) {
            return 3000 + (nosPassers - ennemyPassers) * 800;
        }
        if (ennemyPassers > nosPassers) {
            return -(3000 + (ennemyPassers - nosPassers) * 800);
        }

        // ── Near-terminal threats (severe) ──────────────────────────────────
        int score = 0;

        if (ennemyTempoMin <= 2) score -= 15000;
        else if (ennemyTempoMin == 3) score -= 3000;

        if (nosTempoMin <= 2)    score += 15000;
        else if (nosTempoMin == 3) score += 3000;

        // ── Positional evaluation ────────────────────────────────────────────
        if (ennemyTempoMin > 3 && nosTempoMin > 3) {
            score += evaluatePositionQuality(mark, adversaire);
        } else {
            score += evaluateAdvancementIntensity(mark, adversaire);
        }

        return score;
    }

    // =========================================================================
    //  BACKWARD COMPATIBILITY: old signature without isMyTurn.
    //  Assumes it is the given mark's turn (maximizing node).
    //  Replace usages in your minmax with the 2-argument version.
    // =========================================================================
    public int evaluate(Mark mark)
    {
        return evaluate(mark, true);
    }


    // =========================================================================
    //  BUG FIX #1: canReachGoalNow — completely rewritten.
    //
    //  OLD (broken): added a canBeCapturedBy() guard that made no sense.
    //    If it is YOUR TURN, you move first — the opponent CANNOT capture you
    //    before you move. The canBeCapturedBy check caused this function to
    //    return false even for guaranteed wins, missing forced-win detection.
    //
    //  NEW: simply checks whether any legal move lands on the goal row.
    //    That is the only condition needed: if a move reaches the goal row,
    //    the game is over immediately — no response from the opponent.
    // =========================================================================
    private boolean canReachGoalNow(Mark mark)
    {
        int goalRow = (mark == Mark.RED) ? boardSize - 1 : 0;
        for (Move m : getMoveList(mark))
        {
            if (m.getTowardsRow() == goalRow) return true;
        }
        return false;
    }


    // =========================================================================
    //  NEW: countPassers — counts pieces that CANNOT be intercepted.
    //
    //  A piece at (x, y) is a "passer" if no enemy piece can reach any square
    //  in the piece's forward cone (columns x-dist..x+dist) before our piece
    //  does, where dist = remaining rows to goal.
    //
    //  Simplified version: an enemy piece can threaten column c if it is within
    //  diagonal reach of the passer's path. We check if any enemy is "close
    //  enough laterally AND ahead enough" to intercept.
    // =========================================================================
    private int countPassers(Mark mark, Mark adversaire)
    {
        int goalRow   = (mark == Mark.RED) ? boardSize - 1 : 0;
        int direction = (mark == Mark.RED) ? 1 : -1;
        int passers   = 0;

        for (int x = 0; x < boardSize; x++)
        {
            for (int y = 0; y < boardSize; y++)
            {
                if (board[x][y] != mark) continue;

                int distToGoal = Math.abs(goalRow - y);
                if (distToGoal == 0) { passers++; continue; } // already won

                boolean interceptable = false;

                // Check every enemy piece
                outer:
                for (int ex = 0; ex < boardSize; ex++)
                {
                    for (int ey = 0; ey < boardSize; ey++)
                    {
                        if (board[ex][ey] != adversaire) continue;

                        // How many moves does the enemy need to reach any square
                        // in our passer's path?
                        // Our passer travels from (x,y) toward (x, goalRow) but
                        // can shift ±1 per step diagonally.
                        // An enemy intercepts if its lateral distance to our column
                        // is <= its distance to our passer's current row (i.e., it
                        // can cut across to meet us).
                        //
                        // The enemy (in direction -direction) needs |ey - y| moves
                        // along our direction to reach our current row level.
                        // Laterally it can cover at most that many columns.
                        // Our passer needs distToGoal moves to reach the goal.
                        //
                        // Interception is possible if:
                        //   lateral_distance(ex, x) <= moves_enemy_has
                        //   AND the enemy is in front of our passer (closer to its goal)
                        //   i.e., |ey - goalRow| < distToGoal

                        int lateralDist   = Math.abs(ex - x);
                        int enemyDistToOurRow = Math.abs(ey - y);

                        // Enemy is "ahead" of our passer (between passer and goal)
                        boolean enemyAhead = (mark == Mark.RED)
                            ? (ey > y)   // RED moves up: enemy is ahead if ey > y
                            : (ey < y);  // BLACK moves down: enemy is ahead if ey < y

                        // Enemy can intercept if it has enough lateral reach
                        // and is positioned ahead of the passer or can cut across
                        if (enemyAhead && lateralDist <= enemyDistToOurRow)
                        {
                            interceptable = true;
                            break outer;
                        }

                        // Enemy behind but can catch up diagonally
                        // Enemy needs to cover lateralDist cols AND distToGoal rows
                        // before our passer covers distToGoal rows → impossible if
                        // enemy is behind AND lateral dist > 0, unless they're very close
                        if (!enemyAhead)
                        {
                            // Rows enemy is behind our passer (in enemy's own direction)
                            int enemyBehindRows = Math.abs(ey - y);
                            // For enemy to intercept, it needs distToGoal + enemyBehindRows
                            // moves total, but our passer only needs distToGoal.
                            // Only possible if lateralDist == 0 AND enemy is RIGHT behind us
                            // (could capture us before we move) — but that's a capture threat
                            // handled elsewhere.
                        }
                    }
                }

                if (!interceptable) passers++;
            }
        }
        return passers;
    }


    // =========================================================================
    //  isCompletelyBlocked — unchanged, kept for minMovesToWin
    // =========================================================================
    private boolean isCompletelyBlocked(int x, int y, Mark mark, int direction)
    {
        int nextY = y + direction;
        if (nextY < 0 || nextY >= boardSize) return true;

        boolean straightBlocked  = (board[x][nextY] != Mark.EMPTY);
        boolean diagLeftBlocked  = (x - 1 < 0)         || (board[x - 1][nextY] == mark);
        boolean diagRightBlocked = (x + 1 >= boardSize) || (board[x + 1][nextY] == mark);

        return straightBlocked && diagLeftBlocked && diagRightBlocked;
    }


    // =========================================================================
    //  minMovesToWin — improved: considers passer status for blocked penalty
    // =========================================================================
    private int minMovesToWin(Mark mark)
    {
        int best      = Integer.MAX_VALUE;
        int goalRow   = (mark == Mark.RED) ? boardSize - 1 : 0;
        int direction = (mark == Mark.RED) ? 1 : -1;

        for (int x = 0; x < boardSize; x++)
        {
            for (int y = 0; y < boardSize; y++)
            {
                if (board[x][y] == mark)
                {
                    int dist = Math.abs(goalRow - y);
                    if (dist == 0) return 0;

                    boolean bloque = isCompletelyBlocked(x, y, mark, direction);

                    // Reduced penalty from +5 to +2: being blocked one step doesn't
                    // mean you're stuck — diagonal capture can often unblock in 1 move.
                    int cout = bloque ? dist + 2 : dist;
                    if (cout < best) best = cout;
                }
            }
        }
        return best;
    }


    // =========================================================================
    //  evaluateAdvancementIntensity — unchanged
    // =========================================================================
    private int evaluateAdvancementIntensity(Mark mark, Mark adversaire)
    {
        int score = 0;
        int nosPieces      = (mark == Mark.RED) ? redPieceCounter   : blackPieceCounter;
        int piecesEnnemies = (mark == Mark.RED) ? blackPieceCounter : redPieceCounter;

        int[] advancementValue = {2, 8, 20, 50, 120, 300, 800, 3000};

        for (int y = 0; y < boardSize; y++)
        {
            for (int x = 0; x < boardSize; x++)
            {
                if (board[x][y] == mark)
                {
                    int rangee = (mark == Mark.RED) ? y : (boardSize - 1 - y);
                    score += advancementValue[rangee];
                }
                else if (board[x][y] == adversaire)
                {
                    int rangee = (adversaire == Mark.RED) ? y : (boardSize - 1 - y);
                    score -= advancementValue[rangee];
                }
            }
        }

        score += (nosPieces - piecesEnnemies) * 50;

        for (int y = 0; y < boardSize; y++)
        {
            for (int x = 0; x < boardSize; x++)
            {
                if (board[x][y] == mark && y > 0)
                {
                    int yDerriere = (mark == Mark.RED) ? y - 1 : y + 1;
                    boolean estProtege = false;
                    if (yDerriere >= 0 && yDerriere < boardSize)
                    {
                        if (x - 1 >= 0        && board[x - 1][yDerriere] == mark) estProtege = true;
                        if (x + 1 < boardSize && board[x + 1][yDerriere] == mark) estProtege = true;
                    }

                    if (estProtege) {
                        int rangee = (mark == Mark.RED) ? y : (boardSize - 1 - y);
                        score += 10 + (3 * rangee);
                    }
                }
            }
        }

        for (int x = 0; x < boardSize; x++)
        {
            for (int y = 0; y < boardSize; y++)
            {
                if (board[x][y] == adversaire)
                {
                    int rangeeAdversaire = (adversaire == Mark.RED) ? y : (boardSize - 1 - y);
                    if (rangeeAdversaire >= 4)
                    {
                        boolean estBloque = false;
                        int yVictoire  = (adversaire == Mark.RED) ? boardSize - 1 : 0;
                        int direction  = (yVictoire > y) ? 1 : -1;

                        for (int checkY = y + direction;
                            checkY >= 0 && checkY < boardSize;
                            checkY += direction)
                        {
                            if (board[x][checkY] == mark)                      { estBloque = true; break; }
                            if (x > 0           && board[x-1][checkY] == mark) { estBloque = true; break; }
                            if (x < boardSize-1 && board[x+1][checkY] == mark) { estBloque = true; break; }
                        }

                        if (!estBloque) score -= 200 * rangeeAdversaire;
                    }
                }
            }
        }

        return score;
    }


    // =========================================================================
    //  evaluatePositionQuality — unchanged
    // =========================================================================
    private int evaluatePositionQuality(Mark mark, Mark adversaire)
    {
        int score = 0;
        int nosPieces      = (mark == Mark.RED) ? redPieceCounter   : blackPieceCounter;
        int piecesEnnemies = (mark == Mark.RED) ? blackPieceCounter : redPieceCounter;

        int[] advancementValue = {3, 12, 30, 80, 200, 500, 1200, 2500};

        for (int y = 0; y < boardSize; y++)
        {
            for (int x = 0; x < boardSize; x++)
            {
                if (board[x][y] == mark)
                {
                    int rangee = (mark == Mark.RED) ? y : (boardSize - 1 - y);
                    score += advancementValue[rangee];
                }
                else if (board[x][y] == adversaire)
                {
                    int rangee = (adversaire == Mark.RED) ? y : (boardSize - 1 - y);
                    score -= advancementValue[rangee];
                }
            }
        }

        score += (nosPieces - piecesEnnemies) * 80;

        for (int y = 0; y < boardSize; y++)
        {
            for (int x = 0; x < boardSize; x++)
            {
                if (board[x][y] == mark)
                {
                    int rangee = (mark == Mark.RED) ? y : (boardSize - 1 - y);
                    int yDerriere = (mark == Mark.RED) ? y - 1 : y + 1;
                    boolean estProtege = false;
                    if (yDerriere >= 0 && yDerriere < boardSize)
                    {
                        if (x - 1 >= 0        && board[x - 1][yDerriere] == mark) estProtege = true;
                        if (x + 1 < boardSize && board[x + 1][yDerriere] == mark) estProtege = true;
                    }
                    if (estProtege) score += 20 + (5 * rangee);
                }
                else if (board[x][y] == adversaire)
                {
                    int rangee = (adversaire == Mark.RED) ? y : (boardSize - 1 - y);
                    int yDerriere = (adversaire == Mark.RED) ? y - 1 : y + 1;
                    boolean estProtege = false;
                    if (yDerriere >= 0 && yDerriere < boardSize)
                    {
                        if (x - 1 >= 0        && board[x - 1][yDerriere] == adversaire) estProtege = true;
                        if (x + 1 < boardSize && board[x + 1][yDerriere] == adversaire) estProtege = true;
                    }
                    if (estProtege) score -= 20 + (5 * rangee);
                }
            }
        }

        return score;
    }

    private int countPieceMobility(int x, int y, Mark mark)
    {
        if (board[x][y] != mark) return 0;
        int direction = (mark == Mark.RED) ? 1 : -1;
        int mobilityCount = 0;
        int nextY = y + direction;
        if (moveValide(x, nextY)) mobilityCount++;
        if (estSurLeBoard(x - 1, nextY) && board[x - 1][nextY] != mark) mobilityCount++;
        if (estSurLeBoard(x + 1, nextY) && board[x + 1][nextY] != mark) mobilityCount++;
        return mobilityCount;
    }

    public int passedPawn() { return 0; }

    public int getBoardSize() { return boardSize; }

    public ArrayList<Move> getMoveListOrdered(Mark markSide)
    {
        return getMoveListOrdered(markSide, null, null);
    }

    public ArrayList<Move> getMoveListOrdered(Mark markSide, Move killer1, Move killer2)
    {
        ArrayList<Move> moves = getMoveList(markSide);

        moves.sort((a, b) -> {
            int captureA = (a.getCaptured() != Mark.EMPTY && a.getCaptured() != null) ? 1 : 0;
            int captureB = (b.getCaptured() != Mark.EMPTY && b.getCaptured() != null) ? 1 : 0;
            if (captureB != captureA) return captureB - captureA;

            boolean killerA = a.equals(killer1) || a.equals(killer2);
            boolean killerB = b.equals(killer1) || b.equals(killer2);
            if (killerA != killerB) return killerB ? 1 : -1;

            int advA = (markSide == Mark.RED) ? a.getTowardsRow() : (7 - a.getTowardsRow());
            int advB = (markSide == Mark.RED) ? b.getTowardsRow() : (7 - b.getTowardsRow());
            if (advB != advA) return advB - advA;

            int centreA = 4 - Math.abs(a.getTowardsCol() - 3);
            int centreB = 4 - Math.abs(b.getTowardsCol() - 3);
            return centreB - centreA;
        });

        return moves;
    }

    public boolean hasWon(int scoreKeeper) { return false; }

    public void setRedPieceCounter(int valeur)   { redPieceCounter = valeur; }
    public void setBlackPieceCounter(int valeur) { blackPieceCounter = valeur; }
    public int getRedPieceCounter()  { return redPieceCounter; }
    public int getBlackPieceCounter() { return blackPieceCounter; }
}