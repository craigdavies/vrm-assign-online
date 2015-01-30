package composition

import _root_.webserviceclients.fakes.DateServiceConstants.{DayValid, MonthValid, YearValid}
import com.tzavellas.sse.guice.ScalaModule
import org.joda.time.DateTime
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear

final class TestDateServiceBinding extends ScalaModule with MockitoSugar {

  def build() = {
    val dateTimeISOChronology: String = new DateTime(
      YearValid.toInt,
      MonthValid.toInt,
      DayValid.toInt,
      0,
      0).toString
    val today = DayMonthYear(
      DayValid.toInt,
      MonthValid.toInt,
      YearValid.toInt
    )
    val dateTime = new DateTime(
      YearValid.toInt,
      MonthValid.toInt,
      DayValid.toInt,
      0,
      0)
    val now = dateTime.toInstant

    val dateService = mock[DateService]
    when(dateService.dateTimeISOChronology).thenReturn(dateTimeISOChronology)
    when(dateService.today).thenReturn(today)
    when(dateService.now).thenReturn(now)

    dateService
  }

  def configure() = {

    bind[DateService].toInstance(build())
  }
}