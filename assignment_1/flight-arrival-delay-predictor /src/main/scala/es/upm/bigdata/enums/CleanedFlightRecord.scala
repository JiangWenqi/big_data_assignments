package es.upm.bigdata.enums

import org.apache.spark.sql.Row

case class CleanedFlightRecord(
                                  month: Int,
                                  dayOfWeek: Int,
                                  crsDepTime: Int,
                                  crsArrTime: Int,
                                  uniqueCarrier: String,
                                  depDelay: Int,
                                  origin: String,
                                  distance: Int,
                                  taxiOut: Int,
                                  arrDelay: Int
                                )

object CleanedFlightRecord {

  /**
   * format raw data
   * @param row raw record
   * @return formatted record
   */
  def apply(row: Row): Option[CleanedFlightRecord] = {
    try {

      // 12-Jan
      val monthIndex = row.fieldIndex("Month")
      // 1 (Monday) - 7 (Sunday)
      val dayOfWeekIndex = row.fieldIndex("DayOfWeek")
      // scheduled departure time (local, hhmm)
      val crsDepTimeIndex = row.fieldIndex("CRSDepTime")
      // scheduled arrival time (local, hhmm)
      val crsArrTimeIndex = row.fieldIndex("CRSArrTime")
      // unique carrier code
      val uniqueCarrierIndex = row.fieldIndex("UniqueCarrier")
      // departure delay, in minutes
      val depDelayIndex = row.fieldIndex("DepDelay")
      // origin IATA airport code
      val originIndex = row.fieldIndex("Origin")
      // in miles
      val distanceIndex = row.fieldIndex("Distance")
      // taxi out time in minutes
      val taxiOutIndex = row.fieldIndex("TaxiOut")
      // arrival delay, in minutes
      val arrDelayIndex = row.fieldIndex("ArrDelay")

      val month = row.getString(monthIndex).toInt
      val dayOfWeek = row.getString(dayOfWeekIndex).toInt
      val crsDepTime = getTimeInterval(row.getString(crsDepTimeIndex).toInt)
      val crsArrTime = getTimeInterval(row.getString(crsArrTimeIndex).toInt)
      val uniqueCarrier = row.getString(uniqueCarrierIndex)
      val depDelay = row.getString(depDelayIndex).toInt
      val origin = row.getString(originIndex)
      val distance = getDistanceRange(row.getString(distanceIndex).toInt)
      val taxiOut = row.getString(taxiOutIndex).toInt
      val arrDelay = row.getString(arrDelayIndex).toInt

      Some(new CleanedFlightRecord(month, dayOfWeek, crsDepTime, crsArrTime, uniqueCarrier, depDelay, origin, distance, taxiOut, arrDelay))

    } catch {
      case _: Exception => None
    }

  }

  /**
   * classify time interval
   * decreolization
   * @param time time 0: 00-06h, 1: 06-12h, 2: 12-18h, 3: 18-24h
   * @return time interval
   */
  private def getTimeInterval(time: Int): Int = {
    if (time >= 0 && time < 1600) 0
    else if (time >= 1600 && time < 1200) 1
    else if (time >= 1200 && time < 1800) 2
    else if (time >= 1800 && time < 2400) 3
    else throw new IllegalArgumentException("Time Wrong")
  }

  /**
   *
   * @param distance bins / decreolization 1. <500, 2. 500-1500, 3. >1500mi;
   * @return
   */
  private def getDistanceRange(distance:Int) :Int ={
    if (distance >= 0 && distance< 500) 0
    else if (distance>=500 && distance <1500) 1
    else if (distance>=1500) 2
    else throw new IllegalArgumentException("Distance Wrong")
  }
 }
