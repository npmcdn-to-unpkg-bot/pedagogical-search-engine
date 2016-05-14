package rsc.prettifier.structure.others

import rsc.prettifier.lexer.KeywordTokenKind
import rsc.prettifier.structure.Structure

case class Keyword(ktk: KeywordTokenKind,
                   oNumber: Option[Int],
                   oText: Option[String])
  extends Structure {

}
