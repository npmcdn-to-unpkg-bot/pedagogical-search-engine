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

            // Bind them in the data
            data.bindTo(entry, resources)
            data.markOk(entry)
          }
        }

        // Mark the entry as done
        data.markDone(entry)

        // Continue the work
        // todo: uncomment
        //workRec()
      }
      case false => {}
    }
    workRec()

    // Flush results
    data.flush()
  }

}
