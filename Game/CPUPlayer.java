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
    //changement pour tester iterative deepening, on commence a un et on augmente tant quil y a du temps.
    private int depth = 8;

    public CPUPlayer(Mark cpu){
        setCpuMark(cpu);
        setOpponentMark((cpu == Mark.RED) ? Mark.BLACK : Mark.RED);
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
        long timeLimit = 4200; // plus conservateur

        ArrayList<Move> bestMoves = new ArrayList<>();
        int bestScore = Integer.MIN_VALUE;

        abMinMax.resetTimer(startTime, timeLimit);

        for (int currentDepth = 1; currentDepth <= 8; currentDepth++)
        {
            ArrayList<Move> candidateMoves = new ArrayList<>();
            int candidateScore = Integer.MIN_VALUE;

            for (Move move : board.getMoveListOrdered(cpuMARK))
            {
                if (abMinMax.timeOut) break; // timeout detecte dans la recursion

                board.play(move, cpuMARK);
                int score = abMinMax.alphaBeta(board, opponentMARK, this, Integer.MIN_VALUE, Integer.MAX_VALUE, currentDepth);
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
                bestMoves = candidateMoves;
                bestScore = candidateScore;
                System.out.println("Depth " + currentDepth + " completee, score = " + bestScore);
            }
            else
            {
                System.out.println("Timeout a depth " + currentDepth + ", on garde depth " + (currentDepth - 1));
                break;
            }
        }

        System.out.println("nb node explored : " + getNumOfExploredNodes());
        System.out.println("Bestscore found = " + bestScore);
        return bestMoves.isEmpty() ? board.getMoveList(cpuMARK) : bestMoves;
    }

    public ArrayList<Move> getMovePossibleUncheck(Board board)
    {
        movePossibleUncheck = board.getMoveList(cpuMARK);
        return movePossibleUncheck;
    }

    public void setCpuMark(Mark cpu)       { cpuMARK = cpu; }
    public Mark getCpuMark()               { return cpuMARK; }
    public void setOpponentMark(Mark o)    { opponentMARK = o; }
    public Mark getOpponentMark()          { return opponentMARK; }
    public void incrementNodeCounter()     { this.numExploredNodes++; }
}