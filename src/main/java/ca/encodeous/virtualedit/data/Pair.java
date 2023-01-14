package ca.encodeous.virtualedit.data;

public class Pair <K, V>{
    public Pair(K a, V b) {
        this.a = a;
        this.b = b;
    }

    public K getA() {
        return a;
    }

    public void setA(K a) {
        this.a = a;
    }

    public V getB() {
        return b;
    }

    public void setB(V b) {
        this.b = b;
    }

    public K a;
    public V b;
}
