package com.scrounger.countrycurrencypicker.library;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionUtil {


    /**
     * Simple backport of filter operator using RxJava's {@link Func1}
     * @param items list of items to filter
     * @param filter filter to apply on each individual item
     * @param <E> type of list item
     * @return sub-selection of the list based on filter or original list if empty
     */
    public static <E> List<E> filter(List<E> items, Func1<E, Boolean> filter) {
        if (items.isEmpty()) {
            return items;
        }
        List<E> filtered = new ArrayList<>();
        for (E item : items) {
            if (filter.call(item)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    /**
     * Simple backport of contains operator using RxJava's {@link Func1}
     * @param items collection of items to find match in
     * @param selector selector to apply on each individual item
     * @param <E> type of collection item
     * @return {@code true} if the collection contains item based on selector, {@code false} if no match found
     */
    public static <E> boolean contains(Collection<E> items, Func1<E, Boolean> selector) {
        return first(items, selector) != null;
    }

    public static <E> E first(Collection<E> items, Func1<E, Boolean> selector) {
        if (items.isEmpty()) {
            return null;
        }
        for (E item : items) {
            if (selector.call(item)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Represents a function with one argument.
     * @param <T> the first argument type
     * @param <R> the result type
     */
    public interface Func1<T, R> {
        R call(T t);
    }
}
