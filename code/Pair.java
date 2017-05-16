import java.io.Serializable;

public class Pair<E,F> implements Serializable {

    private E first;
    private F second;
    
    public Pair(E f, F s) {
        
        this.first = f;
        this.second = s;
    }

    public void setFirst(E f) {
        this.first = f;
    }
    
    public void setSecond(F s) {
        
        this.second = s;
    }
    
    public E getFirst() {
        return this.first;
    }
    
    public F getSecond() {
        
        return this.second;
    }
    
    public String toString() {
        
        return "("+first.toString()+", "+second.toString()+")";
    }

    public boolean equals(Pair<E,F> other) {

        return this.first.equals(other.first) && this.second.equals(other.second);
    }

    @Override
    public boolean equals(Object o) {

        Class c = this.getClass();
        if (o == null || !(c.isInstance(o))) {
            return false;
        }
        Pair<E,F> other = (Pair<E,F>) o;
        return this.equals(other);
    }

    @Override
    public int hashCode() {

        return 31*this.first.hashCode() + this.second.hashCode();
    }
}
