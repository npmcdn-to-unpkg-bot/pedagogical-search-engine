package ws.exploration

import slick.lifted.TableQuery

import slick.driver.MySQLDriver.api._

object Queries {
  private val clicksTQ = TableQuery[mysql.slick.tables.Clicks]
  private val classificationTQ = TableQuery[mysql.slick.tables.Classifications]
  private val messagesTQ = TableQuery[mysql.slick.tables.Messages]

  def allClicks() = {
    val clicks = for {
      click <- clicksTQ
    } yield click
  }
}
