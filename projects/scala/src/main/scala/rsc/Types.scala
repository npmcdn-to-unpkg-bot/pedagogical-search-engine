package rsc

import rsc.attributes.Candidate.Candidate
import rsc.attributes._
import rsc.toc.{Node, Toc}

object Types {
  // rsc types
  type Keywords = List[Keyword]
  type Categories = List[Category]
  type Domains = List[Domain]
  type Subdomains = List[Subdomain]

  type Candidates = List[Candidate]
  type Spots = List[Spot]


  type Tocs = List[Toc]
  type Descriptions = List[Description]

  // other
  type Nodes = List[Node]
  type Resources = List[Resource]
}
