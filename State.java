public abstract class State {
    private static int stateCounter = 0;
    
    private int stateID;
    private String stateName;

    protected State(String stateName) {
        this.stateID = stateCounter++;
        this.stateName = stateName;
    }

    protected State() {
        this(null);
    }

    public int getStateID() {
        return stateID;
    }

    public String getStateName() {
        return stateName;
    }

}
