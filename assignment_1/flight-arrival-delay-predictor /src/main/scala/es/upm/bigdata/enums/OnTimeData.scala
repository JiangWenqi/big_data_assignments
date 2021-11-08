package es.upm.bigdata.enums

import org.apache.spark.sql.Row

case class OnTimeData(
                       year: Option[Int], // 1987-2008
                       month: Option[Int], // 12-Jan
                       dayOfMonth: Option[Int], // 31-Jan
                       dayOfWeek: Option[Int], // 1 (Monday) - 7 (Sunday)
                       depTime: Option[Int], // actual departure time (local, hhmm)
                       crsDepTime: Option[Int], // scheduled departure time (local, hhmm)
                       crsArrTime: Option[Int], // 	scheduled arrival time (local, hhmm)
                       uniqueCarrier: Option[String], // 	unique carrier code
                       flightNum: Option[Int], // 	flight number
                       tailNum: Option[String], // 	plane tail number
                       crsElapsedTime: Option[Int], // in minutes
                       arrDelay: Option[Int], // 	arrival delay, in minutes
                       depDelay: Option[Int], // 	departure delay, in minutes
                       origin: Option[String], // 	origin IATA airport code
                       dest: Option[String], // 	destination IATA airport code
                       distance: Option[Int], // 	in miles/
                       taxiOut: Option[Int], // 	taxi out time in minutes
                       cancelled: Option[Int], // 	was the flight cancelled?
                       cancellationCode: Option[String], // 	reason for cancellation (A = carrier, B = weather, C = NAS, D = security)
                     ) {

}

object OnTimeData {
  def apply(row: Row): OnTimeData = {
    def getIntValue(index: Int): Option[Int] = {
      if (row.isNullAt(index)||row.getString(index).equals("NA")) None else Option(row.getString(index).toInt)
    }

    def getStringValue(index: Int) = {
      if (row.isNullAt(index)) None else Option(row.getString(index))
    }

    val yearIndex = row.fieldIndex("Year")
    val monthIndex = row.fieldIndex("Month")
    val dayOfMonthIndex = row.fieldIndex("DayofMonth")
    val dayOfWeekIndex = row.fieldIndex("DayOfWeek")
    val depTimeIndex = row.fieldIndex("DepTime")
    val crsDepTimeIndex = row.fieldIndex("CRSDepTime")
    val crsArrTimeIndex = row.fieldIndex("CRSArrTime")
    val uniqueCarrierIndex = row.fieldIndex("UniqueCarrier")
    val flightNumIndex = row.fieldIndex("FlightNum")
    val tailNumIndex = row.fieldIndex("TailNum")
    val crsElapsedTimeIndex = row.fieldIndex("CRSElapsedTime")
    val arrDelayIndex = row.fieldIndex("ArrDelay")
    val depDelayIndex = row.fieldIndex("DepDelay")
    val originIndex = row.fieldIndex("Origin")
    val destIndex = row.fieldIndex("Dest")
    val distanceIndex = row.fieldIndex("Distance")
    val taxiOutIndex = row.fieldIndex("TaxiOut")
    val cancelledIndex = row.fieldIndex("Cancelled")
    val cancellationCodeIndex = row.fieldIndex("CancellationCode")


    val year = getIntValue(yearIndex)
    val month = getIntValue(monthIndex)
    val dayOfMonth = getIntValue(dayOfMonthIndex)
    val dayOfWeek = getIntValue(dayOfWeekIndex)
    val depTime = getIntValue(depTimeIndex)
    val crsDepTime = getIntValue(crsDepTimeIndex)
    val crsArrTime = getIntValue(crsArrTimeIndex)
    val uniqueCarrier = getStringValue(uniqueCarrierIndex)
    val flightNum = getIntValue(flightNumIndex)
    val tailNum = getStringValue(tailNumIndex)
    val crsElapsedTime = getIntValue(crsElapsedTimeIndex)
    val arrDelay = getIntValue(arrDelayIndex)
    val depDelay = getIntValue(depDelayIndex)
    val origin = getStringValue(originIndex)
    val dest = getStringValue(destIndex)
    val distance = getIntValue(distanceIndex)
    val taxiOut = getIntValue(taxiOutIndex)
    val cancelled = getIntValue(cancelledIndex)
    val cancellationCode = getStringValue(cancellationCodeIndex)

    OnTimeData(year, month, dayOfMonth, dayOfWeek, depTime, crsDepTime, crsArrTime, uniqueCarrier, flightNum, tailNum, crsElapsedTime, arrDelay, depDelay, origin, dest, distance, taxiOut, cancelled, cancellationCode)
  }

}