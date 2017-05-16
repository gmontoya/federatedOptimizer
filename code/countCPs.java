import java.util.*;

class countCPs {

    public static void main(String[] args) {

        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps = evaluateSPARQLQuery.readCPS(args[0]);
        int s = 0;
        for (Integer cs1 : cps.keySet()) {
            s += cps.get(cs1).size();
        }
        System.out.println(s);
    }
}
