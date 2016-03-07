package rsc

class Worker[U <: HasStatus](data: Data[U], factory: Factory[U]) {
  def work(): Unit = {
    // (re)-Initialize the cursor
    data.init()

    // Do all the work
    def workRec(): Unit = data.hasNext() match {
      case true => {
        val entry = data.next()
        factory.getResource(entry) match {
          case None => data.markNotOk(entry)
          case Some(resource) => {
            resource.write()
            data.markOk(entry)
          }
        }

        // Continue the work
        workRec()
      }
      case false => {}
    }
    workRec()

    // Flush results
    data.flush()
  }

}
