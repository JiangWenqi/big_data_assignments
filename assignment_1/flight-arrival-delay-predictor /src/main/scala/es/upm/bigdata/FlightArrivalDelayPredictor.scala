package es.upm.bigdata

import scala.math.random

import org.apache.spark.sql.SparkSession

/**
 * @author Wenqi Jiang,
 */
object FlightArrivalDelayPredictor {


  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("Spark Pi")
      .getOrCreate()
    val slices = 4
    val max = 100000 * slices // avoid overflow
    val numbers = Seq.range(1, max)
    val count = spark.sparkContext.parallelize(numbers, slices).map { _ =>
      val x = random * 2 - 1
      val y = random * 2 - 1
      if (x * x + y * y <= 1) 1 else 0
    }.reduce(_ + _)
    println(s"Pi is roughly ${4.0 * count / (max - 1)}")
    spark.stop()
  }

}
