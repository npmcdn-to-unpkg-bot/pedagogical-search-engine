package rsc.prettifier.lexer

sealed class Token(val kind: TokenKind) {
  def rawString = kind.rawString
  override def toString: String = kind.toString
}

sealed trait TokenKind {
  def rawString: String
}

object Tokens {
  case object PART extends TokenKind {
    def rawString: String = "part"
  }
  case object CHAPTER extends TokenKind {
    def rawString: String = "chapter"
  }
  case object SECTION extends TokenKind {
    def rawString: String = "section"
  }
  case object SUBSECTION extends TokenKind {
    def rawString: String = "subsection"
  }

  object TEXTKIND extends TokenKind {
    def rawString = "text-kind"
  }

  object NUMERATIONKIND extends TokenKind {
    def rawString = "numeration-kind"
  }

  object SEPARATORKIND extends TokenKind {
    def rawString = "separator-kind"
  }

  object WHITESPACEKIND extends TokenKind {
    def rawString = "whitespace-kind"
  }

  object Kinded {
    def unapply(t: Token): Option[TokenKind] = {
      Some(t.kind)
    }
  }

  case class TEXT(value: String) extends Token(TEXTKIND) {
    override def rawString = value
    override def toString: String = s"TEXT($rawString)"
  }

  case class NUMERATION(xs: List[Int], original: List[String]) extends Token(NUMERATIONKIND) {
    override def rawString = original.mkString(".")
    override def toString: String = s"NUMERATION($rawString)"
  }

  case class SEPARATOR(value: String) extends Token(SEPARATORKIND) {
    override def rawString = value
    override def toString: String = s"SEPARATOR($rawString)"
  }

  case class WHITESPACE(value: String) extends Token(WHITESPACEKIND) {
    override def rawString = value
    override def toString: String = s"WHITESPACE($rawString)"
  }
}
