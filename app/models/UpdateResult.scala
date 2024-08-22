package models

sealed trait UpdateResult
case object Updated extends UpdateResult
case object Failed extends UpdateResult

class CustomException(message: String) extends Exception(message)