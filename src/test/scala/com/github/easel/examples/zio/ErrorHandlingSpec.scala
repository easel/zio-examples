package com.github.easel.examples.zio

import scala.concurrent.Future

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{EitherValues, MustMatchers, WordSpec}
import zio.{DefaultRuntime, FiberFailure, IO, Task, ZIO}

class ErrorHandlingSpec extends WordSpec with MustMatchers with EitherValues with ScalaFutures with DefaultRuntime {


  def produceSuccessful: Future[Either[ExampleError, String]] =
    Future.successful(Right("success!"))

  def produceDefect: Future[Either[ExampleError, String]] =
    Future.successful(Left(KnownError("defect")))

  def produceFailure: Future[Either[ExampleError, String]] = Future.failed(new RuntimeException("failure"))

  "zio error handling" should {
    "run a successful task" in {
      val io: Task[Either[ExampleError, String]] = ZIO.fromFuture(implicit ec => produceSuccessful)
      unsafeRun(io) mustBe Right("success!")
    }
    "run a defective future" in {
      val io: Task[Either[ExampleError, String]] = ZIO.fromFuture(implicit ec => produceDefect)
      unsafeRun(io) mustBe Left(KnownError("defect"))
    }
    "run a defective future into a task" in {
      val io: Task[String] = for {
        a <- ZIO.fromFuture(implicit ec => produceDefect)
        b <- ZIO.fromEither(a)
      } yield b
      unsafeRun(io.either) mustBe Left(KnownError("defect"))
    }
    "run a defective future into an io by mapping" in {
      val io: IO[ExampleError, String] = for {
        a <- ZIO.fromFuture(implicit ec => produceDefect).mapError(c => UnknownError(c))
        b <- ZIO.fromEither(a)
      } yield b
      unsafeRun(io.either) mustBe Left(KnownError("defect"))
    }
    "run a defective future into an io via absolve" in {
      val io: IO[Throwable, String] = ZIO.fromFuture(implicit ec => produceDefect).absolve
      unsafeRun(io.either) mustBe Left(KnownError("defect"))
    }
    "run a failing task" in {
      val io: Task[Either[ExampleError, String]] = ZIO.fromFuture(implicit ec => produceFailure)
      unsafeRun(io.either) mustBe ('left)
    }
    "run a failing task into a future" in {
      val io: Task[Either[ExampleError, String]] = ZIO.fromFuture(implicit ec => produceFailure)
      unsafeRunToFuture(io.either).futureValue mustBe ('left)
    }
  }

}
