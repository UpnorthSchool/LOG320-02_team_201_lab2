package Game;
import java.util.ArrayList;

import Game.Algorythme.AlphaBeta;
import Game.Algorythme.MinMax;

public class CPUPlayer
{
    private int numExploredNodes;
    private Mark cpuMARK;
    private Mark opponentMARK;
    private ArrayList<Move> movePossibleUncheck = new ArrayList<>();
    public AlphaBeta abMinMax;
    public MinMax    minMax;
    private int depth = 8;

    public CPUPlayer(Mark cpu){
        setCpuMark(cpu);
        setOpponentMARK((cpu == Mark.RED) ? Mark.BLACK : Mark.RED);
        abMinMax = new AlphaBeta(cpu);
        minMax   = new MinMax(cpu);
    }

    public int getNumOfExploredNodes(){
        return numExploredNodes;
    }

    public ArrayList<Move> getNextMoveMinMax(Board board)
    {
        numExploredNodes = 0;
        int bestScore = Integer.MIN_VALUE;
        ArrayList<Move> bestMoves = new ArrayList<>();

        for (Move move : board.getMoveList(cpuMARK))
        {
            board.play(move, cpuMARK);
            int score = minMax.minMax(board, opponentMARK, this, depth);
            board.undoMove(move, cpuMARK);

            if (score > bestScore)
            {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            }
            else if (score == bestScore)
            {
                bestMoves.add(move);
            }
        }

        System.out.println("nb node explored : " + getNumOfExploredNodes());
        System.out.println("Bestscore found = " + bestScore);
        return bestMoves;
    }

    public ArrayList<Move> getNextMoveAB(Board board)
    {
        numExploredNodes = 0;
        long startTime = System.currentTimeMillis();
        long timeLimit = 4200;

        ArrayList<Move> bestMoves = new ArrayList<>();
        int bestScore = Integer.MIN_VALUE;

        abMinMax.resetTimer(startTime, timeLimit);

        for (int currentDepth = 1; currentDepth <= 8; currentDepth++)
        {
            ArrayList<Move> candidateMoves = new ArrayList<>();
            int             candidateScore = Integer.MIN_VALUE;

            for (Move move : board.getMoveListOrdered(cpuMARK))
            {
                if (abMinMax.timeOut) break;

                board.play(move, cpuMARK);
                // Pass depth-1 to alpha-beta; true for isMyTurn means it's opponent's turn next
                int score = abMinMax.alphaBeta(
                    board,
                    opponentMARK,
                    this,
                    Integer.MIN_VALUE + 1,
                    Integer.MAX_VALUE,
                    currentDepth - 1
                );
                board.undoMove(move, cpuMARK);

                if (abMinMax.timeOut) break;

                if (score > candidateScore)
                {
                    candidateScore = score;
                    candidateMoves.clear();
                    candidateMoves.add(move);
                }
                else if (score == candidateScore)
                {
                    candidateMoves.add(move);
                }
            }

            if (!abMinMax.timeOut)
            {
                System.out.println("Depth " + currentDepth + " completee, score = " + candidateScore);

                // ── WHY THE ODD/EVEN CHECK ─────────────────────────────────────────────
                //
                // The root loop plays ONE cpu ply before calling alphaBeta(depth-1).
                // Total plies searched = currentDepth:
                //
                //   Odd  depth (1,3,5,7) → last ply belongs to CPU → leaf reflects
                //                          CPU's extra advancement → score is POSITIVE
                //                          and meaningful.
                //
                //   Even depth (2,4,6,8) → last ply belongs to opponent → from any
                //                          symmetric position both sides are equally
                //                          advanced → evaluation returns ~0 for ALL
                //                          moves → AI cannot distinguish good from bad.
                //
                // Fix: only commit to results from ODD depths (stable, positive scores).
                //
                // Exception 1: forced win/loss found (|score| >= 29000) — always commit
                //              regardless of parity so we never miss a forced win.
                //
                // Exception 2: if the even-depth score is CLEARLY better than what we
                //              had before (candidateScore > bestScore + 50), it found
                //              real asymmetry (e.g. one move leads to a capture) → commit.
                // ──────────────────────────────────────────────────────────────────────
                boolean forcedTerminal  = Math.abs(candidateScore) >= 29000;
                boolean clearlyBetter   = candidateScore > bestScore + 50;
                boolean isOddDepth      = (currentDepth % 2 == 1);

                if (isOddDepth || forcedTerminal || clearlyBetter)
                {
                    bestMoves = candidateMoves;
                    bestScore = candidateScore;
                }
            }
            else
            {
                System.out.println("Timeout a depth " + currentDepth + ", on garde depth " + (currentDepth - 1));
                break;
            }
        }

        System.out.println("nb node explored : " + getNumOfExploredNodes());
        System.out.println("Bestscore found = " + bestScore);

        System.out.print("Move ayant meme score :\n");
        for (Move m : bestMoves)
            System.out.print(m.getFromCol() + "" + m.getFromRow() + " ; ");
        System.out.println();

        return bestMoves.isEmpty() ? board.getMoveList(cpuMARK) : bestMoves;
    }

    public ArrayList<Move> getMovePossibleUncheck(Board board)
    {
        movePossibleUncheck = board.getMoveList(cpuMARK);
        return movePossibleUncheck;
    }

    public void setCpuMark(Mark cpu)        { cpuMARK     = cpu; }
    public Mark getCpuMark()                { return cpuMARK; }
    public void setOpponentMARK(Mark o)     { opponentMARK = o; }
    public Mark getOpponentMark()           { return opponentMARK; }
    public void incrementNodeCounter()      { this.numExploredNodes++; }
}