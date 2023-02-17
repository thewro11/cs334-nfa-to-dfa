import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NFA {
    private Set<NState> states;
    private Set<String> alphabet;
    private HashMap<String, Set<NState>> transitionFunctions;
    private NState startState;
    private Set<NState> acceptStates;

    public NFA(Set<NState> states, NState startState, Set<NState> acceptStates) {
        this.states = new HashSet<>();
        this.alphabet = new HashSet<>();
        this.transitionFunctions = new HashMap<>();
        this.startState = startState;
        this.acceptStates = new HashSet<>(acceptStates);

        for (NState state : states) {
            this.states.add(state);

            for (NState nextState : state.getNextStates()) {
                this.states.add(nextState);

                for (String symbol : state.findSymbolToState(nextState)) {
                    this.alphabet.add(symbol);
                    appendToTransitionFunctions(symbol, nextState);
                }
            }
        }
    }

    public boolean appendToTransitionFunctions(String symbol, NState state) {
        if (transitionFunctions.get(symbol) == null) {
            transitionFunctions.put(symbol, new HashSet<>());
        }
        return transitionFunctions.get(symbol).add(state);
    }

    public boolean input(String inputString) {
        List<NState> currentStates = new ArrayList<>();
        currentStates.add(this.startState);

        if (inputString != null) {

            for (char c : inputString.toCharArray()) {
                Set<NState> newStates = new HashSet<>();
                for (int i = 0 ; i < currentStates.size() ; ++i) {
                    for (NState epsilonState : currentStates.get(i).walk(null)) {
                        if (!currentStates.contains(epsilonState)) {
                            currentStates.add(epsilonState);
                        }
                    }

                    newStates.addAll(currentStates.get(i).walk(String.valueOf(c)));
                }

                currentStates.clear();
                currentStates.addAll(newStates);

                for (NState state : currentStates) {
                    for (NState epsilonState : state.walk(null)) {
                        if (!currentStates.contains(epsilonState)) {
                            currentStates.add(epsilonState);
                        }
                    }
                }
            }

        }
        
        for (NState state : currentStates) {
            if (acceptStates.contains(state)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Head of row is symbol.
     * Head of column is state.
     * @throws InvalidAlgorithmParameterException
     */
    public static NFA createFrom(   String[][][] tableOfFunctionTransitions, 
                                    String startStateStr, 
                                    String[] acceptStatesStr) 
                                    throws InvalidAlgorithmParameterException {
        NState startState = null;
        Set<NState> acceptStates = new HashSet<>();
        Set<NState> states = new HashSet<>();
        
        for (int r = 1 ; r < tableOfFunctionTransitions.length ; ++r) {
            NState state = findState(states, tableOfFunctionTransitions[r][0][0]);
            if (state == null) {
                state = new NState(tableOfFunctionTransitions[r][0][0]);
                states.add(state);
            }

            for (int c = 1 ; c < tableOfFunctionTransitions[r].length ; ++c) {
                for (int i = 0 ; i < tableOfFunctionTransitions[r][c].length ; ++i) {
                    String stateName = tableOfFunctionTransitions[r][c][i];
                    NState selectedState = findState(states, stateName);
                    if (selectedState == null) {
                        selectedState = new NState(stateName);
                        states.add(selectedState);
                    }
                    state.putNextState(tableOfFunctionTransitions[0][c][0], selectedState);
                }
            }
        }

        for (NState state : states) {
            if (state.getStateName().equals(startStateStr)) {
                startState = state;
            }
            if (Arrays.asList(acceptStatesStr).contains(state.getStateName())) {
                acceptStates.add(state);
            }
        }

        if (startState == null) {
            throw new InvalidAlgorithmParameterException("No start state detected.");
        } else if (acceptStates.isEmpty()) {
            throw new InvalidAlgorithmParameterException("No accept state detected.");
        }

        return new NFA(states, startState, acceptStates);
    }
    
    private static NState findState(Set<NState> states, String stateName) {
        for (NState state : states) {
            if (state.getStateName().equals(stateName)) {
                return state;
            }
        }
        return null;
    }

    public Set<NState> epsilonClosure(NState state) {
        HashSet<NState> epsilonClosureStates = new HashSet<>();
        epsilonClosureStates.add(state);
        epsilonClosureStates.addAll(recursiveEpsilonClosure(epsilonClosureStates, state));
        return epsilonClosureStates;
    }

    private Set<NState> recursiveEpsilonClosure(Set<NState> epsilonClosureStates, NState state) {
        Set<NState> epsilonStates = state.walk(null);
        for (NState epsilonState : epsilonStates) {
            if (!epsilonClosureStates.contains(epsilonState)) {
                epsilonClosureStates.add(state);
                epsilonStates.addAll(recursiveEpsilonClosure(epsilonClosureStates, epsilonState));
            }
        }
        return epsilonStates;
    }

    public Set<NState> epsilonClosureWithoutSelf(NState state) {
        HashSet<NState> epsilonClosureStates = new HashSet<>();
        epsilonClosureStates.addAll(recursiveEpsilonClosureWithoutSelf(epsilonClosureStates, state));
        return epsilonClosureStates;
    }

    private Set<NState> recursiveEpsilonClosureWithoutSelf(Set<NState> epsilonClosureStates, NState state) {
        Set<NState> epsilonStates = state.walk(null);
        for (NState epsilonState : epsilonStates) {
            if (!epsilonClosureStates.contains(epsilonState)) {
                epsilonStates.addAll(recursiveEpsilonClosureWithoutSelf(epsilonClosureStates, epsilonState));
            }
        }
        return epsilonStates;
    }

    public Set<NState> getStates() {
        return states;
    }

    public NState getStartState() {
        return startState;
    }

    public Set<NState> getAcceptStates() {
        return acceptStates;
    }

    public Set<String> getAlphabet() {
        return alphabet;
    }

}
