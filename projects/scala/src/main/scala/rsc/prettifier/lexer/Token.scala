package rsc.prettifier.lexer

sealed class Token(val kind: TokenKind) {
  def rawString = kind.rawString
  override def toString: String = kind.toString
}

sealed trait TokenKind {
  def rawString: String
}

sealed trait BookTokenKind extends TokenKind
sealed trait KeywordTokenKind extends TokenKind

object Tokens {
  case object PART extends BookTokenKind {
    def rawString: String = "part"
  }
  case object CHAPTER extends BookTokenKind {
    def rawString: String = "chapter"
  }
  case object SECTION extends BookTokenKind {
    def rawString: String = "section"
  }

  object TEXTKIND extends TokenKind {
    def rawString = "text-kind"
  }


  object WEEKKIND extends KeywordTokenKind {
    def rawString = "week-kind"
  }

  object SESSIONKIND extends KeywordTokenKind {
    def rawString = "session-kind"
  }

  object QUIZKIND extends KeywordTokenKind {
    def rawString = "quiz-kind"
  }

  object EXERCISEKIND extends KeywordTokenKind {
    def rawString = "exercise-kind"
  }

  object EXAMKIND extends KeywordTokenKind {
    def rawString = "exam-kind"
  }

  object UNITKIND extends KeywordTokenKind {
    def rawString = "unit-kind"
  }

  object MODULEKIND extends KeywordTokenKind {
    def rawString = "module-kind"
  }

  object PARTKEYWORDKIND extends KeywordTokenKind {
    def rawString = "part-keyword-kind"
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

  object BookPart {
    def unapply(t: Token): Option[BookTokenKind] = t.kind match {
      case PART => Some(PART)
      case CHAPTER => Some(CHAPTER)
      case SECTION => Some(SECTION)
      case _ => None
    }
  }

  object KeywordToken {
    def unapply(t: Token): Option[KeywordTokenKind] = t.kind match {
      case WEEKKIND => Some(WEEKKIND)
      case SESSIONKIND => Some(SESSIONKIND)
      case QUIZKIND => Some(QUIZKIND)
      case EXERCISEKIND => Some(EXERCISEKIND)
      case EXAMKIND => Some(EXAMKIND)
      case UNITKIND => Some(UNITKIND)
      case MODULEKIND => Some(MODULEKIND)
      case PARTKEYWORDKIND => Some(PARTKEYWORDKIND)
      case _ => None
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
