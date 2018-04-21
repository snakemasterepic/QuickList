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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author Nathann Hohnbaum
 */
public class QuickListTester
{

    /**
     * Random number generator for random number tests
     */
    public static Random rng = new Random();

    /**
     * Entry point of the program
     *
     * @param args the list of arguments
     *
     */
    public static void main(String[] args)
    {
        if (args.length == 0) {
            System.out.println("No operation to perform");
            System.exit(0);
        }
        // Loop over the operations
        for (int i = 0; i < args.length; i += 4) {
            // Use the same seed to compare functionality
            long commonSeed = rng.nextLong();
            String op = args[i];
            if (i + 3 >= args.length ) {
                incorrectUsage();
            }
            // Determine the lists to use in the operations
            QuickList<List<Integer>> s = new QuickList<>();
            if (args[i + 1].contains("A")) {
                s.add(new ArrayList<Integer>());
            }
            if (args[i + 1].contains("L")) {
                s.add(new LinkedList<Integer>());
            }
            if (args[i + 1].contains("Q")) {
                s.add(new QuickList<Integer>());
            }
            // Determine the size of the lists and how many operations to perform
            int listSize = 0;
            int opCount = 0;
            try {
                listSize = Integer.parseInt(args[i + 2]);
                opCount = Integer.parseInt(args[i + 3]);
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid numbers");
                System.exit(1);
            }

            // Populate the lists
            for (int j = 0; j < listSize; j++) {
                int toAdd=rng.nextInt();
                for (List<Integer> l : s) {
                    l.add(toAdd);
                }
            }

            // Cleanup any QuickLists
            for (List<Integer> q : s) {
                if (q instanceof QuickList) {
                    QuickList<Integer> q1 = (QuickList<Integer>) q;
                    q1.cleanup();
                }
            }

            // Perform the operation
            switch (op) {
                case "R":
                    // Random tests
                    for (List<Integer> l: s) {
                        rng.setSeed(commonSeed);
                        testRandomOperations(l,opCount);
                    }
                    break;
                case "I":
                    // Iterator tests
                    for (List<Integer> l: s) {
                        rng.setSeed(commonSeed);
                        testRandomIterator(l,opCount);
                    }
                    break;
                default:
                    System.out.println("Error: Expected 'R' or 'I' for the command");
                    System.exit(0);
            }
            // Ensure correct functionality
            boolean noProblem = true;
            for (List<Integer> l1 : s) {
                for (List<Integer> l2 : s) {
                    if (!l1.equals(l2)) {
                        noProblem = false;
                    }
                }
            }
            System.out.println();
            System.out.println("Functionality: "+noProblem);
            System.out.println();
        }
    }

    /**
     * Prints incorrect usage for not providing sufficient arguments
     */
    public static void incorrectUsage()
    {
        System.out.println("Error: Incorrect usage");
        System.out.println("Expected: [command] [list flags] [size] [op count]");
        System.exit(1);
    }

    /**
     * Performs random operations on a specified list and prints the results
     *
     * @param l the list for random operations
     * @param n the number of random operations to perform
     */
    public static void testRandomOperations(List<Integer> l, int n)
    {
        long addTotal = 0;
        long remTotal = 0;
        long getTotal = 0;
        for (int i = 0; i < n; i++) {
            int op = rng.nextInt(3);
            long start = 0;
            long stop = 0;
            int index = 0;
            switch (op) {
                case 0:
                    int e = rng.nextInt();
                    index = rng.nextInt(l.size() + 1);
                    start = System.currentTimeMillis();
                    l.add(index, e);
                    stop = System.currentTimeMillis();
                    addTotal += stop - start;
                    break;
                case 1:
                    if (l.isEmpty()) {
                        break;
                    }
                    index = rng.nextInt(l.size());
                    start = System.currentTimeMillis();
                    l.get(index);
                    stop = System.currentTimeMillis();
                    getTotal += stop - start;
                    break;
                case 2:
                    if (l.isEmpty()) {
                        break;
                    }
                    index = rng.nextInt(l.size());
                    start = System.currentTimeMillis();
                    l.remove(index);
                    stop = System.currentTimeMillis();
                    remTotal += stop - start;
            }
        }
        System.out.println("Results for Random Operation Tests On " + l.getClass().getName() + ":");
        System.out.println();
        System.out.println("Operation\t|\tTotal Time");
        System.out.println("----------------+-----------------");
        System.out.println("Add\t\t|\t" + addTotal);
        System.out.println("Get\t\t|\t" + getTotal);
        System.out.println("Remove\t\t|\t" + remTotal);
        System.out.println();
        System.out.println();
    }

    /**
     * Performs random iterator operations involving iterating both forward and
     * backward through a list
     *
     * @param l the list to iterate over
     * @param n the number of pairs of passes (one forward and one backward) to
     * perform with the iterator
     */
    public static void testRandomIterator(List<Integer> l, int n)
    {
        long nextTotal = 0;
        long prevTotal = 0;
        long addTotal = 0;
        long removeTotal = 0;

        for (int i = 0; i < n; i++) {
            ListIterator<Integer> it = l.listIterator();

            while (it.hasNext()) {
                long nStart = System.currentTimeMillis();
                it.next();
                long nStop = System.currentTimeMillis();
                nextTotal += nStop - nStart;
                int roll = rng.nextInt(10);
                long opStart = 0;
                long opStop = 0;
                switch (roll) {
                    case 0:
                        int ins = rng.nextInt();
                        opStart = System.currentTimeMillis();
                        it.add(ins);
                        opStop = System.currentTimeMillis();
                        addTotal += opStop - opStart;
                        break;
                    case 1:
                        opStart = System.currentTimeMillis();
                        it.remove();
                        opStop = System.currentTimeMillis();
                        removeTotal += opStop - opStart;
                }
            }

            while (it.hasPrevious()) {
                long pStart = System.currentTimeMillis();
                it.previous();
                long pStop = System.currentTimeMillis();
                prevTotal += pStop - pStart;
                int roll = rng.nextInt(10);
                long opStart = 0;
                long opStop = 0;
                switch (roll) {
                    case 0:
                        int ins = rng.nextInt();
                        opStart = System.currentTimeMillis();
                        it.add(ins);
                        opStop = System.currentTimeMillis();
                        addTotal += opStop - opStart;
                        break;
                    case 1:
                        opStart = System.currentTimeMillis();
                        it.remove();
                        opStop = System.currentTimeMillis();
                        removeTotal += opStop - opStart;
                }
            }

        }

        System.out.println("Results for Iterator Test On " + l.getClass().getName() + ":");
        System.out.println();
        System.out.println("Operation\t|\tTotal Time");
        System.out.println("----------------+-----------------");
        System.out.println("Next\t\t|\t" + nextTotal);
        System.out.println("Previous\t|\t" + prevTotal);
        System.out.println("Add\t\t|\t" + addTotal);
        System.out.println("Remove\t\t|\t" + removeTotal);
        System.out.println();
        System.out.println();
    }

}
