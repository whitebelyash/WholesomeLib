package ru.whbex.lib.collections;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

// Source: [IDontKnow] ru.whbex.develop.idontknow.util

/**
 * View of Set as a List object.
 * NOTE: can break something if a real list is expected. Use with caution
 * @param <T> List element type
 */
public class SetListView<T> implements List<T> {
    private final Set<T> set;
    public SetListView(Set<T> set){
        this.set = set;
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return set.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        set.forEach(action);
    }

    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return set.toArray(a);
    }

    @Override
    public boolean add(T t) {
        return set.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return set.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return set.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return set.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return set.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return set.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return set.retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        throw new UnsupportedOperationException("Unsupported operation for " + Set.class.getName());
    }

    @Override
    public void sort(Comparator<? super T> c) {
        throw new UnsupportedOperationException("Unsupported operation for " + Set.class.getName());
    }

    @Override
    public void clear() {
        set.clear();

    }

    @Override
    public T get(int index) {
        throw new UnsupportedOperationException("Unsupported operation for " + Set.class.getName());
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException("Unsupported operation for " + Set.class.getName());
    }

    @Override
    public void add(int index, T element) {
        set.add(element);

    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException("Unsupported operation for " + Set.class.getName());
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException("Unsupported operation for " + Set.class.getName());

    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("Unsupported operation for " + Set.class.getName());
    }

    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException("Unsupported operation for " + Set.class.getName());

    }

    @Override
    public ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException("Unsupported operation for " + Set.class.getName());

    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Unsupported operation for " + Set.class.getName());

    }

    @Override
    public Spliterator<T> spliterator() {
        return set.spliterator();
    }

    @Override
    public Stream<T> stream() {
        return set.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return set.parallelStream();
    }
}
