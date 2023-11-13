package Pawntastic;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

public class Game {
    public int size = 4;
    public boolean inTermState = false;
    public double utilityVal = -1;
    public ArrayList<ArrayList<Pawn>> board;
    public ArrayList<Pawn> PlayerPawns = new ArrayList<Pawn>();
    public ArrayList<Pawn> AIPawns = new ArrayList<Pawn>();
    public Stack<Move> moveHistory = new Stack<>();

    public Game(int size) {
        this.size = size;

        board = new ArrayList<ArrayList<Pawn>>(size);
        for (int r = 0; r < size; r++) {
            ArrayList<Pawn> row = new ArrayList<>();
            
            for (int c = 0; c < size; c++) {
                row.add(null); 
            }
            
            board.add(row);
        }
        for (int c = 0; c < size; c++) {//initialize board/lists with starting pawns
            Pawn p = new Pawn(size-2, c, true);
            Pawn a = new Pawn(1, c, false);
            board.get(size-2).set(c,p);
            PlayerPawns.add(p);
            board.get(1).set(c,a);
            AIPawns.add(a);
        }
    }
    
    /*PRIMARY METHODS*/

    // main method for processing and making a valid move, returns false if invalid.

    public boolean validateMove(Move m) {
        boolean valid = false;
        //deconstruct move
        if (m == null) {
            return false;
        }
        Pawn p = m.pawn;
        int destRow = m.row;
        int destCol = m.col;
        
        //get attempted move type & validate
        if (p.isPlayer) {//Player move

            if((destRow == p.row-1) && (p.col == destCol)) {//Forward
                if(destRow >= 0){
                        if (this.board.get(destRow).get(destCol) == null) {//check obstacles
                        valid = true;
                    }
                }
            }
            else if ((destRow == p.row-2) && (p.col == destCol)) {//DForward
                if (destRow >= 0) {
                    if ((this.board.get(destRow+1).get(destCol) == null) && (this.board.get(destRow).get(destCol) == null) && (p.canMoveTwice)) {
                        valid = true;
                    }
                }
            }
            
            else if ((destRow == p.row-1) && (p.col-1 == destCol)) {//LDiag
                if ((destCol >= 0) && (destRow >= 0)) {
                    if(this.board.get(destRow).get(destCol) != null) {
                        if(this.board.get(destRow).get(destCol).isPlayer == false) {//ensure we're attacking enemy pawn
                            valid = true;
                        }
                    }
                }
            }
            else if ((destRow == p.row-1) && (p.col+1 == destCol)) {//RDiag
                if ((destCol < size) && (destRow >= 0)) {
                    if(this.board.get(destRow).get(destCol) != null) {
                        if(this.board.get(destRow).get(destCol).isPlayer == false) {
                            valid = true;
                        }
                    }
                }
            }
        }

        else {//AI move
            if((destRow == p.row+1) && (p.col == destCol)) {//Forward
                if(destRow < size){
                        if (this.board.get(destRow).get(destCol) == null) {//check obstacles
                        valid = true;
                    }
                }
            }
            else if ((destRow == p.row+2) && (p.col == destCol)) {//DForward
                if (destRow < size) {
                    if ((this.board.get(destRow-1).get(destCol) == null) && (this.board.get(destRow).get(destCol) == null) && (p.canMoveTwice)) {
                        valid = true;
                    }
                }
            }
            
            else if ((destRow == p.row+1) && (p.col+1 == destCol)) {//LDiag
                if ((destCol < size) && (destRow < size)) {
                    if(this.board.get(destRow).get(destCol) != null) {
                        if(this.board.get(destRow).get(destCol).isPlayer == true) {//ensure we're attacking enemy pawn
                            valid = true;
                        }
                    }
                }
            }
            else if ((destRow == p.row+1) && (p.col-1 == destCol)) {//RDiag
                if ((destCol >= 0) && (destRow < size)) {
                    if(this.board.get(destRow).get(destCol) != null) {
                        if(this.board.get(destRow).get(destCol).isPlayer == true) {
                            valid = true;
                        }
                    }
                }
            }
        }

        return valid;
    }
    public boolean Move(Move m) {
        double oldUtility = this.utilityVal;
        if (!validateMove(m)) {
            return false;
        }
        Pawn p = m.pawn;
        int destR = m.row;
        int destC = m.col;
        m.prevRow = p.row;
        m.prevCol = p.col;
        
        // process forward variants
        if (board.get(destR).get(destC) == null) {
            //update board
            board.get(p.row).set(p.col, null);
            board.get(destR).set(destC, p);
            //update pawn
            p.row = destR;
            p.col = destC;
        }
        // process diag attack variants
        else {
            Pawn toDelete = board.get(destR).get(destC);
            m.capturedPawn = toDelete;
            //update board
            board.get(p.row).set(p.col, null);
            board.get(destR).set(destC, p);
            //update pawn
            p.row = destR;
            p.col = destC;
            //process deletion
            if (toDelete.isPlayer) {
                PlayerPawns.remove(toDelete);
            }
            else {
                AIPawns.remove(toDelete);
            }
        }

        if (p.canMoveTwice) {
            p.canMoveTwice = false;
            m.forwardFlagChanged = true;
        }
        
        updateUtility(p);
        if (oldUtility != this.utilityVal) {
            m.prevUtility = oldUtility;
            m.utilityChanged = true;
        }
        moveHistory.push(m);// records state change history
        return true;
    }

    //undo most recent move and restores game state
    public void undoMove() {
        try {
            if (this.moveHistory.size() > 0) {
                Move m = this.moveHistory.pop();
                Pawn p = m.pawn;
                //restore pawn position
                board.get(m.prevRow).set(m.prevCol, p);
                board.get(p.row).set(p.col, null);
                p.row = m.prevRow;
                p.col = m.prevCol;

                //restore deletion if applicable
                if (m.capturedPawn != null) {
                    if (m.capturedPawn.isPlayer) {
                        this.PlayerPawns.add(m.capturedPawn);
                    }
                    else {
                        this.AIPawns.add(m.capturedPawn);
                    }
                    board.get(m.capturedPawn.row).set(m.capturedPawn.col, m.capturedPawn);
                }
                //restore move 2x flag
                if (m.forwardFlagChanged) {
                    p.canMoveTwice = true;
                }
                //restore utility value
                if (m.utilityChanged) {
                    this.inTermState = false;
                    this.utilityVal = m.prevUtility;
                }
            }
            else {
                throw new EmptyStackException();
            }
        } catch (Exception e){
            System.out.println("ERROR GETTING PREVIOUS MOVE: " + e.getMessage());
        }
    }

    // preprocesses a move and does initial validation check
    public Move processMove(int[] coords) {
        Pawn currPawn = null;
        Move m = null;
        int currRow = coords[0];
        int currCol = coords[1];
        int destRow = coords[2];
        int destCol = coords[3];

        if(this.board.get(currRow).get(currCol) != null) {
            currPawn = this.board.get(currRow).get(currCol);
            m = new Move(currPawn, destRow, destCol);
        }
        return m;
    }

        /*  
        - gets a list of legal moves for all pawns on the board
        - checks forward-double, forward, diagonal left, diagonal right
        - return structure: [pawn, destRow, destCol]
        */
    public ArrayList<Move> getLegalOps(boolean player) {
        ArrayList<Move> legalOps = new ArrayList<>();
        
        if (player) {
            for (Pawn p: this.PlayerPawns) {
                int r = p.row;
                int c = p.col;
                Move m = null;
                        
                //normal forward
                m = new Move(p,r-1,c);
                if (validateMove(m)) {
                    legalOps.add(m);
                }

                //double forward
                m = new Move(p,r-2,c);
                if (validateMove(m)) {
                    legalOps.add(m);
                }

                //left diagonal
                m = new Move(p, r-1, c-1);
                if (validateMove(m)) {
                    legalOps.add(m);
                }

                //right diagonal
                m = new Move(p, r-1, c+1);
                if (validateMove(m)) {
                    legalOps.add(m);
                }
            }
        }

        else {
            for (Pawn p: this.AIPawns) {
                int r = p.row;
                int c = p.col;
                Move m = null;

                //normal forward
                m = new Move(p,r+1,c);
                if (validateMove(m)) {
                    legalOps.add(m);
                }

                //double forward
                m = new Move(p,r+2,c);
                if (validateMove(m)) {
                    legalOps.add(m);
                }

                //left diagonal
                m = new Move(p, r+1, c+1);
                if (validateMove(m)) {
                    legalOps.add(m);
                }

                //right diagonal
                m = new Move(p, r+1, c-1);
                if (validateMove(m)) {
                    legalOps.add(m);
                }
            }
        }
        return legalOps;
    }
    // update global utility and term state values 
    public void updateUtility(Pawn p) {

        //player win
        if ((p.row == 0) && ((p.col >= 0) && (p.col < size))) {
            this.inTermState = true;
            this.utilityVal = 0;
        }
   
        //AI win
        else if ((p.row == size-1) && ((p.col >= 0) && (p.col < size))) {
            this.inTermState = true;
            this.utilityVal = 1;
        }

        //stalemate, since no legal operations available
        else if ((getLegalOps(true).size() == 0) && (getLegalOps(false).size() == 0)) {
            this.inTermState = true;
            this.utilityVal = 0.5;
        }
    }

    public void paintBoard() {
        String brdStr = "";
        
        // Generate the top label
        brdStr += "  ";
        for (char c = 'a'; c < 'a' + size; c++) {
            brdStr += c + " ";
        }
        brdStr += "\n";

        // Generate the board rows
        for (int r = 0; r < size; r++) {
            brdStr += (size - r) + " ";
            for (int c = 0; c < size; c++) {
                if (board.get(r).get(c) != null) {
                    if (board.get(r).get(c).isPlayer) {
                        //unicode for pawn
                        brdStr += "\u265F ";
                    } else {
                        brdStr += "\u2659 ";
                    }
                } else {
                    brdStr += ". ";
                }
            }
            brdStr += (size - r) + "\n";
        }

        // Generate the bottom label
        brdStr += "  ";
        for (char c = 'a'; c < 'a' + size; c++) {
            brdStr += c + " ";
        }
        brdStr += "\n";

        System.out.println(brdStr);
    }

    /*INTERNAL CLASS DEFINITIONS */
    static class Pawn {
    private int row;
    private int col;
    private boolean isPlayer;
    private boolean canMoveTwice = true;

        public Pawn(int r, int c, boolean isPlayer) {
            this.row = r;
            this.col = c;
            this.isPlayer = isPlayer;
        }

        public int getRow() {
            return this.row;
        }

        public int getCol() {
            return this.col;
        }
    }

    public static class Move {
        private Pawn pawn;
        public int row;
        public int col;
        public int prevRow = -1;
        public int prevCol = -1;
        public Pawn capturedPawn = null;
        private boolean forwardFlagChanged = false;
        private boolean utilityChanged = false;
        private double prevUtility = -1;
    
        public Move(Pawn pawn, int row, int col) {
            this.pawn = pawn;
            this.row = row;
            this.col = col;
        }
        

        @Override
        public String toString() {
            return "Move: {" +
                    "pawn=" + pawn.row + " " + pawn.col +
                    ", row=" + row +
                    ", col=" + col +
                    '}';
        }
    }
}
