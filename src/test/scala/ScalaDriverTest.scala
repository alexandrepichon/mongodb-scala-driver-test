
import org.junit.Test
import org.junit.Ignore
import org.scalatest.Matchers
import org.scalatest.junit.JUnitSuite

import org.mongodb.scala._

import scala.util.Success
import scala.util.Failure
import scala.util.Try
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ScalaDriverTest extends JUnitSuite with Matchers {

  val mongoClient: MongoClient = MongoClient("mongodb://localhost")
  val db = mongoClient.getDatabase("test")

  @Test def dbName {
    db.name should be("test")
  }

  trait UsersCollection {
    val usersCollection = db.getCollection("users")
  }

  @Test def insert {
    new UsersCollection {
      val doc = Document("""{name: 'bob'}""")
      usersCollection.insertOne(doc) should replyCompleted
    }
  }

  @Test def findOne {
    new UsersCollection {
      val observable = usersCollection.find()
      val future = observable.toFuture
      val res = Await.result(future, 100 millis)
      res should not be('empty)
    }
  }

  @Test def writeConcern {
    val collection = db.getCollection("users").withWriteConcern(WriteConcern.UNACKNOWLEDGED)
    val doc = Document("""{name: 'UNACKNOWLEDGED'}""")
    collection.insertOne(doc) should replyCompleted
  }

  private def replyCompleted = new org.scalatest.matchers.Matcher[Observable[Completed]] {
    def apply(observable: Observable[Completed]) = {
      val res: Try[Boolean] = Try {
        Await.result(observable.toFuture, 100 millis).head == Completed()
      }
      org.scalatest.matchers.MatchResult(
        res.isSuccess,
        res match {
          case Failure(e) => res + " was not a success " + res.asInstanceOf[Failure[Throwable]].get.getStackTrace.toString
          case _ => ""
        },
        observable + " is completed"
      )
    }
  }

}
