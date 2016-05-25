package ws.exploration.statistics

import ws.indices.indexentry.EngineType

class Printer(stat: Statistics) {
  def clickCount(): String = {
    val bing = stat.clickCount(EngineType.Bing)
    val wc = stat.clickCount(EngineType.Wikichimp)
    val wcft = stat.clickCount(EngineType.WikichimpFT)
    s"Count(clicks): Bing($bing), wc($wc), wcft($wcft)"
  }
}
