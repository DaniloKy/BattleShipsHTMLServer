import java.util.HashMap;

public class Board {
    private char[][] board = new char[11][11];

    private int numAirCraft = 0;
    private int numBattleShip = 0;
    private int numSubmarine = 0;
    private int numDestroyer = 0;
    private int numPatrolBoat = 0;
    private int numCruiser = 0;
    private final int boardSize =  10;

    private HashMap<Character, Ship> ships = new HashMap<Character, Ship>();

    public Board() {
        initBoard();
    }
    public Board(String boardContent) {
        initBoard(boardContent);
    }

    public void initBoard() {
        board[0][0] = ' ';
        int num = 1;
        // first column
        for (int i = 48; i <= 57; i++) {
            board[num][0] = (char) i;
            num++;
        }
        // first row
        num = 1;
        for (char i = 48; i <= 57; i++) {
            board[0][num] = (char) i ;
            num++;
        }

        // Rest of the board
        for (int i = 1; i < board.length; i++) {
            for (int j = 1; j < board[0].length; j++) {
                board[i][j] = '~';
            }
        }
    }

    public void initBoard(String boardContent){
        board[0][0] = ' ';
        for (int i = 1; i <= boardSize; i++) {
            board[0][i] = (char) ('0' + (i - 1));
            board[i][0] = (char) ('A' + (i - 1));
        }

        String[] rows = boardContent.split("\n");
        for (int i = 1; i <= boardSize; i++) {
            char[] rowContent = rows[i - 1].toCharArray();
            for (int j = 1; j <= boardSize; j++) {
                char cell = rowContent[j - 1];
                board[i][j] = cell;
                if (cell != '~') {
                    Ship ship = ships.get(cell);
                    if (ship == null) {
                        ship = new Ship(cell);
                        ships.put(cell, ship);
                    }
                }
            }
        }
    }

    public HashMap<Character, Ship> getShips(){
        return this.ships;
    }

    public boolean isValidAttack(Coordinate crd) {
        int yCoordinate = crd.getY() + 1;
        int xCoordinate = crd.getX() + 1;

        if (xCoordinate < 1 || xCoordinate >= board[0].length) {
            return false;
        }
        if (yCoordinate < 1 || yCoordinate >= board.length) {
            return false;
        }
        return true;
    }


    public char resultHitMiss(Coordinate crd, Player playerAttacking, Player opposition) {
        int yCoordinate = crd.getY() + 1;
        int xCoordinate = crd.getX() + 1;


        if (opposition.getBoard()[yCoordinate][xCoordinate] == 'A') {
            System.out.println("Hit aircraft");
            numAirCraft++;
            if (numAirCraft == 5) {
                opposition.getShipByLetter('A').shipDestroyed();
                System.out.println("Your sunk your opponent's aircraft carrier!");
            }
        } else if (opposition.getBoard()[yCoordinate][xCoordinate] == 'B') {
            System.out.println("Hit batt");
            numBattleShip++;
            if (numBattleShip == 4) {
                opposition.getShipByLetter('B').shipDestroyed();
                System.out.println("Your sunk your opponent's battleship!");
            }
        } else if (opposition.getBoard()[yCoordinate][xCoordinate] == 'S') {
            System.out.println("Hit sub");
            numSubmarine++;
            if (numSubmarine == 3) {
                opposition.getShipByLetter('S').shipDestroyed();
                System.out.println("Your sunk your opponent's submarine!");
            }
        } else if (opposition.getBoard()[yCoordinate][xCoordinate] == 'D') {
            System.out.println("Hit dest");
            numDestroyer++;
            if (numDestroyer == 4) {
                opposition.getShipByLetter('D').shipDestroyed();
                System.out.println("Your sunk your opponent's destroyer!");
            }
        } else if (opposition.getBoard()[yCoordinate][xCoordinate] == 'P') {
            System.out.println("Hit patrol");
            numPatrolBoat++;
            if (numPatrolBoat == 1) {
                opposition.getShipByLetter('P').shipDestroyed();
                System.out.println("Your sunk your opponent's patrol boat!");
            }
        } else if (opposition.getBoard()[yCoordinate][xCoordinate] == 'C') {
            System.out.println("Hit cruiser");
            numCruiser++;
            if (numCruiser == 2) {
                opposition.getShipByLetter('C').shipDestroyed();
                System.out.println("Your sunk your opponent's cruiser!");
            }
        }

        if (opposition.getBoard()[yCoordinate][xCoordinate] != '~') {
            System.out.println("HIT something");
            opposition.getBoard()[yCoordinate][xCoordinate] = 'H';
            return 'H';
        }
        System.out.println("MISS");
        opposition.getBoard()[yCoordinate][xCoordinate] = 'M';
        return 'M';
    }



    public void printResult(char result) {
        if (result == 'M') {
            System.out.println("Tough luck soldier! You MISSED!");
        } else {
            System.out.println("Great strike soldier! You successfully HIT the enemy ship!");
        }
    }

    public char[][] getBoard() {
        return board;
    }

    public Ship getShipByLetter(char letter){
        return this.ships.get(letter);
    }

    public void printBoard() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                System.out.print(board[i][j]+ "    ");
            }
            System.out.println();
        }
    }

}
