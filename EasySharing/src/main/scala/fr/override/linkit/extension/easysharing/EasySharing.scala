package fr.`override`.linkit.extension.easysharing

import fr.`override`.linkit.skull.Relay
import fr.`override`.linkit.skull.internal.plugin.Plugin
import fr.`override`.linkit.extension.controller.ControllerExtension
import fr.`override`.linkit.extension.controller.cli.CommandManager
import fr.`override`.linkit.extension.easysharing.clipboard.{RemoteClipboard, RemotePasteCommand}
import fr.`override`.linkit.extension.easysharing.screen.{RemoteScreen, RemoteScreenCommand}

class EasySharing(relay: Relay) extends Plugin(relay) {

    private val remoteClipboard = new RemoteClipboard()
    private val remoteScreen = new RemoteScreen(relay.network)

    override def onLoad(): Unit = {
        putFragment(remoteClipboard)
        putFragment(remoteScreen)
    }

    override def onEnable(): Unit = {
        val commandManager = getFragmentOrAbort(classOf[ControllerExtension], classOf[CommandManager])
        commandManager.register("paste", new RemotePasteCommand(relay))
        commandManager.register("spectate", new RemoteScreenCommand(remoteScreen))
    }

}
