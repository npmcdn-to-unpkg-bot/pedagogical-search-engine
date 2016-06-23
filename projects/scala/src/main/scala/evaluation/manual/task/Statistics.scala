package evaluation.manual.task

import evaluation.manual.json.Annotation
import utils.StringUtils.normalizeUri


class Statistics(annotation: Annotation, N: Int) {

  // Normalize the uris in the annotations
  // This solves comparisons issues
  private lazy val normAnnotation
  : Annotation = {
    val newElements = annotation.annotations.map(element => {
      val newUris = element.uris.map(normalizeUri)
      val newIndexes = element.indexes.map(xs => xs.map(normalizeUri))
      element.copy(uris = newUris, indexes = newIndexes)
    })
    annotation.copy(annotations = newElements)
  }

  def precision()
  : List[Double] = {
    val precisions = normAnnotation.annotations.flatMap(element => {
      val indexes = element.indexes.getOrElse(Nil)
      (1 to N).toList.map {
        case n =>
          val nbCommon = common(n, indexes, element.uris)
          val precision = indexes.isEmpty match {
            case true => 1
            case false => nbCommon.toDouble / math.min(n.toDouble, indexes.size.toDouble)
          }
          (n, precision)
      }
    })

    val averages = precisions.groupBy(_._1).toList.map {
      case (n, xs) =>
        val tot = xs.map(_._2).sum
        val avg = tot / xs.size.toDouble
        (n, avg)
    }

    averages.sortBy(_._1).map(_._2)
  }

  def recall()
  : List[Double] = {
    val recalls = normAnnotation.annotations.flatMap(element => {
      val indexes = element.indexes.getOrElse(Nil)
      val uris = element.uris
      (1 to N).toList.map {
        case n =>
          val nbCommon = common(n, indexes, uris)
          val recall = nbCommon.toDouble / uris.size.toDouble
          (n, recall)
      }
    })

    val averages = recalls.groupBy(_._1).toList.map {
      case (n, xs) =>
        val tot = xs.map(_._2).sum
        val avg = tot / xs.size.toDouble
        (n, avg)
    }

    averages.sortBy(_._1).map(_._2)
  }

  def f1()
  : List[Double] = {
    precision().zip(recall()).map {
      case (pre, rec) =>
        2 * pre * rec / (pre + rec)
    }
  }

  private def common(n: Int, indexes: List[String], correct: List[String])
  : Int = indexes.take(n).count(correct.contains)
}
