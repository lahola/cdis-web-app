package models

import java.util.Date

/**
 * Created by lou on 5/1/15.
 */
case class StockReport(name: String,
  symbol: String,
  stockExchange: String,
  date: Date,
  current: BigDecimal,
  previousMonth: BigDecimal,
  yearHigh: BigDecimal,
  yearLow: BigDecimal,
  pegRatio: BigDecimal,
  peTtm: BigDecimal,
  beta: BigDecimal,
  yearlyYield: BigDecimal,
  yearlyYieldPercent: BigDecimal,
  eps: BigDecimal,
  debtEquityRatio: BigDecimal)

//class StockReports(tag: Tag) extends Table[StockReport](tag, "STOCKS") {
//  def name = column[String]("name")
//  def symbol = column[String]("symbol")
//  def stockExchange = column[String]("stockExchange")
//
//  def * = (name, symbol) <> (StockReport.tupled, StockReport.unapply)
//}

