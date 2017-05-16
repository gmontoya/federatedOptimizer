import java.io.*;
import java.util.*;

class mergeDataset {

    public static void main(String[] args) {

        String fileName1 = args[0];
        String fileName2 = args[1];
        Boolean subjOrd = Boolean.parseBoolean(args[2]);
        try {
            BufferedReader br1 = new BufferedReader(new FileReader(fileName1));
            BufferedReader br2 = new BufferedReader(new FileReader(fileName2));
            String l1 = br1.readLine();
            String l2 = br2.readLine();
            while (l1!=null && l2 != null) {
                int i = l1.indexOf(" ");
                String s1 = l1.substring(0, i);
                String r = l1.substring(i+1);
                i = r.indexOf(" ");
                String p1 = r.substring(0, i);
                String o1 = r.substring(i+1);
                i = l2.indexOf(" ");
                String s2 = l2.substring(0, i);
                r = l2.substring(i+1);
                i = r.indexOf(" ");
                String p2 = r.substring(0, i);
                String o2 = r.substring(i+1);
                boolean c = true;
                if (subjOrd) {
                    c = ((s1.compareTo(s2) <0) || (s1.compareTo(s2)==0 && p1.compareTo(p2) < 0) || (s1.compareTo(s2)==0 && p1.compareTo(p2) == 0 && o1.compareTo(o2) <= 0));
                } else {
                    c = ((o1.compareTo(o2) <0) || (o1.compareTo(o2)==0 && p1.compareTo(p2) < 0) || (o1.compareTo(o2)==0 && p1.compareTo(p2) == 0 && s1.compareTo(s2) <= 0));
                }
                if (c) {
                    System.out.println(s1+" "+p1+" "+o1);
                    l1 = br1.readLine();
                } else {
                    System.out.println(s2+" "+p2+" "+o2);
                    l2 = br2.readLine();
                }
            }
            while (l1 != null) {
                int i = l1.indexOf(" ");
                String s1 = l1.substring(0, i);
                String r = l1.substring(i+1);
                i = r.indexOf(" ");
                String p1 = r.substring(0, i);
                String o1 = r.substring(i+1);
                System.out.println(s1+" "+p1+" "+o1);
                l1 = br1.readLine();
            }
            while (l2 != null) {
                int i = l2.indexOf(" ");
                String s2 = l2.substring(0, i);
                String r = l2.substring(i+1);
                i = r.indexOf(" ");
                String p2 = r.substring(0, i);
                String o2 = r.substring(i+1);
                System.out.println(s2+" "+p2+" "+o2);
                l2 = br2.readLine();
            }
            br1.close();
            br2.close();
        } catch (IOException e) {
            System.err.println("Problems reading file: "+fileName1+" or "+fileName2);
            System.exit(1);
        }
    }
}
