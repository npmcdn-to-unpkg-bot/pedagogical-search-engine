package rtoc

abstract class Factory[U] {
  def produceResources(data: U): List[Resource]
}
