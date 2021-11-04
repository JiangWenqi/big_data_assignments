package es.upm.bigdata.enums

import org.apache.spark.sql.Row

case class OnTimeData(
                       arrTime: String, //actual arrival time (local, hhmm)
                       actualElapsedTime: Int, // in minutes
                       airTime: Int, // in minutes
                       taxiIn: Int, // taxi in time, in minutes
                       diverted: Int, // 1 = yes, 0 = no
                       carrierDelay: Int, // in minutes
                       weatherDelay: Int, // in minutes
                       NASDelay: Int, // in minutes
                       securityDelay: Int, // in minutes
                       lateAircraftDelay: Int, // in minutes
                       arrDelay: Int // arrival delay, in minutes
                     )

object OnTimeData {
  def apply(row: Row): Option[OnTimeData] = {
    try {
      val arrTimeIndex = row.fieldIndex("ArrTime")
      val arrTime = row.getString(arrTimeIndex)

      val actualElapsedTimeIndex = row.fieldIndex("ActualElapsedTime")
      val actualElapsedTime = row.getString(actualElapsedTimeIndex).toInt

      val airTimeIndex = row.fieldIndex("AirTime")
      val airTime = row.getString(airTimeIndex).toInt

      val taxiInIndex = row.fieldIndex("TaxiIn")
      val taxiIn = row.getString(taxiInIndex).toInt

      val divertedIndex = row.fieldIndex("Diverted")
      val diverted = row.getString(divertedIndex).toInt

      val carrierDelayIndex = row.fieldIndex("CarrierDelay")
      val carrierDelay = row.getString(carrierDelayIndex).toInt

      val weatherDelayIndex = row.fieldIndex("WeatherDelay")
      val weatherDelay = row.getString(weatherDelayIndex).toInt

      val NASDelayIndex = row.fieldIndex("NASDelay")
      val NASDelay = row.getString(NASDelayIndex).toInt

      val securityDelayIndex = row.fieldIndex("SecurityDelay")
      val securityDelay = row.getString(securityDelayIndex).toInt

      val lateAircraftDelayIndex = row.fieldIndex("LateAircraftDelay")
      val lateAircraftDelay = row.getString(lateAircraftDelayIndex).toInt

      val arrDelayIndex = row.fieldIndex("ArrDelay")
      val arrDelay = row.getString(arrDelayIndex).toInt

      Some(OnTimeData(arrTime, actualElapsedTime, airTime, taxiIn, diverted, carrierDelay, weatherDelay, NASDelay, securityDelay, lateAircraftDelay, arrDelay))
    } catch {
      case _: Exception => None
    }


  }

}