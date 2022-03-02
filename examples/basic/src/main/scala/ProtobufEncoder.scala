import com.google.protobuf.ByteString
import feast.proto.types.EntityKey.EntityKey
import feast.proto.types.Value.Value.Val
import feast.proto.types.Value.{ BoolList, BytesList, DoubleList, FloatList, Int32List, Int64List, StringList, Value }
import org.apache.spark.sql.{ Row, SparkSession }

import java.io.{ ByteArrayOutputStream, FileOutputStream }
import java.nio.{ ByteBuffer, ByteOrder }
import java.sql.{ Date, Timestamp }

object ProtobufEncoder {
  /**
   * Function serializes entity key. That is just re-implementation of feast
   * serialization procedure in Scala.
   * @param value spark row with fields, that constitute entity key
   * @return serialized key as byte array
   */
  def serializeKey(value: Row): Array[Byte] = {
    val joinKeys = value.schema.fieldNames
    val entityValues =
      value.schema.fieldNames.map { name =>
        val fieldValue = value.get(value.fieldIndex(name))
        valueToProto(fieldValue, getTypeByValue(fieldValue))
      }
    serializeStable(new EntityKey(joinKeys, entityValues))
  }

  /**
   * Function serializes feature value. That is just re-implementation of feast
   * serialization procedure in Scala.
   * @param value feature value
   * @param featureType Data type of feature
   * @return serialized feature value as byte array
   */
  def serializeValue(value: Any, featureType: String): Array[Byte] = {
    valueToProto(value, FeatureType.withName(featureType)).toByteArray
  }

  /**
   * registers scala functions as UDF in spark session
   * @param spark spark session
   */
  def registerUDF(spark: SparkSession) = {
    spark.udf.register("serializeValue", ProtobufEncoder.serializeValue _)
    spark.udf.register("serializeKey", ProtobufEncoder.serializeKey _)
  }

  private def serializeStable(entityKey: EntityKey): Array[Byte] = {
    val (sortedKeys, sortedValues) = entityKey.joinKeys.zip(entityKey.entityValues).sortBy(_._1).unzip
    val bos                        = new ByteArrayOutputStream()
    sortedKeys.foreach { k =>
      bos.write(intToBytes(FeatureType.STRING.id))
      bos.write(k.getBytes("UTF-8"))
    }
    sortedValues.foreach { kv =>
      val v = kv.`val`
      v.stringVal.foreach { str =>
        bos.write(intToBytes(FeatureType.STRING.id))
        bos.write(intToBytes(str.length))
        bos.write(str.getBytes("UTF-8"))
      }
      v.bytesVal.foreach { b =>
        bos.write(intToBytes(FeatureType.BYTES.id))
        bos.write(b.size)
        bos.write(b.toByteArray)
      }
      v.int32Val.foreach { i =>
        bos.write(intToBytes(FeatureType.INT32.id))
        bos.write(intToBytes(4))
        bos.write(i)
      }
      v.int64Val.foreach { l =>
        bos.write(intToBytes(FeatureType.INT64.id))
        bos.write(intToBytes(8))
        bos.write(longToBytes(l))
      }
    }
    bos.toByteArray
  }

  private def intToBytes(i: Int): Array[Byte] = {
    val buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
    buffer.putInt(i)
    buffer.array()
  }

  private def longToBytes(l: Long): Array[Byte] = {
    val buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
    buffer.putLong(l)
    buffer.array()
  }

  private def getTypeByValue(value: Any): FeatureType.Value = {
    import FeatureType._
    value match {
      case _: Byte                         => INT32
      case _: Short                        => INT32
      case _: Int                          => INT32
      case _: Long                         => INT64
      case _: Float                        => FLOAT
      case _: Double                       => DOUBLE
      case _: java.math.BigDecimal         => DOUBLE
      case _: String                       => STRING
      case Seq(_: Byte, _)                 => BYTES
      case _: Boolean                      => BOOL
      case _: Timestamp                    => INT64
      case _: Date                         => INT64
      case Seq(_: Short, _)                => INT32_LIST
      case Seq(_: Int, _)                  => INT32_LIST
      case Seq(_: Long, _)                 => INT64_LIST
      case Seq(_: Float, _)                => FLOAT_LIST
      case Seq(_: Double, _)               => DOUBLE_LIST
      case Seq(_: java.math.BigDecimal, _) => DOUBLE_LIST
      case Seq(_: String, _)               => STRING_LIST
      case Seq(Seq(_: Byte, _), _)         => BYTES_LIST
      case Seq(_: Boolean, _)              => BOOL_LIST
      case Seq(_: Timestamp, _)            => INT64_LIST
      case Seq(_: Date, _)                 => INT64_LIST
      case _                               => throw new IllegalArgumentException(s"Unsupported type for entity key: ${value.getClass}")
    }
  }

  private def valueToProto(value: Any, featureType: FeatureType.Value): Value = {
    import ProtobufEncoder.FeatureType._
    featureType match {
      case UNIX_TIMESTAMP      => serializeTimestamp(value)
      case UNIX_TIMESTAMP_LIST => serializeTimestampList(value)
      case INT32               => serializeInt(value)
      case INT32_LIST          => serializeIntList(value)
      case INT64               => serializeLong(value)
      case INT64_LIST          => serializeLongList(value)
      case FLOAT               => serializeFloat(value)
      case FLOAT_LIST          => serializeFloatList(value)
      case DOUBLE              => serializeDouble(value)
      case DOUBLE_LIST         => serializeDoubleList(value)
      case STRING              => serializeString(value)
      case STRING_LIST         => serializeStringList(value)
      case BOOL                => serializeBoolean(value)
      case BOOL_LIST           => serializeBooleanList(value)
      case BYTES               => serializeBytes(value)
      case BYTES_LIST          => serializeBytesList(value)
    }
  }

  private def serializeTimestamp(value: Any): Value = {
    value match {
      case ts: Timestamp =>
        Value(Val.Int64Val(ts.getTime))
      case d: Date =>
        Value(Val.Int64Val(d.getTime))
      case other =>
        throw new IllegalArgumentException(s"Expected timestamp compatible, but found: ${other.getClass}")
    }
  }

  private def serializeTimestampList(value: Any): Value = {
    value match {
      case Seq() =>
        Value(Val.Int64ListVal(Int64List(Seq.empty)))
      case e @ Seq(_: Timestamp, _) =>
        Value(Val.Int64ListVal(Int64List(e.asInstanceOf[Seq[Timestamp]].map(_.getTime()))))
      case e @ Seq(_: Date, _) =>
        Value(Val.Int64ListVal(Int64List(e.asInstanceOf[Seq[Date]].map(_.getTime()))))
      case other =>
        throw new IllegalArgumentException(s"Expected timestamp list compatible, but found: ${other.getClass}")
    }
  }

  private def serializeLong(value: Any): Value = {
    value match {
      case b: Byte =>
        Value(Val.Int64Val(b))
      case s: Short =>
        Value(Val.Int64Val(s))
      case i: Int =>
        Value(Val.Int64Val(i))
      case l: Long =>
        Value(Val.Int64Val(l))
      case other =>
        throw new IllegalArgumentException(s"Expected long compatible, but found: ${other.getClass}")
    }
  }

  private def serializeLongList(value: Any): Value = {
    value match {
      case Seq() =>
        Value(Val.Int64ListVal(Int64List(Seq.empty)))
      case e @ Seq(_: Byte, _) =>
        Value(Val.Int64ListVal(Int64List(e.asInstanceOf[Seq[Byte]].map(_.longValue()))))
      case e @ Seq(_: Short, _) =>
        Value(Val.Int64ListVal(Int64List(e.asInstanceOf[Seq[Short]].map(_.longValue()))))
      case e @ Seq(_: Int, _) =>
        Value(Val.Int64ListVal(Int64List(e.asInstanceOf[Seq[Int]].map(_.longValue()))))
      case e @ Seq(_: Long, _) =>
        Value(Val.Int64ListVal(Int64List(e.asInstanceOf[Seq[Long]])))
      case other =>
        throw new IllegalArgumentException(s"Expected long list compatible, but found: ${other.getClass}")
    }
  }

  private def serializeInt(value: Any): Value = {
    value match {
      case b: Byte =>
        Value(Val.Int32Val(b))
      case s: Short =>
        Value(Val.Int32Val(s))
      case i: Int =>
        Value(Val.Int32Val(i))
      case other =>
        throw new IllegalArgumentException(s"Expected int compatible, but found: ${other.getClass}")
    }
  }

  private def serializeIntList(value: Any): Value = {
    value match {
      case Seq() =>
        Value(Val.Int32ListVal(Int32List(Seq.empty)))
      case e @ Seq(_: Byte, _) =>
        Value(Val.Int32ListVal(Int32List(e.asInstanceOf[Seq[Byte]].map(_.intValue()))))
      case e @ Seq(_: Short, _) =>
        Value(Val.Int32ListVal(Int32List(e.asInstanceOf[Seq[Short]].map(_.intValue()))))
      case e @ Seq(_: Int, _) =>
        Value(Val.Int32ListVal(Int32List(e.asInstanceOf[Seq[Int]])))
      case other =>
        throw new IllegalArgumentException(s"Expected int list compatible, but found: ${other.getClass}")
    }
  }

  private def serializeDouble(value: Any): Value = {
    value match {
      case f: Float =>
        Value(Val.DoubleVal(f))
      case d: Double =>
        Value(Val.DoubleVal(d))
      case dc: java.math.BigDecimal =>
        Value(Val.DoubleVal(dc.doubleValue()))
      case other =>
        throw new IllegalArgumentException(s"Expected double compatible, but found: ${other.getClass}")
    }
  }

  private def serializeDoubleList(value: Any): Value = {
    value match {
      case Seq() =>
        Value(Val.DoubleListVal(DoubleList(Seq.empty)))
      case e @ Seq(_: Float, _) =>
        Value(Val.DoubleListVal(DoubleList(e.asInstanceOf[Seq[Float]].map(_.doubleValue()))))
      case e @ Seq(_: Double, _) =>
        Value(Val.DoubleListVal(DoubleList(e.asInstanceOf[Seq[Double]])))
      case e @ Seq(_: BigDecimal, _) =>
        Value(Val.DoubleListVal(DoubleList(e.asInstanceOf[Seq[BigDecimal]].map(_.doubleValue()))))
      case other =>
        throw new IllegalArgumentException(s"Expected double list compatible, but found: ${other.getClass}")
    }
  }

  private def serializeFloat(value: Any): Value = {
    value match {
      case f: Float =>
        Value(Val.FloatVal(f))
      case dc: java.math.BigDecimal =>
        Value(Val.FloatVal(dc.floatValue()))
      case other =>
        throw new IllegalArgumentException(s"Expected float compatible, but found: ${other.getClass}")
    }
  }

  private def serializeFloatList(value: Any): Value = {
    value match {
      case Seq() =>
        Value(Val.FloatListVal(FloatList(Seq.empty)))
      case e @ Seq(_: Float, _) =>
        Value(Val.FloatListVal(FloatList(e.asInstanceOf[Seq[Float]])))
      case e @ Seq(_: BigDecimal, _) =>
        Value(Val.FloatListVal(FloatList(e.asInstanceOf[Seq[BigDecimal]].map(_.floatValue()))))
      case other =>
        throw new IllegalArgumentException(s"Expected float list compatible, but found: ${other.getClass}")
    }
  }

  private def serializeString(value: Any): Value = {
    value match {
      case s: String =>
        Value(Val.StringVal(s))
      case other =>
        throw new IllegalArgumentException(s"Expected string compatible, but found: ${other.getClass}")
    }
  }

  private def serializeStringList(value: Any): Value = {
    value match {
      case Seq() =>
        Value(Val.StringListVal(StringList(Seq.empty)))
      case e @ Seq(_: String, _) =>
        Value(Val.StringListVal(StringList(e.asInstanceOf[Seq[String]])))
      case other =>
        throw new IllegalArgumentException(s"Expected string list compatible, but found: ${other.getClass}")
    }
  }

  private def serializeBoolean(value: Any): Value = {
    value match {
      case b: Boolean =>
        Value(Val.BoolVal(b))
      case other =>
        throw new IllegalArgumentException(s"Expected boolean compatible, but found: ${other.getClass}")
    }
  }

  private def serializeBooleanList(value: Any): Value = {
    value match {
      case Seq() =>
        Value(Val.BoolListVal(BoolList(Seq.empty)))
      case e @ Seq(_: Boolean, _) =>
        Value(Val.BoolListVal(BoolList(e.asInstanceOf[Seq[Boolean]])))
      case other =>
        throw new IllegalArgumentException(s"Expected boolean list compatible, but found: ${other.getClass}")
    }
  }

  private def serializeBytes(value: Any): Value = {
    value match {
      case Seq() =>
        Value(Val.BytesVal(ByteString.EMPTY))
      case b @ Seq(_: Byte, _) =>
        Value(Val.BytesVal(ByteString.copyFrom(b.asInstanceOf[Seq[Byte]].toArray)))
      case other =>
        throw new IllegalArgumentException(s"Expected bytes sequence compatible, but found: ${other.getClass}")
    }
  }

  private def serializeBytesList(value: Any): Value = {
    value match {
      case Seq() =>
        Value(Val.BytesListVal(BytesList(Seq.empty)))
      case b @ Seq(e1, _) =>
        e1 match {
          case Seq(_: Byte, _) =>
            Value(Val.BytesListVal(BytesList(b.asInstanceOf[Seq[Seq[Byte]]].map(a => ByteString.copyFrom(a.toArray)))))
          case other =>
            throw new IllegalArgumentException(s"Expected bytes sequences compatible, but found: ${other.getClass}")
        }
      case other =>
        throw new IllegalArgumentException(s"Expected bytes sequences compatible, but found: ${other.getClass}")
    }
  }
  private object FeatureType extends Enumeration {
    type FeatureType = Value
    val UNKNOWN, BYTES, STRING, INT32, INT64, DOUBLE, FLOAT, BOOL, UNIX_TIMESTAMP, BYTES_LIST, STRING_LIST, INT32_LIST,
      INT64_LIST, DOUBLE_LIST, FLOAT_LIST, BOOL_LIST, UNIX_TIMESTAMP_LIST, NULL = Value
  }

  def main(args: Array[String]): Unit = {
    val entity =
      new EntityKey(Seq("driver_id", "entity_id"), Seq(Value(Val.Int64Val(1004)), Value(Val.StringVal("Hello"))))
    val fos = new FileOutputStream("""C:\tmp\sparkserialie.bin""")
    fos.write(serializeStable(entity))
    fos.close()
  }
}
