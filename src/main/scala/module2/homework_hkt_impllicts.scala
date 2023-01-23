package module2

import module2.homework_hkt_impllicts.Tuple.fToBindable

import scala.language.implicitConversions

object homework_hkt_impllicts{

    /**
      * 
      * Доработать сигнатуру tupleF и реализовать его
      * По итогу должны быть возможны подобные вызовы
      *   val r1 = println(tupleF(optA, optB))
      *   val r2 = println(tupleF(list1, list2))
      * 
      */
    def tupleF[F[_], A, B](fa: F[A], fb: F[B]) = fa.flatMap(a => fb.map(b => (a, b)))


    trait Bindable[F[_], A] {
        def map[B](f: A => B): F[B]
        def flatMap[B](f: A => F[B]): F[B]
    }

  object Tuple {
    implicit def fToBindable[F[_], A](f1: F[A]): Bindable[F, A] =
      f1.asInstanceOf[Bindable[F, A]]

  }

  val optA: Option[Int] = Some(1)
  val optB: Option[Int] = Some(2)

  val list1 = List(1, 2, 3)
  val list2 = List(4, 5, 6)

  val r1 = println(tupleF(optA, optB))
  val r2 = println(tupleF(list1, list2))
}