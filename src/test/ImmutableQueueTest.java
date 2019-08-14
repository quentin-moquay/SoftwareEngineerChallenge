import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.util.LinkedList;

class ImmutableQueueTest {

    private Runtime runtime = Runtime.getRuntime();
    private static int MB = 1024 * 1024;
    private static int MAX = 5_000; // need to be configured according to the computer

    /**
     * Simply to verify that the Queue is working properly
     */
    @Test
    void simpleTest() {

        // Init is OK
        Queue<String> step = new ImmutableQueue<>();
        assertNull(step.head());
        assertEquals("[]", step.toString());
        assertTrue(step.isEmpty());

        // adding one is OK
        Queue<String> step1 = step.enQueue("first");
        assertEquals("first", step1.head());
        assertFalse(step1.isEmpty());

        // adding second is OK, head is unchanged
        Queue<String> step2 = step1.enQueue("second");
        assertEquals("first", step2.head());
        assertEquals("[first,second]", step2.toString());

        // we can dequeue, step1 is unchanged, step3 is empty
        Queue<String> step3 = step1.deQueue();
        assertEquals("first", step1.head());
        assertNull(step3.head());
        assertTrue(step3.isEmpty());
        assertEquals("[]", step3.toString());

        // we can add 3 or 4 items, every Queue is different
        Queue<String> step4 = step2.enQueue("three");
        Queue<String> step5 = step4.enQueue("four");
        assertEquals("[first,second,three]", step4.toString());
        assertEquals("[first,second,three,four]", step5.toString());

        // after dequeue of step4, everything is unchanged
        // step6 is step4 without first element, head changed
        Queue<String> step6 = step4.deQueue();
        assertEquals("[first,second,three]", step4.toString());
        assertEquals("[first,second,three,four]", step5.toString());
        assertEquals("[second,three]", step6.toString());
        assertEquals("second", step6.head());

        // if we dequeue the same Queue, new Queues are identicals
        // isEmpty is working properly
        Queue<String> step7 = step5.deQueue();
        Queue<String> step8 = step5.deQueue();
        assertEquals("[second,three,four]", step7.toString());
        assertEquals("[second,three,four]", step8.toString());
        assertFalse(step8.isEmpty());

        // we can dequeue 3 elements without errors
        Queue<String> step9 = step7
                .deQueue()
                .deQueue()
                .deQueue();
        assertEquals("[]", step9.toString());
        assertNull(step9.head());
        assertTrue(step9.isEmpty());

        // we can reuse the empty Queue to put elements
        Queue<String> step10 = step9.enQueue("one").enQueue("two").enQueue("three");
        assertEquals("[one,two,three]", step10.toString());
        assertEquals("one", step10.head());

        // we can reuse step7 and adding elements too
        Queue<String> step11 = step7.enQueue("one").enQueue("two").enQueue("three");
        assertEquals("[second,three,four,one,two,three]", step11.toString());
        assertEquals("second", step11.head());

    }

    /**
     * Let's try if we have a lot of Queues in memory at the same time
     * This one can cause lots of heapspace.
     * JUnit asserts costs a lot too.
     */
    @Test
    void memoryTest() {
        System.out.println("Keep Var - memory at start : " + this.memoryUsed());
        LinkedList<Queue<Integer>> queuesJam = new LinkedList<>();
        Queue<Integer> queue = new ImmutableQueue<>();
        long maxMemoryUsed = 0L;
        for (Integer i = 0; i < MAX; i++) {
            Queue<Integer> qPrevious = (i > 0) ? queuesJam.getLast() : new ImmutableQueue<>();
            queuesJam.add(qPrevious.enQueue(i));
            if (i % 1_000 == 0) {
                long memory = this.memoryUsed();
                if (maxMemoryUsed < memory) {
                    maxMemoryUsed = memory;
                }
            }
            if (i > 0) {
                Queue<Integer> qCurrent = queuesJam.getLast();
                assertNotEquals(qPrevious, qCurrent);
                assertEquals(Integer.valueOf(0), qPrevious.head());
                assertEquals(Integer.valueOf(0), qCurrent.head());
                assertEquals(generateArray(i - 1), qPrevious.toString());
                assertEquals(generateArray(i), qCurrent.toString());
            }
        }
        for (int i = MAX-1; i < 0; i--) {
            Queue<Integer> qPrevious = queuesJam.get(i);
            queuesJam.add(queue.deQueue());
            Queue<Integer> qCurrent = queuesJam.getLast();
            assertNotEquals(qPrevious, qCurrent);
            assertEquals(Integer.valueOf(0), qPrevious.head());
            assertEquals(Integer.valueOf(1), qCurrent.head());
        }
        long memory = this.memoryUsed();
        System.out.println("Keep Var - max memory consumed : " + maxMemoryUsed);
        System.out.println("Keep Var - memory at the end : " + memory);
    }

    private long memoryUsed() {
        return (runtime.totalMemory() - runtime.freeMemory()) / MB;
    }

    // faster test exec
    private StringBuffer sb = new StringBuffer();
    private int lastIGenerated = -1;

    private String generateArray(int size) {

        for (int i = lastIGenerated; i <= size; i++) {
            if (i > lastIGenerated) {
                sb.append(i);
                sb.append(",");
            }
        }
        lastIGenerated = size;

        return "[" + sb.toString().substring(0, sb.length() - 1) + "]";
    }
}