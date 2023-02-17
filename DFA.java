import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class DFA {
    private Set<DState> states;
    private Set<String> alphabet;
    private HashMap<String, DState> transitionFunctions;
    private DState startState;
    private Set<DState> acceptStates;

    public DFA(Set<DState> states, DState startState, Set<DState> acceptStates) {
        this.states = new HashSet<>();
        this.alphabet = new HashSet<>();
        this.transitionFunctions = new HashMap<>();
        this.startState = startState;
        this.acceptStates = new HashSet<>(acceptStates);

        for (DState state : states) {
            this.states.add(state);

            for (DState nextState : state.getNextStates()) {
                this.states.add(nextState);

                String symbol = state.findSymbolToState(nextState);
                this.alphabet.add(symbol);
                appendToTransitionFunctions(symbol, nextState);
            }
        }
    }

    public void appendToTransitionFunctions(String symbol, DState state) {
        transitionFunctions.put(symbol, state);
    }

    public boolean input(String inputString) throws InvalidAlgorithmParameterException {
        DState currentState = startState;

        if (inputString != null) {
            for (char c : inputString.toCharArray()) {
                currentState = currentState.walk(String.valueOf(c));
            }
        }
        
        if (acceptStates.contains(currentState)) {
            return true;
        }
        return false;
    }

    /**
     * Head of row is symbol.
     * Head of column is state.
     * @throws InvalidAlgorithmParameterException
     */
    public static DFA createFrom(String[][] tableOfFunctionTransitions, String startStateStr, String[] acceptStatesStr) throws InvalidAlgorithmParameterException {
        DState startState = null;
        Set<DState> acceptStates = new HashSet<>();
        Set<DState> states = new HashSet<>();

        for (int r = 1 ; r < tableOfFunctionTransitions.length ; ++r) {
            DState state = findState(states, tableOfFunctionTransitions[r][0]);
            if (state == null) {
                state = new DState(tableOfFunctionTransitions[r][0]);
                states.add(state);
            }

            for (int c = 1 ; c < tableOfFunctionTransitions[r].length ; ++c) {
                String stateName = tableOfFunctionTransitions[r][c];
                DState selectedState = findState(states, stateName);
                if (selectedState == null) {
                    selectedState = new DState(stateName);
                    states.add(selectedState);
                }
                state.putNextState(tableOfFunctionTransitions[0][c], selectedState);
            }
        }

        for (DState state : states) {
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

        return new DFA(states, startState, acceptStates);
    }


    private static DState findState(Set<DState> states, String stateName) {
        for (DState state : states) {
            if (state.getStateName().equals(stateName)) {
                return state;
            }
        }
        return null;
    }

    public static DFA convertFrom(NFA nfa) {
        List<Set<NState>> currentStates = new ArrayList<>();

        Set<NState> startStateSet = new HashSet<>();
        startStateSet.add(nfa.getStartState());
        currentStates.add(startStateSet);

        Map<Vector<Object>, Set<NState>> transitionFunctions = new HashMap<>();
        Set<NState> nonnormalizedStartState = new HashSet<>();

        for (int i = 0 ; i < currentStates.size() ; ++i) {
            Set<NState> epsilonStates = new HashSet<>();
            for (NState state : currentStates.get(i)) {
                epsilonStates.addAll(nfa.epsilonClosure(state));
            }

            if (i == 0) {
                nonnormalizedStartState = epsilonStates;
            }

            for (String symbol : nfa.getAlphabet()) {
                if (symbol == null) continue;

                Set<NState> walkedStates = new HashSet<>();
                for (NState state : epsilonStates) {
                    walkedStates.addAll(state.walk(symbol));
                }
                for (NState walkedState : walkedStates) {
                    walkedStates.addAll(nfa.epsilonClosure(walkedState));
                }

                Vector<Object> d = new Vector<>();
                d.add(epsilonStates);
                d.add(symbol);
                transitionFunctions.put(d, walkedStates);

                if (!walkedStates.isEmpty()) {
                    if (!contains(currentStates, walkedStates)) {
                        currentStates.add(walkedStates);
                    }
                }
            }
        }

        Set<DState> states = new HashSet<>();
        DState startState = new DState();
        Set<DState> acceptStates = new HashSet<>();

        DState emptySetState = new DState("{}");
        for (String symbol : nfa.getAlphabet()) {
            if (symbol == null) continue;
            
            emptySetState.putNextState(symbol, emptySetState);
        }

        states.add(emptySetState);

        for (Vector<Object> d : transitionFunctions.keySet()) {
            Set<NState> fromStates = (Set<NState>)d.get(0);
            String symbol = (String)d.get(1);
            Set<NState> toStates = transitionFunctions.get(d);

            List<NState> fromStatesList = new ArrayList<>(fromStates);
            fromStatesList.sort(new Comparator<NState>() {

                @Override
                public int compare(NState o1, NState o2) {
                    return o2.getStateName().compareTo(o1.getStateName());
                }
                
            });

            String newDStateName = "{";
            for (NState fromState : fromStates) {
                newDStateName += fromState.getStateName() + ", ";
            }
            newDStateName = newDStateName.strip();
            if (newDStateName.length() > 1) {
                newDStateName = newDStateName.substring(0, newDStateName.length()-1) + "}";
            } else {
                newDStateName = "";
            }

            DState newDState = find(states, newDStateName);
            if (newDState == null) {
                newDState = new DState(newDStateName);
                states.add(newDState);
            }

            String newNextDStateName = "{";
            for (NState toState : toStates) {
                newNextDStateName += toState.getStateName() + ", ";
            }
            newNextDStateName = newNextDStateName.strip();
            if (newNextDStateName.length() > 1) {
                newNextDStateName = newNextDStateName.substring(0, newNextDStateName.length()-1) + "}";
            } else {
                newNextDStateName = "";
            }

            DState newNextDState;
            if (!newNextDStateName.equals("")) {
                newNextDState = find(states, newNextDStateName);
                if (newNextDState == null) {
                    newNextDState = new DState(newNextDStateName);
                    states.add(newNextDState);
                }
            } else {
                newNextDState = emptySetState;
            }

            newDState.putNextState(symbol, newNextDState);

            if (fromStates.containsAll(nonnormalizedStartState) && nonnormalizedStartState.containsAll(fromStates)) {
                startState = newDState;
            }

            for (State acceptState : nfa.getAcceptStates()) {
                if (fromStates.contains(acceptState)) {
                    acceptStates.add(newDState);
                    break;
                }
            }
        }

        return new DFA(states, startState, acceptStates);
        
    }

    private static boolean contains(List<Set<NState>> states, Set<NState> targets) {
        for (Set<NState> statesSet : states) {
            if (statesSet.containsAll(targets) && targets.containsAll(statesSet)) return true;
        }
        return false;
    }

    private static DState find(Set<DState> states, String target) {
        for (DState state : states) {
            if (state.getStateName().equals(target)) return state;
        }
        return null;
    }

    @Override 
    public String toString() {
        String result = "----- " + super.toString() + " -----\n";

        result += "States: \n";
        for (State state : states) {
            if (state.getStateName() == "") {
                result += "<e>";
            }
            result += state.getStateName() + "\n";
        }
        result += "\n";

        result += "Alphabet: \n";
        for (String symbol : alphabet) {
            result += symbol + "\n";
        }
        result += "\n";

        result += "Transition Functions: \n";
        for (DState state : states) {
            for (String symbol : alphabet) {
                result +=   "Move From: " + state.getStateName() +
                            " To: " + state.getNextState(symbol).getStateName() +
                            " By: " + symbol + "\n";
            }
        }
        result += "\n";

        result += "Start state: " + startState.getStateName() + "\n";
        result += "\n";
        
        result += "Accept state:\n";
        for (State state : acceptStates) {
            result += state.getStateName() + "\n";
        }

        return result.strip();
    }

}
