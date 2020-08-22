/*
 * Copyright (c) 2003, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * Copyright (c) 2020 PhasmidSoftware.
 *
 */
package com.phasmidsoftware.util;

import java.util.*;
import java.util.function.Consumer;

/**
 * An unbounded priority {@linkplain Queue queue} based on a priority heap.
 * The elements of the priority queue are ordered according to their
 * {@linkplain Comparable natural ordering}, or by a {@link Comparator}
 * provided at queue construction time, depending on which constructor is
 * used.  A priority queue does not permit {@code null} elements.
 * A priority queue relying on natural ordering also does not permit
 * insertion of non-comparable objects (doing so may result in
 * {@code ClassCastException}).
 *
 * <p>The <em>head</em> of this queue is the <em>least</em> element
 * with respect to the specified ordering.  If multiple elements are
 * tied for least value, the head is one of those elements -- ties are
 * broken arbitrarily.  The queue retrieval operations {@code poll},
 * {@code remove}, {@code peek}, and {@code element} access the
 * element at the head of the queue.
 *
 * <p>A priority queue is unbounded, but has an internal
 * <i>capacity</i> governing the size of an array used to store the
 * elements on the queue.  It is always at least as large as the queue
 * size.  As elements are added to a priority queue, its capacity
 * grows automatically.  The details of the growth policy are not
 * specified.
 *
 * <p>This class and its iterator implement all of the
 * <em>optional</em> methods of the {@link Collection} and {@link
 * Iterator} interfaces.  The Iterator provided in method {@link
 * #iterator()} is <em>not</em> guaranteed to traverse the elements of
 * the priority queue in any particular order. If you need ordered
 * traversal, consider using {@code Arrays.sort(pq.toArray())}.
 *
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * Multiple threads should not access a {@code PriorityQueueJava}
 * instance concurrently if any of the threads modifies the queue.
 * Instead, use the thread-safe {@link
 * java.util.concurrent.PriorityBlockingQueue} class.
 *
 * <p>Implementation note: this implementation provides
 * O(log(n)) time for the enqueuing and dequeuing methods
 * ({@code offer}, {@code poll}, {@code remove()} and {@code add});
 * linear time for the {@code remove(Object)} and {@code contains(Object)}
 * methods; and constant time for the retrieval methods
 * ({@code peek}, {@code element}, and {@code size}).
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @param <E> the type of elements held in this collection
 * @author Josh Bloch, Doug Lea
 * @since 1.5
 */
public class PriorityQueueJava<E> extends AbstractQueue<E>
        implements java.io.Serializable {

    @Override
    public String toString() {
        Object[] objects = new Object[size];
        System.arraycopy(heap, 0, objects, 0, size);
        return "PriorityQueueJava{" +
                "size=" + size +
                ", queue=" + Arrays.toString(objects) +
                '}';
    }

    /**
     * Immutably, inserts the specified element into this priority queue and returns the new priority queue.
     *
     * @return a new PriorityQueueJava
     * @throws ClassCastException   if the specified element cannot be
     *                              compared with elements currently in this priority queue
     *                              according to the priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    public PriorityQueueJava<E> insert(E e) {
        if (e == null)
            throw new NullPointerException();
        System.out.println("insert: this: " + this);
        PriorityQueueJava<E> result = (size >= this.heap.length) ? grow(heap.length * 2) : new PriorityQueueJava<>(this);
        result.doInsert(e);
        System.out.println("insert: result: " + result);
        return result;
    }

    /**
     * Immutably, removes and returns the smallest element from this priority queue.
     *
     * @return a tuple of the new PriorityQueueJava (without its smallest element) and the smallest element.
     */
    public DeleteResult<E> del() {
        System.out.println("del: this: " + this);
        if (size == 0)
            return new DeleteResult<>(this, null);
        PriorityQueueJava<E> result = new PriorityQueueJava<>(this);
        System.out.println("del: result (1): " + result);
        E e = result.doDel();
        System.out.println("del: result (2): " + result);
        return new DeleteResult<>(result, e);
    }

    /**
     * Inserts the specified element into this priority queue.
     *
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws UnsupportedOperationException always
     */
    public boolean add(E e) {
        return offer(e);
    }

    /**
     * Inserts the specified element into this priority queue.
     *
     * @return {@code true} (as specified by {@link Queue#offer})
     * @throws UnsupportedOperationException always
     */
    public boolean offer(E e) {
        throw new UnsupportedOperationException("offer");
    }

    /**
     * Method to peek at the smallest element, without affecting the priority queue.
     *
     * @return the smallest element.
     */
    @SuppressWarnings("unchecked")
    public E peek() {
        return (size == 0) ? null : (E) heap[0];
    }

    /**
     * Removes a single instance of the specified element from this queue,
     * if it is present.  More formally, removes an element {@code e} such
     * that {@code o.equals(e)}, if this queue contains one or more such
     * elements.  Returns {@code true} if and only if this queue contained
     * the specified element (or equivalently, if this queue changed as a
     * result of the call).
     *
     * @param o element to be removed from this queue, if present
     * @return {@code true} if this queue changed as a result of the call
     */
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("remove");
    }

    /**
     * Returns {@code true} if this queue contains the specified element.
     * More formally, returns {@code true} if and only if this queue contains
     * at least one element {@code e} such that {@code o.equals(e)}.
     *
     * @param o object to be checked for containment in this queue
     * @return {@code true} if this queue contains the specified element
     */
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    /**
     * @return the number of elements in the heap.
     */
    public int size() {
        return size;
    }

    /**
     * Removes all of the elements from this priority queue.
     * The queue will be empty after this call returns.
     */
    public void clear() {
        modCount++;
        for (int i = 0; i < size; i++)
            heap[i] = null;
        size = 0;
    }

    /**
     * Unsupported method.
     *
     * @return nothing.
     * @throws UnsupportedOperationException always.
     */
    public E poll() {
        throw new UnsupportedOperationException("poll");
    }

    /**
     * Returns an array containing all of the elements in this queue.
     * The elements are in no particular order.
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this queue.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this queue
     */
    public Object[] toArray() {
        return Arrays.copyOf(heap, size);
    }

    /**
     * Returns an array containing all of the elements in this queue; the
     * runtime type of the returned array is that of the specified array.
     * The returned array elements are in no particular order.
     * If the queue fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this queue.
     *
     * <p>If the queue fits in the specified array with room to spare
     * (i.e., the array has more elements than the queue), the element in
     * the array immediately following the end of the collection is set to
     * {@code null}.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose {@code x} is a queue known to contain only strings.
     * The following code can be used to dump the queue into a newly
     * allocated array of {@code String}:
     *
     * <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     * <p>
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     *
     * @param a the array into which the elements of the queue are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing all of the elements in this queue
     * @throws ArrayStoreException  if the runtime type of the specified array
     *                              is not a supertype of the runtime type of every element in
     *                              this queue
     * @throws NullPointerException if the specified array is null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        final int size = this.size;
        if (a.length < size)
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(heap, size, a.getClass());
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(heap, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    /**
     * Returns an iterator over the elements in this queue, in their increasing order.
     *
     * @return an iterator over the elements.
     */
    public Iterator<E> iterator() {
        return new PriorityQueueIterator<>(new PriorityQueueJava<>(this));
    }

    /**
     * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
     * and <em>fail-fast</em> {@link Spliterator} over the elements in this
     * queue.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, and {@link Spliterator#NONNULL}.
     * Overriding implementations should document the reporting of additional
     * characteristic values.
     *
     * @return a {@code Spliterator} over the elements in this queue
     * @since 1.8
     */
    public final Spliterator<E> spliterator() {
        return new PriorityQueueJava.PriorityQueueSpliterator<>(this, 0, -1, 0);
    }

    /**
     * Creates a {@code PriorityQueueJava} with the default initial
     * capacity (11) that orders its elements according to their
     * {@linkplain Comparable natural ordering}.
     *
     * @param heap       binary heap.
     * @param size       the number of elements in the binary heap.
     * @param comparator the comparator: if null, then E will be cast to Comparable.
     */
    public PriorityQueueJava(Object[] heap, Comparator<? super E> comparator, int size) {
        this.heap = checkNoNulls(heap, size);
        this.comparator = comparator;
        this.size = size;
    }

    /**
     * Creates a {@code PriorityQueueJava} with the specified initial capacity
     * that orders its elements according to the specified comparator.
     *
     * @param initialCapacity the initial capacity for this priority queue
     * @param comparator      the comparator that will be used to order this
     *                        priority queue.  If {@code null}, the {@linkplain Comparable
     *                        natural ordering} of the elements will be used.
     * @throws IllegalArgumentException if {@code initialCapacity} is
     *                                  less than 1
     */
    public PriorityQueueJava(int initialCapacity,
                             Comparator<? super E> comparator) {
        this(new Object[initialCapacity], comparator, 0);
    }

    /**
     * Creates a {@code PriorityQueueJava} with the specified initial capacity
     * that orders its elements according to the specified comparator.
     *
     * @param initialCapacity the initial capacity for this priority queue
     * @throws IllegalArgumentException if {@code initialCapacity} is
     *                                  less than 1
     */
    public PriorityQueueJava(int initialCapacity) {
        this(initialCapacity, null);
    }

    /**
     * Creates a {@code PriorityQueueJava} with the specified initial capacity
     * that orders its elements according to the specified comparator.
     *
     * @throws IllegalArgumentException if {@code initialCapacity} is
     *                                  less than 1
     */
    public PriorityQueueJava() {
        this(32);
    }

    /**
     * Creates a {@code PriorityQueueJava} with the specified initial capacity
     * that orders its elements according to the specified comparator.
     *
     * @throws IllegalArgumentException if {@code initialCapacity} is
     *                                  less than 1
     */
    public PriorityQueueJava(E e, Comparator<? super E> comparator) {
        this(initialize(e, 32), comparator, 1);
    }

    /**
     * Creates a {@code PriorityQueueJava} with the specified initial capacity
     * that orders its elements according to the specified comparator.
     *
     * @throws IllegalArgumentException if {@code initialCapacity} is
     *                                  less than 1
     */
    public PriorityQueueJava(Comparator<? super E> comparator) {
        this(0, comparator);
    }

    /**
     * Creates a {@code PriorityQueueJava} with the specified initial capacity
     * that orders its elements according to the specified comparator.
     *
     * @throws IllegalArgumentException if {@code initialCapacity} is
     *                                  less than 1
     */
    public PriorityQueueJava(E e) {
        this(e, null);
    }

    private PriorityQueueJava(PriorityQueueJava<E> other) {
        this(other.heap, other.comparator, other.size);
    }

    public static class DeleteResult<E> {
        public DeleteResult(PriorityQueueJava<E> pq, E value) {
            this.pq = pq;
            this.value = value;
        }

        public PriorityQueueJava<E> getPq() {
            return pq;
        }

        public E getValue() {
            return value;
        }

        private final PriorityQueueJava<E> pq;
        private final E value;
    }

    private static final long serialVersionUID = -7720805057305804111L;

    private static final int DEFAULT_INITIAL_CAPACITY = 11;

    /**
     * Priority queue represented as a balanced binary heap: the two
     * children of queue[n] are queue[2*n+1] and queue[2*(n+1)].  The
     * priority queue is ordered by comparator, or by the elements'
     * natural ordering, if comparator is null: For each node n in the
     * heap and each descendant d of n, n <= d.  The element with the
     * lowest value is in queue[0], assuming the queue is nonempty.
     */
    final Object[] heap; // non-private to simplify nested class access

    /**
     * The number of elements in the priority queue.
     */
    private int size;

    /**
     * The comparator, or null if priority queue uses elements'
     * natural ordering.
     */
    private final Comparator<? super E> comparator;

    /**
     * The number of times this priority queue has been
     * <i>structurally modified</i>.  See AbstractList for gory details.
     */
    transient int modCount = 0; // non-private to simplify nested class access

    private static <E> Object[] initialize(E e, int initialCapacity) {
        if (initialCapacity < 1) throw new IllegalArgumentException("PriorityQueueJava capacity must be at least 1");
        Object[] objects = new Object[initialCapacity];
        objects[0] = e;
        return objects;
    }

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * Increases the capacity of the array.
     *
     * @param minCapacity the desired minimum capacity
     */
    private PriorityQueueJava<E> grow(int minCapacity) {
        int oldCapacity = heap.length;
        // Double size if small; else grow by 50%
        int newCapacity = oldCapacity + ((oldCapacity < 64) ?
                (oldCapacity + 2) :
                (oldCapacity >> 1));
        // overflow-conscious code
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        return new PriorityQueueJava<>(Arrays.copyOf(heap, newCapacity), comparator, size);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    private void doInsert(E e) {
        modCount++;
        int i = size;
        size = i + 1;
        if (i == 0)
            this.heap[0] = e;
        else
            siftUp(i, e);
        checkNoNulls(heap, size);
    }

    private int indexOf(Object o) {
        if (o != null) {
            for (int i = 0; i < size; i++)
                if (o.equals(heap[i]))
                    return i;
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    private E doDel() {
        modCount++;
        E result = (E) heap[0];
        E x = (E) heap[size - 1];
        if (x == null)
            throw new RuntimeException("logic error");
        heap[--size] = null;
        if (size > 0)
            siftDown(0, x);
        checkNoNulls(heap, size);
        return result;
    }

    private static Object[] checkNoNulls(final Object[] heap, final int size) {
        for (int i = 0; i < size; i++)
            if (heap[i] == null)
                throw new RuntimeException("Heap has null at index " + i);
        if (heap.length == size)
            return heap;
        if (heap.length > size && heap[size] == null)
            return heap;
        else
            throw new RuntimeException("Heap has not-null at index " + size);
    }

    /**
     * Inserts item x at position k, maintaining heap invariant by
     * promoting x up the tree until it is greater than or equal to
     * its parent, or is the root.
     * <p>
     * To simplify and speed up coercions and comparisons. the
     * Comparable and Comparator versions are separated into different
     * methods that are otherwise identical. (Similarly for siftDown.)
     *
     * @param k the position to fill
     * @param x the item to insert
     */
    private void siftUp(int k, E x) {
        if (comparator != null)
            siftUpUsingComparator(k, x);
        else
            siftUpComparable(k, x);
    }

    @SuppressWarnings("unchecked")
    private void siftUpComparable(int k, E x) {
        Comparable<? super E> key = (Comparable<? super E>) x;
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = heap[parent];
            if (key.compareTo((E) e) >= 0)
                break;
            heap[k] = e;
            k = parent;
        }
        heap[k] = key;
    }

    @SuppressWarnings("unchecked")
    private void siftUpUsingComparator(int k, E x) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = heap[parent];
            if (comparator.compare(x, (E) e) >= 0)
                break;
            heap[k] = e;
            k = parent;
        }
        heap[k] = x;
    }

    /**
     * Inserts item x at position k, maintaining heap invariant by
     * demoting x down the tree repeatedly until it is less than or
     * equal to its children or is a leaf.
     *
     * @param k the position to fill
     * @param x the item to insert
     */
    private void siftDown(int k, E x) {
        if (comparator != null)
            siftDownUsingComparator(k, x);
        else
            siftDownComparable(k, x);
    }

    @SuppressWarnings("unchecked")
    private void siftDownComparable(int k, E x) {
        Comparable<? super E> key = (Comparable<? super E>) x;
        int half = size >>> 1;        // loop while a non-leaf
        while (k < half) {
            int child = (k << 1) + 1; // assume left child is least
            Object c = heap[child];
            int right = child + 1;
            if (right < size &&
                    ((Comparable<? super E>) c).compareTo((E) heap[right]) > 0)
                c = heap[child = right];
            if (key.compareTo((E) c) <= 0)
                break;
            heap[k] = c;
            k = child;
        }
        heap[k] = key;
    }

    @SuppressWarnings("unchecked")
    private void siftDownUsingComparator(int k, E x) {
        int half = size >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            Object c = heap[child];
            int right = child + 1;
            if (right < size &&
                    comparator.compare((E) c, (E) heap[right]) > 0)
                c = heap[child = right];
            if (comparator.compare(x, (E) c) <= 0)
                break;
            heap[k] = c;
            k = child;
        }
        heap[k] = x;
    }

    private static final class PriorityQueueIterator<X> implements Iterator<X> {
        public PriorityQueueIterator(PriorityQueueJava<X> pq) {
            this.pq = pq;
        }

        private final PriorityQueueJava<X> pq;

        private final int cursor = 0;


        public boolean hasNext() {
            return cursor < pq.size;
        }

        public X next() {
            if (cursor < pq.size)
                return pq.doDel();
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }

    static final class PriorityQueueSpliterator<E> implements Spliterator<E> {
        /*
         * This is very similar to ArrayList Spliterator, except for
         * extra null checks.
         */
        private final PriorityQueueJava<E> pq;
        private int index;            // current index, modified on advance/split
        private int fence;            // -1 until first use
        private int expectedModCount; // initialized when fence set

        /**
         * Creates new spliterator covering the given range
         */
        PriorityQueueSpliterator(PriorityQueueJava<E> pq, int origin, int fence,
                                 int expectedModCount) {
            this.pq = pq;
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() { // initialize fence to size on first use
            int hi;
            if ((hi = fence) < 0) {
                expectedModCount = pq.modCount;
                hi = fence = pq.size;
            }
            return hi;
        }

        public PriorityQueueJava.PriorityQueueSpliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                    new PriorityQueueJava.PriorityQueueSpliterator<>(pq, lo, index = mid,
                            expectedModCount);
        }

        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // hoist accesses and checks from loop
            PriorityQueueJava<E> q;
            Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((q = pq) != null && (a = q.heap) != null) {
                if ((hi = fence) < 0) {
                    mc = q.modCount;
                    hi = q.size;
                } else
                    mc = expectedModCount;
                if ((i = index) >= 0 && (index = hi) <= a.length) {
                    for (E e; ; ++i) {
                        if (i < hi) {
                            if ((e = (E) a[i]) == null) // must be CME
                                break;
                            action.accept(e);
                        } else if (q.modCount != mc)
                            break;
                        else
                            return;
                    }
                }
            }
            throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            int hi = getFence(), lo = index;
            if (lo >= 0 && lo < hi) {
                index = lo + 1;
                @SuppressWarnings("unchecked") E e = (E) pq.heap[lo];
                if (e == null)
                    throw new ConcurrentModificationException();
                action.accept(e);
                if (pq.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public long estimateSize() {
            return getFence() - index;
        }

        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.NONNULL;
        }
    }
}
