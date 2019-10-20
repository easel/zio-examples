package com.github.easel.examples.zio

abstract class ExampleError extends Throwable {
  def message: String
  override def getMessage: String = message
}

final case class KnownError(message: String) extends ExampleError
final case class UnknownError(cause: Throwable) extends ExampleError {
  def message = cause.getMessage
}
