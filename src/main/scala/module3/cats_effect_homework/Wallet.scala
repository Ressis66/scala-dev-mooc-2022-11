package module3.cats_effect_homework

import cats.effect.{IO, Sync}
import cats.implicits._
import Wallet._
import cats.MonadError

import java.nio.file.{Files, Paths}

// DSL управления электронным кошельком
trait Wallet[F[_]] {
  // возвращает текущий баланс
  def balance: F[BigDecimal]
  // пополняет баланс на указанную сумму
  def topup(amount: BigDecimal): F[Unit]
  // списывает указанную сумму с баланса (ошибка если средств недостаточно)
  def withdraw(amount: BigDecimal): F[Either[WalletError, Unit]]
}

// Игрушечный кошелек который сохраняет свой баланс в файл
// todo: реализовать используя java.nio.file._
// Насчёт безопасного конкуррентного доступа и производительности не заморачиваемся, делаем максимально простую рабочую имплементацию. (Подсказка - можно читать и сохранять файл на каждую операцию).
// Важно аккуратно и правильно завернуть в IO все возможные побочные эффекты.
//
// функции которые пригодятся:
// - java.nio.file.Files.write
// - java.nio.file.Files.readString
// - java.nio.file.Files.exists
// - java.nio.file.Paths.get
final class FileWallet[F[_]: Sync](id: WalletId) extends Wallet[F]  {

  def balance: F[BigDecimal] =
    Sync[F].delay(if(Files.exists(Paths.get(id))) BigDecimal.apply(Files.readString(Paths.get(id)))
    else IO.raiseError(new RuntimeException("Boom!")))

  def topup(amount: BigDecimal): F[Unit] = for{
   bal <-balance.map(_ + amount)
    _ <- Sync[F].delay(Files.write(Paths.get(id), bal.toBigInt.toByteArray))
   } yield()

  def withdraw(amount: BigDecimal): F[Either[WalletError, Unit]] = {
    case Left(BalanceTooLow) => Wallet.BalanceTooLow
    case Right(_)  => for{
      bal <-balance.map(_ - amount)
      _ <- Sync[F].delay(Files.write(Paths.get(id), bal.toBigInt.toByteArray))
    } yield()
  }

}

object Wallet {

  // todo: реализовать конструктор
  // внимание на сигнатуру результата - инициализация кошелька имеет сайд-эффекты
  // Здесь нужно использовать обобщенную версию уже пройденного вами метода IO.delay,
  // вызывается она так: Sync[F].delay(...)
  // Тайпкласс Sync из cats-effect описывает возможность заворачивания сайд-эффектов
  def fileWallet[F[_]: Sync](id: WalletId): F[Wallet[F]] = Sync[F].delay(new FileWallet[F](id))
  type WalletId = String

  sealed trait WalletError
  case object BalanceTooLow extends WalletError
}