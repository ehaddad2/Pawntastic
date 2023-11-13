package Pawntastic;
import Pawntastic.Game.Move;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.ArrayList;
import Pawntastic.Game.*;

public class AI {
    int aiType = 0;
    double depth = Double.POSITIVE_INFINITY;
    boolean playerTurn = false;
    boolean useABPruning = true;
    
    public AI(int aiType, boolean playerTurn) {
        this.aiType = aiType;
        this.playerTurn = playerTurn;
    }

    public AI(int aiType, boolean playerTurn, double depth, boolean useABPruning) {
        this.aiType = aiType;
        this.depth = depth;
        this.playerTurn = playerTurn;
        this.useABPruning = useABPruning;
    }

    public Move callAI(Game g) {
        Move m = null;
        playerTurn = false;
        if (this.aiType == 0) {
            m = MinMax(g);
        }
        else if (this.aiType == 1) {
            m = MinMaxEnhanced(g, this.depth);
        }
        return m;
    }

    /*---REGULAR MINMAX---*/
    private Move MinMax(Game g) {
        HashMap<Move, Double> maxVals = new HashMap<>();
        ArrayList<Move> possibleMoves = g.getLegalOps(this.playerTurn);
        for (Move move : possibleMoves) {
            g.Move(move);
            maxVals.put(move, MinMaxHelper(g, !this.playerTurn));
            g.undoMove();
        }
        return Collections.max(maxVals.entrySet(), Map.Entry.comparingByValue()).getKey();//find key of max val
    }

    private Double MinMaxHelper(Game g, boolean playerTurn) {
        if (g.inTermState) {
            return g.utilityVal;
        }

        if (!playerTurn) {//MAX
            Double highestVal = Double.NEGATIVE_INFINITY;
            ArrayList<Move> possibleMoves = g.getLegalOps(playerTurn);
            for (Move m : possibleMoves) {
                g.Move(m);
                playerTurn = true;
                highestVal = Math.max(highestVal, MinMaxHelper(g, playerTurn));
                g.undoMove();
            }
            return highestVal;
        }

        else {//MIN
            Double lowestVal = Double.POSITIVE_INFINITY;
            ArrayList<Move> possibleMoves = g.getLegalOps(playerTurn);
            for (Move m : possibleMoves) {
                g.Move(m);
                playerTurn = false;
                lowestVal = Math.min(lowestVal, MinMaxHelper(g, playerTurn));
                g.undoMove();
            }
            return lowestVal;
        }
    }

    /*---ENHANCED MINMAX---*/
    private Move MinMaxEnhanced(Game g, double depth) {
        return MinMaxEnhancedHelper(g, false, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, depth).getMove();
    }

    private MoveTuple MinMaxEnhancedHelper(Game g, boolean playerTurn, double alpha, double beta, double depthRemaining) {
        if (g.inTermState) {
            return new MoveTuple(null, g.utilityVal);
        }

        if (depthRemaining == 0) {
            return heuristic(g, playerTurn);
        }

        if (!playerTurn) {
            Double highestVal = Double.NEGATIVE_INFINITY;
            Move highestValMove = null;
            ArrayList<Move> possibleMoves = g.getLegalOps(playerTurn);
            for (Move m : possibleMoves) {
                g.Move(m);
                playerTurn = true;
                MoveTuple MT = MinMaxEnhancedHelper(g, playerTurn, alpha, beta, depthRemaining-1);
                if (MT.getUtility() > highestVal) {
                    highestVal = MT.getUtility();
                    highestValMove = m;
                    alpha = Math.max(alpha, highestVal);
                }
                g.undoMove();

                if (this.useABPruning) {
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            return new MoveTuple(highestValMove, highestVal);
        }

        else {
            Double lowestVal = Double.POSITIVE_INFINITY;
            Move lowestValMove = null;
            ArrayList<Move> possibleMoves = g.getLegalOps(playerTurn);
            for (Move m : possibleMoves) {
                g.Move(m);
                playerTurn = false;
                MoveTuple MT = MinMaxEnhancedHelper(g, playerTurn, alpha, beta, depthRemaining-1);
                if (MT.getUtility() < lowestVal) {
                    lowestVal = MT.getUtility();
                    lowestValMove = m;
                    beta = Math.min(beta, lowestVal);
                }
                g.undoMove();

                if (this.useABPruning) {
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            return new MoveTuple(lowestValMove, lowestVal);
        }
    }

    private MoveTuple heuristic(Game g, boolean player) {
        double P_NPA = 0;//increases as player pawns get to winning side
        double E_NPA = 0;
        double P_DPA = 0;//increases when there are less pawns in front of player pawns
        double E_DPA = 0;
        double w1=0.15, w2=0.35;
        double heuristic = 0;//0 <= heuristic <= 1, will act as terminal node

        for (Pawn p : g.PlayerPawns) {
            P_NPA += ((g.size-1)-p.getRow())/(double)(g.size-1);
            P_DPA += emptyRowsAheadCalc(p, g, true);
        }
        P_NPA *= w2/(double)g.PlayerPawns.size();
        P_DPA *= w1/(double)g.PlayerPawns.size();

        for (Pawn p : g.AIPawns) {
            E_NPA += (p.getRow())/(double)(g.size-1);
            E_DPA += emptyRowsAheadCalc(p, g, false);
        }

        E_NPA *= w2/g.AIPawns.size();
        E_DPA *= w1/g.AIPawns.size();

        heuristic = (E_NPA + E_DPA - P_NPA - P_DPA+2)/g.size;
        return new MoveTuple(null, heuristic);
    }

    private double emptyRowsAheadCalc(Pawn p, Game g, boolean player) {
        double numEmptyRows = 0;
        double numTotalRows = 0;

        if (player) {
            for (int i = p.getRow()-1; i >=0; i--) {
                numTotalRows++;
                if (g.board.get(i).get(p.getCol()) == null) {
                    numEmptyRows++;
                }
            }
        }
        else {
            for (int i = p.getRow()+1; i < g.size; i++) {
                numTotalRows++;
                if (g.board.get(i).get(p.getCol()) == null) {
                    numEmptyRows++;
                }
            }
        }
        return numEmptyRows/numTotalRows;
    }
    static private class MoveTuple {
        Move m = null;
        Double utility = -1.0;

        MoveTuple(Move m, Double utlity) {
            this.m = m;
            this.utility = utlity;
        }

        private Move getMove() {
            return this.m;
        }

        private Double getUtility() {
            return this.utility;
        }
    }
}
