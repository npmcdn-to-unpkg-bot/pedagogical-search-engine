package utils

import java.io.File

object Files {
  def explore(rootFolder: File): List[RelativeFile] = {
    def rec(folder: File, parents: List[String]): List[RelativeFile] = {
      // Add current folder to parents
      val folderName = folder.getName
      val newParents = parents:::List(folderName)

      // Get files in the current folder
      val fsNodes = folder.listFiles().toList
      val files = fsNodes.filter(node => node.isFile)

      // .. make them relative-files
      val newFiles = files.map(file => RelativeFile(file, newParents, folder))

      // Continue for sub-folders
      fsNodes.filter(node => node.isDirectory) match {
        case Nil => newFiles
        case folders => newFiles:::folders.flatMap(f => rec(f, newParents))
      }
    }

    // Remove the top folder from the "parents" field
    rec(rootFolder, Nil).map(file => file.copy(parents = file.parents.tail))
  }
}

case class RelativeFile(file: File, parents: List[String], rootFolder: File)
