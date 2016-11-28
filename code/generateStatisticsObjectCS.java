import java.io.*;
import java.util.*;

class generateStatisticsObjectCS {

    public static void write(HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer,Integer>>>> css, HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps, HashMap<String, Integer> invertIndexObject, HashMap<String, HashMap<String, HashMap<Integer,Integer>>> invertIndexSubject, String base) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(base+"_css_obj")));
            out.writeObject(css);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+base+"_css_obj");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(base+"_cps_obj")));
            out.writeObject(cps);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+base+"_cps_obj");
            System.exit(1);
        }
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(base+"_iis_obj")));
            out.writeObject(invertIndexSubject);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+base+"_iis_obj");
            System.exit(1);
        }

        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(base+"_iio_obj")));
            out.writeObject(invertIndexObject);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+base+"_iio_obj");
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

    public static void update(HashMap<Integer, HashMap<String, HashSet<String>>> csPsSs, Integer ikey, HashMap<String, HashSet<String>> subjects) {

        HashMap<String, HashSet<String>> psSs = csPsSs.get(ikey);
        if (psSs == null) {
            psSs = new HashMap<String, HashSet<String>>();
        }
        for (String p : subjects.keySet()) {
            HashSet<String> ss = psSs.get(p);
            if (ss == null) {
                ss = new HashSet<String>();
            }
            ss.addAll(subjects.get(p));
            psSs.put(p, ss);
        }
        csPsSs.put(ikey, psSs);
    }

    public static void updateNO(HashMap<Integer, HashMap<String, HashSet<String>>> csPsSs, HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css) {

        for (Integer cs : csPsSs.keySet()) {
            HashMap<String, HashSet<String>> psSs = csPsSs.get(cs);
            Pair<Integer, HashMap<String, Pair<Integer, Integer>>> countPsMultNO = css.get(cs);
            Integer count = countPsMultNO.getFirst();
            HashMap<String, Pair<Integer, Integer>> psMultNO = countPsMultNO.getSecond();
            for (String p : psSs.keySet()) {
                Integer num = psSs.get(p).size();
                Integer mult = psMultNO.get(p).getFirst();
                psMultNO.put(p, new Pair<Integer, Integer>(mult, num));
            }
            css.put(cs, new Pair<Integer, HashMap<String, Pair<Integer, Integer>>>(count, psMultNO));
        }
    }

    public static void main(String[] args) {

        String fileName = args[0];
        String base = args[1];
        // CS_Id -> count -> Predicate -> < count, numDifSubj >
        HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>> css = new HashMap<Integer, Pair<Integer, HashMap<String, Pair<Integer, Integer>>>>();
        HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> cps = new HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>();
        HashMap<String, Integer> invertIndexObject = new HashMap<String, Integer>();
        // Subject -> (Property -> (CS -> Integer))
        HashMap<String, HashMap<String, HashMap<Integer, Integer>>> invertIndexSubject = new HashMap<String, HashMap<String, HashMap<Integer, Integer>>>();
        //  CS -> Property -> Subjets
        HashMap<Integer, HashMap<String, HashSet<String>>> csPsSs = new HashMap<Integer, HashMap<String, HashSet<String>>>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String l = br.readLine();
            String pe = null;
            String key = "";
            Integer ikey = -1;
            HashMap<String, Pair<Integer, Integer>> tmp = new HashMap<String, Pair<Integer,Integer>>();
            HashMap<String, HashSet<String>> subjects = new HashMap<String, HashSet<String>>();
            String s = null;
            String o = null;
            while (l!=null) {
                int i = l.indexOf(" ");
                s = l.substring(0, i).trim();
                String r = l.substring(i+1);
                i = r.indexOf(" ");
                String p = r.substring(0, i).trim();
                int j = r.lastIndexOf('.');
                o = r.substring(i+1, j).trim();

                if ((pe != null) && (!pe.equals(o))) {
                    ikey = key.hashCode();
                    // adding or updating the characteristic set
                    Pair<Integer, HashMap<String, Pair<Integer, Integer>>> cs = css.get(ikey);
                    if (cs == null) {
                        cs = new Pair(1, tmp);
                    } else {
                        update(cs, 1, tmp);    
                    }
                    css.put(ikey, cs);
                    update(csPsSs, ikey, subjects);
                    // updating the index for subjects
                    //System.out.println("adding "+pe+" to the iio");
                    invertIndexObject.put(pe, ikey);
                    // the property values of this entity
                    for (String pAux : subjects.keySet()) {
                      for (String subj : subjects.get(pAux)) {
                          // check if there is information already for this subject
                          HashMap<String, HashMap<Integer,Integer>> psCsCount = invertIndexSubject.get(subj);
                          if (psCsCount == null) {
                              psCsCount = new HashMap<String, HashMap<Integer,Integer>>();
                          }
                          // check if there is information already for this subject-property
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
                          invertIndexSubject.put(subj, psCsCount);

                          // update links from this entity to other entities
                          Integer other = invertIndexObject.get(subj);
                          if (other != null) {
                              HashMap<Integer, HashMap<String, Integer>> fst = cps.get(other);
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
                              numPairs++;
                              snd.put(pAux, numPairs);
                              fst.put(ikey, snd);
                              cps.put(other, fst);
                          }
                      }
                    }
                    // update links from other entities to this entity
                    HashMap<String, HashMap<Integer,Integer>> psCsCount = invertIndexSubject.get(pe);
                    //System.out.println("is "+pe+" the subject of some entity?\n"+invertIndexSubject+"\n"+psCsCount);
                    if (psCsCount != null) {
                        for (String pAux : psCsCount.keySet()) {
                            for (Integer aux : psCsCount.get(pAux).keySet()) {
                                    HashMap<Integer, HashMap<String, Integer>> fst = cps.get(ikey);
                                    if (fst == null) {
                                        fst = new HashMap<Integer, HashMap<String, Integer>>();
                                    }                          
                                    HashMap<String, Integer> snd = fst.get(aux);
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
                                    fst.put(aux, snd);
                                    cps.put(ikey, fst);
                            }
                        }
                    }
                    key = "";
                    tmp = new HashMap<String, Pair<Integer, Integer>>();
                    subjects = new HashMap<String, HashSet<String>>();
                }
                if (!tmp.containsKey(p)) {
                    key = key + p;
                }                
                Pair<Integer, Integer> ss = tmp.get(p);
                Integer fss, sss;
                if (ss == null) {
                    fss = 0;
                    sss = 0;
                } else {
                    fss = ss.getFirst();
                    sss = ss.getSecond();
                }
                fss = fss + 1;
                ss = new Pair<Integer, Integer>(fss, sss);
                tmp.put(p, ss);
                //System.out.println("considering subject "+s);
                //if (o.charAt(0) == '<') {
                    //System.out.println("adding subject "+s+" for predicate "+p+" and object "+o);
                    HashSet<String> subjAux = subjects.get(p);
                    if (subjAux == null) {
                        subjAux = new HashSet<String>();
                    }
                    subjAux.add(s.trim());
                    subjects.put(p, subjAux);
                //}
                //count = count + 1;

                l = br.readLine();
                pe = o;
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
                    update(csPsSs, ikey, subjects);
                    // updating the index for objects
                    invertIndexObject.put(pe, ikey);
                    // the property values of this entity
                    for (String pAux : subjects.keySet()) {
                      for (String subj : subjects.get(pAux)) {
                          // check if there is information already for this subject
                          HashMap<String, HashMap<Integer,Integer>> psCsCount = invertIndexSubject.get(subj);
                          if (psCsCount == null) {
                              psCsCount = new HashMap<String, HashMap<Integer,Integer>>();
                          }
                          // check if there is information already for this subject-property
                          HashMap<Integer,Integer> csCountAux = psCsCount.get(pAux);
                          if (csCountAux == null) {
                              csCountAux = new HashMap<Integer,Integer>();
                          }

                          Integer countAux = csCountAux.get(ikey);
                          if (countAux == null) {
                              countAux = 0;
                          }
                          countAux++;

                          // Add the new CS for this object-property, update of subject index
                          csCountAux.put(ikey, countAux);
                          psCsCount.put(pAux, csCountAux);
                          invertIndexSubject.put(subj, psCsCount);

                          // update links from this entity to other entities
                          Integer other = invertIndexObject.get(subj);
                          if (other != null) {
                              HashMap<Integer, HashMap<String, Integer>> fst = cps.get(other);
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
                              numPairs++;
                              snd.put(pAux, numPairs);
                              fst.put(ikey, snd);
                              cps.put(other, fst);
                          }
                      }
                    }
                    // update links from other entities to this entity
                    HashMap<String, HashMap<Integer,Integer>> psCsCount = invertIndexSubject.get(pe);
                    if (psCsCount != null) {
                        for (String pAux : psCsCount.keySet()) {
                            for (Integer aux : psCsCount.get(pAux).keySet()) {
                                    HashMap<Integer, HashMap<String, Integer>> fst = cps.get(ikey);
                                    if (fst == null) {
                                        fst = new HashMap<Integer, HashMap<String, Integer>>();
                                    }                          
                                    HashMap<String, Integer> snd = fst.get(aux);
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
                                    fst.put(aux, snd);
                                    cps.put(ikey, fst);
                            }
                        }
                    }
            }
            updateNO(csPsSs, css);
            write(css, cps, invertIndexObject, invertIndexSubject, base);
            //System.out.println(css);
            //System.out.println(cps);
            //System.out.println(invertIndexSubject);
            //System.out.println(invertIndexObject);
        } catch (IOException e) {
            System.err.println("Problems reading file: "+fileName);
            System.exit(1);
        }
        
    }
}
