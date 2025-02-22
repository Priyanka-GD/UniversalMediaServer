/*
 * This file is part of Universal Media Server, based on PS3 Media Server.
 *
 * This program is a free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; version 2 of the License only.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package net.pms.encoders;

import java.io.Serializable;

/**
 * This class is similar to a marker interface, it's only purpose is to ensure
 * that subclass instances only equals themselves and that {@link #toString()}
 * returns the "name" of the {@link EngineId}. This allows for an "enum-like"
 * implementation where {@code ==} can be used for comparison while it's still
 * extendible.
 *
 * @author Nadahar
 */
public abstract class EngineId implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * @return The name/textual representation of this {@link EngineId}, not the
	 *         name of the corresponding {@link Engine}.
	 */
	public abstract String getName();

	@Override
	public final boolean equals(Object object) {
		return (object instanceof EngineId && this == object);
	}

	@Override
	public final int hashCode() {
		return super.hashCode();
	}

	@Override
	public final String toString() {
		return getName();
	}
}
