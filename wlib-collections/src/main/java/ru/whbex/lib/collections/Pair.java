package ru.whbex.lib.collections;

/**
 * A simple immutable Pair object
 * @param <F> First element
 * @param <S> Second element
 */
public final class Pair<F, S> {
    private final F first;
    private final S second;
    public Pair(F first, S second){
        this.first = first;
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }
}
