package rsc.prettifier.lexer

/*
  Note:
  -> ns (Numeral system): may be
    NS_1	I, II, III, IV, V, VI, VII, VIII, IX, X, XI, ...
    NS_2	i, ii, iii, iv, v, vi, vii, viii, ix, x, xi, ...
    NS_3	1, 2, 3, 4, 5, ...
    NS_4	A, B, C, D, E, ...
    NS_5	a, b, c, d, e, ...
*/
object NumeralSystem {
  // no strict check
  val romanUpper = """[IVXLCDM]+""".r
  val romanlower = """[ivxlcdm]+""".r

  // strict
  val hinduArabic = """[0-9]|[1-9][0-9]+""".r
  val alphabeticalUpper = """[A-Z]""".r
  val alphabeticalLower = """[a-z]""".r

  def isNumeral(s: String): Boolean = s.toUpperCase() match {
    case romanUpper() => true
    case alphabeticalUpper() => true
    case hinduArabic() => true
    case _ => false
  }

  def asInt(s: String): Int = s.toUpperCase() match {
    case romanUpper() => romanToInt(s)
    case alphabeticalUpper() => alphaToInt(s)
    case _ => Integer.valueOf(s)
  }

  // succ(eci) gives the conventional instance after eci
  // e.g. succ(I.1.a) = I.1.b
  def succ(s: String): String = s match {
    case romanUpper() => intToRomanUpper(romanToInt(s) + 1)
    case romanlower() => intToRomanUpper(romanToInt(s) + 1).toLowerCase
    case alphabeticalUpper() => intToUpperAlphabetical(alphaToInt(s) + 1)
    case alphabeticalLower() => intToUpperAlphabetical(alphaToInt(s) + 1).toLowerCase
    case _ => String.valueOf(Integer.valueOf(s) + 1)
  }

  // source: http://rosettacode.org/wiki/Roman_numerals/Decode#Scala
  val romanLetters = Map('I' -> 1, 'V' -> 5, 'X' -> 10, 'L' -> 50, 'C' -> 100, 'D' -> 500, 'M' -> 1000)

  def romanToInt(s: String): Int = {
    s.toUpperCase.map(romanLetters).foldLeft((0, 0)) {
      case ((sum, last), curr) => (sum + curr + (if (last < curr) -2 * last else 0), curr)
    }._1
  }

  // source: http://rosettacode.org/wiki/Roman_numerals/Encode#Scala_Using_foldLeft
  val romanNumerals = List(1000 -> "M", 900 -> "CM", 500 -> "D", 400 -> "CD", 100 -> "C", 90 -> "XC",
    50 -> "L", 40 -> "XL", 10 -> "X", 9 -> "IX", 5 -> "V", 4 -> "IV", 1 -> "I")

  def intToRomanUpper(v: Int): String = {
    var n = v
    romanNumerals.foldLeft("") {
      (s, t) => {
        val c = n / t._1;
        n = n - t._1 * c;
        s + (t._2 * c)
      }
    }
  }

  val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

  def intToUpperAlphabetical(n: Int): String = alphabet((n - 1) % 26).toString

  def alphaToInt(s: String): Int = alphabet.indexOf(s.toUpperCase()) + 1
}
