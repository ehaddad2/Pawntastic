import Pawntastic.*;
import Pawntastic.Game.Move;
import java.util.ArrayList;
import java.util.Scanner;
public class Program {
    public static void main(String[] args) {
        int boardSize = 4;
        AI ai = null;
        boolean playerTurn = false;
        boolean verbose = false;
        double MinMaxDepth = Double.POSITIVE_INFINITY;
        System.out.println("Welcome to Pawntastic!\nPlease indicate your desired size for your board (must be >=4):");
        Scanner s = new Scanner(System.in);
        boardSize = s.nextInt();
        s.nextLine();
        assert(boardSize >=4);
        Game g = new Game(boardSize);

        // choose opponent
        System.out.println("Choose your opponent:");
        System.out.println("1. An agent that uses MINIMAX");
        System.out.println("2. An agent that uses MINIMAX with alpha-beta pruning");
        System.out.println("3. An agent that uses H-MINIMAX with a fixed depth cutoff");
        System.out.println("4. An agent that uses H-MINIMAX with a fixed depth cutoff and alpha-beta pruning");
        
        int opponentChoice = s.nextInt();
        s.nextLine(); 

        playerTurn = chooseInitialPlayer(s);

        System.out.println("Would you like to trace opponent agent? y/n:");
        verbose = traceAgent(s);
        if (verbose) {
            System.out.println("Tracking agent...\n");
        }
        else {
            System.out.println("Not tracking agent\n");
        }

        switch (opponentChoice) {
            case 1:
            ai = new AI(0, playerTurn);
            break;
            
            case 2:
            ai = new AI(1,playerTurn,MinMaxDepth, true);
            break;

            case 3:
            System.out.println("Enter your fixed depth cutoff for AI type 3");
            MinMaxDepth = s.nextInt();
            s.nextLine();
            ai = new AI(1, playerTurn, MinMaxDepth, false);
            break;

            case 4:
            System.out.println("Enter your fixed depth cutoff for AI type 4");
            MinMaxDepth = s.nextInt();
            s.nextLine();
            ai = new AI(1, playerTurn, MinMaxDepth, true);
            break;

            default:
            System.out.println("ERROR: cannot parse type of AI you want to use, please enter 1, 2, 3, or 4 for the type.\nTerminating...");
            System.exit(1);
        }

        while(!g.inTermState) {
            g.paintBoard();

            if (playerTurn) {
                System.out.println("Your move: ");
                String rawMove = s.nextLine();
                if (!g.Move(g.processMove(encodeMove(rawMove, boardSize)))) {
                    System.out.println("---INVALID MOVE---");
                    continue;
                }
                playerTurn = false;
            }

            else {
                System.out.println("AI thinking... \n");
                Move aiMove = ai.callAI(g);
                if (verbose) {
                    System.out.println("AI move: " + decodeMove(new int[] {aiMove.prevRow, aiMove.prevCol, aiMove.row, aiMove.col}, boardSize));
                }
                g.Move(aiMove);
                playerTurn = true;
            }
        }

        g.paintBoard();
        if (g.utilityVal == 0) {
            System.out.println("Player Won!");
        }
        else if(g.utilityVal == 1) {
            System.out.println("AI Won!");
        }
        else {
            System.out.println("Tie!");
        }
    s.close();
    }   

    //encode and decode methods for pawn locations
    public static int[] encodeMove(String move, int boardSize) {// format ex: a2 b3
        char[] decompMove = move.toCharArray();

        int c = decompMove[0] - (int)'a';//b-a --> 0
        int r = boardSize - Character.getNumericValue(decompMove[1]);//size - 4 --> 0
        int destC = decompMove[3] - (int)'a';
        int destR = boardSize - Character.getNumericValue(decompMove[4]);
        
        return new int[]{r,c,destR,destC};
    }

    public static String decodeMove(int[] encodedMove, int boardSize) {
        int r = encodedMove[0];
        int c = encodedMove[1];
        int destR = encodedMove[2];
        int destC = encodedMove[3];
    
        char startColumn = (char) ('a' + c);
        int startRow = boardSize - r;
    
        char destinationColumn = (char) ('a' + destC);
        int destinationRow = boardSize - destR;
    
        return startColumn + String.valueOf(startRow) + " -> " + destinationColumn + String.valueOf(destinationRow);
    }    

    //returns true if user wants to play first (white)
    public static boolean chooseInitialPlayer(Scanner s) {
        System.out.print("Would you like to play first or would you like black(AI) to play first? (1) to play first, (0) to let AI play first");
        int choice = s.nextInt();
        s.nextLine();
        return choice == 1;
    }

    public static boolean traceAgent(Scanner s) {
        if (s.nextLine().equals("y")) {
            return true;
        }
        return false;
    }

    //displays legal operations at each point in game
    public static void displayLegalOps(ArrayList<Object> arr)
    {
        for (int i = 0; i < arr.size(); i++) {
            System.out.print(arr.get(i).toString() + " ");
        }
        System.out.println();
    }
}
