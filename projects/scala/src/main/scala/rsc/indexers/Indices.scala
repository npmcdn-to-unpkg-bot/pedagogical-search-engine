package rsc.indexers

import rsc.snippets.Snippet
import utils.StringUtils

case class Indices(values: List[Index],
                   oSnippet: Option[Snippet] = None,
                   entryId: String = StringUtils.uuid36()) {

}
