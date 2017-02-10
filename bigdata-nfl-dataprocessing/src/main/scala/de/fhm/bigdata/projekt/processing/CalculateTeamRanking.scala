import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

import org.apache.spark.sql.hive._

import org.apache.hadoop.hbase.{HBaseConfiguration, HTableDescriptor}
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.HTable;

import org.apache.hadoop.hbase.client.{HBaseAdmin, Result}
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.CellUtil
import scala.collection.JavaConverters._
import org.apache.hadoop.hbase.KeyValue.Type

object CalculateTeamRating {
  def main(args: Array[String]) {

    val sc = new SparkContext(new SparkConf().setAppName("Calculate Team Rating"))
    
    val conf = HBaseConfiguration.create()
    val tableHashtags = "hashtags"
    
    conf.set(TableInputFormat.INPUT_TABLE, tableHashtags)

    val adminHashtags = new HBaseAdmin(conf)
    if (!adminHashtags.isTableAvailable(tableHashtags)) {
      val tableDescHashtags = new HTableDescriptor(tableHashtags)
      adminHashtags.createTable(tableDescHashtags)
    }
    val hBaseRDDHashtags = sc.newAPIHadoopRDD(conf, classOf[TableInputFormat], classOf[ImmutableBytesWritable], classOf[Result])
    //hBaseRDDHashtags.map(row => Bytes.toString(row._2.getValue("hashtag_family, hashtag".getBytes(), "hashtags_family:counter".getBytes()))).collect().foreach(println)
    
    val keyValue = hBaseRDDHashtags.map(x => x._2).map(_.list)
    
    val hashtags = hBaseRDDHashtags.map(row => Bytes.toString(row._2.getValue("hashtag_family".getBytes(), "hashtag".getBytes()))).collect().foreach(println)
    val counter = hBaseRDDHashtags.map(row => Bytes.toString(row._2.getValue("hashtag_family".getBytes(), "counter".getBytes()))).collect().foreach(println)

    //outPut is a RDD[String], in which each line represents a record in HBase
    //val outPut = keyValue.flatMap(x =>  x.asScala.map(cell =>
    //    "columnFamily=%s,qualifier=%s,timestamp=%s,type=%s,value=%s".format(
      //    Bytes.toStringBinary(CellUtil.cloneFamily(cell)),
        //  Bytes.toStringBinary(CellUtil.cloneQualifier(cell)),
          //cell.getTimestamp.toString,
         // Type.codeToType(cell.getTypeByte),
         // Bytes.toStringBinary(CellUtil.cloneValue(cell))
       // )
     // )
    //)

    //outPut.foreach(println)
    
    //System.out.println(outPut)
    
    val tableName = "teams"
    
    conf.set(TableInputFormat.INPUT_TABLE, tableName)

    val admin = new HBaseAdmin(conf)
    if (!admin.isTableAvailable(tableName)) {
      val tableDesc = new HTableDescriptor(tableName)
      admin.createTable(tableDesc)
    }

    val hBaseRDD = sc.newAPIHadoopRDD(conf, classOf[TableInputFormat], classOf[ImmutableBytesWritable], classOf[Result])
    println("Number of Records found : " + hBaseRDD.count())

    
    val test2 = hBaseRDD.map(row => Bytes.toString(row._2.getValue("team_family".getBytes(), "name".getBytes()))).collect().foreach(println)
    val synonyms = hBaseRDD.map(row => Bytes.toString(row._2.getValue("team_family".getBytes(), "synonyms".getBytes()))).collect().foreach(println)
    /**val test = hBaseRDD.flatMap(arg => {
      var str = arg.get("synonyms").toString
      //str.split(" ")
    })
    .map(word => (word, 1))
    .reduceByKey((a, b) => a + b)
    
    
    sc.stop()
    
    println("Konvertierung: " + test)
    */
   //System.out.println(test2.mkString(","))
    
    val test = hBaseRDD

    val threshold = args(1).toInt
    var timestamp = args(0).toString
    
    val wordCounts = test.map((_, 1)).reduceByKey(_ + _)
    
    System.out.println(wordCounts.collect().mkString(", "))
  
    val ranking = wordCounts.sortBy(_._2)
       
    //put data into table
    val myTable = new HTable(conf, tableName);
        wordCounts.collect().foreach(y => {
      var p = new Put(new String(timestamp).getBytes()); //hashtag und timestamp?
	  p.add("team_family".getBytes(), "name".getBytes(), 
						timestamp.getBytes());
	  p.add("team_family".getBytes(), "id".getBytes(), 
						timestamp.getBytes());
		p.add("team_family".getBytes(), "rank".getBytes(), timestamp.getBytes());
		p.add("team_family".getBytes(), "rank".getBytes(), timestamp.getBytes());
		p.add("team_family".getBytes(), "rank".getBytes(), timestamp.getBytes());
		p.add("team_family".getBytes(), "synonyms".getBytes(), timestamp.getBytes());
	  //myTable.put(p);
        } )
	myTable.flushCommits();
   
    
  }
}
