/**
 * It was assumed that all T instances will not be cloned between ImmutableQueue.
 *
 * @param <T>
 */
public class ImmutableQueue<T> implements Queue<T> {

    /**
     * This queue always knows the head and tail element.
     * Actually, we could find use only "head" or "tail" and iterate on the Nodes
     * to find the other, but I think it's gonna cost less time to get this reference
     * in here.
     */
    private final Node<T> head;
    private final Node<T> tail;

    /**
     * Only for Empty Queues
     */
    public ImmutableQueue() {
        this(null, null);
    }

    /**
     * Queue with at least 1 Node inside. First and last nodes can be the same.
     *
     * @param head first element to be deQueue and head of the queue
     * @param tail
     */
    private ImmutableQueue(Node<T> head, Node<T> tail) {
        super();
        this.head = head;
        this.tail = tail;
        assert(this.head == null || this.head.left == null);
        assert(this.tail == null || this.tail.right == null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Queue<T> enQueue(T object) {
        if (this.head == null) {
            Node<T> element = new Node<T>(object);
            return new ImmutableQueue<T>(element, element);
        } else {
            Node<T> last = new Node<T>(object);
            Node<T> first = this.head.copyValue();
            if (this.head.right == null) {
                first.next(last);
            } else {
                copyTree(this.head.right, first, last, false);
            }
            return new ImmutableQueue<T>(first, last);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Queue<T> deQueue() {
        if (this.head == null || this.head.right == null) {
            // one or zero item on this queue
            return new ImmutableQueue<T>();
        } else if (this.head.right == this.tail) {
            // there is only two items, we just keeping the last one
            Node<T> unique = this.tail.copyValue();
            return new ImmutableQueue<T>(unique, unique);
        } else {
            // every case with more than two items
            Node<T> newHead = this.head.right.copyValue();
            Node<T> newTail = this.tail.copyValue();
            if (this.head.right == this.tail.left) {
                newHead.next(newTail);
            } else {
                copyTree(this.head.right.right, newHead, newTail, true);
            }
            return new ImmutableQueue<T>(newHead, newTail);
        }
    }

    /**
     * Create a copy of Nodes between two intervals.
     *
     * We want to reconstruct every nodes to avoid collisions
     * between Queues since we are Immutable.
     *
     * TODO : Why is is different between deQueue and enQueue
     *
     * @param oldNetworkEntry : From where we start to copy
     * @param newHead : newHead element of the new network
     * @param newTail : newTail element of the new network
     * @param deQueue : is it deQueue (important for proper exit)
     */
    private void copyTree(Node<T> oldNetworkEntry, Node<T> newHead, Node<T> newTail, boolean deQueue) {
        Node<T> currentNode = newHead;
        Node<T> successor = oldNetworkEntry;
        boolean done = false;
        while(!done) {
            Node<T> copySuccessor = successor.copyValue();
            currentNode.next(copySuccessor);

            done = deQueue ? (successor == this.tail.left) : (successor.right == null);
            if(done) {
                copySuccessor.next(newTail);
            } else {
                currentNode = copySuccessor;
                successor = successor.right;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");

        Node<T> node = this.head;
        while (node != null) {
            if (node != this.head) {
                sb.append(",");
            }
            sb.append(node.value);
            node = node.right;
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     *
     * We could send {@link java.util.NoSuchElementException} but nothing mandatory since {@link Queue} don't say so.
     *
     * @return instance of T or null if no element was found.
     */
    @Override
    public T head() {
        return this.head == null ? null : this.head.value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.head == null;
    }

    /**
     * Inner class because nobody needs to use this except {@link ImmutableQueue}
     *
     * @param <T>
     */
    static class Node<T> {

        final T value;
        Node<T> left;
        Node<T> right;

        Node(T value) {
            this.value = value;
        }

        /**
         * Return only value inside a new Node
         *
         * @return
         */
        Node<T> copyValue() {
            return new Node<T>(this.value);
        }

        void next(Node<T> successor) {
            this.right = successor;
            successor.left = this;
        }
    }
}
