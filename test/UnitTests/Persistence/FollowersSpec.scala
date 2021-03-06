package UnitTests.Persistence

import java.util.UUID

import Utility.CustomSpec
import models.Follower
import org.scalatest.time.SpanSugar._
import services.{Followed, Following}

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

/**
 * Created by Fer on 31/05/2015.
 */

//These test rely on each other. if one the above fails the others behind that one will probably fail

// IMPORTANT: Cassandra doesn't permit duplicates, when you insert the same data on the same table
// and keyspace it will update it, so i do not test against it
class FollowersSpec extends CustomSpec{

  val accountA = "accountA"
  val networkA = "networkA"
  val networkB = "networkB"

  val deviceA = "deviceA"
  val deviceB = "deviceB"
  val deviceC = "deviceC"

  val deviceA_ID = UUID.randomUUID()
  val deviceB_ID = UUID.randomUUID()
  val deviceC_ID = UUID.randomUUID()

  val followerA = Follower(accountA,networkA,deviceA_ID,deviceB_ID,deviceA,deviceB)
  val followerB = Follower(accountA,networkA,deviceA_ID,deviceC_ID,deviceA,deviceC)
  val followerC = Follower(accountA,networkA,deviceB_ID,deviceC_ID,deviceB,deviceC)

  "The Follower Persistence Layer" should "be able to insert new followers" in{
    //We create some mock followers (no real devices)
    // (B <- A) ; (C <- A) ; (C <- B)
    val insertionSequence = Future.sequence(List(
      Followed.insertNewFollowed(followerA),
      Followed.insertNewFollowed(followerB),
      Followed.insertNewFollowed(followerC)
    ))
     //Synchronous (only for unit testing))
    Await.ready(insertionSequence, 5 seconds).onFailure{case t => fail("Fail to insert rows in the Followed Table")}
  }

  it should "be able to insert new followings" in{
    //We create some mock followers (no real devices)
    // (A -> B) ; (A -> C) ; (B -> C)
    val insertionSequence = Future.sequence(List(
      Following.insertNewFollowing(followerA),
      Following.insertNewFollowing(followerB),
      Following.insertNewFollowing(followerC)
    ))
    //Synchronous (only for unit testing))
    Await.ready(insertionSequence, 5 seconds).onFailure{case t => fail("Fail to insert rows in the Followed Table")}
  }


  it should "be able to get the device's followers" in{
    Followed.getFollowersOfDevice(accountA,networkA,deviceC_ID) onComplete{
      case Success(followers) => assertResult(2)(followers.size)
      case Failure(t) => fail("There was an error retrieving the Followers")
    }
  }

  it should "be able to get the device's followings" in{
    Following.getFollowingsOfDevice(accountA,networkA,deviceA_ID) onComplete{
      case Success(followings) => assertResult(2)(followings.size)
      case Failure(t) => fail("There was an error retrieving the Followings")
    }
  }

  it should "be able to delete a follower relation" in{
    val delete = Followed.deleteFollowed(followerB)

    delete onFailure{
      case failure => fail("Could not delete the row")
    }
    //TODO: onSuccess compare the number of Followers with 'assert'
  }

  it should "be able to delete a following relation" in {
    val delete = Following.deleteFollowing(followerB)

    delete onFailure{
      case failure => fail("Could not delete the row")
    }
    //TODO: onSuccess compare the number of Followers with 'assert'
  }

  it should "be able to get all the 'Follower' objects belonging to a network from the DB" is pending

}
