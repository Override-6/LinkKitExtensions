package fr.`override`.linkit.`extension`.controller.cli

import java.util.concurrent.PriorityBlockingQueue

import scala.io.StdIn

object InputConsole {

    private val ticketQueue = new PriorityBlockingQueue[InputRequestTicket]()

    def requestNextInput(priority: Int = 0): String = {
        val requestTicket = new InputRequestTicket(defNextPriority(priority))
        ticketQueue.add(requestTicket)
        requestTicket.getLine
    }

    def ask(msg: String, possibleResponses: String*): String = {
        println(msg)
        var input = requestNextInput(10)
        if (possibleResponses.isEmpty)
            input
        else {
            while (possibleResponses.forall(_.toLowerCase != input)) {
                println(msg)
                input = requestNextInput(10)
            }
            input
        }
    }

    private def defNextPriority(priority: Int): Int = {
        var queuePriority = priority
        ticketQueue.forEach(incPriority)

        def incPriority(ticket: InputRequestTicket): Unit = {
            if (ticket.priority < priority)
                return
            if (ticket.priority == queuePriority)
                queuePriority += 1
        }

        queuePriority
    }


    private def start(): Unit = {
        val consoleThread = new Thread(() => {
            while (true) {
                val ticket = ticketQueue.take()
                val line = StdIn.readLine()
                ticket.setLine(line)
            }
        })
        consoleThread.setName("Console Inputs Queue")
        consoleThread.start()
    }

    start()

    class InputRequestTicket(val priority: Int) extends Comparable[InputRequestTicket] {
        @volatile private var line: String = _
        private val threadOwner = Thread.currentThread()

        def getLine: String = {
            if (line == null)
                synchronized {
                    wait()
                }
            line
        }

        override def toString: String = s"owner : $threadOwner"

        def setLine(line: String): Unit = {
            this.line = line
            synchronized {
                notify()
            }
        }

        override def compareTo(o: InputRequestTicket): Int =
            o.priority - priority
    }

}
