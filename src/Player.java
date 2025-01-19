import java.time.LocalDateTime;
import java.util.HashMap;

public class Player {

    public String name;
    public LocalDateTime lastActiveTime;
    public Board board = null;
    public boolean playerBoardReady = false;

    public Player(String name){
        this.name = name;
    }

    public void setBoard(Board board){
        this.board = board;
    }

    public void printBoard() {
        this.board.printBoard();
    }
    public char [][] getBoard() {
        return board.getBoard();
    }
    public String getName(){
        return this.name;
    }
    public boolean isPlayerBoardReady(){
        return playerBoardReady;
    }
    public void setPlayerBoardReady(boolean bool){
        this.playerBoardReady = bool;
    }

    public HashMap<Character, Ship> getShips(){
        return this.board.getShips();
    }
    public Coordinate playerGuessAttack(Coordinate attackCoordinates) {
        if (board.isValidAttack(attackCoordinates) == false) {
            System.out.println("That is an invalid attack location. Please enter again:");
        }
        return attackCoordinates;
    }

    public boolean fireAndAttackOpp(Player opp, Coordinate attack) {
        return board.resultHitMiss(attack, this, opp)=='H';

    }


    public void updateLastTimeRequest(){
        this.lastActiveTime = LocalDateTime.now();
    }

    public LocalDateTime getLastActiveTime(){
        return this.lastActiveTime;
    }

    public Ship getShipByLetter(char letter) {
        return board.getShipByLetter(letter);
    }

}







