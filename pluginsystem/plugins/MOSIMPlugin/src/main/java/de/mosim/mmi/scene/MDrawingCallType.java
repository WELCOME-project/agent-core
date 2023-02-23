/*
 * Copyright (C) 2020 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package de.mosim.mmi.scene;


@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2020-09-11")
public enum MDrawingCallType implements org.apache.thrift.TEnum {
  DrawLine2D(0),
  DrawLine3D(1),
  DrawPoint2D(2),
  DrawPoint3D(3),
  DrawText(4),
  Custom(5);

  private final int value;

  private MDrawingCallType(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  @org.apache.thrift.annotation.Nullable
  public static MDrawingCallType findByValue(int value) { 
    switch (value) {
      case 0:
        return DrawLine2D;
      case 1:
        return DrawLine3D;
      case 2:
        return DrawPoint2D;
      case 3:
        return DrawPoint3D;
      case 4:
        return DrawText;
      case 5:
        return Custom;
      default:
        return null;
    }
  }
}
