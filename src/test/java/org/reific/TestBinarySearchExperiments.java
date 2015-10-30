/*
    Copyright (C) 2015 James Scriven

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.reific;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestBinarySearchExperiments {

	@Test
	/**
	 * Test a variety of array sizes for correct behaviour.
	 */
	public void testManySizesBranchFree() throws Exception {
		int MAX_SIZE = 1024 * 1024;
		for (int size = 1; size <= MAX_SIZE; size = (int) (size * 1.10 + 1)) {
			int haystack[] = new int[size];
			for (int i = 0; i < haystack.length; i++) {
				haystack[i] = i * 2;
			}
			for (int i = 0; i < haystack.length; i++) {
				assertEquals(i, BinarySearchExperiments.binarySearchBranchFree(haystack, 0, haystack.length, i * 2));
				assertTrue(BinarySearchExperiments.binarySearchBranchFree(haystack, 0, haystack.length, i * 2 - 1) < 0);
			}
			assertTrue(BinarySearchExperiments.binarySearchBranchFree(haystack, 0, haystack.length, -1) < 0);
			assertTrue(BinarySearchExperiments.binarySearchBranchFree(haystack, 0, haystack.length, Integer.MAX_VALUE) < 0);
			assertTrue(BinarySearchExperiments.binarySearchBranchFree(haystack, 0, haystack.length, Integer.MIN_VALUE) < 0);

		}
	}

	@Test
	/**
	 * Test a variety of array sizes for correct behaviour.
	 */
	public void testManySizesBranchFreePrefetch() throws Exception {
		int MAX_SIZE = 1024 * 1024;
		for (int size = 4; size <= MAX_SIZE; size *= 2) {
			int haystack[] = new int[size - 1];
			for (int i = 0; i < haystack.length; i++) {
				haystack[i] = i * 2;
			}
			for (int i = 0; i < haystack.length; i++) {
				assertEquals(i,
						BinarySearchExperiments.binarySearchBranchFreePrefetch(haystack, 0, haystack.length, i * 2));
				assertTrue(BinarySearchExperiments.binarySearchBranchFreePrefetch(haystack, 0, haystack.length,
						i * 2 - 1) < 0);
			}
			assertTrue(BinarySearchExperiments.binarySearchBranchFreePrefetch(haystack, 0, haystack.length, -1) < 0);
			assertTrue(BinarySearchExperiments.binarySearchBranchFreePrefetch(haystack, 0, haystack.length,
					Integer.MAX_VALUE) < 0);
			assertTrue(BinarySearchExperiments.binarySearchBranchFreePrefetch(haystack, 0, haystack.length,
					Integer.MIN_VALUE) < 0);

		}
	}


	@Test
	/**
	 * Test a variety of array sizes for correct behaviour.
	 */
	public void testBranchFreeUnrolledStatic() throws Exception {

		int haystack[] = new int[16];
		for (int i = 0; i < haystack.length; i++) {
			haystack[i] = i * 2;
		}
		for (int i = 0; i < haystack.length; i++) {
			assertEquals(i, BinarySearchExperiments.binarySearchBranchFreeUnrolledStatic16(haystack, i * 2));
			assertTrue(BinarySearchExperiments.binarySearchBranchFreeUnrolledStatic16(haystack, i * 2 - 1) < 0);
		}
		assertTrue(BinarySearchExperiments.binarySearchBranchFreeUnrolledStatic16(haystack, -1) < 0);
		assertTrue(BinarySearchExperiments.binarySearchBranchFreeUnrolledStatic16(haystack, Integer.MAX_VALUE) < 0);
		assertTrue(BinarySearchExperiments.binarySearchBranchFreeUnrolledStatic16(haystack, Integer.MIN_VALUE) < 0);

		int haystack2[] = { Integer.MIN_VALUE, Integer.MIN_VALUE + 1, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6,
				Integer.MAX_VALUE - 1, Integer.MAX_VALUE };

		for (int j = 0; j < haystack2.length; j++) {
			assertEquals(j, BinarySearchExperiments.binarySearchBranchFreeUnrolledStatic16(haystack2, haystack2[j]));
		}

	}

}
