package es.upm.bigdata

import es.upm.bigdata.enums.OnTimeData
import org.apache.spark.sql.{Dataset, SparkSession}

/**
 * @author Wenqi Jiang,
 */
object FlightArrivalDelayPredictor {


  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("Flight Arrival Delay Predictor")
      .getOrCreate()
    import spark.implicits._

    val rawDataPath = "file:///Users/vinci/BooksAndResources/DataScience/BigData/big_data_assignment_1/*.csv"
    val rawData = spark.read
      .format("csv")
      .option("header", "true")
      .load(rawDataPath)

    val onTimeData = rawData.map(OnTimeData(_))

    onTimeData.sample(withReplacement = false, 0.00001).show(300, truncate = false)
    spark.stop()
  }

}
