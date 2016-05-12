package rsc.prettifier.lexer

/*
  Note:
  -> ec (EntryClass): is a sequence of numeral system (NS)
		e.g. EC_1 = (NS_1, NS_4, NS_5)
		ps:
      NS_1	I, II, III, IV, V, VI, VII, VIII, IX, X, XI, ...
      NS_2	i, ii, iii, iv, v, vi, vii, viii, ix, x, xi, ...
      NS_3	1, 2, 3, 4, 5, ...
      NS_4	A, B, C, D, E, ...
      NS_5	a, b, c, d, e, ...

	-> eci (instance of ec): is made by elements of its NS (NumeralSystem)
		regex: ((NS)(.NS)*.?)
		e.g. instance of EC_1 may be: I.B.a or V.A.e
 */
object Eci {
  // e.g. 1.a.i. => [1, a, i]
  def getChunks(s: String): List[String] = s.split('.').filter(_.length != 0).toList

  // e.g. succ(1.a) = 1.b / succ(B.iii) = B.iv
  def succ(chunks: List[String]): String = {
    (chunks.dropRight(1) ::: List(NumeralSystem.succ(chunks.last))).mkString(".")
  }

  def succ(s: String): String = succ(getChunks(s))

  def isOne(chunks: List[String]): Boolean = chunks match {
    case Nil => false
    case _ => chunks.foldRight(true)((c, acc) => acc && NumeralSystem.isNumeral(c))
  }

  def isOne(s: String): Boolean = isOne(getChunks(s))

  def unapply(arg: String): Option[List[String]] = isOne(arg) match {
    case true => Some(getChunks(arg))
    case false => None
  }
}
