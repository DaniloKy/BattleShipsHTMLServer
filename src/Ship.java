public class Ship {

    private char letter;
    private boolean isDestroyed;

    public Ship (char let) {
        this.letter = let;
        this.isDestroyed = false;
    }

    public char getLetter() {
        return letter;
    }


    public void shipDestroyed(){
        this.isDestroyed = true;
    }

    public boolean isShipDestroyed(){
        return this.isDestroyed;
    }
}
