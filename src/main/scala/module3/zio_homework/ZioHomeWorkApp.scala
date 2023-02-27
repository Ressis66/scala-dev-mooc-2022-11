package module3.zio_homework

import zio.clock.Clock
import zio.console.Console
import zio.random.Random
import zio.{ExitCode, URIO, ZLayer}

object ZioHomeWorkApp extends zio.App {

  val env: ZLayer[Any, Throwable, PrintEffectRunningTimeService] = PrintEffectRunningTimeService.live
  override def run(args: List[String]): URIO[Clock with Random with Console, ExitCode] =
    appWithTimeLogg.provideSomeLayer[Clock with Random with Console] (env).exitCode
}
