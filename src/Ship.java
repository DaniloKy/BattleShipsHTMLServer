
public class Ship {

    /* Aircraft of size 5 represented by the letter "A"
     * Battleship of size 4 represented by the letter "B"
     * Submarine of size 3 represented by the letter "S"
     * Destroyer ship of size 3 represented by the letter "D"
     * Patrol Boat of size 2 represented by the letter "P"
     *
     *
     */

    private int size;
    private char letter;
    private char direction;
    private Coordinate coord;

    public Ship (char let, char dir, Coordinate crd) {
        if( let == 'A') {
            size=5;
        } else if ( let == 'B') {
            size=4;
        } else if (let == 'S' || let == 'D' ) {
            size=3;
        }else {
            size=2;
        }
    }
    public int getSize () {
        return size;
    }

    public char getLetter() {
        return letter;
    }

    public char getDirection() {
        return direction;
    }

    public Coordinate getCoordinate() {
        return coord;
    }
}
