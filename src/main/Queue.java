public interface Queue<T> {

    /**
     * Add to the end of the Queue
     *
     * @param t
     * @return
     */
    Queue<T> enQueue(T t);

    /**
     * Removes the element at the beginning of the immutable queue,and returns the new queue.
     *
     * @return new Queue<T> without last element
     */
    Queue<T> deQueue();

    /**
     * Give first element of Queue
     *
     * @return
     */
    T head();

    /**
     * Boolean if there is nothing in the Queue
     *
     * @return
     */
    boolean isEmpty();

}
