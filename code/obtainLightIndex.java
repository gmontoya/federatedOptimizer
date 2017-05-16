import java.io.*;
import java.util.*;

class obtainLightIndex {

    public static void main(String[] args) {
        String fileIIS1 = args[0];
        String fileIIS2 = args[1];
        String fileIIO1 = args[2];
        String fileIIO2 = args[3];
        HashMap<String, Integer> iis1 = readIIS(fileIIS1);
        HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio1 = readIIO(fileIIO1);
        //System.out.println(iis1);
        //System.out.println(iio1);
        HashMap<Integer, TreeSet<String>> is = getSubjectIndex(iis1);
        HashMap<Integer, HashMap<String,TreeSet<String>>> io = getObjectIndex(iio1);
        writeIndex(is, fileIIS2);
        writeObjectIndex(io, fileIIO2);
        //System.out.println(is);
        //System.out.println(io);
    }

    public static void writeObjectIndex(HashMap<Integer, HashMap<String,TreeSet<String>>> index, String fileName) {

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

    public static void writeIndex(HashMap<Integer, TreeSet<String>> index, String fileName) {

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

    public static HashMap<Integer, TreeSet<String>> getSubjectIndex (HashMap<String, Integer> iis) {
        HashMap<Integer, TreeSet<String>> is = new HashMap<Integer, TreeSet<String>>();
        for (String s : iis.keySet()) {
            Integer cs = iis.get(s);
            TreeSet<String> ss = is.get(cs);
            if (ss == null) {
                ss = new TreeSet<String>();
            } 
            ss.add(s);
            is.put(cs, ss);
        }
        return is;
    }

    public static HashMap<Integer, HashMap<String,TreeSet<String>>> getObjectIndex (HashMap<String, HashMap<String, HashMap<Integer,Integer>>> iio) {
        HashMap<Integer, HashMap<String, TreeSet<String>>> io = new HashMap<Integer, HashMap<String,TreeSet<String>>>();
        for (String o : iio.keySet()) {
            HashMap<String, HashMap<Integer,Integer>> psCsCount = iio.get(o);
            for (String p : psCsCount.keySet()) {
                HashMap<Integer,Integer> csCount = psCsCount.get(p);
                for (Integer cs : csCount.keySet()) {
                    HashMap<String,TreeSet<String>> pos = io.get(cs);
                    if (pos == null) {
                        pos = new HashMap<String,TreeSet<String>>();
                    } 
                    TreeSet<String> os = pos.get(p);
                    if (os == null) {
                        os = new TreeSet<String>();
                    } 
                    os.add(o);
                    pos.put(p, os);
                    io.put(cs, pos);
                }
            }
        }
        return io;
    }


}
