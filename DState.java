import java.security.InvalidAlgorithmParameterException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DState extends State {
    private HashMap<String, DState> nextStates;

    public DState(String stateName) {
        super(stateName);
        this.nextStates = new HashMap<>();
    }

    public DState() {
        this(null);
    }

    public void putNextState(String symbol, DState nextState) {
        nextStates.put(symbol, nextState);
    }

    public String findSymbolToState(DState nextState) {
        for (String symbol : nextStates.keySet()) {
            if (nextStates.get(symbol) == nextState) {
                return symbol;
            }
        }
        return null;
    }

    public Set<DState> getNextStates() {
        return new HashSet<>(nextStates.values());
    }

    public DState getNextState(String symbol) {
        return nextStates.get(symbol);
    }

    public DState walk(String symbol) throws InvalidAlgorithmParameterException {
        DState state = nextStates.get(symbol);
        
        if (state == null) {
            throw new InvalidAlgorithmParameterException("Invalid symbol or transition function is incomplete.");
        }
        return nextStates.get(symbol);
    }

}
