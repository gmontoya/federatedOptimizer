import java.util.*;

class checkMissingCPs {
    public static void main(String[] args) {
        Integer cs1 = Integer.parseInt(args[0]);
        String p = args[1];
        Integer cs2 = Integer.parseInt(args[2]);
        String fileIO1 = args[3];
        String fileIS2 = args[4];
        String fileRadixOne1 = args[5];
        String fileRadixOne2 = args[6];
        HashMap<Integer, HashMap<String,TreeSet<String>>> io1 = obtainRadixBasedIndex.readIO(fileIO1);
        HashMap<Integer, TreeSet<String>> is2 = obtainRadixBasedIndex.readIS(fileIS2);
        Set<String> es1 = io1.get(cs1).get(p);        
        Set<String> es2 = is2.get(cs2);
        Set<String> es = new TreeSet<String>(es1);
        es.retainAll(es2);
        RadixTreeOne rTree1 = addInterDatasetLinksRTreeOne.readRadixTreeOne(fileRadixOne1);
        RadixTreeOne rTree2 = addInterDatasetLinksRTreeOne.readRadixTreeOne(fileRadixOne2);
        //System.out.println("getOverlap r trees: "+rTree1.getOverlap(rTree2));
        for (String str : es) {
            System.out.println("e: "+str);
            String s = str;
            if (str.startsWith("<")) {
                s = s.substring(1, s.length()-1);
            }
        String value;
        int i = s.indexOf("/");
        int j = s.indexOf("/", i+1);
        if (j == i+1) {
            j = s.indexOf("/", j+1);
        }
        if (j>0) {
            value = s.substring(j+1);
            s = s.substring(0, j);
        } else if (i>0) {
            value = s.substring(i+1);
            s = s.substring(0, i);
        } else {
            continue;
        }
        /*    int pos = s.lastIndexOf("/"); //getBreak(str); //str.lastIndexOf("/");
            String value;
            if (pos < 0) {
                continue;
            } else {
                value = s.substring(pos+1);
                s = s.substring(0, pos);
            }*/
            System.out.println("s: "+s+". value: "+value);
            QTreeOne qt1 = rTree1.getQTree(s);
            //System.out.println("qt1: "+qt1);
            //System.out.println("qt2: "+qt2);
            QTreeOne qt2 = rTree2.getQTree(s);
            //System.out.println("qt2: "+qt2);
            if (qt1 == null || qt2 == null) {
                return;
            }
            //System.out.println("rTree1.getOverlap(rTree2): "+rTree1.getOverlap(rTree2));
            System.out.println("qt1.getOverlap(qt2).get(\"<http://dbpedia.org/ontology/profession>\"): "+qt1.getOverlap(qt2).get("<http://dbpedia.org/ontology/profession>"));
            //System.out.println("qt2: "+qt2);
            Set<QTreeOne.QTreeOneLeaf> ls1 = qt1.getLeaves(value.hashCode());
            Set<QTreeOne.QTreeOneLeaf> ls2 = qt2.getLeaves(value.hashCode());
            for (QTreeOne.QTreeOneLeaf l1 : ls1) {
            for (QTreeOne.QTreeOneLeaf l2 : ls2) {
            System.out.println("1: l1.valuesO.get(p).get(cs1): "+l1.valuesO.get(p).get(cs1));
            System.out.println("2: l2.valuesS.get(cs2): "+l2.valuesS.get(cs2));
            HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> cps = QTreeOne.getOverlap(l1, l2);
            HashMap<Integer, HashMap<Integer, Integer>> cs1Cs2Count = cps.get(p);
            System.out.println("cs1Cs2Count is null? "+(cs1Cs2Count == null));
            if (cs1Cs2Count == null)
                continue;
            HashMap<Integer, Integer> cs2Count = cs1Cs2Count.get(cs1);
            System.out.println("cs2Count is null? "+(cs2Count == null));
            if (cs2Count == null)
                continue;
            System.out.println(cs2Count.get(cs2));
            }
            }
        }     
    }
}
