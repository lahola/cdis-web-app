package controllers

import java.util.{ Date, Calendar }

import models.StockReport
//import org.apache.commons.math3.stat.correlation.Covariance
//import org.apache.commons.math3.stat.descriptive.moment.Variance
import play.api._
import play.api.db._
import play.api.libs.ws.{ WSResponse, WS }
import play.api.mvc._

import scala.collection.JavaConversions._

import yahoofinance._

import scala.concurrent.Future
import scala.util.matching.Regex
import scala.util.{ Failure, Success }
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

object StockReporter extends Controller {

  def invalidReport(symbol: String): StockReport = {
    StockReport(
      "Stock Not Found",
      symbol,
      "Invalid",
      new Date(),
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0)
  }

  def index(symbol: String) = Action.async {
    println("index called")
    val stock = YahooFinance.get(symbol)
    val quote = stock.getQuote
    val history = stock.getHistory

    println(history)
    if (history.eq(null) || history.isEmpty) {
      scala.concurrent.Future { println("Invalid Report!") }.map(a => Ok(views.html.stockReport(invalidReport(symbol))))
    } else {
      val stats = stock.getStats
      val dividend = stock.getDividend

      try {
        models.Stock.create(stock.getName, symbol)
      } catch {
        case e: Exception => println(e.printStackTrace())
      }

      //		val name = stock.getName
      //		val theSymbol = stock.getSymbol
      //		val exchange = stock.getStockExchange
      //		val date = new Date()
      //		val current = quote.getPrice
      //		val previous = history.get(1).getClose
      //		val high = quote.getYearHigh
      //		val low = quote.getYearLow
      //		val peg = stats.getPeg
      //		val pe = stats.getPe
      //		val theYield = dividend.getAnnualYield
      //		val theYieldPercent = dividend.getAnnualYieldPercent
      //		val eps = stats.getEps

      // Get Beta and Debt/Equity
      WS.url(s"http://finance.yahoo.com/q/ks?s=$symbol+Key+Statistics").get().map { response =>

        def getDebtEquity: BigDecimal = {
          val debtEquityRatio = findStat(response.body, debtEquityPattern)
          val debtEquityRatioTrimmed = debtEquityRatio.replaceAll(",", "")
          debtEquityRatioTrimmed match {
            case null =>
              0.0
            case "" =>
              0.0
            case _ =>
              BigDecimal(debtEquityRatioTrimmed)
          }
        }

        def getBeta: BigDecimal = {
          val beta = findStat(response.body, betaPattern)
          val betaTrimmed = beta.replaceAll(",", "")
          betaTrimmed match {
            case null =>
              0.0
            case "" =>
              0.0
            case _ =>
              BigDecimal(betaTrimmed)
          }
        }

        println("done")
        val report = StockReport(
          stock.getName,
          stock.getSymbol,
          stock.getStockExchange,
          new Date(),
          quote.getPrice,
          history.get(1).getClose,
          quote.getYearHigh,
          quote.getYearLow,
          stats.getPeg,
          stats.getPe,
          getBeta,
          dividend.getAnnualYield,
          dividend.getAnnualYieldPercent,
          stats.getEps,
          getDebtEquity)

        Ok(views.html.stockReport(report))
      }
    }
  }

  def findStat(html: String, pattern: Regex): String = {
    val matches = pattern.findFirstMatchIn(html)
    matches match {
      case Some(m) => m.group(1)
      case None => ""
    }
  }

  val betaPattern = """Beta:</td><td class="yfnc_tabledata1">(\d+.\d+)</td></tr>""".r
  val debtEquityPattern = """Total Debt/Equity \(mrq\):</td><td class="yfnc_tabledata1">(\d+.\d+)</td></tr>""".r
  val namePattern = """<div class="hd"><div class="title"><h2>(.+) \(.+\)</h2>""".r
  val symbolPattern = """<div class="hd"><div class="title"><h2>.+ \((.+)\)</h2>""".r
  val pePattern = """Trailing P/E \(ttm, intraday\):</td><td class=\"yfnc_tabledata1\">(\d+.\d+)</td>""".r

  def advanced(symbol: String) = Action {
    val futureResult: Future[WSResponse] = WS.url(s"http://finance.yahoo.com/q/ks?s=$symbol+Key+Statistics").get()

    futureResult onComplete {
      case Success(response) =>
        val theStock = YahooFinance.get(symbol)
        val name = theStock.getName
        val theSymbol = theStock.getSymbol
        val exchange = theStock.getStockExchange
        val date = new Date()
        val peRatio = findStat(response.body, pePattern)

      case Failure(_) => println("failure")
    }

    Ok("Yup")
  }

  def beta(symbol: String) = Action {
    Ok("WHAT")
  }

  def percentageChange(list: List[Double]): List[Double] = list match {
    case Nil => Nil
    case x :: Nil => Nil
    case x :: xs => math.round(((x - xs.head) / xs.head) * 10000) / 100 :: percentageChange(xs)
  }

  def calculateBeta(symbol: String): Double = {

    println("CalculateBetaCAlled")
    val fromDate = Calendar.getInstance
    fromDate.add(Calendar.MONTH, -37)
    val toDate = Calendar.getInstance

    val stock = YahooFinance.get(symbol)
    val stockHistory = stock.getHistory(fromDate, toDate)
    val stockCloseHistory = (stockHistory map (_.getClose.doubleValue())).toList
    val stockPercentageChange: List[Double] = percentageChange(stockCloseHistory)

    val snp = YahooFinance.get("SPY")
    val snpHistory = snp.getHistory(fromDate, toDate)
    val snpCloseHistory: List[Double] = (snpHistory map (_.getClose.doubleValue())).toList
    val snpPercentageChange: List[Double] = percentageChange(snpCloseHistory)

    //    val covariance = new Covariance()
    //    val variance = new Variance()
    //    val theBeta = math.round((covariance.covariance(stockPercentageChange.toArray, snpPercentageChange.toArray) / variance.evaluate(snpPercentageChange.toArray)) * 100)
    //    val betaRounded: Double = theBeta / 100.0
    //    return betaRounded
    0.0
  }
}