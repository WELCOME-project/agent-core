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

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2020-09-11")
public class MBoxColliderProperties implements org.apache.thrift.TBase<MBoxColliderProperties, MBoxColliderProperties._Fields>, java.io.Serializable, Cloneable, Comparable<MBoxColliderProperties> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MBoxColliderProperties");

  private static final org.apache.thrift.protocol.TField SIZE_FIELD_DESC = new org.apache.thrift.protocol.TField("Size", org.apache.thrift.protocol.TType.STRUCT, (short)1);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MBoxColliderPropertiesStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MBoxColliderPropertiesTupleSchemeFactory();

  public @org.apache.thrift.annotation.Nullable de.mosim.mmi.math.MVector3 Size; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    SIZE((short)1, "Size");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // SIZE
          return SIZE;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.SIZE, new org.apache.thrift.meta_data.FieldMetaData("Size", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, de.mosim.mmi.math.MVector3.class)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MBoxColliderProperties.class, metaDataMap);
  }

  public MBoxColliderProperties() {
  }

  public MBoxColliderProperties(
    de.mosim.mmi.math.MVector3 Size)
  {
    this();
    this.Size = Size;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public MBoxColliderProperties(MBoxColliderProperties other) {
    if (other.isSetSize()) {
      this.Size = new de.mosim.mmi.math.MVector3(other.Size);
    }
  }

  public MBoxColliderProperties deepCopy() {
    return new MBoxColliderProperties(this);
  }

  @Override
  public void clear() {
    this.Size = null;
  }

  @org.apache.thrift.annotation.Nullable
  public de.mosim.mmi.math.MVector3 getSize() {
    return this.Size;
  }

  public MBoxColliderProperties setSize(@org.apache.thrift.annotation.Nullable de.mosim.mmi.math.MVector3 Size) {
    this.Size = Size;
    return this;
  }

  public void unsetSize() {
    this.Size = null;
  }

  /** Returns true if field Size is set (has been assigned a value) and false otherwise */
  public boolean isSetSize() {
    return this.Size != null;
  }

  public void setSizeIsSet(boolean value) {
    if (!value) {
      this.Size = null;
    }
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case SIZE:
      if (value == null) {
        unsetSize();
      } else {
        setSize((de.mosim.mmi.math.MVector3)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case SIZE:
      return getSize();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case SIZE:
      return isSetSize();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof MBoxColliderProperties)
      return this.equals((MBoxColliderProperties)that);
    return false;
  }

  public boolean equals(MBoxColliderProperties that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_Size = true && this.isSetSize();
    boolean that_present_Size = true && that.isSetSize();
    if (this_present_Size || that_present_Size) {
      if (!(this_present_Size && that_present_Size))
        return false;
      if (!this.Size.equals(that.Size))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetSize()) ? 131071 : 524287);
    if (isSetSize())
      hashCode = hashCode * 8191 + Size.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(MBoxColliderProperties other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetSize()).compareTo(other.isSetSize());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSize()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.Size, other.Size);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  @org.apache.thrift.annotation.Nullable
  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("MBoxColliderProperties(");
    boolean first = true;

    sb.append("Size:");
    if (this.Size == null) {
      sb.append("null");
    } else {
      sb.append(this.Size);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (Size == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'Size' was not present! Struct: " + toString());
    }
    // check for sub-struct validity
    if (Size != null) {
      Size.validate();
    }
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class MBoxColliderPropertiesStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MBoxColliderPropertiesStandardScheme getScheme() {
      return new MBoxColliderPropertiesStandardScheme();
    }
  }

  private static class MBoxColliderPropertiesStandardScheme extends org.apache.thrift.scheme.StandardScheme<MBoxColliderProperties> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, MBoxColliderProperties struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // SIZE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.Size = new de.mosim.mmi.math.MVector3();
              struct.Size.read(iprot);
              struct.setSizeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, MBoxColliderProperties struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.Size != null) {
        oprot.writeFieldBegin(SIZE_FIELD_DESC);
        struct.Size.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class MBoxColliderPropertiesTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public MBoxColliderPropertiesTupleScheme getScheme() {
      return new MBoxColliderPropertiesTupleScheme();
    }
  }

  private static class MBoxColliderPropertiesTupleScheme extends org.apache.thrift.scheme.TupleScheme<MBoxColliderProperties> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, MBoxColliderProperties struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.Size.write(oprot);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, MBoxColliderProperties struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.Size = new de.mosim.mmi.math.MVector3();
      struct.Size.read(iprot);
      struct.setSizeIsSet(true);
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

