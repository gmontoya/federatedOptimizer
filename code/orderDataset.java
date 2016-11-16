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

    public static void main(String[] args) {

        String fileName = args[0];
        Vector<Triple> triples = new Vector<Triple>();

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
                Triple t = new Triple(s,p,o);
                triples.add(t);
                l = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Problems reading file: "+fileName);
            System.exit(1);
        }
        Collections.sort(triples);
        for (Triple t : triples) {
            System.out.println(t.toString());
        }
    }
}
