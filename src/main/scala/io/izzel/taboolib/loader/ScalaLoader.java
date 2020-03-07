package io.izzel.taboolib.loader;

import io.izzel.taboolib.module.dependency.TDependency;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;

public class ScalaLoader extends BasePlugin {

    private ScalaPluginInterface scalaMain;

    @Override
    public void onLoading() {
        TDependency.requestLib("org.scala-lang:scala-library:2.13.1",TDependency.MAVEN_REPO,"");
        InputStream stream = getClass().getResourceAsStream("/plugin.yml");
        Object load = new Yaml().load(stream);
        String main = (String) ((Map<?, ?>) load).get("scalaMain");
        try {
            Class<?> cl = Class.forName(main + "$");
            Field field = cl.getDeclaredField("MODULE$");
            field.setAccessible(true);
            scalaMain = (ScalaPluginInterface) field.get(null);
            scalaMain.onLoading();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onStarting() {
        scalaMain.onStarting();
    }

    @Override
    public void onActivated() {
        scalaMain.onActivated();
    }

    @Override
    public void onStopping() {
        scalaMain.onStopping();
    }
}
