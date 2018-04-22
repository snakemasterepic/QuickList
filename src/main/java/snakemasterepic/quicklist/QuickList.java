/*
 * Copyright (C) 2018 Nathann Hohnbaum
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package snakemasterepic.quicklist;

import java.util.AbstractList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * The QuickList is a list implementation that works well when needing to
 * perform large numbers of insertions and deletions over short time intervals
 * without many insertions or deletions with long periods of idle time between.
 *
 * The QuickList works by storing the elements in a linked list structure with
 * an array of nodes known as the backbone storing the majority of the elements
 * and a singly-linked list structure known as the wrinkles storing information
 * about discrepancies between the backbone indexes and the true indexes of the
 * list.
 *
 * @author Nathann Hohnbaum
 * @param <E>
 */
public class QuickList<E> extends AbstractList<E>
{

    /**
     * The Node class stores a single node within the quick list.
     *
     * @param <E> the type of element for the node to store
     */
    private static class Node<E>
    {

        /**
         * The previous node in the list
         */
        public Node<E> prev;
        /**
         * The next node in the list
         */
        public Node<E> next;
        /**
         * The item stored by the node in the list
         */
        public E item;

        /**
         * Creates a new Node object with a given element and previous and next
         * nodes.
         *
         * @param prev the previous node in the list
         * @param next the next node in the list
         * @param item the item to be stored in the node
         */
        public Node(Node<E> prev, Node<E> next, E item)
        {
            this.prev = prev;
            this.next = next;
            this.item = item;
        }
    }

    /**
     * The Wrinke class stores data about discrepancies between the backbone
     * indexes and the true indexes of the elements of the list. It uses a
     * double-linked structure to allow for quick use by iterators.
     */
    private static class Wrinkle
    {

        /**
         * The index within the backbone of the first element affected by this
         * discrepancy
         */
        public int index;
        /**
         * The offset that occurs as a result of this discrepancy. Positive
         * values indicate that the list index goes up while negative values
         * indicate that the list index goes down.
         */
        public int offset;
        /**
         * A reference to the next wrinkle
         */
        public Wrinkle next;
        /**
         * A reference to the previous wrinkle
         */
        public Wrinkle prev;

        /**
         * Creates a Wrinkle for a given discrepancy
         *
         * @param index the index within the backbone where this discrepancy
         * occurs
         * @param offset the amount by which this discrepancy offsets the list
         * indexes relative to the backbone indexes.
         * @param next a reference to the next Wrinkle
         */
        public Wrinkle(Wrinkle prev, int index, int offset, Wrinkle next)
        {
            this.prev = prev;
            this.index = index;
            this.offset = offset;
            this.next = next;
        }
    }

    /**
     * The first node in the list
     */
    private Node<E> head;

    /**
     * The last node in the list
     */
    private Node<E> foot;

    /**
     * The backbone
     */
    private Node<E>[] backbone;

    /**
     * The number of elements in this list
     */
    private int size;

    /**
     * The first wrinkle within the list
     */
    private Wrinkle firstWrinkle;

    /**
     * Creates a new empty QuickList
     */
    public QuickList()
    {
        backbone = (Node<E>[]) new Node[0];
        size = 0;
        head = null;
        foot = null;
        firstWrinkle = null;
        modCount = 0;
    }

    /**
     * Cleans up the list and places it in the flat state
     */
    public void cleanup()
    {
        // Recreate the backbone
        backbone = (Node<E>[]) new Node[size];
        // Populate the backbone
        Node<E> temp = head;
        for (int i = 0; i < size; i++) {
            backbone[i] = temp;
            temp = temp.next;
        }
        // Cleanup operation is a modification
        modCount++;
    }

    /**
     * Computes the index within the list of the specified index within the
     * backbone.
     *
     * @param backboneIndex the index within the backbone
     * @return the index within the list
     */
    private int listIndex(int backboneIndex)
    {
        // Copy of the backbone index to be incremented by the offsets of all wrinkles at or before this index
        int index = backboneIndex;
        // Start with the first wrinkle and add the offsets
        Wrinkle w = firstWrinkle;
        while (w != null && w.index <= backboneIndex) {
            index += w.offset;
            w = w.next;
        }
        return index;
    }

    /**
     * Calculates the index within the backbone of the first element within the
     * backbone that comes after the specified list index
     *
     * @param listIndex the index within the list
     * @return the index within the backbone or backbone.length if the list
     * index is beyond the backbone
     */
    private int backboneIndex(int listIndex)
    {
        // Keeps track of the backbone index of the last wrinkle reached
        int inBackbone = 0;
        // Keeps track of the total offset of all wrinkles so far
        int totalOffset = 0;
        Wrinkle w = firstWrinkle;
        // Loops until the total offset + the index of the next wrinkle exceeds the specified list index
        while (w != null && w.index + totalOffset <= listIndex) {
            inBackbone = w.index;
            totalOffset += w.offset;
            w = w.next;
        }

        if (inBackbone + totalOffset > listIndex) {
            // Case 1: the node is in the next wrinkle wrinle
            return inBackbone;
        } else if (listIndex - totalOffset > backbone.length) {
            // Case 2: the node is in the tail
            return backbone.length;
        } else {
            // Case 3: the node is in the backbone
            return listIndex - totalOffset;
        }
    }

    /**
     * Grabs a node from the list
     *
     * @param backboneIndex the backbone index to start at
     * @param listIndex the index of the node to access
     * @return the node at the specified list index
     */
    private Node<E> grab(int backboneIndex, int listIndex)
    {
        if (listIndex == 0) {
            // Special case: the first element in the list
            return head;
        } else if (listIndex == size - 1) {
            // Special case: the last element in the list
            return foot;
        } else {
            int index;
            Node<E> temp;
            if (backboneIndex == backbone.length) {
                // Accessing the last element in the backbone
                temp = foot;
                index = size - 1;
            } else {
                // Accessing from within the backbone
                temp = backbone[backboneIndex];
                index = listIndex(backboneIndex);
            }
            // Work backward to the node at the specified list index
            while (index > listIndex) {
                index--;
                temp = temp.prev;
            }
            return temp;
        }
    }

    /**
     * Creates a wrinkle to track a discrepancy between the indexes of the nodes
     * in the backbone and the indexes of the nodes in the list.
     *
     * @param index the index within the backbone where the discrepancy takes
     * place
     * @param offset how much the indexes are offset by
     */
    private void addWrinkle(int index, int offset)
    {
        if (index >= backbone.length) {
            return; // Don't add wrinkles for the tail
        }
        if (firstWrinkle == null) {
            // Create the first wrinkle
            firstWrinkle = new Wrinkle(null, index, offset, null);
        } else {
            // It is known at this point that firstWrinkle is not null
            // Grab the first wrinkle and stop when the next wrinkle is at or above the targeted index
            Wrinkle w = firstWrinkle;
            while (w.next != null && w.next.index < index) {
                w = w.next;
            }
            // Create the wrinkle
            if (w == firstWrinkle && w.index >= index) {
                // Special case: the wrinkle to be created is to be the new first wrinkle
                if (w.index == index) {
                    // The first wrinkle is to be updated
                    w.offset += offset;
                    if (w.offset == 0) {
                        firstWrinkle = firstWrinkle.next;
                    }
                } else {
                    // Simply create a new first wrinkle
                    firstWrinkle = new Wrinkle(null, index, offset, w);
                    w.prev = firstWrinkle;
                }
            } else if (w.next == null) {
                // Special case: last wrinkle
                w.next = new Wrinkle(w, index, offset, null);
            } else if (w.next.index == index) {
                // Increase the offset of the next wrinkle since its index is the same as the passed index
                w.next.offset += offset;
                if (w.next.offset == 0) {
                    // If the offset of the next wrinkle is zero, remove it
                    w.next = w.next.next;
                    if (w.next != null) {
                        w.next.prev = w;
                    }
                }
            } else {
                // Create a new wrinkle
                w.next = new Wrinkle(w, index, offset, w.next);
                w.next.next.prev = w.next;
            }
        }
    }

    /**
     * Throws an IndexOutOfBoundsException when the index is out of bounds for
     * accessing elements
     *
     * @param index the index to check
     */
    private void checkIndex(int index)
    {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(generateOutOfBoundsMessage(index));
        }
    }

    /**
     * Generates an IndexOutOfBoundsException message for a specified index
     *
     * @param index the index that is out of bounds
     * @return the string for the specified IndexOutOfBoundsException message
     */
    private String generateOutOfBoundsMessage(int index)
    {
        return "Index: " + index + ", Size: " + size;
    }

    // List methods
    @Override
    public boolean add(E item)
    {
        if (foot == null) {
            // Create the first node of the list
            foot = new Node<>(null, null, item);
            head = foot;
        } else {
            // Create a new foot
            foot.next = new Node<>(foot, null, item);
            foot.next.prev = foot;
            foot = foot.next;
        }
        // Increment the size and modCount
        size++;
        modCount++;
        return true;

    }

    @Override
    public void add(int index, E item)
    {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException(generateOutOfBoundsMessage(index));
        }
        if (index == size) {
            // Special case: adding to the end of the list
            if (foot == null) {
                foot = new Node<>(null, null, item);
                head = foot;
            } else {
                foot.next = new Node<>(foot, null, item);
                foot.next.prev = foot;
                foot = foot.next;
            }
        } else {
            int backboneIndex = backboneIndex(index);
            // Grab the node that comes after the node to insert
            Node<E> after = grab(backboneIndex, index);
            // Create and insert the new node
            Node<E> newNode = new Node(after.prev, after, item);
            if (after.prev != null) {
                after.prev.next = newNode;
            } else {
                // Update the head
                head = newNode;
            }
            after.prev = newNode;
            // Create a wrinkle
            addWrinkle(backboneIndex, 1);
        }
        // Increment the size and modCount
        size++;
        modCount++;

    }

    @Override
    public E get(int index)
    {
        checkIndex(index);
        int backboneIndex = backboneIndex(index);
        Node<E> temp = grab(backboneIndex, index);
        return temp.item;

    }

    @Override
    public E set(int index, E item)
    {

        checkIndex(index);
        int backboneIndex = backboneIndex(index);
        Node<E> target = grab(backboneIndex, index);
        E temp = target.item;
        target.item = item;
        return temp;

    }

    @Override
    public E remove(int index)
    {
        // Index checking
        checkIndex(index);
        // Grab the target node
        int backboneIndex = backboneIndex(index);
        Node<E> target = grab(backboneIndex, index);
        // Delete the item from the list
        E item = target.item;
        target.item = null;
        if (target.next != null) {
            // Update previous reference in next node
            target.next.prev = target.prev;
        } else {
            // Update the foot
            foot = foot.prev;
        }
        if (target.prev != null) {
            // Update next refernce in the previous node
            target.prev.next = target.next;
        } else {
            // Update the head
            head = head.next;
        }
        // Special case: removing from the backbone could also involve removing the base of a wrinkle
        if (backboneIndex < backbone.length && backbone[backboneIndex] == target && target.prev != null) {
            backbone[backboneIndex] = target.prev;
        }
        // Create a wrinkle
        addWrinkle(backboneIndex, -1);
        // Decrement the size and increase the modcount
        size--;
        modCount++;
        return item;

    }

    @Override
    public Iterator<E> iterator()
    {
        return new ListItr(modCount);
    }

    @Override
    public ListIterator<E> listIterator(int index)
    {
        return new ListItr(index, modCount);
    }

    @Override
    public ListIterator<E> listIterator()
    {
        return new ListItr(modCount);
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public void clear()
    {
        size = 0;
        head = null;
        foot = null;
        backbone = (Node<E>[]) new Node[0];
        modCount++;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex)
    {
        // Iteration is fastest when done backward
        ListIterator<E> iter = listIterator(toIndex);
        while (iter.previousIndex() >= fromIndex) {
            iter.previous();
            iter.remove();
        }
    }

    /**
     * Returns a String representing the structure of the list.
     *
     * Each node is represented by the item it contains casted t a String.
     *
     * The backbone is enclosed in curly braces {}.
     *
     * Nodes within the backbone are enclosed in square brackets [].
     *
     * For each positive wrinkle (result of insertion), the nodes including the
     * base are enclosed in parentheses.
     *
     * For each negative wrinkle (result of deletion), the nodes are represented
     * as 'X's without the square brackets.
     *
     * Nodes in the tail are simply placed after the backbone, and each is
     * enclosed within its own set of parentheses.
     *
     * @return a String representing the structure of this QuickList
     */
    public String structure()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Node<E> temp = head;
        int bi = 0;
        Wrinkle w = firstWrinkle;

        while (bi < backbone.length) {
            if (bi > 0) {
                sb.append(", ");
            }
            if (w != null && w.index == bi) {
                if (w.offset == -1) {
                    sb.append("X");
                } else {
                    sb.append("(");
                    for (int i = 0; i <= w.offset; i++) {
                        if (i > 0) {
                            sb.append(", ");
                        }
                        if (i == w.offset) {
                            sb.append("[");
                        }
                        sb.append(temp.item);
                        if (i == w.offset) {
                            sb.append("]");
                        }
                        temp = temp.next;
                    }
                    sb.append(")");
                }
                w = w.next;
            } else {
                sb.append("[");
                sb.append(temp.item);
                sb.append("]");
                temp = temp.next;
            }

            bi++;
        }

        sb.append("}");

        while (temp != null) {
            sb.append(", (");
            sb.append(temp.item);
            sb.append(")");
            temp = temp.next;
        }

        return sb.toString();
    }

    /**
     * The QuickListIterator class iterates over a QuickList.
     */
    private class ListItr implements ListIterator<E>
    {

        /**
         * The next node to iterate over going forward
         */
        private Node<E> next;
        /**
         * The node most recently iterated over
         */
        private Node<E> last;
        /**
         * The index within the list
         */
        private int listIndex;
        /**
         * The index within the backbone
         */
        private int backboneIndex;

        /**
         * The backbone index of the last returned node
         */
        private int lastBackboneIndex;

        /**
         * The modification count at the time this ListIterator was created
         */
        private int initModCount;

        /**
         * Creates a QuickListIterator starting at a specific index
         *
         * @param start the index to start at
         */
        public ListItr(int index, int initModCount)
        {
            if (index < 0 || index > size) {
                throw new IndexOutOfBoundsException("Index: " + index);
            }
            listIndex = index;
            backboneIndex = backboneIndex(index);
            next = grab(backboneIndex, index);
            this.initModCount = initModCount;
            last = null;

        }

        /**
         * Creates a QuickListIterator that starts at the top of the list
         */
        public ListItr(int initModCount)
        {
            this(0, initModCount);
        }

        @Override
        public boolean hasNext()
        {
            return next != null;
        }

        /**
         * Checks for concurrent modifications and throws an exception if one
         * occurred
         */
        private void checkForModifications()
        {
            if (modCount != initModCount) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public E next()
        {
            checkForModifications();
            if (next == null) {
                throw new NoSuchElementException();
            }
            // Retrieve the next node
            last = next;
            next = next.next;
            listIndex++;
            updateBackboneIndexForward();
            return last.item;
        }

        @Override
        public boolean hasPrevious()
        {
            return listIndex != 0;
        }

        @Override
        public E previous()
        {
            checkForModifications();
            if (listIndex == 0) {
                throw new NoSuchElementException();
            }
            // Retrieve the previous node
            if (next == null) { // At the end of the list
                next = foot;

            } else {
                next = next.prev;
            }

            last = next;
            listIndex--;
            updateBackboneIndexBackward();
            return last.item;
        }

        /**
         * Updates the backbone index moving forward through the list
         */
        private void updateBackboneIndexForward()
        {
            lastBackboneIndex = backboneIndex;
            backboneIndex=backboneIndex(listIndex);
        }

        /**
         * Updates the back bone index moving backward through the list
         */
        private void updateBackboneIndexBackward()
        {
            backboneIndex=backboneIndex(listIndex);
            lastBackboneIndex=backboneIndex;
        }

        @Override
        public int nextIndex()
        {
            return listIndex;
        }

        @Override
        public int previousIndex()
        {
            return listIndex - 1;
        }

        @Override
        public void remove()
        {
            checkForModifications();
            if (last == null) {
                throw new IllegalStateException();
            }
            // Update references
            if (last.prev == null) {
                head = head.next;
            } else {
                last.prev.next = last.next;
            }
            if (last.next == null) {
                foot = foot.prev;
            } else {
                last.next.prev = last.prev;
            }
            last.item = null;
            // Handle wrinkle collapsing
            if (lastBackboneIndex < backbone.length && backbone[lastBackboneIndex] == last) {
                backbone[lastBackboneIndex] = last.prev;
            }
            // Update the wrinkles
            addWrinkle(lastBackboneIndex, -1);// Create a wrinkle
           
            if (last != next) { // Last call moved the iterator forward, so go back
                listIndex--;
                //updateBackboneIndexBackward();
            } else {
                next = next.next;
            }
            size--;
            modCount++;
            initModCount++;
            last = null;
        }

        @Override
        public void set(E e)
        {
            checkForModifications();
            if (last == null) {
                throw new IllegalStateException();
            }
            last.item = e;
        }

        @Override
        public void add(E e)
        {
            checkForModifications();
            if (foot == null) {
                // Special case: insert the first node
                head = new Node<>(null, null, e);
                foot = head;
                listIndex++;
            } else if (next == null) {
                // Add to the end of the list
                foot.next = new Node<>(foot, null, e);
                foot = foot.next;
                listIndex++;
            } else if (next.prev == null) {
                // Add to the front of the list
                head.prev = new Node<>(null, head, e);
                head = head.prev;
                listIndex++;
                addWrinkle(0, 1);
            } else {
                // Insert the element anywhere else in the list
                Node<E> newNode = new Node<>(next.prev, next, e);
                next.prev.next = newNode;
                next.prev = newNode;
                listIndex++;
                addWrinkle(backboneIndex, 1);
            }
            size++;
            modCount++;
            initModCount++;
        }

    }

}
