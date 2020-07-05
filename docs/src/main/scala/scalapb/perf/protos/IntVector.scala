// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package scalapb.perf.protos

@SerialVersionUID(0L)
final case class IntVector(
    ints: _root_.scala.Seq[_root_.scala.Int] = _root_.scala.Seq.empty,
    unknownFields: _root_.scalapb.UnknownFieldSet = _root_.scalapb.UnknownFieldSet.empty
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[IntVector] {
    private[this] def intsSerializedSize = {
      if (__intsSerializedSizeField == 0) __intsSerializedSizeField = {
        var __s: _root_.scala.Int = 0
        ints.foreach(__i => __s += _root_.com.google.protobuf.CodedOutputStream.computeInt32SizeNoTag(__i))
        __s
      }
      __intsSerializedSizeField
    }
    @transient private[this] var __intsSerializedSizeField: _root_.scala.Int = 0
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      if(ints.nonEmpty) {
        val __localsize = intsSerializedSize
        __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__localsize) + __localsize
      }
      __size += unknownFields.serializedSize
      __size
    }
    override def serializedSize: _root_.scala.Int = {
      var read = __serializedSizeCachedValue
      if (read == 0) {
        read = __computeSerializedValue()
        __serializedSizeCachedValue = read
      }
      read
    }
    def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): _root_.scala.Unit = {
      if (ints.nonEmpty) {
        _output__.writeTag(1, 2)
        _output__.writeUInt32NoTag(intsSerializedSize)
        ints.foreach(_output__.writeInt32NoTag)
      };
      unknownFields.writeTo(_output__)
    }
    def clearInts = copy(ints = _root_.scala.Seq.empty)
    def addInts(__vs: _root_.scala.Int*): IntVector = addAllInts(__vs)
    def addAllInts(__vs: Iterable[_root_.scala.Int]): IntVector = copy(ints = ints ++ __vs)
    def withInts(__v: _root_.scala.Seq[_root_.scala.Int]): IntVector = copy(ints = __v)
    def withUnknownFields(__v: _root_.scalapb.UnknownFieldSet) = copy(unknownFields = __v)
    def discardUnknownFields = copy(unknownFields = _root_.scalapb.UnknownFieldSet.empty)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => ints
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PRepeated(ints.iterator.map(_root_.scalapb.descriptors.PInt).toVector)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)
    def companion = scalapb.perf.protos.IntVector
}

object IntVector extends scalapb.GeneratedMessageCompanion[scalapb.perf.protos.IntVector] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[scalapb.perf.protos.IntVector] = this
  def merge(`_message__`: scalapb.perf.protos.IntVector, `_input__`: _root_.com.google.protobuf.CodedInputStream): scalapb.perf.protos.IntVector = {
    val __ints = (_root_.scala.collection.immutable.Vector.newBuilder[_root_.scala.Int] ++= `_message__`.ints)
    var `_unknownFields__`: _root_.scalapb.UnknownFieldSet.Builder = null
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 8 =>
          __ints += _input__.readInt32()
        case 10 => {
          val length = _input__.readRawVarint32()
          val oldLimit = _input__.pushLimit(length)
          while (_input__.getBytesUntilLimit > 0) {
            __ints += _input__.readInt32
          }
          _input__.popLimit(oldLimit)
        }
        case tag =>
          if (_unknownFields__ == null) {
            _unknownFields__ = new _root_.scalapb.UnknownFieldSet.Builder(_message__.unknownFields)
          }
          _unknownFields__.parseField(tag, _input__)
      }
    }
    scalapb.perf.protos.IntVector(
        ints = __ints.result(),
        unknownFields = if (_unknownFields__ == null) _message__.unknownFields else _unknownFields__.result()
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[scalapb.perf.protos.IntVector] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), "FieldDescriptor does not match message type.")
      scalapb.perf.protos.IntVector(
        ints = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Seq[_root_.scala.Int]]).getOrElse(_root_.scala.Seq.empty)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = ProtosProto.javaDescriptor.getMessageTypes.get(4)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = ProtosProto.scalaDescriptor.messages(4)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__number)
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = scalapb.perf.protos.IntVector(
    ints = _root_.scala.Seq.empty
  )
  implicit class IntVectorLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, scalapb.perf.protos.IntVector]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, scalapb.perf.protos.IntVector](_l) {
    def ints: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[_root_.scala.Int]] = field(_.ints)((c_, f_) => c_.copy(ints = f_))
  }
  final val INTS_FIELD_NUMBER = 1
  def of(
    ints: _root_.scala.Seq[_root_.scala.Int]
  ): _root_.scalapb.perf.protos.IntVector = _root_.scalapb.perf.protos.IntVector(
    ints
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[scalapb.perf.IntVector])
}