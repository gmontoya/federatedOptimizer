import java.util.*;
class getCS {

    public static void main(String[] args) {

        String file = args[0];
        HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css = produceStarJoinOrdering.readCSS(file);
        for (Integer cs : css.keySet()) {
            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> pair =css.get(cs);
            if (pair.getFirst()>3 && pair.getSecond().keySet().size() < 5) {
                System.out.println("Found! "+pair.getSecond().keySet().toString());
            }
        }
    }
}
