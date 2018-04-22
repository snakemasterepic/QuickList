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

import java.util.Iterator;
import java.util.ListIterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * Structure documentation:
 *
 * [N]: N is within the backbone A-B: A is connected to B [XX]: Node removed
 * from the backbone
 *
 * @author Nathann Hohnbaum
 */
public class QuickListTest
{

    // Structure: (flat)
    //
    // [B0]-[B1]-[B2]-[B3]-[B4]-[B5]-[B6]-[B7]-[B8]-[B9]
    // Expected toString: "[B0, B1, B2, B3, B4, B5, B6, B7, B8, B9]"
    private QuickList<String> q1;
    // Structure:
    //
    //     /-I2
    //     | |
    //  I0 | I3                                                T2
    //  |  | |                                                /
    //  I1 | I4 /-I5           /-I6                         T1
    //  |  / |  / |            / |                         /
    // [B0]-[B1]-[B2]-[B3]-[B4]-[B5]-[B6]-[B7]-[B8]-[B9]-T0
    // Expected toString: "[I0, I1, B0, I2, I3, I4, B1, I5, B2, B3, B4, I6, B5, B6, B7, B8, B9, T0, T1, T2]"
    private QuickList<String> q2;
    // Structure:
    // 
    //
    //                         /-I1
    //                         | | 
    //                         | I2      /-I5
    //                         | |       | |
    //               /-I0      | I3 /-I4 | I6
    //               / |       / |  / |  / |
    // [B0]-[B1]-[B2]-[B3]-[B4]-[B5]-[B6]-[B7]-[B8]-[B9]
    // Expected toString: "[B0, B1, B2, I0, B3, B4, I1, I2, I3, B5, I4, B6, I5, I6, B7, B8, B9]"
    private QuickList<String> q3;
    // Structure:
    //
    // [B0]-[B1]-[B2]-[XX]-[B4]-[B5]-[XX]-[XX]-[B8]-[B9]
    // Expected toString: "[B0, B1, B2, B4, B5, B8, B9]"
    private QuickList<String> q4;
    // Structure:
    //
    // 
    //               /------I2               T1
    //               |      |               /
    //       I0      |      I3 /------I4  T0
    //       |       /      |  /      |  /
    // [XX]-[B1]-[I1]-[XX]-[B4]-[XX]-[B6]-[XX]-[XX]-[XX]
    // Expected toString: "[I0, B1, I1, I2, I3, B4, I4, B6, T0, T1]"
    // Note: I4 was previously in a wrinkle whose base was removed from the backbone
    private QuickList<String> q5;
    // Structure: (no cleanup)
    //
    //             T3
    //            /
    //          T2
    //         /
    //       T1
    //      /
    //    T0
    //   /
    // []
    //Expected toString: "[T0, T1, T2, T3]"
    private QuickList<String> q6;

    @BeforeClass
    public static void setUpClass()
    {
    }

    @AfterClass
    public static void tearDownClass()
    {
    }

    @Before
    public void setUp()
    {
        q1 = new QuickList<>();
        q2 = new QuickList<>();
        q3 = new QuickList<>();
        q4 = new QuickList<>();
        q5 = new QuickList<>();
        q6 = new QuickList<>();

        q1.add("B0");
        q1.add("B1");
        q1.add("B2");
        q1.add("B3");
        q1.add("B4");
        q1.add("B5");
        q1.add("B6");
        q1.add("B7");
        q1.add("B8");
        q1.add("B9");
        q1.cleanup();

        q2.add("B0");
        q2.add("B1");
        q2.add("B2");
        q2.add("B3");
        q2.add("B4");
        q2.add("B5");
        q2.add("B6");
        q2.add("B7");
        q2.add("B8");
        q2.add("B9");
        q2.cleanup();
        q2.add(0, "I0");
        q2.add(1, "I1");
        q2.add(3, "I2");
        q2.add(4, "I3");
        q2.add(5, "I4");
        q2.add(7, "I5");
        q2.add(11, "I6");
        q2.add("T0");
        q2.add("T1");
        q2.add("T2");

        q3.add("B0");
        q3.add("B1");
        q3.add("B2");
        q3.add("B3");
        q3.add("B4");
        q3.add("B5");
        q3.add("B6");
        q3.add("B7");
        q3.add("B8");
        q3.add("B9");
        q3.cleanup();
        q3.add(3, "I0");
        q3.add(6, "I1");
        q3.add(7, "I2");
        q3.add(8, "I3");
        q3.add(10, "I4");
        q3.add(12, "I6");
        q3.add(12, "I5"); // Add in different orders

        q4.add("B0");
        q4.add("B1");
        q4.add("B2");
        q4.add("B3");
        q4.add("B4");
        q4.add("B5");
        q4.add("B6");
        q4.add("B7");
        q4.add("B8");
        q4.add("B9");
        q4.cleanup();
        q4.remove(7);
        q4.remove(6);
        q4.remove(3);

        q5.add("B0");
        q5.add("B1");
        q5.add("B2");
        q5.add("B3");
        q5.add("B4");
        q5.add("B5");
        q5.add("B6");
        q5.add("B7");
        q5.add("B8");
        q5.add("B9");
        q5.cleanup();
        q5.remove(9);
        q5.remove(8);
        q5.add("T0");
        q5.add("T1");
        q5.remove(7);
        q5.add(6, "I4");
        q5.remove(5);
        q5.add(4, "I3");
        q5.add(4, "I2");
        q5.remove(3);
        q5.add(2, "I1");
        q5.remove(3); // Remove the base of a wrinkle
        q5.remove(0);
        q5.add(0, "I0");

        q6.add("T0");
        q6.add("T1");
        q6.add("T2");
        q6.add("T3");
    }

    @After
    public void tearDown()
    {
    }

    @Test
    public void testAdd()
    {
        q1.add("T0");
        q1.add("T1");
        q1.add(9, "I0");

        try {
            q1.add(-1, "H");
            fail("Expected IndexOutOfBoundsException, but none was thrown");
        } catch (IndexOutOfBoundsException e) {

        }

        try {
            q1.add(14, "H");
            fail("Expected IndexOutOfBoundsException, but none was thrown");
        } catch (IndexOutOfBoundsException e) {

        }

        assertEquals("[B0, B1, B2, B3, B4, B5, B6, B7, B8, I0, B9, T0, T1]", q1.toString());
    }

    @Test
    public void testGet1()
    {
        try {
            q1.get(-1);
            fail("Expected IndexOutOfBoundsException, but none was thrown");
        } catch (IndexOutOfBoundsException e) {

        }
        assertEquals("B0", q1.get(0));
        assertEquals("B1", q1.get(1));
        assertEquals("B2", q1.get(2));
        assertEquals("B3", q1.get(3));
        assertEquals("B4", q1.get(4));
        assertEquals("B5", q1.get(5));
        assertEquals("B6", q1.get(6));
        assertEquals("B7", q1.get(7));
        assertEquals("B8", q1.get(8));
        assertEquals("B9", q1.get(9));
        try {
            q1.get(10);
            fail("Expected IndexOutOfBoundsException, but none was thrown");
        } catch (IndexOutOfBoundsException e) {

        }
    }

    @Test
    public void testGet2()
    {

        try {
            q2.get(-1);
            fail("Expected IndexOutOfBoundsException, but none was thrown");
        } catch (IndexOutOfBoundsException e) {

        }
        assertEquals("I0", q2.get(0));
        assertEquals("I1", q2.get(1));
        assertEquals("B0", q2.get(2));
        assertEquals("I2", q2.get(3));
        assertEquals("I3", q2.get(4));
        assertEquals("I4", q2.get(5));
        assertEquals("B1", q2.get(6));
        assertEquals("I5", q2.get(7));
        assertEquals("B2", q2.get(8));
        assertEquals("B3", q2.get(9));
        assertEquals("B4", q2.get(10));
        assertEquals("I6", q2.get(11));
        assertEquals("B5", q2.get(12));
        assertEquals("B6", q2.get(13));
        assertEquals("B7", q2.get(14));
        assertEquals("B8", q2.get(15));
        assertEquals("B9", q2.get(16));
        assertEquals("T0", q2.get(17));
        assertEquals("T1", q2.get(18));
        assertEquals("T2", q2.get(19));
        try {
            q2.get(20);
            fail("Expected IndexOutOfBoundsException, but none was thrown");
        } catch (IndexOutOfBoundsException e) {

        }
    }

    @Test
    public void testGet3()
    {
        try {
            q3.get(-1);
            fail("Expected IndexOutOfBoundsException, but none was thrown");
        } catch (IndexOutOfBoundsException e) {

        }
        assertEquals("B0", q3.get(0));
        assertEquals("B1", q3.get(1));
        assertEquals("B2", q3.get(2));
        assertEquals("I0", q3.get(3));
        assertEquals("B3", q3.get(4));
        assertEquals("B4", q3.get(5));
        assertEquals("I1", q3.get(6));
        assertEquals("I2", q3.get(7));
        assertEquals("I3", q3.get(8));
        assertEquals("B5", q3.get(9));
        assertEquals("I4", q3.get(10));
        assertEquals("B6", q3.get(11));
        assertEquals("I5", q3.get(12));
        assertEquals("I6", q3.get(13));
        assertEquals("B7", q3.get(14));
        assertEquals("B8", q3.get(15));
        assertEquals("B9", q3.get(16));
        try {
            q3.get(17);
            fail("Expected IndexOutOfBoundsException, but none was thrown");
        } catch (IndexOutOfBoundsException e) {

        }
    }

    @Test
    public void testGet4()
    {
        try {
            q4.get(-1);
            fail("Expected IndexOutOfBoundsException, but none was thrown");
        } catch (IndexOutOfBoundsException e) {

        }
        assertEquals("B0", q4.get(0));
        assertEquals("B1", q4.get(1));
        assertEquals("B2", q4.get(2));
        assertEquals("B4", q4.get(3));
        assertEquals("B5", q4.get(4));
        assertEquals("B8", q4.get(5));
        assertEquals("B9", q4.get(6));
        try {
            q4.get(7);
            fail("Expected IndexOutOfBoundsException, but none was thrown");
        } catch (IndexOutOfBoundsException e) {

        }
    }

    @Test
    public void testGet5()
    {
        try {
            q5.get(-1);
            fail("Expected IndexOutOfBoundsException, but none was thrown");
        } catch (IndexOutOfBoundsException e) {

        }
        assertEquals("I0", q5.get(0));
        assertEquals("B1", q5.get(1));
        assertEquals("I1", q5.get(2));
        assertEquals("I2", q5.get(3));
        assertEquals("I3", q5.get(4));
        assertEquals("B4", q5.get(5));
        assertEquals("I4", q5.get(6));
        assertEquals("B6", q5.get(7));
        assertEquals("T0", q5.get(8));
        assertEquals("T1", q5.get(9));
        try {
            q5.get(10);
            fail("Expected IndexOutOfBoundsException, but none was thrown");
        } catch (IndexOutOfBoundsException e) {

        }
    }

    @Test
    public void testGet6()
    {
        try {
            q6.get(-1);
            fail("Expected IndexOutOfBoundsException, but none was thrown");
        } catch (IndexOutOfBoundsException e) {

        }
        assertEquals("T0", q6.get(0));
        assertEquals("T1", q6.get(1));
        assertEquals("T2", q6.get(2));
        assertEquals("T3", q6.get(3));
        try {
            q6.get(4);
            fail("Expected IndexOutOfBoundsException, but none was thrown");
        } catch (IndexOutOfBoundsException e) {

        }
    }

    @Test
    public void testRemove()
    {
        // Tests involving removing from the backbone are handled in the creation of the QuickLists.
        // This includes removing the base of a wrinkle.

        // Removing from a Wrinkle
        assertEquals("I1", q3.remove(6));
        // Ensure the rest of the list is intact
        assertEquals("B0", q3.get(0));
        assertEquals("B1", q3.get(1));
        assertEquals("B2", q3.get(2));
        assertEquals("I0", q3.get(3));
        assertEquals("B3", q3.get(4));
        assertEquals("B4", q3.get(5));
        assertEquals("I2", q3.get(6));
        assertEquals("I3", q3.get(7));
        assertEquals("B5", q3.get(8));
        assertEquals("I4", q3.get(9));
        assertEquals("B6", q3.get(10));
        assertEquals("I5", q3.get(11));
        assertEquals("I6", q3.get(12));
        assertEquals("B7", q3.get(13));
        assertEquals("B8", q3.get(14));
        assertEquals("B9", q3.get(15));
        try {
            q3.get(16);
            fail("Expected IndexOutOfBoundsException, but none was thrown");
        } catch (IndexOutOfBoundsException e) {

        }
        assertEquals(16, q3.size());
        assertEquals("[B0, B1, B2, I0, B3, B4, I2, I3, B5, I4, B6, I5, I6, B7, B8, B9]", q3.toString());
    }

    @Test
    public void testSize()
    {
        assertEquals(10, q1.size());
        assertEquals(20, q2.size());
        assertEquals(17, q3.size());
        assertEquals(7, q4.size());
        assertEquals(10, q5.size());
        assertEquals(4, q6.size());
    }

    @Test
    public void testIteratorRemove()
    {
        // test removing from the backbone moving forward
        ListIterator<String> li = q2.listIterator(8);
        assertEquals("B2", li.next());
        li.remove();
        assertEquals("I5", q2.get(7));
        assertEquals("B3", q2.get(8));
    }

    @Test
    public void testIteratorRemove2()
    {
        // Test removing from the backbone moving backward
        ListIterator<String> li = q2.listIterator(9);
        assertEquals("B2", li.previous());
        li.remove();
        assertEquals("I5", q2.get(7));
        assertEquals("B3", q2.get(8));
    }

    @Test
    public void testIteratorAdd1()
    {
        ListIterator<String> li = q4.listIterator(5);
        li.add("H1");
        assertEquals("H1", q4.get(5));
        assertEquals("H1", li.previous());
        assertEquals("B5", li.previous());
        li.add("H2");
        assertEquals("H2", q4.get(4));
    }

    @Test
    public void testIteratorAdd2()
    {
        ListIterator<String> li = q4.listIterator(4);
        assertEquals("B5", li.next());
        li.add("H1");
        assertEquals("H1", q4.get(5));
    }

    @Test
    public void testIteratorAdd3()
    {
        QuickList<Integer> q=new QuickList<>();
        ListIterator<Integer> it=q.listIterator();
        it.add(2);
        assertEquals(new Integer(2),q.get(0));
    }
    
    @Test
    public void testToString()
    {
        assertEquals("[B0, B1, B2, B3, B4, B5, B6, B7, B8, B9]", q1.toString());
        assertEquals("[I0, I1, B0, I2, I3, I4, B1, I5, B2, B3, B4, I6, B5, B6, B7, B8, B9, T0, T1, T2]", q2.toString());
        assertEquals("[B0, B1, B2, I0, B3, B4, I1, I2, I3, B5, I4, B6, I5, I6, B7, B8, B9]", q3.toString());
        assertEquals("[B0, B1, B2, B4, B5, B8, B9]", q4.toString());
        assertEquals("[I0, B1, I1, I2, I3, B4, I4, B6, T0, T1]", q5.toString());
        assertEquals("[T0, T1, T2, T3]", q6.toString());
    }
    
    

}
