import scala.io.Source
import info.mukel.telegrambot4s.api._
import info.mukel.telegrambot4s.methods._
import info.mukel.telegrambot4s.models._


object TelegramBot extends TelegramBot with Polling {
  lazy val token = Source.fromFile("src\\main\\scala\\bot.token").getLines().mkString // safe token

  override def receiveMessage(msg: Message): Unit = {
    for (text <- msg.text)
      request(SendMessage(msg.source, {
        ???
      }
      ))
  }

}