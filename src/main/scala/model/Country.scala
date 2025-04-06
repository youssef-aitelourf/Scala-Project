package model

import csv.CsvParser

case class Country(
                    id: Int,
                    code: String,
                    name: String,
                    continent: Option[String],
                    wikipediaLink: Option[String],
                    keywords: Option[String]
                  )

object Country:
  // Assumes the CSV header is: id,code,name,continent,wikipedia_link,keywords
  def fromCsvLine(line: String): Option[Country] =
    CsvParser.splitLine(line) match
      case idStr :: code :: name :: continent :: wikipediaLink :: keywords :: Nil =>
        idStr.toIntOption.map { id =>
          Country(
            id,
            code,
            name,
            nonEmpty(continent),
            nonEmpty(wikipediaLink),
            nonEmpty(keywords)
          )
        }
      case _ => None

  private def nonEmpty(s: String): Option[String] =
    if s.trim.isEmpty then None else Some(s.trim)