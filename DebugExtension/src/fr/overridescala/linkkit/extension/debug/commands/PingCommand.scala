package fr.overridescala.linkkit.`extension`.debug.commands

import fr.overridescala.linkkit.`extension`.controller.cli.{CommandException, CommandExecutor}
import fr.overridescala.linkkit.`extension`.debug.PingTask
import fr.overridescala.linkkit.`extension`.controller.cli.CommandExecutor
import fr.overridescala.linkkit.api.Relay


class PingCommand(relay: Relay) extends CommandExecutor {
    override def execute(implicit args: Array[String]): Unit = {
        if (args.length != 1)
            throw CommandException("use : ping <target identifier>")
        val target = args(0)
        val ownIdentifier = relay.identifier
        println("pinging...")
        relay.scheduleTask(PingTask(target))
                .queue(
                    ping => println(s"$ownIdentifier <- $ping ms -> $target"),
                    errorMsg => Console.err.println(errorMsg)
                )
    }
}