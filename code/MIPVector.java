import java.util.*;

class MIPVector implements java.io.Serializable {

    public static int U = 991205981;
    public static int seedA = 12345678;
    public static int seedB = 98765432;
    private Vector<Integer> set = new Vector<Integer>();
    private Vector<Long> mip = null;
    private int N;
    private int size;

    public MIPVector() {
        this.N = 0;
        this.size = 0;
    }

    public MIPVector(int n) {
        this.N = n;
        this.size = 0;
    }

    public void add(int i) {
        this.set.add(i);
        this.size++;
    }

    public void generateMIP() {

        if (mip == null) {
        Random ra = new Random(this.seedA);
        Random rb = new Random(this.seedB);
        mip = new Vector<Long>();
        for (int j = 0; j < Math.min(this.N, this.set.size()); j++) {
            int ai = ra.nextInt();
            int bi = rb.nextInt();
            long mini = Long.MAX_VALUE;

            for (int k = 0; k < this.set.size(); k++) {
                long hi = (ai * set.get(k) + bi) % U;
                if (hi < mini) {
                    mini = hi;
                }
            }
            this.mip.add(mini);
        }
        this.set.clear(); 
        }
    }
    public Vector<Long> getMIP() {
        return this.mip;
    }

    public void clearMIP() {
        this.mip = new Vector<Long>();
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int s) {
        this.size = s;
    }

    public void setN(int n) {
        this.N = n;
    }

    public double getResemblance(MIPVector other) {
        double r = 0;
        int min = Math.min(this.N, other.N);
        for (int i = 0; i < min; i++) {
            if (this.mip.get(i).equals(other.mip.get(i))) {
                r++;
            }
        }
        if (min > 0) {
            r = r / min;
        }
        return r;
    }

    public long getOverlap(MIPVector other) {
        double r = getResemblance(other);
        long o = Math.round(Math.ceil((r*(this.size+other.size))/(r+1)));
        return o;
    }

    public String toString() {

        return "{ s: "+size+", n: "+N+", v: "+mip+" }";
    }
}
