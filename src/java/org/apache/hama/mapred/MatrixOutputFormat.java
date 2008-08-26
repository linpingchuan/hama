package org.apache.hama.mapred;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.io.BatchUpdate;
import org.apache.hadoop.hbase.mapred.TableOutputFormat;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapred.FileAlreadyExistsException;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.InvalidJobConfException;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.Progressable;

public class MatrixOutputFormat extends
    FileOutputFormat<IntWritable, BatchUpdate> {

  /** JobConf parameter that specifies the output table */
  public static final String OUTPUT_TABLE = "hbase.mapred.outputtable";
  private final Log LOG = LogFactory.getLog(TableOutputFormat.class);

  /**
   * Convert Reduce output (key, value) to (HStoreKey, KeyedDataArrayWritable)
   * and write to an HBase table
   */
  protected static class TableRecordWriter implements
      RecordWriter<IntWritable, BatchUpdate> {
    private HTable m_table;

    /**
     * Instantiate a TableRecordWriter with the HBase HClient for writing.
     * 
     * @param table
     */
    public TableRecordWriter(HTable table) {
      m_table = table;
    }

    /** {@inheritDoc} */
    public void close(@SuppressWarnings("unused")
    Reporter reporter) {
      // Nothing to do.
    }

    /** {@inheritDoc} */
    public void write(@SuppressWarnings("unused")
    IntWritable key, BatchUpdate value) throws IOException {
      m_table.commit(value);
    }
  }

  /** {@inheritDoc} */
  @Override
  @SuppressWarnings("unchecked")
  public RecordWriter getRecordWriter(@SuppressWarnings("unused")
  FileSystem ignored, JobConf job, @SuppressWarnings("unused")
  String name, @SuppressWarnings("unused")
  Progressable progress) throws IOException {

    // expecting exactly one path

    String tableName = job.get(OUTPUT_TABLE);
    HTable table = null;
    try {
      table = new HTable(new HBaseConfiguration(job), tableName);
    } catch (IOException e) {
      LOG.error(e);
      throw e;
    }
    return new TableRecordWriter(table);
  }

  /** {@inheritDoc} */
  @Override
  @SuppressWarnings("unused")
  public void checkOutputSpecs(FileSystem ignored, JobConf job)
      throws FileAlreadyExistsException, InvalidJobConfException, IOException {

    String tableName = job.get(OUTPUT_TABLE);
    if (tableName == null) {
      throw new IOException("Must specify table name");
    }
  }
}