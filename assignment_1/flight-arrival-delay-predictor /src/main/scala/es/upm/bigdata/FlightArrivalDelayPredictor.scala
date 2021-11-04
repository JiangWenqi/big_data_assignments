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

    val raw_data_path = "file:///Users/vinci/BooksAndResources/DataScience/BigData/big_data_assignment_1/2007.csv"
    val on_time_data: Dataset[OnTimeData] = spark.read
      .format("csv")
      .option("header", "true")
      .load(raw_data_path)
      .select(
        $"ArrTime",
        $"ActualElapsedTime",
        $"AirTime",
        $"TaxiIn",
        $"Diverted",
        $"CarrierDelay",
        $"WeatherDelay",
        $"NASDelay",
        $"SecurityDelay",
        $"LateAircraftDelay",
        $"ArrDelay"
      ).flatMap(OnTimeData(_))



    on_time_data.sample(withReplacement = false, 0.001).show(300,truncate = false)
    spark.stop()
  }

}
