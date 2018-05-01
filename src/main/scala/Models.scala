import java.time.LocalDateTime
import scalaz.State


package object Models {
  case class Poll(name: String,
                  anonymity: Boolean,
                  visibility: Boolean,
                  startTime: Option[LocalDateTime],
                  stopTime: Option[LocalDateTime])

  object Poll {
    def couldStart(mp: Option[Poll]): Boolean = mp match {
      case Some(p) =>
        p.startTime match {
          case None => true
          case Some(_) => false
        }
      case None => false
    }

    def couldStop(mp: Option[Poll]): Boolean = mp match {
      case Some(p) =>
        p.stopTime match {
          case Some(_) => false
          case None if Poll.isStarted(LocalDateTime.now)(p) => true
          case _ => false
        }
      case None => false
    }

    def isStarted(now: LocalDateTime)(poll: Poll): Boolean =
      poll.startTime.exists(now.isAfter)

    def isStopped(now: LocalDateTime)(poll: Poll): Boolean  =
      poll.stopTime.exists(now.isAfter)

    def isActive(now: LocalDateTime)(poll: Poll): Boolean =
      isStarted(now)(poll) && !isStopped(now)(poll)

    def isVisible(now: LocalDateTime)(poll: Poll): Boolean =
      poll.visibility && isStarted(now)(poll) || !poll.visibility && isStopped(now)(poll)
  }

  case class Polls(map: Map[Int, Poll])

  object Polls {
    private val id = Stream.from(1).iterator

    def addPoll(p: Poll): State[Polls, Int] = {
      val id = this.id.next()
      for {
        _ <- State.modify[Polls] { s => s.copy(map = s.map + (id -> p)) }
      } yield id
    }

    def deletePoll(id: Int): State[Polls, Option[Int]] =
      for {
        mp <- State.gets[Polls, Option[Poll]](s => s.map.get(id))
        r <- State.apply[Polls, Option[Int]]( s => mp match {
          case Some(_) => s.copy(map = s.map - id) -> Some(id)
          case None => s -> None
        } )
      } yield r

    def startPoll(id: Int): State[Polls, Option[Int]] =
      for {
        mp <- State.gets[Polls, Option[Poll]](s => s.map.get(id))
        r <- State.apply[Polls, Option[Int]]{ s =>
          if (Poll.couldStart(mp))
            s.copy(map = s.map.updated(id, mp.get.copy(startTime = Option(LocalDateTime.now())))) -> Option(id)
          else
            s -> None
        }
      } yield r

    def stopPoll(id: Int): State[Polls, Option[Int]] =
      for {
        mp <- State.gets[Polls, Option[Poll]](s => s.map.get(id))
        r <- State.apply[Polls, Option[Int]]{ s =>
          if (Poll.couldStop(mp))
            s.copy(map = s.map.updated(id, mp.get.copy(stopTime = Option(LocalDateTime.now())))) -> Option(id)
          else
            s -> None
        }
      } yield r
  }
}
