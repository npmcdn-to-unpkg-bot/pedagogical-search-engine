package ws.autocomplete.query

import slick.jdbc.GetResult
import slick.driver.MySQLDriver.api._
import utils.StringUtils._
import ws.autocomplete.results._

object Queries {
  object Codes {
    val ignore = 0
    object Exact {
      val disambiguation = 1
      val title = 2
      val redirect = 3
    }
    object Prefix {
      val disambiguation = 4
      val title = 5
      val redirect = 6
    }
  }
  val defaultLimit = 10

  val separator = ",¬" // Ensure that no labels or uris contain this

  /* mysql GROUP_CONCAT can be truncated, we can send a funky
   * row filled with garbage to set the non-truncated window
   * size. (dirty hack)
   * View also this candidate "solution" http://stackoverflow.com/a/23608554/3890306
   * It has no real impact on the performances. Benchmark: 4-letters
   *   [.., 512] freq: 10ms - avg-resp: 55ms
   *   [512, 8k] freq: 10?-20?ms - avg-resp: 55ms
   */
  val strPadding = math.pow(2, 13).toInt
  // The added row is dropped before being returned with this "Limit 1, x" trick
  val maxNbRows = math.pow(2, 10).toInt

  // Create the query-action
  def getAction(text: String, n: Int) = text.length match {
    case one if one == 1 => Queries.one(text, n)
    case twoThre if twoThre == 2 || twoThre == 3 => Queries.twoThre(text, n)
    case _ => Queries.fourPlus(text, n)
  }

  def withWilcards(text: String): String = {
    val max = 4
    val sep = " "
    glue(text.split(sep).toList, max, sep) match {
      case Nil => ""
      case head::Nil => head + "%"
      case head::tail => head + "%" + tail.mkString(" ") + "%"
    }
  }

  implicit val getSearchResult: GetResult[Result] = GetResult(r => {
    val source = r.nextInt()

    source match {
      case Codes.Exact.disambiguation | Codes.Prefix.disambiguation =>
        val labelA = r.nextString()
        val labelB = r.nextString().split(separator).toList
        val uriA = r.nextString()
        val uriB = r.nextString().split(separator).toList
        val inB = r.nextString().split(separator).toList.map(_.toInt)

        val bs = (labelB, uriB, inB).zipped.map {
          case (label, uri, in) => PageElement(uri, label, in)
        }
        Disambiguation(uriA, labelA, bs)

      case Codes.Exact.title | Codes.Prefix.title =>
        r.skip
        val label = r.nextString()
        r.skip
        val uri =  r.nextString()
        val in = r.nextInt()
        Title(label, uri, in)

      case Codes.Exact.redirect | Codes.Prefix.redirect =>
        val labelA = r.nextString()
        val labelB = r.nextString()
        r.skip
        val uriB = r.nextString()
        val inB = r.nextInt()
        Redirect(labelA, labelB, uriB, inB)

    }
  })

  def one(i: String, n: Int = defaultLimit) = {
    val text = escapeSQLWildcards(i)
    sql"""
    (
      SELECT
        #${Codes.ignore} as `Source`,
        REPEAT('a', #$strPadding) as `LabelA`,
        REPEAT('a', #$strPadding) as `LabelB`,
        REPEAT('a', #$strPadding) as `UriA`,
        REPEAT('a', #$strPadding) as `UriB`,
        1 as `InB`
    ) UNION (
      SELECT
        #${Codes.Exact.disambiguation} as `Source`,
        MIN(`LabelA`) as `LabelA`,
        GROUP_CONCAT(`LabelB` ORDER BY `InB` DESC SEPARATOR '#$separator') as `LabelB`,
        `A` as `UriA`,
        GROUP_CONCAT(`B` ORDER BY `InB` DESC SEPARATOR '#$separator') as `UriB`,
        GROUP_CONCAT(`InB` ORDER BY `InB` DESC SEPARATOR '#$separator') as `InB`
      FROM `dictionary-disambiguation`
      WHERE
        `LabelA` LIKE $text AND
        `Available` = 1
      GROUP BY `A`
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Exact.title} as `Source`,
        NULL as `LabelA`,
        `Label` as `LabelB`,
            NULL as `UriA`,
        `Uri` as `UriB`,
        `In` as `InB`
      FROM `dictionary-titles`
      WHERE
        `Label` LIKE $text AND
        `Available` = 1
      ORDER BY `In` DESC
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Exact.redirect} as `Source`,
        `LabelA` as `LabelA`,
        `LabelB` as `LabelB`,
        NULL as `UriA`,
        `UriB` as `UriB`,
        `InB` as `InB`
      FROM `dictionary-redirects`
      WHERE
        `LabelA` LIKE $text AND
        `Available` = 1
      ORDER BY `InB` DESC
      LIMIT #$n
    ) ORDER BY `Source` ASC LIMIT 1, #$maxNbRows;
    """.as[Result]
  }

  def twoThre(i: String, n: Int = defaultLimit) = {
    val text = escapeSQLWildcards(i)
    val textPercent = text + "%"
    sql"""
    (
      SELECT
        #${Codes.ignore} as `Source`,
        REPEAT('a', #$strPadding) as `LabelA`,
        REPEAT('a', #$strPadding) as `LabelB`,
        REPEAT('a', #$strPadding) as `UriA`,
        REPEAT('a', #$strPadding) as `UriB`,
        1 as `InB`
    ) UNION (
      SELECT
        #${Codes.Exact.disambiguation} as `Source`,
        MIN(`LabelA`) as `LabelA`,
        GROUP_CONCAT(`LabelB` ORDER BY `InB` DESC SEPARATOR '#$separator') as `LabelB`,
        `A` as `UriA`,
        GROUP_CONCAT(`B` ORDER BY `InB` DESC SEPARATOR '#$separator') as `UriB`,
        GROUP_CONCAT(`InB` ORDER BY `InB` DESC SEPARATOR '#$separator') as `InB`
      FROM `dictionary-disambiguation`
      WHERE
        `LabelA` LIKE $text AND
        `Available` = 1
      GROUP BY `A`
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Exact.title} as `Source`,
        NULL as `LabelA`,
        `Label` as `LabelB`,
        NULL as `UriA`,
        `Uri` as `UriB`,
        `In` as `InB`
      FROM `dictionary-titles`
      WHERE
        `Label` LIKE $text AND
        `Available` = 1
      ORDER BY `In` DESC
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Exact.redirect} as `Source`,
        `LabelA` as `LabelA`,
        `LabelB` as `LabelB`,
        NULL as `UriA`,
        `UriB` as `UriB`,
        `InB` as `InB`
      FROM `dictionary-redirects`
      WHERE
        `LabelA` LIKE $text AND
        `Available` = 1
      ORDER BY `InB` DESC
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Prefix.disambiguation} as `Source`,
        MIN(`LabelA`) as `LabelA`,
        GROUP_CONCAT(`LabelB` ORDER BY `InB` DESC SEPARATOR '#$separator') as `LabelB`,
        `A` as `UriA`,
        GROUP_CONCAT(`B` ORDER BY `InB` DESC SEPARATOR '#$separator') as `UriB`,
        GROUP_CONCAT(`InB` ORDER BY `InB` DESC SEPARATOR '#$separator') as `InB`
      FROM `dictionary-disambiguation`
      WHERE
        `LabelA` LIKE $textPercent AND
        `Available` = 1
      GROUP BY `A`
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Prefix.title} as `Source`,
        NULL as `LabelA`,
        `Label` as `LabelB`,
        NULL as `UriA`,
        `Uri` as `UriB`,
        `In` as `InB`
      FROM `dictionary-titles`
      WHERE
        `Label` LIKE $textPercent AND
        `Available` = 1
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Prefix.redirect} as `Source`,
        `LabelA` as `LabelA`,
        `LabelB` as `LabelB`,
        NULL as `UriA`,
        `UriB` as `UriB`,
        `InB` as `InB`
      FROM `dictionary-redirects`
      WHERE
        `LabelA` LIKE $textPercent AND
        `Available` = 1
      LIMIT #$n
    ) ORDER BY `Source` ASC LIMIT 1, #$maxNbRows;
    """.as[Result]
  }

  def fourPlus(i: String, n: Int = defaultLimit) = {
    val text = escapeSQLWildcards(i)
    val textPercent = withWilcards(text)
    sql"""
    (
      SELECT
        #${Codes.ignore} as `Source`,
        REPEAT('a', #$strPadding) as `LabelA`,
        REPEAT('a', #$strPadding) as `LabelB`,
        REPEAT('a', #$strPadding) as `UriA`,
        REPEAT('a', #$strPadding) as `UriB`,
        1 as `InB`
    ) UNION (
      SELECT
        #${Codes.Exact.disambiguation} as `Source`,
        MIN(`LabelA`) as `LabelA`,
        GROUP_CONCAT(`LabelB` ORDER BY `InB` DESC SEPARATOR '#$separator') as `LabelB`,
        `A` as `UriA`,
        GROUP_CONCAT(`B` ORDER BY `InB` DESC SEPARATOR '#$separator') as `UriB`,
        GROUP_CONCAT(`InB` ORDER BY `InB` DESC SEPARATOR '#$separator') as `InB`
      FROM `dictionary-disambiguation`
      WHERE
        `LabelA` LIKE $text AND
        `Available` = 1
      GROUP BY `A`
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Exact.title} as `Source`,
        NULL as `LabelA`,
        `Label` as `LabelB`,
        NULL as `UriA`,
        `Uri` as `UriB`,
        `In` as `InB`
      FROM `dictionary-titles`
      WHERE
        `Label` LIKE $text AND
        `Available` = 1
      ORDER BY `In` DESC
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Exact.redirect} as `Source`,
        `LabelA` as `LabelA`,
        `LabelB` as `LabelB`,
        NULL as `UriA`,
        `UriB` as `UriB`,
        `InB` as `InB`
      FROM `dictionary-redirects`
      WHERE
        `LabelA` LIKE $text AND
        `Available` = 1
      ORDER BY `InB` DESC
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Prefix.disambiguation} as `Source`,
        MIN(`LabelA`) as `LabelA`,
        GROUP_CONCAT(`LabelB` ORDER BY `InB` DESC SEPARATOR '#$separator') as `LabelB`,
        `A` as `UriA`,
        GROUP_CONCAT(`B` ORDER BY `InB` DESC SEPARATOR '#$separator') as `UriB`,
        GROUP_CONCAT(`InB` ORDER BY `InB` DESC SEPARATOR '#$separator') as `InB`
      FROM `dictionary-disambiguation`
      WHERE
        `LabelA` LIKE $textPercent AND
        `Available` = 1
      GROUP BY `A`
      ORDER BY length(MIN(`LabelA`)) ASC
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Prefix.title} as `Source`,
        NULL as `LabelA`,
        `Label` as `LabelB`,
        NULL as `UriA`,
        `Uri` as `UriB`,
        `In` as `InB`
      FROM `dictionary-titles`
      WHERE
        `Label` LIKE $textPercent AND
        `Available` = 1
      ORDER BY length(`Label`) ASC, `In` DESC
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Prefix.redirect} as `Source`,
        `LabelA` as `LabelA`,
        `LabelB` as `LabelB`,
        NULL as `UriA`,
        `UriB` as `UriB`,
        `InB` as `InB`
      FROM `dictionary-redirects`
      WHERE
        `LabelA` LIKE $textPercent AND
        `Available` = 1
      ORDER BY length(`LabelA`) ASC, `InB` DESC
      LIMIT #$n
    ) ORDER BY `Source` ASC LIMIT 1, #$maxNbRows;
    """.as[Result]
  }
}
