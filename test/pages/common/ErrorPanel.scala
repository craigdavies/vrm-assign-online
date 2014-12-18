package pages.common

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDSL
import org.openqa.selenium.{By, WebDriver}

object ErrorPanel extends WebBrowserDSL {
  def numberOfErrors(implicit driver: WebDriver): Int =
    driver.findElement(By.cssSelector(".validation-summary")).findElements(By.tagName("li")).size

  def text(implicit driver: WebDriver): String =
    driver.findElement(By.cssSelector(".validation-summary")).getText
}