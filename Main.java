import java.security.InvalidAlgorithmParameterException;

public class Main {
    public static void testNFA() {
        // String[][][] transitionFunctions = {
        //     { {},               {"0"},                 {"1"},               {null}      },
        //     { {"p0"},           {},                    {"p1"},              {"p2"}      },
        //     { {"p1"},           {},                    {"p2"},              {}          },
        //     { {"p2"},           {"p2"},                {"p2"},              {}          }
        // };

        String[][][] transitionFunctions = {
            { {},               {"0"},                 {"1"},               {null}      },
            { {"p0"},           {},                    {"p1","p0"},         {}          },
            { {"p1"},           {},                    {"p2"},              {}          },
            { {"p2"},           {"p2"},                {"p2"},              {}          }
        };

        String inputString = "1100001010101";
        String startStateStr = "p0";
        String[] acceptStateStr = {"p2"};

        try {

            NFA nfa;
            nfa = NFA.createFrom(transitionFunctions, startStateStr, acceptStateStr);
            System.out.println(DFA.convertFrom(nfa).toString());

        } catch (InvalidAlgorithmParameterException e) {

            e.printStackTrace();
            
        }
    }

    public static void main(String[] args) {
        testNFA();
    }
}
