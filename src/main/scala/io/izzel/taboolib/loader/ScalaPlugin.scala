package io.izzel.taboolib.loader

import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class ScalaPlugin extends ScalaPluginInterface {

  private val plugin = {
    JavaPlugin.getPlugin(classOf[ScalaLoader])
  }

  override def onLoading(): Unit = {}

  override def onStarting(): Unit = {}

  override def onActivated(): Unit = {}

  override def onStopping(): Unit = {}
}

object ScalaPlugin {
  implicit def toPlugin(x: ScalaPlugin): Plugin = x.plugin
}