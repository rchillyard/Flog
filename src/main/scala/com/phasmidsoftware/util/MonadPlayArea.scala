package com.phasmidsoftware.util

trait Monad[F[_]] {
    def bind[A, B]: (A => F[B]) => F[A] => F[B]
    def pure[A]: A => F[A]
}

object Monad {
    def sequence[F[_]: Monad, A]: List[F[A]] => F[List[A]] = {
        ???
//        afs: List[F[A]] => for (af <- afs) yield for (a <- af) yield
    }

}