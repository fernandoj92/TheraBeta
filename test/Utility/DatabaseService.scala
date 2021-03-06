package Utility

import com.websudos.phantom.Implicits.ResultSet
import com.websudos.phantom.testing.PhantomCassandraConnector
import org.scalatest.time.SpanSugar._
import play.api.libs.concurrent.Execution.Implicits._
import services._

import scala.concurrent.{Await, Future}

/**
 * Created by Fer on 04/03/2015.
 */
object DatabaseService extends PhantomCassandraConnector{

  def init(): Future[List[ResultSet]] = {
    val create = Future.sequence(List(
      Devices.create.future(),
      DevicesByAccount.create.future(),
      Messages.create.future(),
      Accounts.create.future(),
      Networks.create.future(),
      Followed.create.future(),
      Following.create.future()
    ))

    Await.ready(create, 5 seconds)
  }

  def cleanup(): Future[List[ResultSet]] = {
    val truncate = Future.sequence(List(
      Devices.truncate.future(),
      DevicesByAccount.truncate.future(),
      Messages.truncate.future(),
      Accounts.truncate.future(),
      Networks.create.future(),
      Followed.truncate.future(),
      Following.truncate.future()
    ))
    Await.ready(truncate, 5 seconds)
  }
}
