package es.upm.bigdata.utils

/**
 * TODO
 */
object Constant {
  val FORBIDDEN_VARIABLES = Seq(
    "ArrTime",
    "ActualElapsedTime",
    "AirTime",
    "TaxiIn",
    "Diverted",
    "CarrierDelay",
    "WeatherDelay",
    "NASDelay",
    "SecurityDelay",
    "LateAircraftDelay"
  )

  val CATEGORICAL_VARIABLES = Seq("UniqueCarrier", "tailNum","Origin", "Dest")
  val TARGET_VARIABLE = "ArrDelay"
}
