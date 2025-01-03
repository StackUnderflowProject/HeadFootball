package si.um.feri.project.soccer;

public enum BallType {
    BOUNCY(1),NORMAL(0),DULL(2);
    private int value;

    private BallType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
