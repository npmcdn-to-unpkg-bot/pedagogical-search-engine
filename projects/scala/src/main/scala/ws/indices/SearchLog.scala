package ws.indices

import ws.indices.spraythings.FilterParameterType._
import ws.indices.spraythings.SearchTerm


case class SearchLog(from: Int,
                     to: Int,
                     filter: FilterParameter,
                     searchTerms: List[SearchTerm])
