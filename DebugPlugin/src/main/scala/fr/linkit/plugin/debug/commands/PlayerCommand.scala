/*
 *  Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can only use it for personal uses, studies or documentation.
 *  You can download this source code, and modify it ONLY FOR PERSONAL USE and you
 *  ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 *
 *  Please contact maximebatista18@gmail.com if you need additional information or have any
 *  questions.
 */

package fr.linkit.plugin.debug.commands

import fr.linkit.api.connection.cache.SharedCacheManager
import fr.linkit.api.connection.cache.repo.description.annotation.InvocationKind
import fr.linkit.engine.connection.cache.repo.DefaultEngineObjectCenter
import fr.linkit.engine.connection.cache.repo.description.WrapperBehaviorBuilder
import fr.linkit.engine.connection.cache.repo.description.WrapperBehaviorBuilder.MethodControl
import fr.linkit.engine.connection.cache.repo.description.annotation.AnnotationBasedMemberBehaviorFactory
import fr.linkit.plugin.controller.cli.{CommandException, CommandExecutor, CommandUtils}

import java.util.Scanner
import scala.collection.mutable.ListBuffer

class PlayerCommand(cacheHandler: SharedCacheManager, supportIdentifier: String) extends CommandExecutor {

    /*println("Press enter to continue...")
    new Scanner(System.in).nextLine()*/

    private val bhv     = new WrapperBehaviorBuilder[ListBuffer[Player]](new AnnotationBasedMemberBehaviorFactory()) {
        annotateAll by MethodControl(InvocationKind.ONLY_LOCAL)
        annotateAll("+=") and "addOne" by MethodControl(InvocationKind.LOCAL_AND_REMOTES, false, "1")
    }.build
    private val repo    = cacheHandler.getCache(50, DefaultEngineObjectCenter[ListBuffer[Player]]())
    private val players = repo.findObject(0).getOrElse(repo.postObject(0, ListBuffer.empty[Player], bhv))
    println(s"players = ${players}")
    /*println(s"players.getClass.getDeclaredFields = ${players.getClass.getDeclaredFields.mkString("Array(", ", ", ")")}")
    println(s"LOL")*/

    override def execute(implicit args: Array[String]): Unit = {
        val order = if (args.length == 0) "" else args(0)
        //println(s"players.toSeq = ${players}")
        //println(s"players.getChoreographer.isMethodExecutionForcedToLocal = ${players.getChoreographer.isMethodExecutionForcedToLocal}")
        order match {
            case "test" =>
                println(s"Thread.currentThread() = ${Thread.currentThread()}")
                println(s"players.getClass.getDeclaredFields = ${players.getClass.getDeclaredFields}")
            case "create" => createPlayer(args.drop(1)) //remove first arg which is obviously 'create'
            case "update" => updatePlayer(args.drop(1)) //remove first arg which is obviously 'update'
            case "list"   => players.getChoreographer.forceLocalInvocation(println(s"players: $players"))
            case "desc"   => describePlayerClass()
            case _        => throw CommandException("usage: player [create|update] [...]")
        }
    }

    private def createPlayer(args: Array[String]): Unit = {
        implicit val usage: String = "usage: player create [id=?|name=?|x=?|y=?]"

        val id     = CommandUtils.getValue("id", args).toInt
        val name   = CommandUtils.getValue("name", args)
        val x      = CommandUtils.getValue("x", args).toInt
        val y      = CommandUtils.getValue("y", args).toInt
        val player = Player(id, supportIdentifier, name, x, y)

        //println(s"Created $player ! (identifier = $id)")
        players += player
        println(s"Added player $player in $players")
    }

    private def describePlayerClass(): Unit = {
        //println(s"Class ${classOf[Player]}:")
        classOf[Player].getDeclaredFields.foreach(println)
    }

    private def updatePlayer(args: Array[String]): Unit = {
        implicit val usage: String = "usage: player update [id=?] <name=?|x=?|y=?>"
        val id     = CommandUtils.getValue("id", args).toInt
        val player = players.find(_.id == id).getOrElse(throw CommandException("Player not found."))

        val name = CommandUtils.getValue("name", player.name, args)
        val x    = CommandUtils.getValue("x", player.x.toString, args).toInt
        val y    = CommandUtils.getValue("y", player.y.toString, args).toInt

        //println(s"Updating player $player...")
        player.x = x
        player.y = y
        player.name = name
        //println(s"Player is now $player")
    }

}