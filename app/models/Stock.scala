package models

import scalikejdbc._
import skinny.orm._
import org.joda.time._

/**
 * Created by lou on 5/2/15.
 */
case class Stock(name: String,
  symbol: String)

object Stock extends SkinnyCRUDMapper[Stock] {
  override lazy val defaultAlias = createAlias("st")
  private[this] lazy val t = defaultAlias

  override def extract(rs: WrappedResultSet, rn: ResultName[Stock]): Stock = new Stock(
    name = rs.get(rn.name),
    symbol = rs.get(rn.symbol)
  )

  def create(name: String, symbol: String) = {
    createWithNamedValues(
      column.name -> name,
      column.symbol -> symbol
    )
  }

  def findStockBySymbol(symbol: String): Option[Stock] = {
    where(sqls.eq(t.symbol, symbol)).apply().headOption
  }
}

