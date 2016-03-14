package spotlight

import org.json4s.JValue
import rsc.Types.{Candidates, Spots}
import rsc.attributes.Candidate.Spotlight
import rsc.attributes.{Scores, Spot}

object Types {
  // Transform some of the json values returned by the web-service
  // that have illegal or bad-taste names
  def transformFields(json: JValue): JValue = json transformField {
    // Annotation
    case ("@text", x) => ("text", x)
    case ("surfaceForm", x) => ("surfaceForms", x)

    // SurfaceForm
    case ("@name", x) => ("name", x)
    case ("@offset", x) => ("offset", x)
    case ("resource", x) => ("resources", x)

    // Resource
    case ("@label", x) => ("label", x)
    case ("@uri", x) => ("uri", x)
    case ("@contextualScore", x) => ("contextualScore", x)
    case ("@percentageOfSecondRank", x) => ("percentageOfSecondRank", x)
    case ("@support", x) => ("support", x)
    case ("@priorScore", x) => ("priorScore", x)
    case ("@finalScore", x) => ("finalScore", x)
    case ("@types", x) => ("types", x)
  }

  case class Annotation(text: String, surfaceForms: List[SurfaceForm]) {
    def spots(): Spots = surfaceForms.flatMap(surfaceForm => {
      val begin = surfaceForm.offset
      val end = begin + surfaceForm.name.length
      val name = surfaceForm.name

      // Filter incoherent data
      text.substring(begin, end).equals(name) match {
        case false => Nil
        case true => {
          val candidates = surfaceForm.allCandidates()
          // 1 surface-form = 1 spot
          List(Spot(begin, end, candidates))
        }
      }
    })

    def allCandidates(): Candidates = spots().flatMap(spot => spot.candidates)
  }

  case class SurfaceForm(name: String, offset: Int, resources: List[Resource]) {
    def allCandidates(): Candidates = resources.map(_.asCandidate())
  }

  case class Resource(label: String,
                       uri: String,
                       contextualScore: Double,
                       percentageOfSecondRank: Double,
                       support: Int,
                       priorScore: Double,
                       finalScore: Double,
                       types: String) {
    def asCandidate(): Spotlight = {
      val scores = Scores(contextualScore,
        percentageOfSecondRank,
        support,
        priorScore,
        finalScore)
      Spotlight(label, uri, scores, types)
    }
  }
}
