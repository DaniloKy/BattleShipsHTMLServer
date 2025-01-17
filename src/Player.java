import java.time.LocalDateTime;

public class Player {

    public String name;
    public LocalDateTime lastActiveTime;
    public Board board;

    public Player(String name){
        this.name = name;
    }

    public void setBoard(Board board){
        this.board = board;
    }

    public void updateLastTimeRequest(){
        this.lastActiveTime = LocalDateTime.now();
    }

    public LocalDateTime getLastActiveTime(){
        return this.lastActiveTime;
    }

}
