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
package org.reific.binarysearch.biased.evolve;

import java.util.Random;

public enum Mutater {

	M1()
	{
		@Override
		/**
		 * Flip one random bit.
		 */
		public int mutate(int value) {
			return value ^ (1 << (random.nextInt(28)));
		}
	},
	M2()
	{
		@Override
		/**
		 * Flip two random consecutive bits.
		 */
		public int mutate(int value) {
			int nextInt = random.nextInt(28);
			value = value ^ (1 << (nextInt));
			value = value ^ (1 << ((nextInt + 1) % 28));
			return value;
		}
	},
	M3()
	{
		@Override
		/**
		 * Clear one random bit.
		 */
		public int mutate(int value) {
			return value & ~(1 << (random.nextInt(28)));
		}
	},
	M4()
	{
		@Override
		/**
		 * Set one random bit.
		 */
		public int mutate(int value) {
			return value | (1 << (random.nextInt(28)));
		}
	},
	M5()
	{
		@Override
		/**
		 * Invert all bits (and clear HO nibble).
		 */
		public int mutate(int value) {
			return (~value) & 0x0fffffff;
		}
	},
	M6()
	{
		@Override
		/**
		 * Shift all bits left one place (and clear HO nibble)
		 */
		public int mutate(int value) {
			return (value << 1) & 0x0fffffff;
		}
	},
	M7()
	{
		@Override
		/**
		 * Shift all bits right one place.
		 */
		public int mutate(int value) {
			return value >>> 1;
		}
	},
	NULL_MUTATER()
	{
		@Override
		/**
		 * Do nothing.
		 */
		public int mutate(int value) {
			return value;
		}
	};

	private static Random random = new Random(0);

	public abstract int mutate(int value);

}
