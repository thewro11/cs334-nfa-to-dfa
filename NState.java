import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NState extends State {
    private HashMap<String, Set<NState>> nextStates;

    public NState(String stateName) {
        super(stateName);
        this.nextStates = new HashMap<>();
    }

    public NState() {
        this(null);
    }

    public boolean putNextState(String symbol, NState nextState) {
        if (nextStates.get(symbol) == null) {
            nextStates.put(symbol, new HashSet<>());
        }

        return nextStates.get(symbol).add(nextState);
    }

    public Set<String> findSymbolToState(NState nextState) {
        Set<String> symbols = new HashSet<>();
        for (String symbol : nextStates.keySet()) {
            if (nextStates.get(symbol).contains(nextState)) {
                symbols.add(symbol);
            }
        }

        return symbols;
    }

    public Set<NState> getNextStates(String symbol) {
        return nextStates.get(symbol);
    }

    public Set<NState> getNextStates() {
        Set<NState> states = new HashSet<>();
        Iterator<Set<NState>> iter = nextStates.values().iterator();
        while (iter.hasNext()) {
            states.addAll(iter.next());
        }

        return states;
    }

    public Set<NState> walk(String symbol) {
        Set<NState> nextStates = this.getNextStates(symbol);
        if (nextStates == null) {
            nextStates = new HashSet<>();
        }

        return nextStates;
    }

}
