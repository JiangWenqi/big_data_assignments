package es.upm.bigdata

import es.upm.bigdata.enums.CleanedFlightRecord
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{OneHotEncoder, StringIndexer, VectorAssembler}
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.tuning.{ParamGridBuilder, TrainValidationSplit}
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
    // clean data
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
      .setInputCols(Array("month", "dayOfWeek", "crsDepTime", "crsArrTime", "distance", "sizeOfOrigin", "uniqueCarrierIndexer"))
      .setOutputCols(Array("monthCode", "dayOfWeekCode", "crsDepTimeCode", "crsArrTimeCode", "distanceCode", "sizeOfOriginCode", "uniqueCarrierCode"))


    val vector = new VectorAssembler()
      .setInputCols(Array("monthCode", "dayOfWeekCode", "depDelay", "taxiOut", "crsDepTimeCode", "crsArrTimeCode", "distanceCode", "sizeOfOriginCode", "uniqueCarrierCode"))
      .setOutputCol("features")

    val linear = new LinearRegression()
      .setFeaturesCol("features")
      .setLabelCol("arrDelay")
      .setPredictionCol("prediction")

    val pipeline = new Pipeline().setStages(Array(indexer, oneHot, vector, linear))

    val paramGrid = new ParamGridBuilder()
      .addGrid(linear.maxIter, Array(10, 50, 100))
      .addGrid(linear.regParam, Array(0.3, 0.1, 0.01))
      .build()

    val evaluator = new RegressionEvaluator()
      .setLabelCol("arrDelay")
      .setPredictionCol("prediction")
      .setMetricName("r2")

    val trainValidationSplit = new TrainValidationSplit()
      .setEstimator(pipeline)
      .setEstimatorParamMaps(paramGrid)
      .setEvaluator(evaluator)
      // 80% of the data will be used for training and the remaining 20% for validation.
      .setTrainRatio(0.8)
      // Evaluate up to 2 parameter settings in parallel
      .setParallelism(2)

    val linearModel = trainValidationSplit.fit(training).bestModel
    val prediction = linearModel.transform(test)

    val testEvaluator = new RegressionEvaluator()
      .setLabelCol("arrDelay")
      .setPredictionCol("prediction")
      .setMetricName("r2")
    val r2 = testEvaluator.evaluate(prediction)
    printf(s"R2:$r2")
    cleanedRecords.unpersist()
    spark.stop()
  }

}
