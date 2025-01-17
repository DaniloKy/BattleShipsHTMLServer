public class Coordinate {
    private int xCor;
    private int yCor;

    public Coordinate() {
        xCor=1;
        yCor=1;
    }
    public Coordinate(int x, int y) {
        xCor=x;
        yCor=y;

    }
    public int getX() {
        return xCor;
    }

    public int getY(){
        return yCor;
    }

    @Override
    public String toString() {
        return "(" + xCor + ", " + yCor + ")";
    }
}