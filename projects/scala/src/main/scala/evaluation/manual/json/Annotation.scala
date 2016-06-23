package evaluation.manual.json

import evaluation.manual.json.Type.Type

case class Annotation(`type`: Type,
                      title: String,
                      annotations: List[AnnotationElement],
                      toc: List[TocNode]) {

}
