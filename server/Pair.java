package bgu.spl.net.impl.tftp;

public class Pair<K, V> {

    private final K first;
    private final V second;

    public Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    // public static <K, V> Pair<K, V> of(K first, V second) {
    //     return new Pair<>(first, second);
    // }

    public K getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }
}