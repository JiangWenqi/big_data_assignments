package es.upm.bigdata

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.mean
import org.junit.Assert._
import org.junit._

@Test
class FlightArrivalDelayPredictorTest {

  @Test
  def testOK(): Unit = assertTrue(true)

  val spark: SparkSession = SparkSession.builder
    .master("local[6]")
    .appName("Flight Arrival Delay Predictor")
    .config("spark.driver.memory", "14g")
    //      .config("spark.executor.memory", "2g")
    .config("spark.dynamicAllocation.maxExecutors", 10)
    .config("spark.debug.maxToStringFields", 512)
    .config("spark.sql.debug.maxToStringFields", 1024)
    .getOrCreate()
  //    @Test
  //    def testKO() = assertTrue(false)

  import org.apache.spark.ml.clustering.KMeans
  import org.apache.spark.ml.feature.VectorAssembler

  var df = spark.read
    .option("header", "true")
    .option("sep", ";")
    .csv("hdfs://tmp/coordinates.csv")
  df = df
    .select(df("id"), df("lat"), df("lon").cast("double").as("lon"))

  val assembler = new VectorAssembler()
    .setInputCols(Array("lat", "lon"))
    .setOutputCol("features")
  val dataset = assembler.transform(df)
  val kmeans = new KMeans().setFeaturesCol("features").setK(2)
  // fit
  val model = kmeans.fit(dataset)

  var predDf = model
    .transform(dataset)
    .select("id", "lat", "lon")
    .withColumnRenamed("prediction", "cluster")

  predDf.createOrReplaceTempView("cluster")
  val predScoreDf = spark
    .sql("select * from cluster join score on cluster.id == score.id")
  predScoreDf
    .groupBy("cluster")
    .agg(mean("score").as("score"))
    .show
}


