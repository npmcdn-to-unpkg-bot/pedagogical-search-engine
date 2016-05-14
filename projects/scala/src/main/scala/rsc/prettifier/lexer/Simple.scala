package rsc.prettifier.lexer

import utils.StringUtils
import utils.StringUtils.splitButKeep

object Simple {

  import Tokens._

  val keywords = List("part", "chapter", "section",
    "week", "session", "quiz", "quizz", "exercise", "exam", "unit", "module")

  val SpaceRegex = """(\s)""".r

  def process(s: String)
  : List[Token] = {

    def withDelemiter(c: Char) = s"((?<=%1$c)|(?=%1$c))"

    val text = s.trim.toLowerCase
    val words = splitButKeep(text, """[\s,:;]""").toList.flatMap {
      case x => StringUtils.splitButKeep(x, keywords.mkString("|")).filter(_.length > 0)
    }

    val space = " "

    words.map {
      case word => word match {
        case "," => SEPARATOR(",")
        case ":" => SEPARATOR(":")
        case ";" => SEPARATOR(";")
        case SpaceRegex(x) => WHITESPACE(x)

        case "part" => new Token(PART)
        case "chapter" => new Token(CHAPTER)
        case "section" => new Token(SECTION)

        case "week" => new Token(WEEKKIND)
        case "session" => new Token(SESSIONKIND)
        case "quiz" | "quizz" => new Token(QUIZKIND)
        case "exercise" => new Token(EXERCISEKIND)
        case "exam" => new Token(EXAMKIND)
        case "unit" => new Token(UNITKIND)
        case "module" => new Token(MODULEKIND)

        case Eci(xs) => NUMERATION(xs.map(NumeralSystem.asInt), xs)
        case _ => TEXT(word)
      }
    }
  }
}
