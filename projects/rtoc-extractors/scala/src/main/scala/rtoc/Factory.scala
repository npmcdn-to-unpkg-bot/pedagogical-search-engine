package rtoc

import java.io.File

abstract class Factory[U](outputFolder: File) {
  def produceResources(data: U): List[Resource]
}
