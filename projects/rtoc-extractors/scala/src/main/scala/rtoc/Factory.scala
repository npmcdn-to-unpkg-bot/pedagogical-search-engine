package rtoc

import java.io.File

abstract class Factory[U <: HasStatus](outputFolder: File) {
  def produceResources(data: U): List[Resource]
}
