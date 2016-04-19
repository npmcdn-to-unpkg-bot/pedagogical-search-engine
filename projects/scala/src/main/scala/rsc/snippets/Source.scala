package rsc.snippets

object Source extends Enumeration {
  type Source = Value

  val title = Value("title")
  val toc = Value("toc")
  val description = Value("description")
  val keywords = Value("keywords")
  val categories = Value("categories")
  val domains = Value("domains")
  val subdomains = Value("subdomains")
}
