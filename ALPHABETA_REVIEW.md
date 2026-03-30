# AlphaBeta Implementation Review

## Critical Issues Found

### 1. **CRITICAL BUG: Quiescence Search Doesn't Handle MIN Nodes Correctly**

**Location:** `AlphaBeta.java`, `quiescence()` method

**Problem:**
The quiescence search always maximizes `alpha`, but it can be called from both MAX and MIN nodes. When called from a MIN node, it should minimize `beta` instead.

Currently:
```java
private int quiescence(Board board, Mark mark, ...) {
    int standPat = board.evaluate(cpuMARK);
    
    if (standPat >= beta) return beta;      // ← Works for MAX
    if (standPat > alpha) alpha = standPat; // ← Works for MAX
    
    for (Move move : board.getMoveListOrdered(mark, null, null)) {
        // ... always updates alpha (MAX behavior)
        if (score >= beta) return beta;
        if (score > alpha) alpha = score;   // ← MAX only!
    }
    return alpha;
}
```

**Why It's Wrong:**
- When quiescence is called from a MIN node (opponent's turn), you're trying to find the **minimum** score, not maximum
- The current code always treats it as a MAX node
- This inverts the search logic for opponent moves

**Example of Impact:**
```
Opponent at depth 0:
  Option A: leads to score -100 (bad for CPU)  
  Option B: leads to score -50  (less bad for CPU)
  
Expected: Return -100 (minimum)
Actual: Returns -50 (because it's maximizing alpha)
Result: CPU thinks opponent's move is better than it is
```

### 2. **Solution: Add MAX/MIN Branching to Quiescence**

Replace the entire `quiescence()` method with proper MAX/MIN handling:

```java
private int quiescence(Board board, Mark mark, CPUPlayer nbExploredNode, int alpha, int beta) {
    if (System.currentTimeMillis() - startTime > timeLimit) {
        timeOut = true;
        return 0;
    }

    nbExploredNode.incrementNodeCounter();

    int standPat = board.evaluate(cpuMARK);
    
    boolean isMax = (mark == cpuMARK);
    
    if (isMax) {
        // MAX node: maximize
        if (standPat >= beta) return beta;
        if (standPat > alpha) alpha = standPat;

        for (Move move : board.getMoveListOrdered(mark, null, null)) {
            if (move.getCaptured() == null || move.getCaptured() == Mark.EMPTY) continue;

            board.play(move, mark);
            int score = quiescence(board, getOpponentMark(mark), nbExploredNode, alpha, beta);
            board.undoMove(move, mark);

            if (timeOut) return 0;

            if (score >= beta) return beta;
            if (score > alpha) alpha = score;
        }
        return alpha;
    } else {
        // MIN node: minimize
        if (standPat <= alpha) return alpha;
        if (standPat < beta) beta = standPat;

        for (Move move : board.getMoveListOrdered(mark, null, null)) {
            if (move.getCaptured() == null || move.getCaptured() == Mark.EMPTY) continue;

            board.play(move, mark);
            int score = quiescence(board, getOpponentMark(mark), nbExploredNode, alpha, beta);
            board.undoMove(move, mark);

            if (timeOut) return 0;

            if (score <= alpha) return alpha;
            if (score < beta) beta = score;
        }
        return beta;
    }
}
```

### 3. **Secondary Issue: Victory Check Depth Penalty/Bonus**

**Location:** `AlphaBeta.java`, `alphaBeta()` method

**Current Code:**
```java
if (board.verifierVictoire(cpuMARK))
    return 30000 + depth;  // ← Rewards LONGER wins!

if (board.verifierVictoire(getOpponentMark(cpuMARK)))
    return -30000 - depth; // ← Penalizes FASTER opponent wins!
```

**Problem:**
The depth signs are inverted! You want to:
- Prefer **faster** wins (prefer depth = 5 win over depth = 10 win)
- Avoid **faster** losses (avoid depth = 2 loss; prefer depth = 10 delay)

**Fix:**
```java
if (board.verifierVictoire(cpuMARK))
    return 30000 - depth;  // ← Prefer faster wins (lower depth = higher score)

if (board.verifierVictoire(getOpponentMark(cpuMARK)))
    return -30000 + depth; // ← Prefer to delay losses (lower depth = lower score)
```

---

## Summary of Changes Needed

| Issue | Severity | Fix |
|-------|----------|-----|
| Quiescence doesn't handle MIN nodes | **CRITICAL** | Add MAX/MIN branching to quiescence |
| Victory depth bonus inverted | **HIGH** | Change `+ depth` to `- depth` and `-depth` to `+depth` |

## Testing Recommendation

After fixes, test:
1. CPU plays against itself at different depths - should show stronger play
2. CPU at depth 6+ should make correct tactical sacrifices
3. Opponent moves should not be overvalued in captured scenarios
