package futures

import HomeworksUtils.TaskSyntax

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object task_futures_sequence {

  /**
   * В данном задании Вам предлагается реализовать функцию fullSequence,
   * похожую на Future.sequence, но в отличии от нее,
   * возвращающую все успешные и не успешные результаты.
   * Возвращаемое тип функции - кортеж из двух списков,
   * в левом хранятся результаты успешных выполнений,
   * в правово результаты неуспешных выполнений.
   * Не допускается использование методов объекта Await и мутабельных переменных var
   */
  /**
   * @param futures список асинхронных задач
   * @return асинхронную задачу с кортежом из двух списков
   */
  def fullSequence[A](futures: List[Future[A]])
                     (implicit ex: ExecutionContext): Future[(List[A], List[Throwable])] = {
    def futureToFutureTry[T](f: Future[T]): Future[Try[T]] =
      f.map(Success(_)).recover { case x => Failure(x)}
    val listOfFutureTrys = futures.map(futureToFutureTry(_))
    val futureListOfTrys = Future.sequence(listOfFutureTrys)
    val successes = futureListOfTrys.map(_.collect{case Success(x)=>x})
    val failures = futureListOfTrys.map(_.collect{case Failure(x)=>x})
    successes.zip(failures)
  }


}
