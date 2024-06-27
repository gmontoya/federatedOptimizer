import java.io.*;
import java.util.*;

class generateStatistics {

    public static void write(HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer,Integer>>>> css, HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, HashMap<String, Integer> invertIndexSubject, HashMap<String, HashMap<String, HashMap<Integer,Integer>>> invertIndexObject, String base) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(base+"_css")));
            out.writeObject(css);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+base+"_css");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(base+"_cps")));
            out.writeObject(cps);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+base+"_cps");
            System.exit(1);
        }
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(base+"_iis")));
            out.writeObject(invertIndexSubject);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+base+"_iis");
            System.exit(1);
        }

        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(base+"_iio")));
            out.writeObject(invertIndexObject);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+base+"_iio");
            System.exit(1);
        }
    }

    public static void update(Pair<Integer, HashMap<String, Pair<Integer,Integer>>> cs, Integer count, HashMap<String,Pair< Integer,Integer>> tmp) {

        Integer countCS =  cs.getFirst();
        HashMap<String, Pair<Integer,Integer>> psCS = cs.getSecond();
        for (String p : tmp.keySet()) {
            Pair<Integer,Integer> cAux = psCS.get(p);
            Integer cAuxF, cAuxS;
            if (cAux == null) {
                cAuxF = 0;
                cAuxS = 0;
            } else {
                cAuxF = cAux.getFirst();
                cAuxS = cAux.getSecond();
            }
            Pair<Integer, Integer> cAux2 = tmp.get(p);
            Integer f = cAuxF + cAux2.getFirst();
            Integer s = cAuxS + cAux2.getSecond();
            psCS.put(p, new Pair<Integer,Integer>(f,s));
        }
        cs.setFirst(countCS + count);
        cs.setSecond(psCS);
    }

    public static void update(HashMap<Integer, HashMap<String, HashSet<String>>> csPsOs, Integer ikey, HashMap<String, HashSet<String>> objects) {

        HashMap<String, HashSet<String>> psOs = csPsOs.get(ikey);
        if (psOs == null) {
            psOs = new HashMap<String, HashSet<String>>();
        }
        for (String p : objects.keySet()) {
            HashSet<String> os = psOs.get(p);
            if (os == null) {
                os = new HashSet<String>();
            }
            os.addAll(objects.get(p));
            psOs.put(p, os);
        }
        csPsOs.put(ikey, psOs);
    }

    public static void updateNO(HashMap<Integer, HashMap<String, HashSet<String>>> csPsOs, HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css) {

        for (Integer cs : csPsOs.keySet()) {
            HashMap<String, HashSet<String>> psOs = csPsOs.get(cs);
            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> countPsMultNO = css.get(cs);
            Integer count = countPsMultNO.getFirst();
            HashMap<String, Pair<Integer, Integer>> psMultNO = countPsMultNO.getSecond();
            for (String p : psOs.keySet()) {
                Integer num = psOs.get(p).size();
                Integer mult = psMultNO.get(p).getFirst();
                psMultNO.put(p, new Pair<Integer, Integer>(mult, num));
            }
            css.put(cs, new Pair<Integer, HashMap<String, Pair<Integer, Integer>>>(count, psMultNO));
        }
    }

    public static void main(String[] args) {

        String fileName = args[0];
        String base = args[1];
        // CS_Id -> count -> Predicate -> < count, numDifObj >
        HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css = new HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>>();
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps = new HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>();
        HashMap<String, Integer> invertIndexSubject = new HashMap<String, Integer>();
        // Object -> (Property -> (CS -> Integer))
        HashMap<String, HashMap<String, HashMap<Integer, Integer>>> invertIndexObject = new HashMap<String, HashMap<String, HashMap<Integer, Integer>>>();
        //  CS -> Property -> Objets
        HashMap<Integer, HashMap<String, HashSet<String>>> csPsOs = new HashMap<Integer, HashMap<String, HashSet<String>>>();
	String l = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            l = br.readLine();
            String pe = null;
            String key = "";
            Integer ikey = -1;
            HashMap<String, Pair<Integer, Integer>> tmp = new HashMap<String, Pair<Integer,Integer>>();
            HashMap<String, HashSet<String>> objects = new HashMap<String, HashSet<String>>();
            String s = null;
            String o = null;
            while (l!=null) {
                int i = l.indexOf(" ");
                s = l.substring(0, i);
                String r = l.substring(i+1);
                i = r.indexOf(" ");
                String p = r.substring(0, i);
                int j = r.lastIndexOf('.');
                o = r.substring(i+1, j);

                if ((pe != null) && (!pe.equals(s))) {
                    ikey = key.hashCode();
                    // adding or updating the characteristic set
                    Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cs = css.get(ikey);
                    if (cs == null) {
                        cs = new Pair(1, tmp);
                    } else {
                        update(cs, 1, tmp);    
                    }
                    css.put(ikey, cs);
                    update(csPsOs, ikey, objects);
                    // updating the index for subjects
                    //System.out.println("adding "+s+" to the iis");
                    invertIndexSubject.put(pe, ikey);
                    // the property values of this entity
                    for (String pAux : objects.keySet()) {
                      for (String obj : objects.get(pAux)) {
                          if (obj.charAt(0) != '<') {
                              continue;
                          }
                          // check if there is information already for this object
                          HashMap<String, HashMap<Integer,Integer>> psCsCount = invertIndexObject.get(obj);
                          if (psCsCount == null) {
                              psCsCount = new HashMap<String, HashMap<Integer,Integer>>();
                          }
                          // check if there is information already for this object-property
                          HashMap<Integer,Integer> csCountAux = psCsCount.get(pAux);
                          if (csCountAux == null) {
                              csCountAux = new HashMap<Integer,Integer>();
                          }
                          Integer countAux = csCountAux.get(ikey);
                          if (countAux == null) {
                              countAux = 0;
                          }
                          countAux++;
                          // Add the new CS for this object-property, update of object index
                          csCountAux.put(ikey, countAux);
                          psCsCount.put(pAux, csCountAux);
                          invertIndexObject.put(obj, psCsCount);

                          // update links from this entity to other entities
                          Integer other = invertIndexSubject.get(obj);
                          if (other != null) {
                              HashMap<Integer, HashMap<String, Integer>> fst = cps.get(ikey);
                              if (fst == null) {
                                  fst = new HashMap<Integer, HashMap<String, Integer>>();
                              }                        
                              HashMap<String, Integer> snd = fst.get(other);
                              if (snd == null) {
                                  snd = new HashMap<String, Integer>();
                              }
                              Integer numPairs = snd.get(pAux);
                              if (numPairs == null) {
                                  numPairs = 0;
                              }
                              numPairs++;
                              snd.put(pAux, numPairs);
                              fst.put(other, snd);
                              cps.put(ikey, fst);
                          }
                      }
                    }
                    // update links from other entities to this entity
                    HashMap<String, HashMap<Integer,Integer>> psCsCount = invertIndexObject.get(pe);
                    //System.out.println("is "+pe+" the object of some entity?\n"+invertIndexObject+"\n"+psCss);
                    if (psCsCount != null) {
                        for (String pAux : psCsCount.keySet()) {
                            for (Integer aux : psCsCount.get(pAux).keySet()) {
                                    HashMap<Integer, HashMap<String, Integer>> fst = cps.get(aux);
                                    if (fst == null) {
                                        fst = new HashMap<Integer, HashMap<String, Integer>>();
                                    }                          
                                    HashMap<String, Integer> snd = fst.get(ikey);
                                    if (snd == null) {
                                        snd = new HashMap<String, Integer>();
                                    }
                                    Integer numPairs = snd.get(pAux);
                                    if (numPairs == null) {
                                        numPairs = 0;
                                    }
                                    Integer numPairsTmp = psCsCount.get(pAux).get(aux);
                                    numPairs += numPairsTmp;
                                    snd.put(pAux, numPairs);
                                    fst.put(ikey, snd);
                                    cps.put(aux, fst);
                            }
                        }
                    }
                    key = "";
                    tmp = new HashMap<String, Pair<Integer, Integer>>();
                    objects = new HashMap<String, HashSet<String>>();
                }
                if (!tmp.containsKey(p)) {
                    key = key + p;
                }                
                Pair<Integer, Integer> os = tmp.get(p);
                Integer fos, sos;
                if (os == null) {
                    fos = 0;
                    sos = 0;
                } else {
                    fos = os.getFirst();
                    sos = os.getSecond();
                }
                fos = fos + 1;
                os = new Pair<Integer, Integer>(fos, sos);
                tmp.put(p, os);
                //System.out.println("considering object "+o);
                //if (o.charAt(0) == '<') {
                    //System.out.println("adding object "+o+" for predicate "+p+" and subject "+s);
                    HashSet<String> objAux = objects.get(p);
                    if (objAux == null) {
                        objAux = new HashSet<String>();
                    }
                    objAux.add(o.trim());
                    objects.put(p, objAux);
                //}
                //count = count + 1;

                l = br.readLine();
                pe = s;
            }
            br.close();
            if ((s != null) && (o != null)) {
                    ikey = key.hashCode();
                    // adding or updating the characteristic set
                    Pair<Integer, HashMap<String, Pair<Integer,Integer>>> cs = css.get(ikey);
                    if (cs == null) {
                        cs = new Pair(1, tmp);
                    } else {
                        update(cs, 1, tmp);    
                    }
                    css.put(ikey, cs);
                    update(csPsOs, ikey, objects);
                    // updating the index for subjects
                    invertIndexSubject.put(pe, ikey);
                    // the property values of this entity
                    for (String pAux : objects.keySet()) {
                      for (String obj : objects.get(pAux)) {
                          // check if there is information already for this object
                          if (obj.charAt(0) != '<') {
                              continue;
                          }
                          HashMap<String, HashMap<Integer,Integer>> psCsCount = invertIndexObject.get(obj);
                          if (psCsCount == null) {
                              psCsCount = new HashMap<String, HashMap<Integer,Integer>>();
                          }
                          // check if there is information already for this object-property
                          HashMap<Integer,Integer> csCountAux = psCsCount.get(pAux);
                          if (csCountAux == null) {
                              csCountAux = new HashMap<Integer,Integer>();
                          }

                          Integer countAux = csCountAux.get(ikey);
                          if (countAux == null) {
                              countAux = 0;
                          }
                          countAux++;

                          // Add the new CS for this object-property, update of object index
                          csCountAux.put(ikey, countAux);
                          psCsCount.put(pAux, csCountAux);
                          invertIndexObject.put(obj, psCsCount);

                          // update links from this entity to other entities
                          Integer other = invertIndexSubject.get(obj);
                          if (other != null) {
                              HashMap<Integer, HashMap<String, Integer>> fst = cps.get(ikey);
                              if (fst == null) {
                                  fst = new HashMap<Integer, HashMap<String, Integer>>();
                              }                        
                              HashMap<String, Integer> snd = fst.get(other);
                              if (snd == null) {
                                  snd = new HashMap<String, Integer>();
                              }
                              Integer numPairs = snd.get(pAux);
                              if (numPairs == null) {
                                  numPairs = 0;
                              }
                              numPairs++;
                              snd.put(pAux, numPairs);
                              fst.put(other, snd);
                              cps.put(ikey, fst);
                          }
                      }
                    }
                    // update links from other entities to this entity
                    HashMap<String, HashMap<Integer,Integer>> psCsCount = invertIndexObject.get(pe);
                    if (psCsCount != null) {
                        for (String pAux : psCsCount.keySet()) {
                            for (Integer aux : psCsCount.get(pAux).keySet()) {
                                    HashMap<Integer, HashMap<String, Integer>> fst = cps.get(aux);
                                    if (fst == null) {
                                        fst = new HashMap<Integer, HashMap<String, Integer>>();
                                    }                          
                                    HashMap<String, Integer> snd = fst.get(ikey);
                                    if (snd == null) {
                                        snd = new HashMap<String, Integer>();
                                    }
                                    Integer numPairs = snd.get(pAux);
                                    if (numPairs == null) {
                                        numPairs = 0;
                                    }
                                    
                                    Integer numPairsTmp = psCsCount.get(pAux).get(aux);
                                    numPairs += numPairsTmp;
                                    snd.put(pAux, numPairs);
                                    fst.put(ikey, snd);
                                    cps.put(aux, fst);
                            }
                        }
                    }
            }
            updateNO(csPsOs, css);
            write(css, cps, invertIndexSubject, invertIndexObject, base);
            //System.out.println(css);
            //System.out.println(cps);
            //System.out.println(invertIndexSubject);
            //System.out.println(invertIndexObject);
        } catch (IOException e) {
            System.err.println("Problems reading file: "+fileName);
            System.exit(1);
        } catch (Exception e) {
            System.err.println("an exception was catched with message: "+e.getMessage());
            System.err.println("l: "+l);
	    System.exit(1);
	}
        
    }
}
