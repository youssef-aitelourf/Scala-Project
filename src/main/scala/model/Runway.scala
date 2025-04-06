package model

import csv.CsvParser

case class Runway(
                   id: Int,
                   airport_ref: Int,
                   airport_ident: String,
                   length_ft: Option[Int],
                   width_ft: Option[Int],
                   surface: Option[String],
                   lighted: Option[Int],
                   closed: Option[Int],
                   le_ident: Option[String]
                 )

object Runway:
  // Assumes the CSV header is:
  // id,airport_ref,airport_ident,length_ft,width_ft,surface,lighted,closed,le_ident,...
  def fromCsvLine(line: String): Option[Runway] =
    CsvParser.splitLine(line) match
      case idStr :: airportRefStr :: airportIdent :: lengthStr :: widthStr ::
        surface :: lightedStr :: closedStr :: le_ident :: _ =>
        for
          id          <- idStr.toIntOption
          airport_ref <- airportRefStr.toIntOption
          length_ft   = lengthStr.toIntOption
          width_ft    = widthStr.toIntOption
          lighted     = lightedStr.toIntOption
          closed      = closedStr.toIntOption
        yield Runway(
          id,
          airport_ref,
          airportIdent,
          length_ft,
          width_ft,
          nonEmpty(surface),
          lighted,
          closed,
          nonEmpty(le_ident)
        )
      case _ => None

  private def nonEmpty(s: String): Option[String] =
    if s.trim.isEmpty then None else Some(s.trim)