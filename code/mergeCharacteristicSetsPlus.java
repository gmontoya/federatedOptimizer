import java.io.*;
import java.util.*;

class mergeCharacteristicSetsPlus {

    public static Integer getIKey(String file) {
        Integer ikey = -1;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            Vector<String> ps = new Vector<String>();
            while (l!=null) {
                ps.add(l);
                l = br.readLine();
            }
            br.close();
            Collections.sort(ps);
            String key = "";
            for (String p : ps) {
               key = key + p;
            }
            ikey = key.hashCode();
        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
        return ikey;
    }

    public static HashSet<Integer> readCSSText(String file) {
        HashSet<Integer> css = new HashSet<Integer>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            //Vector<String> ps = new Vector<String>();
            while (l!=null) {
                css.add(new Integer(l));
                l = br.readLine();
            }
            br.close();
            //Collections.sort(ps);
            //String key = "";
            //for (String p : ps) {
            //   key = key + p;
            //}
            //ikey = key.hashCode();
        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
        return css;
    }

    public static HashSet<String> readPS(String file) {
        HashSet<String> ps = new HashSet<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            //Vector<String> ps = new Vector<String>();
            while (l!=null) {
                ps.add(l);
                l = br.readLine();
            }
            br.close();
            //Collections.sort(ps);
            //String key = "";
            //for (String p : ps) {
            //   key = key + p;
            //}
            //ikey = key.hashCode();
        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
        return ps;
    }

    public static void writeCSS(HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css, String fileName) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeObject(css);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void writeCPS(HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, String fileName) {

        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeObject(cps);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            System.exit(1);
        }
    }

    public static void writeIIS(HashMap<String, Integer> invertIndexSubject, String fileName) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeObject(invertIndexSubject);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            System.exit(1);
        }
    }

    public static void writeIIO(HashMap<String, HashMap<String, HashMap<Integer,Integer>>> invertIndexObject, String fileName) {

        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeObject(invertIndexObject);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            System.exit(1);
        }
    }

    class CharacteristicSet {

        protected int id;
        protected int count;
        protected HashMap<String, Integer> ps;
        public CharacteristicSet(int id, int count, HashMap<String, Integer> ps) {
            this.id = id;
            this.count = count;
            this.ps = ps;
        } 
        public void setCount(int c) {
            this.count = c;
        }
        public int getCount() {
            return this.count;
        }
        public void setPredicateCount(String p, int c) {
            this.ps.put(p, c);
        }
    }
    public static HashMap<Integer, HashSet<String>> getSubjectIndex (HashMap<String, Integer> iis) {
        HashMap<Integer, HashSet<String>> is = new HashMap<Integer, HashSet<String>>();
        for (String s : iis.keySet()) {
            Integer cs = iis.get(s);
            HashSet<String> ss = is.get(cs);
            if (ss == null) {
                ss = new HashSet<String>();
            } 
            ss.add(s);
            is.put(cs, ss);
        }
        return is;
    }

    public static HashMap<Integer, HashSet<String>> getObjectIndex (HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio) {
        HashMap<Integer, HashSet<String>> io = new HashMap<Integer, HashSet<String>>();
        for (String o : iio.keySet()) {
            HashMap<String, HashMap<Integer,Integer>> psCsCount = iio.get(o);
            for (String p : psCsCount.keySet()) {
                HashMap<Integer,Integer> csCount = psCsCount.get(p);
                for (Integer cs : csCount.keySet()) {
                    HashSet<String> os = io.get(cs);
                    if (os == null) {
                        os = new HashSet<String>();
                    } 
                    os.add(o);
                    io.put(cs, os);
                }
            }
        }
        return io;
    }

    public static HashMap<Integer, HashSet<Integer>> getLeftCPSIndex (HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps) {

        HashMap<Integer, HashSet<Integer>> il = new HashMap<Integer, HashSet<Integer>>();
        for (Integer l : cps.keySet()) {
            HashMap<Integer, HashMap<String, Integer>> csRPsCount = cps.get(l);
            for (Integer r : csRPsCount.keySet()) {
                HashSet<Integer> ls = il.get(r);
                if (ls == null) {
                    ls = new HashSet<Integer>();
                } 
                ls.add(l);
                il.put(r, ls);
            }
        }
        return il;
    }

    public static void main2(String[] args) throws Exception {
        int N = Integer.parseInt(args[0]);
        assert N > 10;
    }
    public static void main(String[] args) throws Exception {
        String fileCSS1 = args[0];
        String fileCSS2 = args[1];
        int N = Integer.parseInt(args[2]);
        String fileCPS1 = args[3];
        String fileCPS2 = args[4];
        String fileIIS1 = args[5];
        String fileIIS2 = args[6];
        String fileIIO1 = args[7];
        String fileIIO2 = args[8];
        //String file = args[9];
        //HashSet<Integer> csissues = readCSSText(file);
        int min = Integer.MAX_VALUE;
        Integer idMin = null;
        HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css1 = readCSS(fileCSS1);
        HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> reducedCSS = new HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>>();
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps1 = readCPS(fileCPS1);
        HashMap<String, Integer> iis1 = readIIS(fileIIS1);
        HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio1 = readIIO(fileIIO1);
        HashMap<Integer, HashSet<String>> is = getSubjectIndex(iis1);
        HashMap<Integer, HashSet<String>> io = getObjectIndex(iio1);
        HashMap<Integer, HashSet<Integer>> il = getLeftCPSIndex(cps1); 
        //assert consistent(il, cps1);
        //System.out.println(css1);
        //System.out.println(cps1);
        //System.out.println(iis1);
        //System.out.println(is);
        //System.out.println(iio1);
        //System.out.println(io);
        //System.out.println(il);
        /*int k = 0;
        for (Integer cs1 : css1.keySet()) {
            if (csissues.contains(cs1)) {
                System.out.println("css1["+k+"]: "+cs1+". it is connected to itself? "+(cps1.containsKey(cs1) && cps1.get(cs1).containsKey(cs1)));
            }
            k++;
        }*/

        int i = 0;

        for (Integer cs1 : css1.keySet()) {
            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cPs = css1.get(cs1);
            Integer count = cPs.getFirst();
            if (reducedCSS.size() < N) {
                reducedCSS.put(cs1, cPs);
                if (min > count) {
                    min = count;
                    idMin = cs1;
                }
            } else {
                Pair<Integer, HashMap<String, Pair<Integer,Integer>>> tmpCPs = reducedCSS.get(idMin); 
                if ((count > min) || ((count == min) && (tmpCPs.getSecond().keySet().size() < cPs.getSecond().keySet().size()))) {
                    reducedCSS.remove(idMin);
                    reducedCSS.put(cs1, cPs);
                    //System.out.println("reducingM "+idMin);
                    HashSet<String> danglingSubjects = removeSCSS(iis1, idMin, is);
                    /*if (csissues.contains(idMin)) {
                      assert absentS(iis1, idMin) : "iis1. idMin: "+idMin;
                    }*/
                    HashMap<String, HashMap<String, Integer>> danglingObjects = removeOCSS(iio1, idMin, io);
                    /*if (csissues.contains(idMin)) {
                      assert absentO(iio1, idMin) : "iio1. idMin: "+idMin;
                    }*/
                    Pair<HashMap<Integer, HashMap<String, Integer>>,HashMap<Integer, HashMap<String, Integer>>> pair = removeCPSCSS(cps1, idMin, il);

                    HashMap<Integer, HashMap<String, Integer>> danglingLeftPairs = pair.getFirst(); //removeCPSCSSLeft(cps1, idMin, il);
                    /*if (csissues.contains(idMin)) {                    
                      assert absentL(cps1, idMin) : "cps1. idMin: "+idMin;
                      assert consistent(il, cps1);
                    }*/
                    HashMap<Integer, HashMap<String, Integer>> danglingRightPairs = pair.getSecond(); //removeCPSCSSRight(cps1, idMin, il);
                    /*if (csissues.contains(idMin)) {
                      assert absentR(cps1, idMin) : "cps1R. idMin: "+idMin;
                      assert consistent(il, cps1);
                    }*/
                    mergeCS(idMin, tmpCPs, reducedCSS, iis1, iio1, cps1, danglingSubjects, danglingObjects, danglingLeftPairs, danglingRightPairs, il, is, io);
                } else {
                    //System.out.println("reducing "+cs1);
                    HashSet<String> danglingSubjects = removeSCSS(iis1, cs1, is);
                    /*if (csissues.contains(cs1)) {
                    assert absentS(iis1, cs1) : "iis1. cs1: "+cs1;
                    }*/
                    HashMap<String, HashMap<String, Integer>> danglingObjects = removeOCSS(iio1, cs1, io);
                    /*if (csissues.contains(cs1)) {
                    assert absentO(iio1, cs1) : "iio1. cs1: "+cs1;
                    }*/
                    Pair<HashMap<Integer, HashMap<String, Integer>>,HashMap<Integer, HashMap<String, Integer>>> pair = removeCPSCSS(cps1, cs1, il);
                    HashMap<Integer, HashMap<String, Integer>> danglingLeftPairs = pair.getFirst(); //removeCPSCSSLeft(cps1, cs1, il);
                    /*if (csissues.contains(cs1)) {
                    assert absentL(cps1, cs1) : "cps1. cs1: "+cs1;
                    assert consistent(il, cps1);
                    }*/
                    HashMap<Integer, HashMap<String, Integer>> danglingRightPairs = pair.getSecond(); //removeCPSCSSRight(cps1, cs1, il);
                    /*if (csissues.contains(cs1)) {
                    assert absentR(cps1, cs1) : "cps1. cs1: "+cs1;
                    assert consistent(il, cps1);
                    }*/
                    mergeCS(cs1, cPs, reducedCSS, iis1, iio1, cps1, danglingSubjects, danglingObjects, danglingLeftPairs, danglingRightPairs, il, is, io);
                }
                int tmpMin = Integer.MAX_VALUE;
                Integer tmpIdMin = null;                
                for (Integer tmp : reducedCSS.keySet()) {
                   int tmpCount = reducedCSS.get(tmp).getFirst();
                   if (tmpCount < tmpMin) {
                       tmpMin = tmpCount;
                       tmpIdMin = tmp;
                   }
                }
                min = tmpMin;
                idMin = tmpIdMin;    
            }
            i++;
            if (i % 100 == 0) {
                System.out.print(".");
            }
        }
        assert consistent(il, cps1);
        /*k = 0;
        for (Integer cs1 : reducedCSS.keySet()) {
            if (csissues.contains(cs1)) {
                System.out.println("reducedCSS["+k+"]: "+cs1);
            }
            k++;
        }*/
        if (css1.size() != reducedCSS.size()) {
            writeCSS(reducedCSS, fileCSS2);
            writeCPS(cps1, fileCPS2);
            writeIIS(iis1, fileIIS2);
            writeIIO(iio1, fileIIO2);
            System.out.println("");
            System.out.println(css1.size()+" characteristic sets have been reduced to "+reducedCSS.size());
        } else {
            System.out.println("There were only "+reducedCSS.size()+" characteristic sets");
        }
        //System.out.println(reducedCSS);
        //System.out.println(cps1);
        //System.out.println(iis1);
        //System.out.println(iio1);
    }

    public static boolean consistent(HashMap<Integer, HashSet<Integer>> il, HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps) {

        boolean okay = true;
        for (Integer csL : cps.keySet()) {
            for (Integer csR : cps.get(csL).keySet()) {
                if (!il.get(csR).contains(csL)) {
                    okay = false;
                    System.out.println("");
                    System.out.println(csL);
                    System.out.println(csR);
                    System.out.println(il);
                    System.out.println(cps);
                    break;
                }
            }
        }
        return okay;
    }

    public static boolean absentS(HashMap<String, Integer> iis, Integer cs) {
        boolean okay = true;
        for (String s : iis.keySet()) {
            if (iis.get(s).equals(cs)) {
                okay = false;
                break;
            }
        }
        return okay;
    }

    public static boolean absentO(HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio, Integer cs) {
        boolean okay = true;
        for (String o : iio.keySet()) {
            HashMap<String, HashMap<Integer, Integer>> psCsCount = iio.get(o);
            for (String p : psCsCount.keySet()) {
                HashMap<Integer, Integer> csCount = psCsCount.get(p);
                if (csCount.keySet().contains(cs)) {
                    okay = false;
                    break;
                }
            }
        }
        return okay;
    }

    public static boolean absentL(HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, Integer cs) {
        boolean okay = true;
        if (cps.keySet().contains(cs)) {
            okay = false;
        }
        return okay;
    }

    public static boolean absentR(HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, Integer cs) {
        boolean okay = true;
        for (Integer csL : cps.keySet()) {
            if (cps.get(csL).keySet().contains(cs)) {
                okay = false;
            }
        }
        return okay;
    }

    public static HashSet<String> removeSCSS(HashMap<String, Integer> iis, Integer cs, HashMap<Integer, HashSet<String>> is) {
        HashSet<String> subjects = is.get(cs);
        if (subjects == null) {
            subjects = new HashSet<String>();
        } else {
            is.remove(cs);
        }
        for (String s : subjects) {
            iis.remove(s);
        }
        return subjects;
    }

    public static HashMap<String, HashMap<String, Integer>> removeOCSS(HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio, Integer csR, HashMap<Integer, HashSet<String>> io) {
        // input: entity -> predicate -> cs_id -> count
        // output: entity -> predicate -> count
        HashSet<String> os = io.get(csR);
        if (os == null) {
            os = new HashSet<String>();
        } else {
            io.remove(csR);
        }
        HashMap<String, HashMap<String, Integer>> objects = new HashMap<String, HashMap<String, Integer>>();
        for (String object : os) {
            HashMap<String, Integer> psCount = new HashMap<String, Integer>();
            HashMap<String, HashMap<Integer,Integer>> psCSCount = iio.get(object);
            for (String p : psCSCount.keySet()) {
                HashMap<Integer,Integer> csCount = psCSCount.get(p);
                Integer count = csCount.get(csR);
                if (count != null) {
                    psCount.put(p, count);
                }
            }
            if (psCount.size() > 0) {
                objects.put(object, psCount);
            }
        }
        for (String object : objects.keySet()) {
            HashMap<String, HashMap<Integer,Integer>> psCSCount = iio.get(object);
            for (String predicate : objects.get(object).keySet()) {
                HashMap<Integer,Integer> csCount = psCSCount.get(predicate);
                csCount.remove(csR);
                if (csCount.size() > 0) {
                    psCSCount.put(predicate, csCount);
                } else {
                    psCSCount.remove(predicate);
                }
            }
            if (psCSCount.size()>0) {
                iio.put(object, psCSCount);
            } else {
                iio.remove(object);
            }
        }
        return objects;
    }

    public static HashMap<Integer, HashMap<String, Integer>> removeCPSCSSLeft(HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, Integer cs, HashMap<Integer, HashSet<Integer>> il) {
        // input: CS_ID_L -> CS_ID_R -> Predicate -> Count
        // output: CS_ID_R ->  Predicate -> Count
        HashMap<Integer, HashMap<String, Integer>> cpsR = cps.get(cs);
        cps.remove(cs);
        if (cpsR != null) {
            for (Integer csR : cpsR.keySet()) {
                HashSet<Integer> ls = il.get(csR);
                if (ls != null) {
                    ls.remove(cs);
                    if (ls.size()>0) {
                        il.put(csR, ls);
                    } else {
                        il.remove(csR);
                    }
                }
            }
        } else {
            cpsR = new HashMap<Integer, HashMap<String, Integer>>();
        }
        return cpsR;
    }

    public static Pair<HashMap<Integer, HashMap<String, Integer>>,HashMap<Integer, HashMap<String, Integer>>> removeCPSCSS(HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, Integer cs, HashMap<Integer, HashSet<Integer>> il) {
        // input: CS_ID_L -> CS_ID_R -> Predicate -> Count
        // output: CS_ID_L ->  Predicate -> Count
        HashMap<Integer, HashMap<String, Integer>> cpsR = cps.get(cs);

        HashSet<Integer> ls = il.get(cs);
        if (ls == null) {
            ls = new HashSet<Integer>();
        }
        il.remove(cs);
        HashMap<Integer, HashMap<String, Integer>> cpsL = new HashMap<Integer, HashMap<String, Integer>>();
        for (Integer csL : ls) {
            //System.out.println("cps: "+cps+"\ncsL: "+csL);
            HashMap<Integer, HashMap<String, Integer>> csRPsCount = cps.get(csL);
            HashMap<String, Integer> psCount = csRPsCount.get(cs);
            if (psCount != null) {
                cpsL.put(csL, psCount);
            }
        }
        for (Integer csL : cpsL.keySet()) {
            HashMap<Integer, HashMap<String, Integer>> csRPsCount = cps.get(csL);
            csRPsCount.remove(cs);
            if (csRPsCount.size()>0) {
                cps.put(csL, csRPsCount);
            } else {
                cps.remove(csL);
            }
        }
        //return cpsL;
        if (cpsR != null) {
            for (Integer csR : cpsR.keySet()) {
                ls = il.get(csR);
                if (ls != null) {
                    ls.remove(cs);
                    if (ls.size()>0) {
                        il.put(csR, ls);
                    } else {
                        il.remove(csR);
                    }
                }
            }
        } else {
            cpsR = new HashMap<Integer, HashMap<String, Integer>>();
        }
        cps.remove(cs);
        return new Pair(cpsL, cpsR);
    }

    public static HashMap<Integer, HashMap<String, Integer>> removeCPSCSSRight(HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, Integer cs, HashMap<Integer, HashSet<Integer>> il) {
        // input: CS_ID_L -> CS_ID_R -> Predicate -> Count
        // output: CS_ID_L ->  Predicate -> Count
        HashSet<Integer> ls = il.get(cs);
        if (ls == null) {
            ls = new HashSet<Integer>();
        }
        il.remove(cs);
        HashMap<Integer, HashMap<String, Integer>> cpsL = new HashMap<Integer, HashMap<String, Integer>>();
        for (Integer csL : ls) {
            //System.out.println("cps: "+cps+"\ncsL: "+csL);
            HashMap<Integer, HashMap<String, Integer>> csRPsCount = cps.get(csL);
            HashMap<String, Integer> psCount = csRPsCount.get(cs);
            if (psCount != null) {
                cpsL.put(csL, psCount);
            }
        }
        for (Integer csL : cpsL.keySet()) {
            HashMap<Integer, HashMap<String, Integer>> csRPsCount = cps.get(csL);
            csRPsCount.remove(cs);
            if (csRPsCount.size()>0) {
                cps.put(csL, csRPsCount);
            } else {
                cps.remove(csL);
            }
        }
        return cpsL;
    }

    public static void setMissingCSS(Integer csDeleted, Integer cs, HashMap<String, Integer> iis, HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio, HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, HashSet<String> danglingSubjects, HashMap<String, HashMap<String, Integer>> danglingObjects, HashMap<Integer, HashMap<String, Integer>> danglingLeftPairs, HashMap<Integer, HashMap<String, Integer>> danglingRightPairs, HashMap<Integer, HashSet<Integer>> il, HashMap<Integer, HashSet<String>> is, HashMap<Integer, HashSet<String>> io) {
        for (String s : danglingSubjects) {
            iis.put(s, cs);
            HashSet<String> ss = is.get(cs);
            if (ss == null) {
                ss = new HashSet<String>();
            }
            ss.add(s);
            is.put(cs, ss);
        }
        for (String o : danglingObjects.keySet()) {
            HashMap<String, Integer> psCount = danglingObjects.get(o);
            HashMap<String, HashMap<Integer,Integer>> psCsCount = iio.get(o);
            if (psCsCount == null) {
                psCsCount = new HashMap<String, HashMap<Integer,Integer>>();
            }
            for (String p : psCount.keySet()) {
                Integer c = psCount.get(p);
                HashMap<Integer,Integer> csCount = psCsCount.get(p);
                if (csCount == null) {
                    csCount = new HashMap<Integer,Integer>();
                }
                Integer tmpC = csCount.get(cs);
                if (tmpC == null) {
                    tmpC = c;
                } else {
                    tmpC += c;
                }
                csCount.put(cs, tmpC);
                psCsCount.put(p, csCount);
            }
            iio.put(o, psCsCount);
            HashSet<String> os = io.get(cs);
            if (os == null) {
                os = new HashSet<String>();
            }
            os.add(o);
            io.put(cs, os);
        }
        // Replacing the csDeleted by cs in danglingLeftPairs (connections to itself)
        HashMap<String, Integer> psCountAux1 = danglingLeftPairs.get(csDeleted);  /////////////////////////////
        danglingLeftPairs.remove(csDeleted);
        if (psCountAux1 != null) {
            HashMap<String, Integer> psCountAux2 = danglingLeftPairs.get(cs);
            if (psCountAux2 != null) {
                for (String p : psCountAux1.keySet()) {
                    Integer c = psCountAux2.get(p);
                    if (c==null) {
                       c = 0;
                    }
                    c+= psCountAux1.get(p);
                    psCountAux2.put(p, c);
                }
                danglingLeftPairs.put(cs, psCountAux2);
            } else {
                danglingLeftPairs.put(cs, psCountAux1);
            }
        }
        // Replacing the csDeleted by cs in danglingRightPairs (connections to itself)
        psCountAux1 = danglingRightPairs.get(csDeleted);  /////////////////////////////
        danglingRightPairs.remove(csDeleted);
        if (psCountAux1 != null) {
            HashMap<String, Integer> psCountAux2 = danglingRightPairs.get(cs);
            if (psCountAux2 != null) {
                for (String p : psCountAux1.keySet()) {
                    Integer c = psCountAux2.get(p);
                    if (c==null) {
                       c = 0;
                    }
                    c+= psCountAux1.get(p);
                    psCountAux2.put(p, c);
                }
                danglingRightPairs.put(cs, psCountAux2);
            } else {
                danglingRightPairs.put(cs, psCountAux1);
            }
        }
        // danglingLeftPairs: CS_ID_R ->  Predicate -> Count
        HashMap<Integer, HashMap<String, Integer>> csRPsCount = cps.get(cs);
        if (csRPsCount == null) {
            csRPsCount = new HashMap<Integer, HashMap<String, Integer>>();
        }
        for (Integer csR : danglingLeftPairs.keySet()) {
            HashMap<String, Integer> psCount = csRPsCount.get(csR);
            if (psCount == null) {
                psCount = new HashMap<String, Integer>();
            }
            HashMap<String, Integer> psCountToAdd = danglingLeftPairs.get(csR);
            for (String p : psCountToAdd.keySet()) {
                Integer count = psCount.get(p);
                if (count == null) {
                    count = 0;
                }
                count += psCountToAdd.get(p);
                psCount.put(p, count);
            }
            csRPsCount.put(csR, psCount);
            HashSet<Integer> ls = il.get(csR);
            if (ls==null) {
                ls=new HashSet<Integer>();
            }
            ls.add(cs);
            il.put(csR, ls);
        }
        cps.put(cs, csRPsCount);
        // danglingRightPairs: CS_ID_L ->  Predicate -> Count
        for (Integer csL : danglingRightPairs.keySet()) {
            csRPsCount = cps.get(csL);
            if (csRPsCount == null) {
                csRPsCount = new HashMap<Integer,HashMap<String, Integer>>();
            }
            HashMap<String, Integer> psCountToAdd = danglingRightPairs.get(csL);
            HashMap<String, Integer> psCount = csRPsCount.get(cs);
            if (psCount == null) {
                psCount = new HashMap<String, Integer>();
            }
            for (String p : psCountToAdd.keySet()) {
                Integer count = psCount.get(p);
                if (count == null) {
                    count = 0;
                }
                count += psCountToAdd.get(p);
                psCount.put(p, count);
            }
            csRPsCount.put(cs, psCount);
            cps.put(csL, csRPsCount);
            HashSet<Integer> ls = il.get(cs);
            if (ls==null) {
                ls=new HashSet<Integer>();
            }
            ls.add(csL);
            il.put(cs, ls);
        }
    }

    public static void mergeCS(Integer csDeleted, Pair<Integer, HashMap<String, Pair<Integer, Integer>>> countPreds, HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> reducedCSS,HashMap<String, Integer> iis, HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio, HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, HashSet<String> danglingSubjects, HashMap<String, HashMap<String, Integer>> danglingObjects, HashMap<Integer, HashMap<String, Integer>> danglingLeftPairs, HashMap<Integer, HashMap<String, Integer>> danglingRightPairs, HashMap<Integer, HashSet<Integer>> il, HashMap<Integer, HashSet<String>> is, HashMap<Integer, HashSet<String>> io) {

        Integer tmpCount = countPreds.getFirst();
        HashMap<String, Pair<Integer, Integer>> tmpPsCount = countPreds.getSecond();
        Set<String> tmpPs = tmpPsCount.keySet();

        Integer best = null;
        Integer bestCount = null;
        HashMap<String, Pair<Integer, Integer>> bestPsCount = null;
        Set<String> bestPs = null;

        for (Integer cs2 : reducedCSS.keySet()) {
            Integer auxCount = reducedCSS.get(cs2).getFirst();
            HashMap<String, Pair<Integer, Integer>> auxPsCount = reducedCSS.get(cs2).getSecond();
            Set<String> auxPs = auxPsCount.keySet();

            if (auxPs.containsAll(tmpPs)) {
                if ((best == null) || (bestPs.size()>auxPs.size()) || ((bestPs.size()==auxPs.size()) && (bestCount<auxCount))) {
                    best = cs2;
                    bestCount = auxCount;
                    bestPsCount = auxPsCount;
                    bestPs = auxPs;
                }
            }
        }
        if (best != null) {
            bestCount += tmpCount;
            for (String p : tmpPs) {
                Integer c = bestPsCount.get(p).getFirst() + tmpPsCount.get(p).getFirst();
                Integer d = bestPsCount.get(p).getSecond() + tmpPsCount.get(p).getSecond();
                //System.out.println("c: "+c+". p: "+p+". bestPsCount.get(p): "+bestPsCount.get(p)+". tmpPsCount.get(p): "+ tmpPsCount.get(p));
                bestPsCount.put(p, new Pair<Integer, Integer>(c, d));
            }
            reducedCSS.put(best, new Pair(bestCount, bestPsCount));
            setMissingCSS(csDeleted, best, iis, iio, cps, danglingSubjects, danglingObjects, danglingLeftPairs, danglingRightPairs, il, is, io);
            return;
        } 
        // best = null;
        HashSet<String> intersection = null;
        for (Integer cs2 : reducedCSS.keySet()) {
            HashSet<String> tmp = new HashSet<String>(reducedCSS.get(cs2).getSecond().keySet());
            tmp.retainAll(tmpPs);
            if ((tmp.size()>0) && ((best == null) || intersection.size() < tmp.size())) {
                best = cs2;
                intersection = tmp;
            }
        }
        if (best != null) {
            Set<String> complement = new HashSet<String>(tmpPs);
            complement.removeAll(intersection);
            HashMap<String, Pair<Integer, Integer>> tmpPsCountI = new HashMap<String, Pair<Integer, Integer>>();
            HashMap<String, Pair<Integer, Integer>> tmpPsCountC = new HashMap<String, Pair<Integer, Integer>>();
            // Entity -> Predicate -> Count
            HashMap<String, HashMap<String, Integer>> danglingObjectsI = new HashMap<String, HashMap<String, Integer>>();
            HashMap<String, HashMap<String, Integer>> danglingObjectsC = new HashMap<String, HashMap<String, Integer>>();
            // CS_ID -> Predicate -> Count
            HashMap<Integer, HashMap<String, Integer>> danglingLeftPairsI = new HashMap<Integer, HashMap<String, Integer>>();
            HashMap<Integer, HashMap<String, Integer>> danglingLeftPairsC = new HashMap<Integer, HashMap<String, Integer>>();
            // CS_ID -> Predicate -> Count
            HashMap<Integer, HashMap<String, Integer>> danglingRightPairsI = new HashMap<Integer, HashMap<String, Integer>>();
            HashMap<Integer, HashMap<String, Integer>> danglingRightPairsC = new HashMap<Integer, HashMap<String, Integer>>();

            for (String p : tmpPs) {
                Pair<Integer, Integer> cd = tmpPsCount.get(p);
                if (intersection.contains(p)) {
                    tmpPsCountI.put(p, cd);
                } else {
                    tmpPsCountC.put(p, cd);
                }
            }

            for (String o : danglingObjects.keySet()) {
                HashMap<String, Integer> psCount = danglingObjects.get(o);
                HashMap<String, Integer> psCountI = danglingObjectsI.get(o);
                if (psCountI == null) {
                    psCountI = new HashMap<String, Integer>();
                }
                HashMap<String, Integer> psCountC = danglingObjectsC.get(o);
                if (psCountC == null) {
                    psCountC = new HashMap<String, Integer>();
                }
                for (String p : psCount.keySet()) {
                    Integer c = psCount.get(p);
                    if (intersection.contains(p)) {
                        psCountI.put(p,c);
                    } else {
                        psCountC.put(p,c);
                    }
                }
                if (psCountI.size()>0) {
                    danglingObjectsI.put(o, psCountI);
                }
                if (psCountC.size()>0) {
                    danglingObjectsC.put(o, psCountC);
                }
            }

            for (Integer csR : danglingLeftPairs.keySet()) {
                HashMap<String, Integer> psCount = danglingLeftPairs.get(csR);
                HashMap<String, Integer> psCountI = danglingLeftPairsI.get(csR);
                if (psCountI == null) {
                    psCountI = new HashMap<String, Integer>();
                }
                HashMap<String, Integer> psCountC = danglingLeftPairsC.get(csR);
                if (psCountC == null) {
                    psCountC = new HashMap<String, Integer>();
                }
                for (String p : psCount.keySet()) {
                    Integer c = psCount.get(p);
                    if (intersection.contains(p)) {
                        psCountI.put(p,c);
                    } else {
                        psCountC.put(p,c);
                    }
                }
                if (psCountI.size()>0) {
                    danglingLeftPairsI.put(csR, psCountI);
                }
                if (psCountC.size()>0) {
                    danglingLeftPairsC.put(csR, psCountC);
                }
            }

            for (Integer csL : danglingRightPairs.keySet()) {
                HashMap<String, Integer> psCount = danglingRightPairs.get(csL);
                HashMap<String, Integer> psCountI = danglingRightPairsI.get(csL);
                if (psCountI == null) {
                    psCountI = new HashMap<String, Integer>();
                }
                HashMap<String, Integer> psCountC = danglingRightPairsC.get(csL);
                if (psCountC == null) {
                    psCountC = new HashMap<String, Integer>();
                }
                for (String p : psCount.keySet()) {
                    Integer c = psCount.get(p);
                    if (intersection.contains(p)) {
                        psCountI.put(p,c);
                    } else {
                        psCountC.put(p,c);
                    }
                }
                if (psCountI.size()>0) {
                    danglingRightPairsI.put(csL, psCountI);
                }
                if (psCountC.size()>0) {
                    danglingRightPairsC.put(csL, psCountC);
                }
            }
            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> countPredsI = new Pair(tmpCount, tmpPsCountI);
            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> countPredsC = new Pair(tmpCount, tmpPsCountC);
            //System.out.println(countPredsI);
            mergeCS(csDeleted, countPredsI, reducedCSS, iis, iio, cps, danglingSubjects, danglingObjectsI, danglingLeftPairsI, danglingRightPairsI, il, is, io);
            //System.out.println(countPredsC);
            mergeCS(csDeleted, countPredsC, reducedCSS, iis, iio, cps, danglingSubjects, danglingObjectsC, danglingLeftPairsC, danglingRightPairsC, il, is, io);
        }
    }

    public static void showInfo(Pair<Integer, HashMap<String, Integer>> infoCS) {

        Integer count = infoCS.getFirst();
        HashMap<String, Integer> predsMult = infoCS.getSecond();
        System.out.println("count: "+count);
        for (String p : predsMult.keySet()) {
            Integer m = predsMult.get(p);
            System.out.println("mult("+p+"): "+m); 
        }
    }
    
    public static void showLinks(HashMap<String, Integer> links) {

        for (String p : links.keySet()) {
            Integer m = links.get(p);
            System.out.println("link_mult("+p+"): "+m); 
        }
    }

    public static HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> readCSS(String file) throws Exception {
        final ObjectInputStream in = new ObjectInputStream(
            new BufferedInputStream(new FileInputStream(file)));

        final HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer,Integer>>>> css = (HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer,Integer>>>>) in.readObject();
        return css;
    }

    public static HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> readCPS(String file) throws Exception {
        final ObjectInputStream in = new ObjectInputStream(
            new BufferedInputStream(new FileInputStream(file)));

        final HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps = (HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>) in.readObject();
        return cps;
    }

    public static HashMap<String, Integer> readIIS(String file) {
        HashMap<String, Integer> iis = null;
        try {
            final ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            iis = (HashMap<String, Integer>) in.readObject();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }        
        return iis;
    }

    public static HashMap<String, HashMap<String, HashMap<Integer,Integer>>> readIIO(String file) {
        HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio = null; 
        try {
            final ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            iio = (HashMap<String, HashMap<String, HashMap<Integer,Integer>>>) in.readObject();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }        
        return iio;
    }
}

