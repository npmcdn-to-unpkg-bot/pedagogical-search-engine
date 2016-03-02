package rtoc

import Utils.Logger


class Resource(nodes: List[Node]) {
  def write(): Unit = {
    // todo: Implement
    Logger.info(s"Writing resource")
    nodes.map(println(_))
  }
  def path(): String = {
    // todo: Implement
    "some-path"
  }
}
