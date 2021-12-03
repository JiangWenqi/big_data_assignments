package es.upm.bigdata

import es.upm.bigdata.enums.CleanedFlightRecord
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{OneHotEncoder, StringIndexer, VectorAssembler}
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

/**
 * @author Wenqi Jiang,
 */
object FlightArrivalDelayPredictor {
  val RAW_DATA_PATH = "file:///Users/vinci/BooksAndResources/DataScience/BigData/big_data_assignment_1/*.csv"

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("Flight Arrival Delay Predictor")
      .getOrCreate()

    import spark.implicits._

    // load raw data
    val rawData = spark.read.format("csv")
      .option("header", "true")
      .load(RAW_DATA_PATH)

    val cleanedRecords = rawData
      .filter($"Cancelled".eqNullSafe(0)) // filter some flights were cancelled
      .flatMap(CleanedFlightRecord(_))
      .cache()

    val formattedRecords = cleanedRecords.join(
      // bins / Discretization 0. >150,000, 1. 50,000-150,000, 2. 25,000-49,999, 3. <25,000
      cleanedRecords.groupBy($"origin")
        .agg(count($"origin").as("countOfOrigin"))
        .withColumn(
          "sizeOfOrigin",
          when($"countOfOrigin".gt(150000), 0)
            .when($"countOfOrigin".between(50000, 150000), 1)
            .when($"countOfOrigin".between(25000, 49999), 2)
            .when($"countOfOrigin".lt(25000), 3)
            .otherwise(-1)
        ),
      Seq("origin"),
      "inner"
    )

    // training and test data
    val Array(training, test) = formattedRecords.randomSplit(Array(0.8, 0.2))

    // string indexer for unique_carrier
    val indexer = new StringIndexer().setInputCol("uniqueCarrier").setOutputCol("uniqueCarrierIndexer")

    // categories -> one hot
    val oneHot = new OneHotEncoder()
      .setInputCols(Array("crsDepTime", "crsArrTime", "distance", "sizeOfOrigin", "uniqueCarrierIndexer"))
      .setOutputCols(Array("crsDepTimeCode", "crsArrTimeCode", "distanceCode", "sizeOfOriginCode", "uniqueCarrierCode"))


    val vector = new VectorAssembler()
      .setInputCols(Array("month", "dayOfWeek", "depDelay", "taxiOut", "crsDepTimeCode", "crsArrTimeCode", "distanceCode", "sizeOfOriginCode", "uniqueCarrierCode"))
      .setOutputCol("features")

    val linear = new LinearRegression()
      .setFeaturesCol("features")
      .setLabelCol("arrDelay")
      .setPredictionCol("prediction")
      .setMaxIter(10)
      .setElasticNetParam(0.8)


    val pipeline = new Pipeline().setStages(Array(indexer, oneHot, vector, linear))

    val linearModel = pipeline.fit(training)

    val predictions = linearModel.transform(test)

    predictions.select($"arrDelay", $"prediction")
      .sample(withReplacement = false, 0.0001)
      .show(300, truncate = false)


    val evaluator = new RegressionEvaluator()
      .setLabelCol("arrDelay")
      .setPredictionCol("prediction")
      .setMetricName("rmse")

    val rmse = evaluator.evaluate(predictions)
    println(s"Root Mean Squared Error (RMSE) on test data = $rmse")

    cleanedRecords.unpersist()
    spark.stop()
  }

}
