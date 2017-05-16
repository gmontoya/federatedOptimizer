import java.io.Serializable;

class Bucket implements Serializable {

    private int min;
    private int max;
    private int count;

    public Bucket(int mn, int mx, int c) {
        this.min = mn;
        this.max = mx;
        this.count = c;
    }

    public Bucket() {
        this.min = Integer.MAX_VALUE;
        this.max = Integer.MIN_VALUE;
        this.count = 0;
    }

    public void increaseCount() {
        this.count++;
    }

    public int getCount() {
        return this.count;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
    public boolean equals(Object o) {
        if ((o == null) || !(o instanceof Bucket)) {
            return false;
        }
        Bucket other = (Bucket) o;
        return (min == other.min) && (max == other.max) && (count == other.count);
    }
    public boolean includes(int v) {
        return this.min <= v && v <= this.max;
    }
    public String toString() {
        return "{ min: "+min+", max: "+max+", count: "+count+"} ";
    }
}
