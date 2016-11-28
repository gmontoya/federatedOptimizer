import java.io.*;
import java.util.*;

class orderDataset {

    static class Triple implements Comparable<Triple> {
        protected String subject, predicate, object;
        public Triple(String s, String p, String o) {
            subject = s;
            predicate = p;
            object = o;
        }

        public int compareTo(Triple o) {
            if (subject.compareTo(o.subject) < 0 || ((subject.compareTo(o.subject) == 0) && predicate.compareTo(o.predicate) < 0)) {
                return -1;
            }
            if ((subject.compareTo(o.subject) == 0) && (predicate.compareTo(o.predicate) == 0)) {
                return 0;
            }
            return 1;
        }
        public String toString() {
            return subject+" "+predicate+" "+object;
        }
    }
    public static boolean subjOrd;
    public static void main(String[] args) {

        String fileName = args[0];
        int N = Integer.parseInt(args[1]);
        subjOrd = Boolean.parseBoolean(args[2]);
        String[][] triples = new String[N][3];
        int k = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String l = br.readLine();
            while (l!=null) {
                int i = l.indexOf(" ");
                String s = l.substring(0, i);
                String r = l.substring(i+1);
                i = r.indexOf(" ");
                String p = r.substring(0, i);
                String o = r.substring(i+1);
                triples[k][0] = s;
                triples[k][1] = p;
                triples[k][2] = o;
                //Triple t = new Triple(s,p,o);
                //triples.add(t);
                l = br.readLine();
                k++;
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Problems reading file: "+fileName);
            System.exit(1);
        }
        mergeSort(triples, 0, N);
        //Collections.sort(triples);
        //for (Triple t : triples) {
        //    System.out.println(t.toString());
        //}
        String s = null;
        String p = null;
        String o = null;
        for (int i = 0; i < N; i++) {
            if ((s == null && p == null && o == null) || !s.equals(triples[i][0]) || !p.equals(triples[i][1]) || !o.equals(triples[i][2])) {
                System.out.println(triples[i][0]+" "+triples[i][1]+" "+triples[i][2]);
            }
            s = triples[i][0];
            p = triples[i][1];
            o = triples[i][2];
        }
    }

    public static void mergeSort(String[][] array, int l, int r) {

        if (l+1 < r) {

            int m = (l + r) / 2;
            mergeSort(array, l, m);
            mergeSort(array, m, r);
            merge(array, l, m, r);
        }
    }

    private static void merge(String[][] array, int l, int m, int r) {

        String[][] tmpArray = new String[r-l][3];
        int i1 = l;
        int i2 = m;
        int i3 = 0;
        while (i1 < m && i2 < r) {
            boolean c = true;
            if (subjOrd) {
                c = ((array[i1][0].compareTo(array[i2][0]) < 0) || ((array[i1][0].compareTo(array[i2][0]) == 0) && (array[i1][1].compareTo(array[i2][1]) < 0)) || ((array[i1][0].compareTo(array[i2][0]) == 0) && (array[i1][1].compareTo(array[i2][1]) == 0) && (array[i1][2].compareTo(array[i2][2]) <= 0)));
            } else {
                c = ((array[i1][2].compareTo(array[i2][2]) < 0) || ((array[i1][2].compareTo(array[i2][2]) == 0) && (array[i1][1].compareTo(array[i2][1]) < 0)) || ((array[i1][2].compareTo(array[i2][2]) == 0) && (array[i1][1].compareTo(array[i2][1]) == 0) && (array[i1][0].compareTo(array[i2][0]) <= 0)));
            }
            if (c) {
                tmpArray[i3][0] = array[i1][0];
                tmpArray[i3][1] = array[i1][1];
                tmpArray[i3][2] = array[i1][2];
                i1++;
                i3++;
            } else {
                tmpArray[i3][0] = array[i2][0];
                tmpArray[i3][1] = array[i2][1];
                tmpArray[i3][2] = array[i2][2];
                i2++;
                i3++;
            }
        }
        while (i1 < m) {
            tmpArray[i3][0] = array[i1][0];
            tmpArray[i3][1] = array[i1][1];
            tmpArray[i3][2] = array[i1][2];
            i1++;
            i3++;
        }
        while (i2 < r) {
            tmpArray[i3][0] = array[i2][0];
            tmpArray[i3][1] = array[i2][1];
            tmpArray[i3][2] = array[i2][2];
            i2++;
            i3++;
        }
        for (int i = 0; i < tmpArray.length; i++) {
            array[l+i][0] = tmpArray[i][0];
            array[l+i][1] = tmpArray[i][1];
            array[l+i][2] = tmpArray[i][2];
        }
    }
}
