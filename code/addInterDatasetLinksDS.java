import java.util.*;
import java.io.*;

class addInterDatasetLinksDS {

    public static void main(String[] args) {

        String lisFile1 = args[0];
        String lioFile1 = args[1];
        String lisFile2 = args[2];
        String lioFile2 = args[3];
        String cpsFile1 = args[4];
        String cpsFile2 = args[5];
        HashMap<Integer, HashMap<String, Integer>> lis1 = readLIS(lisFile1);
        HashMap<Integer, HashMap<String, Integer>> lis2 = readLIS(lisFile2);
        HashMap<Integer, HashMap<String, HashMap<String,Integer>>> lio1 = readLIO(lioFile1);
        HashMap<Integer, HashMap<String, HashMap<String,Integer>>> lio2 = readLIO(lioFile2);
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps1 = new HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>();
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps2 = new HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>();
        HashMap<String, HashMap<Integer, Integer>> liis1 = invertSubjectIndex(lis1);
        HashMap<String, HashMap<Integer, Integer>> liis2 = invertSubjectIndex(lis2);
        HashMap<String, HashMap<Integer, HashMap<String,Integer>>> liio1 = invertObjectIndex(lio1);
        HashMap<String, HashMap<Integer, HashMap<String,Integer>>> liio2 = invertObjectIndex(lio2);
        lis1 = null; lis2 = null; lio1 = null; lio2 = null;
        // Links 1 -> 2
        findLinks(liio1, liis2, cps1);
        //System.out.println("Total of computed CPs: "+cps1.size());
        writeCPS(cps1, cpsFile1);
        //System.out.println(cps1);
        cps1 = null;
        findLinks(liio2, liis1, cps2);
        //System.out.println("Total of computed CPs: "+cps2.size());
        writeCPS(cps2, cpsFile2);
        
        //System.out.println(cps2);
    }

    public static HashMap<String, HashMap<Integer, HashMap<String,Integer>>> invertObjectIndex(HashMap<Integer, HashMap<String, HashMap<String,Integer>>> lio) {
        // input: CS_ID -> Predicate -> DS -> count
        // output: DS -> CS_ID -> Predicate -> count
        HashMap<String, HashMap<Integer, HashMap<String,Integer>>> iio = new HashMap<String, HashMap<Integer, HashMap<String,Integer>>>();
        for (Integer cs : lio.keySet()) {
            HashMap<String, HashMap<String,Integer>> pDsCount = lio.get(cs);
            for (String p : pDsCount.keySet()) {
                HashMap<String,Integer> dsCount = pDsCount.get(p);
                for (String ds : dsCount.keySet()) {
                    HashMap<Integer, HashMap<String,Integer>> csPsCount = iio.get(ds);
                    if (csPsCount == null) {
                        csPsCount = new HashMap<Integer, HashMap<String,Integer>>();
                    }
                    HashMap<String,Integer> psCount = csPsCount.get(cs);
                    if (psCount == null) {
                        psCount = new HashMap<String,Integer>();
                    }
                    Integer count = psCount.get(p);
                    if (count == null) {
                        count = 0;
                    }
                    count += dsCount.get(ds);
                    psCount.put(p, count);
                    csPsCount.put(cs, psCount);
                    iio.put(ds, csPsCount);
                }
            }
        }
        return iio;
    }

    public static HashMap<String, HashMap<Integer, Integer>> invertSubjectIndex(HashMap<Integer, HashMap<String, Integer>> lis) {

        // input: CS_ID -> DS -> count
        // output: DS -> CS_ID -> count
        HashMap<String, HashMap<Integer,Integer>> iis = new HashMap<String, HashMap<Integer,Integer>>();
        for (Integer cs : lis.keySet()) {
            HashMap<String,Integer> dsCount = lis.get(cs);
            for (String ds : dsCount.keySet()) {
                HashMap<Integer,Integer> csCount = iis.get(ds);
                if (csCount == null) {
                    csCount = new HashMap<Integer,Integer>();
                } 
                Integer count = csCount.get(cs);
                if (count == null) {
                    count = 0;
                }
                count += dsCount.get(ds);
                csCount.put(cs, count);
                iis.put(ds, csCount);
            }
        }
        return iis;
    }

    // input liio: DS -> CS_ID -> Predicate -> count
    // input liis: DS -> CS_ID -> count
    // output cps: CS_ID -> CS_ID -> Predicate -> count
    public static void findLinks(HashMap<String, HashMap<Integer, HashMap<String,Integer>>> liio, HashMap<String, HashMap<Integer, Integer>> liis, HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps) {   

        for (String objDS : liio.keySet()) {
            //System.out.println("objDS: "+objDS);
            HashMap<Integer,Integer> csCountSubj = liis.get(objDS);
            if (csCountSubj == null) {
                continue;
            }
            HashMap<Integer, HashMap<String,Integer>> csPsCount = liio.get(objDS);
            if (csPsCount == null) {
                continue;
            }

            for (Integer csObject : csPsCount.keySet()) {

                HashMap<String,Integer> psCount = csPsCount.get(csObject);
                if (psCount == null) {
                    continue;
                }
                HashMap<Integer, HashMap<String, Integer>> csPsCountCP = cps.get(csObject);
                if (csPsCountCP == null) {
                    csPsCountCP = new HashMap<Integer, HashMap<String, Integer>>();
                }
                for (String p : psCount.keySet()) {
                    //int n = 0;
                    //System.out.println("p: "+p);
                    int countObject = psCount.get(p);
                    for (Integer csSubject : csCountSubj.keySet()) {
                        //System.out.println("csSubject: "+csSubject);
                        int countSubject = csCountSubj.get(csSubject);
                        /*if (countSubject == null) {
                            continue;
                        }*/

                        HashMap<String, Integer> psCountCP = csPsCountCP.get(csSubject);
                        if (psCountCP == null) {
                            psCountCP = new HashMap<String, Integer>();
                        }                    

                        int count = Math.min(countSubject, countObject);
                        /*Integer countCP = psCountCP.get(p);
                        if (countCP == null) {
                            countCP = 0;
                        }*/
                        int countCP = 0;
                        if (psCountCP.containsKey(p)) {
                            countCP = psCountCP.get(p);
                        }
                        countCP += count;
                        if (countCP > 0) {
                            psCountCP.put(p, countCP);
                        }
                        csPsCountCP.put(csSubject, psCountCP);
                    }
                    
                }
                cps.put(csObject, csPsCountCP); 
            
  //          int s = cps.size();
            //if (s%100==0) {
  //              System.out.println(s+" computed CP");
            //}
            }
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

    public static HashMap<Integer, HashMap<String, Integer>> readLIS(String file) {
        HashMap<Integer, HashMap<String, Integer>> lis = null;
        try {
            final ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            lis = (HashMap<Integer, HashMap<String, Integer>>) in.readObject();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }        
        return lis;
    }

    public static HashMap<Integer, HashMap<String, HashMap<String,Integer>>> readLIO(String file) {
        HashMap<Integer, HashMap<String, HashMap<String,Integer>>> lio = null; 
        try {
            final ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));

            lio = (HashMap<Integer, HashMap<String, HashMap<String,Integer>>>) in.readObject();
        } catch (Exception e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }        
        return lio;
    }
}

