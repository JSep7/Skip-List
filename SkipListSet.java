/*
   Written by: Jaime Sepulveda
   Final Project SkipList Overview:
   This project implements a generic SkipListSet
   data structure that maintains sorted elements
   with logarithmic-time search, insertion, and deletion.
   Class: COP3503C
*/
import java.util.*;

public class SkipListSet<T extends Comparable<T>> implements SortedSet<T> {

    /*
     *  Represents a node in the skip list hold
     *  a payload a height and an array list of pointers
     */
    class SkipListSetItem {
        T payload;
        int height;
        ArrayList<SkipListSetItem> next;

        public SkipListSetItem(T payload, int height) {
            this.payload = payload;
            this.height = height;
            this.next = new ArrayList<>(Collections.nCopies(height, null));
        }
    }

    /*
     * A custom iterator for the skip list. Iterates through
     * elements starting starts from the lowest level.
     */
    class SkipListIterator<T extends Comparable<T>> implements Iterator<T> {
        private SkipListSetItem current;

        public SkipListIterator(SkipListSetItem head) {
            this.current = head.next.get(0); // Start at base
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public T next() {
            if (current == null) {
                return null;
            }
            T value = (T) current.payload;
            current = current.next.get(0); // Moves to the next node
            return value;
        }

        @Override
        public void remove() {
            // Optional
        }
    }

    private SkipListSetItem head = new SkipListSetItem(null, 1); // Sentinel
    private int size = 0; // # of elements


    // Returns the size of the skip list
    @Override
    public int size() {
        return size;
    }

    // Checks if the list is empty
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /*
     * Contains function determines whether the given object
     * exist in the skip list. Uses a top-down traversal
     * starting from the highest level of the head node.
     */
    @Override
    public boolean contains(Object o) {
        T target = (T) o; // Cast the input object
        SkipListSetItem current = head;
        int level = head.height - 1;

        // Traversal from the top level down to 0
        while (level >= 0) {
            // Move forward within the current level while next payload is less than the target
            while (current.next.get(level) != null && current.next.get(level).payload.compareTo(target) < 0 )  {
                current = current.next.get(level);
            }
            // If the next node exists and its payload matches the target, return true
            if (current.next.get(level) != null && current.next.get(level).payload.equals(o)) {
                return true;
            }
            // Else drop down one level and continue searching
            else {
                level--;
            }
        }
        // Return false if target wasn't reached
        return false;
    }

    /*
     * Returns an iterator starting from the first actual element
     * after the head in the base level of the skip list. This enables
     * for-each style iteration over the SkipListSet.
     */
    @Override
    public Iterator<T> iterator() {
        return new SkipListIterator(head);
    }

    /*
     * Converts the skip list into a plain Object array.
     * Iterates through all elements in the skip list
     * and stores them sequentially in the array.
     */
    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size];
        int i = 0;
        for (T val : this) { // Uses the iterator method defined above
            arr[i++] = val;
        }
        return arr;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        int i = 0;

        // If the input array is too small, create a new one of the same type
        if (a.length < size) {
            a = Arrays.copyOf(a, size);
        }

        for (Object val : this) {
            a[i++] = (T) val;
        }

        // If the array is larger than needed, set the next element to null
        if (a.length > size) {
            a[size] = null;
        }

        return a;
    }


    /*
     * Inserts the given element into the skip list
     * while maintaining order and level structure.
     * A random height is chosen for the new node,
     * and the list is updated accordingly across levels.
     */
    @Override
    public boolean add(T e) {
        // If contains element e in skip list, do not add
        if (contains(e)) {
            return false;
        }

        // Random height generation
        int nodeHeight = 1;
        Random rand = new Random();
        while (rand.nextBoolean()) {
            nodeHeight++;
        }

        // Expand head height if needed
        if (nodeHeight > head.height) {
            for (int i = head.height; i < nodeHeight; i++) {
                head.next.add(null);
            }
            head.height = nodeHeight;
        }

        // Tracker of node at each level that should point to new node
        List<SkipListSetItem> update = new ArrayList<>(Collections.nCopies(head.height, null));
        SkipListSetItem current = head;


        for (int level = head.height - 1; level >= 0; level--) {
            while (current.next.get(level) != null && current.next.get(level).payload.compareTo(e) < 0) {
                current = current.next.get(level);
            }
            update.set(level, current);
        }

        // Create the new node with the chosen height
        SkipListSetItem newNode = new SkipListSetItem(e, nodeHeight);

        // Insert the new node into each relevant level, level 0 for now
        for (int level = 0; level < newNode.height; level++) {
            newNode.next.set(level, update.get(level).next.get(level));
            update.get(level).next.set(level, newNode);
        }

        // Increment size to show something was inserted
        size++;
        return true;
    }

    /*
     * Removes the given element from the skip list
     * if it exists. Updates forward pointers at each
     * level and adjusts head height if needed.
     */
    @Override
    public boolean remove(Object o) {
        T target = (T) o;

        // Track nodes at each level where we might need to update pointers
        List<SkipListSetItem> update = new ArrayList<>(Collections.nCopies(head.height, null));
        SkipListSetItem current = head;

        // Traverse from top level to bottom to find the node to remove
        for (int level = head.height - 1; level >= 0; level--) {
            while (current.next.get(level) != null && current.next.get(level).payload.compareTo(target) < 0) {
                current = current.next.get(level);
            }
            update.set(level, current);
        }

        // Target node might be at level 0
        current = current.next.get(0);
        if (current == null || !current.payload.equals(target)) {
            return false; // Not found
        }

        // Update pointers at all levels where the node exists
        for (int level = 0; level < current.height; level++) {
            // Only update pointers where the current node exists in the level
            if (update.get(level).next.get(level) == current) {
                update.get(level).next.set(level, current.next.get(level));
            }
        }

        // Reduce head height if top levels are now empty
        while (head.height > 1 && head.next.get(head.height - 1) == null) {
            head.next.remove(head.height - 1);
            head.height--;
        }

        size--;
        return true;
    }


    /*
     *  Checks if all elements in the given collection
     *  c exist in the current skip list set. Returns false
     *  immediately if any element is not found; true otherwise.
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object item : c) {
            if (!contains(item)) {
                return false;
            }
        }
        return true;
    }

    /*
     * Adds all elements from the given collection c
     * to the skip list set. The change flag tracks
     * whether at least one new element was actually added.
     */
    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean changed = false;
        for (T item : c) {
            if (add(item)) {
                changed = true;
            }
        }
        return changed;
    }

    /*
     * Keeps only the elements that are contained in collection c.
     * Builds a list of elements to remove. Then removes them and
     * returns true if any element were removed.
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        List<T> list = new ArrayList<>(size);
        for(T val : this){
            if(!c.contains(val)) {
                list.add(val);
            }
        }
        for(T val : list) {
            remove(val);
        }
        return !list.isEmpty();
    }

    /*
     * Removes all elements in collection c from the skip list set
     * The changed flag tracks if any element was removed successfully.
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object item : c) {
            if (remove(item)) {
                changed = true;
            }
        }
        return changed;
    }

    /*
     * Clears the entire skip list by resetting the head node and size.
     * The skip list becomes empty and ready to be reused.
     */
    @Override
    public void clear() {
        head = new SkipListSetItem(null, 1);
        head.height = 1;
        head.next = new ArrayList<>(Collections.nCopies(1, null));
        size = 0;
    }

    /*
     * Re-randomizes the height of every node in the skip list.
     * Calculates a new max height based on list size, updates
     * each node’s height, and rebuilds forward pointers for levels above 0.
     */
    public void reBalance() {
        if (size == 0) {
            return;
        }

        // Determine new max height
        int newMaxHeight = Math.max(4, (int)(Math.ceil(Math.log(size) / Math.log(2))) + 1);

        // Reset head to new height if needed
        if (head.height < newMaxHeight) {
            for (int i = head.height; i < newMaxHeight; i++) {
                head.next.add(null);
            }
            head.height = newMaxHeight;
        } else if (head.height > newMaxHeight) {
            head.next.subList(newMaxHeight, head.height).clear();
            head.height = newMaxHeight;
        }

        // Store nodes at level 0 in order
        ArrayList<SkipListSetItem> nodes = new ArrayList<>(size);
        SkipListSetItem node = head.next.get(0);
        while (node != null) {
            nodes.add(node);
            node = node.next.get(0);
        }

        Random rand = new Random();

        // Reset and re-randomize height and next pointers
        for (SkipListSetItem n : nodes) {
            // Regenerate height
            int newHeight = 1;
            while (newHeight < newMaxHeight && rand.nextDouble() < 0.5) {
                newHeight++;
            }

            // Reset to new height
            if (n.height < newHeight) {
                for (int i = n.height; i < newHeight; i++) {
                    n.next.add(null);
                }
            } else if (n.height > newHeight) {
                n.next.subList(newHeight, n.height).clear();
            }
            n.height = newHeight;
        }

        // Rebuild upper level pointers
        for (int level = 1; level < newMaxHeight; level++) {
            SkipListSetItem prev = head;
            for (SkipListSetItem n : nodes) {
                if (n.height > level) {
                    prev.next.set(level, n);
                    prev = n;
                }
            }
            prev.next.set(level, null);
        }
    }

    /*
     * Computes the hash code for the skip list
     * THe has code is the sum of the hash codes
     * of all elems in the set.
     */
    @Override
    public int hashCode() {
        int hash = 0;

        // Loop through each element in the set
        for (T val : this) {
            // If the value is not null add its hashCode
            if (val != null) {
                hash += val.hashCode();
            } else {
                hash += 0;
            }
        }
        return hash;
    }

    /*
     * Determines if the current SkipListSet is
     * equal to another Set. Two sets are considered equal
     * if they contain the same elements, regardless of order.
     */
    @Override
    public boolean equals(Object o) {
        // Check if it's equals then returns true
        if (o == this) {
            return true;
        }

        // CHeck that o is not a set then returns false
        if (!(o instanceof Set<?> other)) { // Compiler says variable can be replaced by pattern variable to get this line
            return false;
        }


        // Checks if size differ as sets can't be equal
        if (this.size() != other.size()) {
            return false;
        }

        // Check each element in the other set is in the set otherwise its false
        for (Object item : other) {
            if (!this.contains(item)) {
                return false;
            }
        }

        // All if statements have been passed so sets are equal
        return true;
    }

    @Override
    public Comparator<? super T> comparator() {
        return null;
    }

    // Set to throw new as professor wanted
    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        throw new UnsupportedOperationException();
    }

    // Set to throw new as professor wanted
    @Override
    public SortedSet<T> headSet(T toElement) {
        throw new UnsupportedOperationException();
    }

    // Set to throw new as professor wanted
    @Override
    public SortedSet<T> tailSet(T fromElement) {
        throw new UnsupportedOperationException();
    }

    /*
     * Returns the first smallest element in the skip
     * list. If the list is empty it returns null.
     */
    @Override
    public T first() {
        if (head.next.get(0) != null) {
            return head.next.get(0).payload;
        } else {
            return null;
        }
    }

    /*
     * Returns the last largest element in the skip
     * list. If the list is empty it returns null.
     */
    @Override
    public T last() {
        SkipListSetItem node = head;
        while (node.next.get(0) != null) {
            node = node.next.get(0);
        }
        return node.payload;
    }
}
