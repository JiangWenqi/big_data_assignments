package es.upm.bigdata

import es.upm.bigdata.enums.CleanedFlightRecord
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{OneHotEncoder, StringIndexer, VectorAssembler}
import org.apache.spark.ml.regression.{GeneralizedLinearRegression, LinearRegression}
import org.apache.spark.ml.tuning.{ParamGridBuilder, TrainValidationSplit}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._


/**
 * @author Wenqi Jiang,
 */
object FlightArrivalDelayPredictor {
  val RAW_DATA_PATH = "file:///Users/vinci/BooksAndResources/DataScience/BigData/big_data_assignment_1/2000.csv"

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
      .config("spark.driver.memory", "12g")
//      .config("spark.executor.memory", "2g")
      .master("local[8]")
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
      .cache()

    // training and test data
    val Array(training, modelTest, test) = formattedRecords.randomSplit(Array(0.7, 0.15, 0.15))

    // string indexer for unique_carrier
    val indexer = new StringIndexer()
      .setInputCols(Array(
        "month",
        "dayOfWeek",
        "uniqueCarrier",
        "crsDepTime",
        "crsArrTime",
        "distance",
        "sizeOfOrigin")
      )
      .setOutputCols(Array(
        "monthIndexer",
        "dayOfWeekIndexer",
        "uniqueCarrierIndexer",
        "crsDepTimeIndexer",
        "crsArrTimeIndexer",
        "distanceIndexer",
        "sizeOfOriginIndexer"
      ))

    // categories -> one hot
    val oneHot = new OneHotEncoder()
      .setInputCols(Array(
        "monthIndexer",
        "dayOfWeekIndexer",
        "uniqueCarrierIndexer",
        "crsDepTimeIndexer",
        "crsArrTimeIndexer",
        "distanceIndexer",
        "sizeOfOriginIndexer"
      ))
      .setOutputCols(Array(
        "monthCode",
        "dayOfWeekCode",
        "uniqueCarrierCode",
        "crsDepTimeCode",
        "crsArrTimeCode",
        "distanceCode",
        "sizeOfOriginCode"
      ))


    val vector = new VectorAssembler()
      .setInputCols(
        Array(
          "depDelay",
          "taxiOut",
          "monthCode",
          "dayOfWeekCode",
          "uniqueCarrierCode",
          "crsDepTimeCode",
          "crsArrTimeCode",
          "distanceCode",
          "sizeOfOriginCode"
        )
      )
      .setOutputCol("features")

    val rmseEvaluator = new RegressionEvaluator()
      .setLabelCol("arrDelay")
      .setPredictionCol("prediction")
      .setMetricName("rmse")

    val r2Evaluator = new RegressionEvaluator()
      .setLabelCol("arrDelay")
      .setPredictionCol("prediction")
      .setMetricName("r2")
    // ------------------------ linear regression -----------------------
    val linear = new LinearRegression()
      .setFeaturesCol("features")
      .setLabelCol("arrDelay")
      .setPredictionCol("prediction")

    val linearParamGrid = new ParamGridBuilder()
      .addGrid(linear.maxIter, Array(25, 100))
      .addGrid(linear.regParam, Array(0.1, 0.01, 0.001))
      .build()

    val linearPipeline = new Pipeline().setStages(Array(indexer, oneHot, vector, linear))

    val linearValidation = new TrainValidationSplit()
      .setEstimator(linearPipeline)
      .setEstimatorParamMaps(linearParamGrid)
      .setEvaluator(rmseEvaluator)
      .setTrainRatio(0.8)
      .setParallelism(4)

    val linearModel = linearValidation.fit(training).bestModel

    // ----------------- GeneralizedLinearRegression ----------------------------------

    val generalizedLinear = new GeneralizedLinearRegression()
      .setFamily("gaussian")
      .setLink("identity")
      .setFeaturesCol("features")
      .setLabelCol("arrDelay")
      .setPredictionCol("prediction")

    val generalizedLinearParamGrid = new ParamGridBuilder()
      .addGrid(generalizedLinear.maxIter, Array(25, 100))
      .addGrid(generalizedLinear.regParam, Array(0.1, 0.01, 0.001))
      .build()

    val generalizedLinearPipeline = new Pipeline()
      .setStages(Array(indexer, oneHot, vector, generalizedLinear))

    val generalizedLinearValidation = new TrainValidationSplit()
      .setEstimator(generalizedLinearPipeline)
      .setEstimatorParamMaps(generalizedLinearParamGrid)
      .setEvaluator(rmseEvaluator)
      .setTrainRatio(0.8)
      .setParallelism(4)

    val generalizedLinearModel = generalizedLinearValidation.fit(training).bestModel

    // --------------------------- chose best model----------------------------------------------------
    val linearPrediction = linearModel.transform(modelTest)
    val generalizedLinearPrediction = generalizedLinearModel.transform(modelTest)

    val linearR2 = r2Evaluator.evaluate(linearPrediction)
    val linearRMSE = rmseEvaluator.evaluate(linearPrediction)
    println("--------------------- linear model Metric ------------------- ")
    println(s"--------------------- R2: $linearR2 ------------------- ")
    println(s"--------------------- RMSE: $linearRMSE ------------------- ")
    val generalizedLinearR2 = r2Evaluator.evaluate(generalizedLinearPrediction)
    val generalizedLinearRMSE = rmseEvaluator.evaluate(generalizedLinearPrediction)
    println("--------------------- generalized Linear model Metric ------------------- ")
    println(s"--------------------- R2: $generalizedLinearR2 ------------------- ")
    println(s"--------------------- RMSE: $generalizedLinearRMSE ------------------- ")


    formattedRecords.unpersist()
    spark.stop()
  }

}
