package io.rml.framework.flink.source

import io.rml.framework.core.item.{EmptyItem, Item}
import io.rml.framework.core.item.csv.CSVItem
import org.apache.flink.api.common.io.GenericInputFormat
import org.apache.flink.core.io.GenericInputSplit
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.column.page.PageReadStore
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter
import org.apache.parquet.hadoop.ParquetFileReader
import org.apache.parquet.hadoop.util.HadoopInputFile
import org.apache.parquet.io.{ColumnIOFactory, ParquetDecodingException}
import org.apache.parquet.schema.{MessageType, Type}

import scala.collection.JavaConverters._
import scala.collection.mutable


class ParquetInputFormat(path: String) extends GenericInputFormat[Item] {
  private var reader: ParquetFileReader = _
  private var schema: MessageType = _
  private var fields: mutable.Buffer[Type] = _

  private var rowGroupCurrentIndex = 0
  private var rowGroupTotal : Int = _
  private var currentGroup: PageReadStore = _
  private var groupReader: GroupReader = _
  private var empty = false
  override def open(inputSplit: GenericInputSplit): Unit = {
    super.open(inputSplit)

    // bootstrap the required fields
    this.reader = ParquetFileReader.open(HadoopInputFile.fromPath(new Path(path), new Configuration()))
    this.schema = reader.getFooter.getFileMetaData.getSchema
    this.fields = schema.getFields.asScala
    this.rowGroupTotal = reader.getRowGroups.size()
    try {
      this.currentGroup = reader.readNextRowGroup()
      this.groupReader = new GroupReader(currentGroup)
    } catch {
      case _:RuntimeException =>
        this.empty = true
    }
  }
  override def reachedEnd(): Boolean = rowGroupCurrentIndex == rowGroupTotal || empty

  override def nextRecord(ot: Item): Item = {
    if (this.groupReader == null) {
      new EmptyItem
    } else {
      if (this.groupReader.hasNext) {
        this.groupReader.next()
      } else {
        try {
          this.currentGroup = reader.readNextRowGroup()
          this.rowGroupCurrentIndex += 1
          this.groupReader = new GroupReader(currentGroup)
          this.groupReader.next()
        } catch {
          // the group has been fully read
          case _: RuntimeException =>
            new EmptyItem()
        }
      }
    }
  }

  /**
   * Reads page and a
   * @param rowGroup
   */
  private class GroupReader(private val rowGroup: PageReadStore) extends Iterator[Item] {
    private val columnIO = new ColumnIOFactory().getColumnIO(schema)
    private val recordReader = columnIO.getRecordReader(rowGroup, new GroupRecordConverter(schema))
    private var currentGroup = recordReader.read()
    override def hasNext: Boolean = currentGroup != null

    override def next(): Item = {
      // convert the Group into a CSVItem instance
      val map = mutable.HashMap[String, String]()
      for (i <- fields.indices) {
        val value = currentGroup.getValueToString(i, 0)
        map.put(fields(i).getName, value)
      }
      try {
        currentGroup = recordReader.read()
      } catch {
        case _:ParquetDecodingException => currentGroup = null // exception thrown due to the group being fully read
      }

      new CSVItem(map.toMap, "")
    }
  }
}
