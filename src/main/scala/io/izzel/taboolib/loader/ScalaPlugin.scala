package io.izzel.taboolib.loader

import org.bukkit.plugin.Plugin
import org.yaml.snakeyaml.Yaml

import scala.jdk.CollectionConverters._
import java.util.{Map => JMap}

import org.bukkit.plugin.java.JavaPlugin

class ScalaPlugin extends ScalaPluginInterface {

  private val plugin = {
    val stream = getClass.getResourceAsStream("/plugin.yml")
    val map = new Yaml().load(stream).asInstanceOf[JMap[String, Any]].asScala
    val cl = Class.forName(map("main").toString).asSubclass(classOf[JavaPlugin])
    JavaPlugin.getPlugin(cl)
  }

  override def onLoading(): Unit = {}

  override def onStarting(): Unit = {}

  override def onActivated(): Unit = {}

  override def onStopping(): Unit = {}
}

object ScalaPlugin {
  implicit def toPlugin(x: ScalaPlugin): Plugin = x.plugin
}