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


class Spec {

	static class SpecBuilder {

		private int numberOfSets;
		private int minArraySizeInBytes;
		private int blockSizeInBytes;
		private final String description;

		public SpecBuilder(String description) {
			this.description = description;
		}

		public Spec build() {
			if (numberOfSets != 0 && blockSizeInBytes != 0 && minArraySizeInBytes != 0) {
				return new Spec(description, numberOfSets, blockSizeInBytes, minArraySizeInBytes);
			}
			throw new IllegalStateException("Builder not fully initialized");
		}

		public SpecBuilder numberOfSets(int numberOfSets) {
			this.numberOfSets = numberOfSets;
			return this;
		}

		public SpecBuilder blockSizeInBytes(int blockSizeInBytes) {
			this.blockSizeInBytes = blockSizeInBytes;
			return this;
		}

		public SpecBuilder minArraySizeInBytes(int minArraySizeInBytes) {
			this.minArraySizeInBytes = minArraySizeInBytes;
			return this;
		}
	}

	public final int numberOfSets;
	public final int minArraySizeInBytes;
	public final int blockSizeInBytes;
	public final String description;

	public static SpecBuilder of(String description) {
		return new SpecBuilder(description);
	}

	private Spec(String description, int numberOfSets, int blockSizeInBytes, int minArraySizeInBytes) {
		this.description = description;
		this.numberOfSets = numberOfSets;
		this.blockSizeInBytes = blockSizeInBytes;
		this.minArraySizeInBytes = minArraySizeInBytes;
	}
}