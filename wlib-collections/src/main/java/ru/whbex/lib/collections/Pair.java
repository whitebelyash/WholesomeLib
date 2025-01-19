package ru.whbex.lib.collections;

/**
 * A simple immutable Pair object
 * @param <F> First element
 * @param <S> Second element
 */
public class Pair<F, S> {
    public final F first;
    public final S second;
    public Pair(F first, S second){
        this.first = first;
        this.second = second;
    }
}
