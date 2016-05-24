
import org.junit.Test
import org.junit.Ignore
import org.scalatest.Matchers
import org.scalatest.junit.JUnitSuite

import org.mongodb.scala._

import scala.util.Success
import scala.util.Failure
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ScalaDriverTest extends JUnitSuite with Matchers {

  val mongoClient: MongoClient = MongoClient("mongodb://localhost")
  val db = mongoClient.getDatabase("test")

  class PrintObserver[T]() extends Observer[T] {
    val prefix = "[PrintObserver]"
    override def onComplete(): Unit = {
      println(s"$prefix onComplete()")
    }
    override def onError(e: Throwable): Unit = {
      println(s"$prefix onError($e)")
    }
    override def onNext(result: T): Unit = {
      println(s"$prefix onNext($result)")
    }
  }

  @Test def dbName {
    db.name should be("test")
  }

  trait UsersCollection {
    val usersCollection = db.getCollection("users")
  }

  @Test def insert {
    new UsersCollection {
      val doc = Document("""{name: 'bob'}""")
      val observable = usersCollection.insertOne(doc)
      observable.subscribe(new PrintObserver[Completed]())
    }
  }

  @Test def findOne {
    new UsersCollection {
      val observable = usersCollection.find()
      observable.subscribe(new PrintObserver[Document]())
    }
  }

  @Test def future {
    new UsersCollection {
      val observable = usersCollection.find()
      val future = observable.toFuture
      val res = Await.result(future, 100 millis)
      res should not be('empty)
    }
  }

}