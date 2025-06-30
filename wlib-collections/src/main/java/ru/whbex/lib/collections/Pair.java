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

    public boolean hasValue(Object value){
        return first == value || second == value;
    }
    public boolean pairEquals(Pair<?, ?> other){
        // Needs checking
        boolean firstEquals = (this.first != null && other.first != null) && this.first.equals(other.first);
        boolean secondEquals = (this.second != null && other.second != null) && this.second.equals(other.second);
        return firstEquals == secondEquals;
    }


    // Overrides
    @Override
    public boolean equals(Object other){
        if(this == other) return true;
        if(other == null || other.getClass() != this.getClass()) return false;
        return pairEquals((Pair<?, ?>) other);
    }
    @Override
    public int hashCode(){
        int firstHash = 31 * (first == null ? 0 : first.hashCode());
        int secondHash = 31 * (second == null ? 0 : second.hashCode());
        return firstHash + secondHash;
    }
    @Override
    public String toString(){
        return "Pair{" + first + ", " + second + '}';
    }

}
