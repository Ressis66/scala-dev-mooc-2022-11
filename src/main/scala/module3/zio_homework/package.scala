package module3

import module3.zioConcurrency.printEffectRunningTime
import module3.zio_homework.config.Configuration
import zio.{Chunk, Exit, Has, IO, Task, ULayer, URIO, ZIO, ZLayer}
import zio.clock.{Clock, sleep}
import zio.console._
import zio.duration.durationInt
import zio.macros.accessible
import zio.random._
import zio.stream.{Sink, ZStream}

import java.io.IOException
import java.util.concurrent.TimeUnit
import scala.collection.IterableOnce.iterableOnceExtensionMethods
import scala.concurrent.Future
import scala.io.StdIn
import scala.language.postfixOps

package object zio_homework {
  /**
   * 1.
   * Используя сервисы Random и Console, напишите консольную ZIO программу которая будет предлагать пользователю угадать число от 1 до 3
   * и печатать в когнсоль угадал или нет. Подумайте, на какие наиболее простые эффекты ее можно декомпозировать.
   */


  lazy val guessProgram: ZIO[Random with Console, IOException, Unit] = for{
    console <- ZIO.environment[Console].map(_.get)
    random <- ZIO.environment[Random].map(_.get)
    _ <- console.putStrLn("Guess, what integer I have mentioned")
    int1 <- console.getStrLn.map(_.toInt)
    int2 <- random.nextInt
    _ <- console.putStrLn((int1.equals(int2)).toString)
  } yield ()

  /**
   * 2. реализовать функцию doWhile (общего назначения), которая будет выполнять эффект до тех пор, пока его значение в условии не даст true
   * 
   */

  def doWhile  [R, E, A](zio: ZIO[R, E, A])(f: A => Boolean): ZIO[R, E, A] =
  zio.flatMap(a => if (f(a)) doWhile(zio)(f) else ZIO.succeed(a))



  /**
   * 3. Реализовать метод, который безопасно прочитает конфиг из файла, а в случае ошибки вернет дефолтный конфиг
   * и выведет его в консоль
   * Используйте эффект "load" из пакета config
   */


  def loadConfigOrDefault = config.load.orElse(ZIO.effect(println(Configuration.live)))


  /**
   * 4. Следуйте инструкциям ниже для написания 2-х ZIO программ,
   * обратите внимание на сигнатуры эффектов, которые будут у вас получаться,
   * на изменение этих сигнатур
   */


  /**
   * 4.1 Создайте эффект, который будет возвращать случайеым образом выбранное число от 0 до 10 спустя 1 секунду
   * Используйте сервис zio Random
   */
  lazy val eff: ZIO[Random with Clock, Nothing, Int] = for{
    _ <- sleep(1 second)
    random <- ZIO.environment[Random].map(_.get)
    int <- random.nextInt
  } yield (int)




  /**
   * 4.2 Создайте коллукцию из 10 выше описанных эффектов (eff)
   */
  lazy val effects  = List(eff,eff,eff,eff,eff,eff,eff,eff,eff,eff)

  
  /**
   * 4.3 Напишите программу которая вычислит сумму элементов коллекци "effects",
   * напечатает ее в консоль и вернет результат, а также залогирует затраченное время на выполнение,
   * можно использовать ф-цию printEffectRunningTime, которую мы разработали на занятиях
   */




    val app: ZIO[Random with Clock with Console, Nothing, Unit] = for {
      console <- ZIO.environment[Console].map(_.get)
      e <- ZIO.collectAll(effects).map(x => x.sum)
      _ <- console.putStrLn(e.toString)
     // _ <- printEffectRunningTime(app)
    } yield()


  /**
   * 4.4 Усовершенствуйте программу 4.3 так, чтобы минимизировать время ее выполнения
   */

  lazy val appSpeedUp : ZIO[Random with Clock with Console, Nothing, Unit] = for {
    console <- ZIO.environment[Console].map(_.get)
    e <- ZIO.collectAllPar(effects).map(x => x.sum)
    _ <- console.putStrLn(e.toString)
    _ <- printEffectRunningTime(app)
  } yield()


  /**
   * 5. Оформите ф-цию printEffectRunningTime разработанную на занятиях в отдельный сервис, так чтобы ее
   * молжно было использовать аналогично zio.console.putStrLn например
   */

   type PrintEffectRunningTimeService = Has[PrintEffectRunningTimeService.Service]
   @accessible
   object PrintEffectRunningTimeService{
     trait Service{
        def printEffectRunningTimeService[R, E, A](zio: ZIO[R, E, A]): ZIO[Clock with R, E, A]
     }
     val live =ZLayer.succeed(
       new Service{
         override def printEffectRunningTimeService[R, E, A](zio: ZIO[R, E, A]): ZIO[Clock with R, E, A] = printEffectRunningTime(zio)
       }
     )
   }

  /**
     * 6.
     * Воспользуйтесь написанным сервисом, чтобы созадть эффект, который будет логировать время выполнения прогаммы из пункта 4.3
     *
     * 
     */

  lazy val appWithTimeLogg  = ZIO.accessM[PrintEffectRunningTimeService with Random  with Clock with Console](_.get.printEffectRunningTimeService(app))

  /**
    * 
    * Подготовьте его к запуску и затем запустите воспользовавшись ZioHomeWorkApp
    */
  val list:List[String] = List("a", "b")

  lazy val runApp = ZioHomeWorkApp.run(list)


}
