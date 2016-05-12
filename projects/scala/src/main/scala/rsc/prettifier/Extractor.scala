package rsc.prettifier

import rsc.prettifier.structure.Structure

class Extractor {

  def extract(s: String)
  : Structure =
    parser.Simple.process(lexer.Simple.process(s))
}
