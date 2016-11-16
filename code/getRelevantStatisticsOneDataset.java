import java.io.*;
import java.util.*;

class getRelevantStatisticsOneDataset {

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

    public static void main(String[] args) throws Exception {
        String fileStar1 = args[0];
        String fileStar2 = args[1];
        String fileCSS1 = args[2];
        String fileCPS1 = args[4];
        HashSet<String> ps1 = readPS(fileStar1);
        HashSet<String> ps2 = readPS(fileStar2);
        HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css1 = readCSS(fileCSS1);
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps1 = readCPS(fileCPS1);
        int c = 0;
        int n = 0;
        Set<String> csMin1 = null;
        Set<String> csMin2 = null;
        HashMap<String, Integer> mult = new HashMap<String, Integer>();
        for (Integer cs1 : css1.keySet()) {
            Pair<Integer, HashMap<String, Integer>> cPs = css1.get(cs1);
            HashMap<String, Integer> ps = cPs.getSecond();
            if (ps.keySet().containsAll(ps1)) {
               /* Set<String> tmpCS = ps.keySet();
                if ((csMin1 == null) || tmpCS.size() < csMin1.size()) {
                    csMin1 = tmpCS;
                }*/
                Integer count = cPs.getFirst();
                HashMap<String, Integer> predsMult = cPs.getSecond();
                c+= count;
                for (String p : ps1) {
                    //System.out.println(p+" mult: "+m1);
                    Integer m1 = predsMult.get(p);
                    Integer m2 = mult.get(p);
                    if (m2 == null) {
                        m2 = 0;
                    }
                    Integer tmp = m1+m2;
                    mult.put(p, tmp);
                    //System.out.println(p+" mult: "+m1);
                }
                n++;
            }
        }
        System.out.println("Sub-query 1, "+n+" relevant CS, count: "+c);
        for (String p : ps1) {
            Integer tmp = mult.get(p);
            System.out.println("mult("+p+"): "+tmp);
        }
        c = 0;
        n = 0;
        mult = new HashMap<String, Integer>();
        for (Integer cs1 : css1.keySet()) {
            Pair<Integer, HashMap<String, Integer>> cPs = css1.get(cs1);
            HashMap<String, Integer> ps = cPs.getSecond();
            if (ps.keySet().containsAll(ps2)) {
                /*Set<String> tmpCS = ps.keySet();
                if ((csMin2 == null) || tmpCS.size() < csMin2.size()) {
                    csMin2 = tmpCS;
                }*/
                Integer count = cPs.getFirst();
                HashMap<String, Integer> predsMult = cPs.getSecond();
                //System.out.println(ps.keySet());
                c+= count;
                for (String p : ps2) {
                    Integer m1 = predsMult.get(p);
                    Integer m2 = mult.get(p);
                    if (m2 == null) {
                        m2 = 0;
                    }
                    Integer tmp = m1+m2;
                    mult.put(p, tmp);
                    //System.out.println(p+" mult: "+m1);
                }
                n++;
            }
        }
        System.out.println("Sub-query 2, "+n+" relevant CS, count: "+c);
        for (String p : ps2) {
            Integer tmp = mult.get(p);
            System.out.println("mult("+p+"): "+tmp);
        }
        //System.out.println("csMin1: "+csMin1);
        //System.out.println("csMin2: "+csMin2);
        String tmpS1 = null;
        String tmpS2 = null;
        HashMap<String, Integer> mult12 = new HashMap<String, Integer>();
        HashMap<String, Integer> mult21 = new HashMap<String, Integer>();
        HashMap<String, Integer> c12 = new HashMap<String, Integer>();;
        HashMap<String, Integer> c21 = new HashMap<String, Integer>();
        for (Integer cs1 : css1.keySet()) {
            Pair<Integer, HashMap<String, Integer>> cPs = css1.get(cs1);
            HashMap<String, Integer> ps = cPs.getSecond();
            if (ps.keySet().containsAll(ps1)) {

                for (Integer cs2 : css1.keySet()) {
                    Pair<Integer, HashMap<String, Integer>> cPs2 = css1.get(cs2);
                    HashMap<String, Integer> psB = cPs2.getSecond();
                    if (psB.keySet().containsAll(ps2)) {

                        HashMap<Integer, HashMap<String, Integer>> css2Ps = cps1.get(cs1);
                        if (css2Ps != null) {
                            //System.out.println("Links 1 -> 2");
                            HashMap<String, Integer> links12 = css2Ps.get(cs2);
                            if (links12 != null) {
                              String auxS1="";
                              for (String p : links12.keySet()) {
                                Integer m1 = links12.get(p);
                                Integer m2 = mult12.get(p);
                                if (m2 == null) {
                                    m2 = 0;
                                }
                                mult12.put(p, m1+m2);
                                m2 = c12.get(p);
                                if (m2 == null) {
                                    m2 = 0;
                                }
                                c12.put(p, m2+1);
                                
                                auxS1+=("mult("+p+"): "+m1);
                              }
                              if (((csMin1 == null) || (csMin2 == null)) || csMin1.size()+csMin2.size()>ps.keySet().size()+psB.keySet().size()) {
                                 tmpS1 = auxS1;
                                 csMin1 = ps.keySet();
                                 csMin2 = psB.keySet();
                              }
                            }
                        }
                        css2Ps = cps1.get(cs2);
                        if (css2Ps != null) {
                            //System.out.println("Links 2 -> 1");
                            HashMap<String, Integer> links21 = css2Ps.get(cs1);
                            if (links21 != null) {
                              String auxS2="";
                              for (String p : links21.keySet()) {
                                Integer m1 = links21.get(p);
                                Integer m2 = mult21.get(p);
                                if (m2 == null) {
                                    m2 = 0;
                                }
                                mult21.put(p, m1+m2);
                                m2 = c21.get(p);
                                if (m2 == null) {
                                    m2 = 0;
                                }
                                c21.put(p, m2+1);
                                
                                auxS2+=("mult("+p+"): "+m1);
                              }
                              if (((csMin1 == null) || (csMin2 == null)) || csMin1.size()+csMin2.size()>ps.keySet().size()+psB.keySet().size()) {
                                 tmpS2 = auxS2;
                                 csMin1 = ps.keySet();
                                 csMin2 = psB.keySet();
                              }
                            }
                        }
                    }
                }

            }
        }
        System.out.println("Links 1 -> 2");
        for (String p : mult12.keySet()) {
            Integer tmpM = mult12.get(p);
            Integer tmpC = c12.get(p);
            System.out.println("mult("+p+"): "+tmpM+", count: "+tmpC);
        }
        System.out.println("Links 2 -> 1");
        for (String p : mult21.keySet()) {
            Integer tmpM = mult21.get(p);
            Integer tmpC = c21.get(p);
            System.out.println("mult("+p+"): "+tmpM+", count: "+tmpC);
        }
        System.out.println("csMin1: "+csMin1);
        System.out.println("csMin2: "+csMin2);
        System.out.println("tmpS1: "+tmpS1);
        System.out.println("tmpS2: "+tmpS2);
        //HashMap<String, Integer> invertIndexSubject = readIIS(fileISS);
        //HashMap<String, HashSet<Integer>> invertIndexObject = readIIO(fileIIO);
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

    public static HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> readCSS(String file) throws Exception {
        final ObjectInputStream in = new ObjectInputStream(
            new BufferedInputStream(new FileInputStream(file)));

        final HashMap<Integer, Pair<Integer, HashMap<String, Integer>>> css = (HashMap<Integer, Pair<Integer, HashMap<String, Integer>>>) in.readObject();
        return css;
    }

    public static HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> readCPS(String file) throws Exception {
        final ObjectInputStream in = new ObjectInputStream(
            new BufferedInputStream(new FileInputStream(file)));

        final HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps = (HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>) in.readObject();
        return cps;
    }

    public static HashMap<String, Integer> readIIS(String file) throws Exception {
        final ObjectInputStream in = new ObjectInputStream(
            new BufferedInputStream(new FileInputStream(file)));

        final HashMap<String, Integer> iis = (HashMap<String, Integer>) in.readObject();
        return iis;
    }

    public static HashMap<String, HashSet<Integer>> readIIO(String file) throws Exception {
        final ObjectInputStream in = new ObjectInputStream(
            new BufferedInputStream(new FileInputStream(file)));

        final HashMap<String, HashSet<Integer>> iio = (HashMap<String, HashSet<Integer>>) in.readObject();
        return iio;
    }
}
