package model

import csv.CsvParser

case class Airport(
                    id: Int,
                    ident: String,
                    airportType: String,
                    name: String,
                    latitude_deg: Option[Double],
                    longitude_deg: Option[Double],
                    elevation_ft: Option[Int],
                    continent: Option[String],
                    iso_country: Option[String],
                    iso_region: Option[String],
                    municipality: Option[String],
                    scheduled_service: Option[String],
                    gps_code: Option[String],
                    iata_code: Option[String],
                    local_code: Option[String],
                    home_link: Option[String],
                    wikipedia_link: Option[String],
                    keywords: Option[String]
                  )

object Airport:
  // Assumes the CSV header is:
  // id,ident,type,name,latitude_deg,longitude_deg,elevation_ft,
  // continent,iso_country,iso_region,municipality,scheduled_service,
  // gps_code,iata_code,local_code,home_link,wikipedia_link,keywords
  def fromCsvLine(line: String): Option[Airport] =
    CsvParser.splitLine(line) match
      case idStr :: ident :: airportType :: name :: latStr :: lonStr :: elevStr ::
        continent :: iso_country :: iso_region :: municipality :: scheduled_service ::
        gps_code :: iata_code :: local_code :: home_link :: wikipedia_link :: keywords :: Nil =>
        for
          id   <- idStr.toIntOption
          lat  = latStr.toDoubleOption
          lon  = lonStr.toDoubleOption
          elev = elevStr.toIntOption
        yield Airport(
          id,
          ident,
          airportType,
          name,
          lat,
          lon,
          elev,
          nonEmpty(continent),
          nonEmpty(iso_country),
          nonEmpty(iso_region),
          nonEmpty(municipality),
          nonEmpty(scheduled_service),
          nonEmpty(gps_code),
          nonEmpty(iata_code),
          nonEmpty(local_code),
          nonEmpty(home_link),
          nonEmpty(wikipedia_link),
          nonEmpty(keywords)
        )
      case _ => None

  private def nonEmpty(s: String): Option[String] =
    if s.trim.isEmpty then None else Some(s.trim)