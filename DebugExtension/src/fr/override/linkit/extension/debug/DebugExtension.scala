package fr.`override`.linkit.extension.debug

import fr.`override`.linkit.`extension`.controller.ControllerExtension
import fr.`override`.linkit.`extension`.controller.cli.CommandManager
import fr.`override`.linkit.`extension`.debug._
import fr.`override`.linkit.`extension`.debug.commands.{NetworkCommand, PingCommand, SendMessageCommand}
import fr.`override`.linkit.`extension`.debug.tests.{TestCommand, TestRemoteFragment}
import fr.`override`.linkit.api.Relay
import fr.`override`.linkit.api.`extension`.RelayExtension

class DebugExtension(relay: Relay) extends RelayExtension(relay) {

    override def onLoad(): Unit = {
        putFragment(new TestRemoteFragment(relay))
    }

    override def onEnable(): Unit = {
        val completerHandler = relay.taskCompleterHandler

        completerHandler.register(PingTask.Type, (_, _) => PingTask.Completer())

        val commandManager = getFragmentOrAbort(classOf[ControllerExtension], classOf[CommandManager])

        commandManager.register("ping", new PingCommand(relay))
        commandManager.register("msg", new SendMessageCommand(relay))
        commandManager.register("test", new TestCommand(relay))
        commandManager.register("network", new NetworkCommand(relay.network))

        val network = relay.network
        network.listEntities.foreach(_.addOnStateUpdate(println))
        network.addOnEntityAdded(_.addOnStateUpdate(println))
    }
}
