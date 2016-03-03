package rtoc

class Worker[U](data: Data[U], factory: Factory[U]) {
  def work(): Unit = {
    // (re)-Initialize the cursor
    data.init()

    // Do all the work
    def workRec(): Unit = data.hasNext() match {
      case true => {
        val entry = data.next()
        factory.produceResources(entry) match {
          case Nil => data.markNotOk(entry)
          case resources => {
            // Write the resources
            resources.map(resource => {
              resource.write()
            })

            // Mark the entry as OK
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
