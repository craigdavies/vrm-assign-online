package helpers.steps.hooks

import composition.TestGlobal
import cucumber.api.java.After
import cucumber.api.java.Before
import play.api.test.FakeApplication
import play.api.test.TestServer

final class TestServerHooks {

  import helpers.steps.hooks.TestServerHooks._

  private val testServer: TestServer = TestServer(port = port, application = fakeAppWithTestGlobal)

  @Before(order = 500)
  def startServer() = {
    testServer.start()
  }

  @After(order = 500)
  def stopServer() = {
    testServer.stop()
  }
}

object TestServerHooks {

  private final val port: Int = 9005
  private lazy val fakeAppWithTestGlobal: FakeApplication = FakeApplication(withGlobal = Some(TestGlobal))
}