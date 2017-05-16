import java.io.*;
import java.util.*;

class obtainMIPBasedIndex {
 
    public static void main(String[] args) {
        String fileIIS = args[0];
        String fileIS = args[1];
        String fileIIO = args[2];
        String fileIO = args[3];
        int N = Integer.parseInt(args[4]);
        HashMap<String, Integer> iis = readIIS(fileIIS);
        HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio = readIIO(fileIIO);
        //System.out.println(iis1);
        //System.out.println(iio1);
        HashMap<Integer, MIPVector> is = getSubjectIndex(iis, N);
        HashMap<Integer, HashMap<String,MIPVector>> io = getObjectIndex(iio, N);
        writeIndexText(is, fileIS);
        writeObjectIndexText(io, fileIO);
        //System.out.println(is);
        //System.out.println(io);
    }

    public static void writeObjectIndexText(HashMap<Integer, HashMap<String,MIPVector>> index, String fileName) {

        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileName));
            for (Integer cs : index.keySet()) {
                 HashMap<String,MIPVector> psVs = index.get(cs);
                 osw.write(cs+"\n");
                 for (String p : psVs.keySet()) {
                     MIPVector v = psVs.get(p);
                     v.generateMIP();
                     Vector<Long> mip = v.getMIP();
                     int size = v.getSize();
                     osw.write(p.hashCode()+" "+size+" "+mip+"\n");
                 }
            }
            osw.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            System.exit(1);
        }
    }

    public static void writeIndexText(HashMap<Integer, MIPVector> index, String fileName) {

        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileName));
            for (Integer cs : index.keySet()) {
                     MIPVector v = index.get(cs);
                     v.generateMIP();
                     Vector<Long> mip = v.getMIP();
                     int size = v.getSize();
                     osw.write(cs+" "+size+" "+mip+"\n");
            }
            osw.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            System.exit(1);
        }
    }

    public static void writeObjectIndex(HashMap<Integer, HashMap<String,MIPVector>> index, String fileName) {

        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeObject(index);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            System.exit(1);
        }
    }

    public static void writeIndex(HashMap<Integer, MIPVector> index, String fileName) {

        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeObject(index);
            out.close();
        } catch (IOException e) {
            System.err.println("Problems writing file: "+fileName);
            System.exit(1);
        }
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

    public static HashMap<Integer, MIPVector> getSubjectIndex (HashMap<String, Integer> iis, int N) {
        HashMap<Integer, MIPVector> is = new HashMap<Integer, MIPVector>();
        for (String s : iis.keySet()) {
            Integer cs = iis.get(s);
            MIPVector v = is.get(cs);
            if (v == null) {
                v = new MIPVector(N);
            } 
            v.add(s.hashCode());
            is.put(cs, v);
        }
        for (Integer cs : is.keySet()) {
            MIPVector v = is.get(cs);        
            v.generateMIP();
        }
        return is;
    }

    public static HashMap<Integer, HashMap<String,MIPVector>> getObjectIndex (HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio, int N) {
        HashMap<Integer, HashMap<String, MIPVector>> io = new HashMap<Integer, HashMap<String,MIPVector>>();
        for (String o : iio.keySet()) {
            HashMap<String, HashMap<Integer,Integer>> psCsCount = iio.get(o);
            for (String p : psCsCount.keySet()) {
                HashMap<Integer,Integer> csCount = psCsCount.get(p);
                for (Integer cs : csCount.keySet()) {
                    HashMap<String,MIPVector> pos = io.get(cs);
                    if (pos == null) {
                        pos = new HashMap<String,MIPVector>();
                    } 
                    MIPVector os = pos.get(p);
                    if (os == null) {
                        os = new MIPVector(N);
                    } 
                    os.add(o.hashCode());
                    pos.put(p, os);
                    io.put(cs, pos);
                }
            }
        }
        for (Integer cs : io.keySet()) {
            HashMap<String, MIPVector> psVs = io.get(cs);
            for (String p : psVs.keySet()) {
                MIPVector v = psVs.get(p);        
                v.generateMIP();
            }
        }
        return io;
    }


}
