/*
    Copyright (C) 2015 James Scriven

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTACILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.reific.binarysearch.biased.evolve;

public enum Crossover {


	C1() {
		public int cross(int a, int b) {
			return (a & 0xFFFF0000) | (b & 0x0000FFFF);
		}
	},
	C2() {
		public int cross(int a, int b) {
			return (a & 0xF0F0F0F0) | (b & 0x0F0F0F0F);
		}
	},
	C3() {
		public int cross(int a, int b) {
			return (a & 0xFF00FF00) | (b & 0x00FF00FF);
		}
	},
	C4() {
		public int cross(int a, int b) {
			return (a & 0xFFF00FFF) | (b & 0x000FF000);
		}
	},
	C5() {
		public int cross(int a, int b) {
			return (a & 0xFFFFFF0F) | (b & 0x000000F0);
		}
	},
	C6() {
		public int cross(int a, int b) {
			return (a & 0xFFFFF0FF) | (b & 0x00000F00);
		}
	},
	C7() {
		public int cross(int a, int b) {
			return (a & 0xFFFF0FFF) | (b & 0x0000F000);
		}
	},
	C8() {
		public int cross(int a, int b) {
			return (a & 0xFFF0FFFF) | (b & 0x000F0000);
		}
	},
	C9() {
		public int cross(int a, int b) {
			return (a & 0xFF0FFFFF) | (b & 0x00F00000);
		}
	},
	C10() {
		public int cross(int a, int b) {
			return (a & 0xF0FFFFFF) | (b & 0x0F000000);
		}
	},
	C11() {
		public int cross(int a, int b) {
			return a | b;
		}
	},
	C12() {
		public int cross(int a, int b) {
			return ((a & 0x0FFF0000) >>> 12) | ((b & 0x0000FFF0) << 12);
		}
	},
	C13() {
		public int cross(int a, int b) {
			return a;
		}
	};

	public abstract int cross(int a, int b);

}
