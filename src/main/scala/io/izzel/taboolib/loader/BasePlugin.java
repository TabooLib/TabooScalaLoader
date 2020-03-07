package io.izzel.taboolib.loader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.izzel.taboolib.PluginLoader;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.NumberConversions;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

/**
 * @Author 坏黑
 * @Since 2019-07-05 9:03
 * @version 1.2 (2019-10-22 13:59:13)
 */
public abstract class BasePlugin extends JavaPlugin {

    /**
     * 版本信息获取地址
     * 优先采用国内地址
     * 防止部分机器封禁海外访问
     */
    public static final String[][] URL = {
        {
            "https://skymc.oss-cn-shanghai.aliyuncs.com/plugins/latest.json",
            "https://skymc.oss-cn-shanghai.aliyuncs.com/plugins/TabooLib.jar"
        },
        {
            "https://api.github.com/repos/Bkm016/TabooLib/releases/latest",
            "https://github.com/Bkm016/TabooLib/releases/latest/download/TabooLib.jar",
        },
    };

    /**
     * 语言文件
     */
    public static final String[][] MESSAGE = {
        {
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 错误 ####################",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  初始化 §4TabooLib §c失败!",
            "§4[TabooLib] §c  无法获取版本信息或下载时出现错误.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  请检查服务器的互联网连接是否有效.",
            "§4[TabooLib] §c  手动将 §4TabooLib.jar §c放入服务端根目录.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  服务端将在 5 秒后继续启动.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  下载地址:",
            "§4[TabooLib] §c   §4https://github.com/Bkm016/TabooLib/releases",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 错误 ####################",
            "§4[TabooLib] §c"
        },
        {
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 错误 ####################",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  初始化 §4TabooLib §c失败!",
            "§4[TabooLib] §c  无法获取版本信息或下载时出现错误.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  请检查服务器的互联网连接是否有效.",
            "§4[TabooLib] §c  手动将 §4TabooLib.jar §c放入服务端根目录.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  下载地址:",
            "§4[TabooLib] §c   §4https://github.com/Bkm016/TabooLib/releases",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 错误 ####################",
            "§4[TabooLib] §c"
        },
        {
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 警告 ####################",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  初始化 §4TabooLib §c失败!",
            "§4[TabooLib] §c  当前运行的版本低于插件所需版本.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  已下载最新版.",
            "§4[TabooLib] §c  服务端将在 5 秒后重新启动.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 警告 ####################",
            "§4[TabooLib] §c"
        },
        {
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 警告 ####################",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  初始化 §4TabooLib §c失败!",
            "§4[TabooLib] §c  当前运行的版本低于插件所需版本.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  已下载最新版.",
            "§4[TabooLib] §c  将在下次启动服务端时启动插件.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 警告 ####################",
            "§4[TabooLib] §c"
        },
        {
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 警告 ####################",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  请勿将 §4TabooLib 5.0 §c放入插件文件夹中.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  已删除 §4{name}",
            "§4[TabooLib] §c  服务端将在 5 秒后重新启动.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 警告 ####################",
            "§4[TabooLib] §c",
        }
    };

    protected static BasePlugin plugin;
    protected static File libFile = new File("libs/TabooLib.jar");

    /**
     * 插件在初始化过程中出现错误
     * 将在 onLoad 方法下关闭插件
     */
    protected static boolean initFailed;
    protected static boolean forge = Loader.forName("net.minecraftforge.classloading.FMLForgePlugin", false, BasePlugin.class.getClassLoader()) != null;

    static {
        init();
    }

    @Override
    public final void onLoad() {
        if (initFailed) {
            setEnabled(false);
            return;
        }
        plugin = this;
        PluginLoader.addPlugin(this);
        PluginLoader.load(this);
        try {
            onLoading();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public final void onEnable() {
        if (initFailed) {
            return;
        }
        PluginLoader.start(this);
        try {
            onStarting();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Bukkit.getScheduler().runTask(this, () -> {
            PluginLoader.active(this);
            onActivated();
        });
    }

    @Override
    public final void onDisable() {
        if (initFailed) {
            return;
        }
        try {
            onStopping();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        PluginLoader.stop(this);
    }

    /**
     * 代替 JavaPlugin 本身的 onLoad 方法
     */
    public void onLoading() {
    }

    /**
     * 代替 JavaPlugin 本身的 onEnable 方法
     */
    public void onStarting() {
    }

    /**
     * 代替 JavaPlugin 本身的 onDisable 方法
     */
    public void onStopping() {
    }

    /**
     * 当服务端完全启动时执行该方法
     * 完全启动指 "控制台可以输入命令且得到反馈时"
     * <p>
     * 使用 @TSchedule 同样可以代替该方法
     */
    public void onActivated() {
    }

    /**
     * 检查 TabooLib 是否已经被载入
     * 跳过 TabooLib 主类的初始化过程
     */
    public static boolean isLoaded() {
        return Loader.forName("io.izzel.taboolib.TabooLib", false, Bukkit.class.getClassLoader()) != null;
    }

    /**
     * 获取 TabooLib 当前运行版本
     */
    public static double getVersion() {
        try {
            ZipFile zipFile = new ZipFile(libFile);
            return NumberConversions.toDouble(readFully(zipFile.getInputStream(zipFile.getEntry("__resources__/version")), StandardCharsets.UTF_8));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取 TabooLib 当前最新版本
     * 获取内容为版本号+访问地址+下载地址
     */
    public static String[] getNewVersion() {
        for (String[] url : URL) {
            String read = readFromURL(url[0]);
            if (read == null) {
                continue;
            }
            try {
                JsonObject jsonObject = (JsonObject) new JsonParser().parse(read);
                if (jsonObject.has("tag_name")) {
                    return new String[] {jsonObject.get("tag_name").getAsString(), url[0], url[1]};
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return null;
    }

    private static void LoadByPlugin() {
        try {
            org.bukkit.plugin.Plugin plugin = Bukkit.getPluginManager().loadPlugin(libFile);
            plugin.onLoad();
            Bukkit.getPluginManager().enablePlugin(plugin);
        } catch (Throwable t) {
            Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c主运行库未完成初始化, 插件停止加载.");
            initFailed = true;
        }
    }

    private static File checkLibPlugin() {
        File pluginDir = new File("plugins");
        for (File plugin : pluginDir.listFiles()) {
            if (plugin.getName().endsWith(".jar")) {
                PluginDescriptionFile desc = getPluginDescription(plugin);
                if (desc != null && desc.getName().equals("TabooLib5")) {
                    return plugin;
                }
            }
        }
        return null;
    }

    private static Class<?> getMainClass() {
        File file = file(new File("plugins/TabooLib/temp/" + UUID.randomUUID()));
        try {
            ZipFile zipFile = new ZipFile(toFile(BasePlugin.class.getProtectionDomain().getCodeSource().getLocation().openStream(), file));
            try (InputStream inputStream = zipFile.getInputStream(zipFile.getEntry("plugin.yml"))) {
                return Class.forName(YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream)).getString("main"));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    private static PluginDescriptionFile getPluginDescription(File file) {
        PluginDescriptionFile descriptionFile = null;
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("plugin.yml");
            if (entry == null) {
                return descriptionFile;
            }
            try (InputStream stream = jar.getInputStream(entry)) {
                descriptionFile = new PluginDescriptionFile(stream);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return descriptionFile;
    }

    private static void init() {
        // 检查 TabooLib 文件是否存在
        if (!libFile.exists()) {
            // 本地资源检测
            InputStream resourceAsStream = BasePlugin.class.getClassLoader().getResourceAsStream("TabooLib.jar");
            if (resourceAsStream != null) {
                // 写入文件
                toFile(resourceAsStream, file(libFile));
            }
            // 在线资源下载
            else if (!downloadFile()) {
                return;
            }
        }
        // 检查 TabooLib 运行版本是否正常
        else {
            double version = getVersion();
            // 本地版本获取失败
            if (version == -1) {
                // 重新下载文件，如果下载失败则停止加载
                if (!downloadFile()) {
                    return;
                }
            }
            Class<?> mainClass = getMainClass();
            if (mainClass != null && mainClass.isAnnotationPresent(Version.class)) {
                double requireVersion = mainClass.getAnnotation(Version.class).value();
                // 依赖版本高于当前运行版本
                if (requireVersion > version) {
                    // 获取版本信息
                    String[] newVersion = getNewVersion();
                    if (newVersion == null) {
                        disablePlugin();
                        return;
                    }
                    // 检查依赖版本是否合理
                    // 如果插件使用不合理的版本则跳过下载防止死循环
                    // 并跳过插件加载
                    if (requireVersion > NumberConversions.toDouble(newVersion[0])) {
                        Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c无效的依赖版本... " + requireVersion + " > " + NumberConversions.toDouble(newVersion[0]));
                        initFailed = true;
                        return;
                    }
                    Bukkit.getConsoleSender().sendMessage("§f[TabooLib] §7正在下载资源文件...");
                    if (downloadFile(newVersion[2], file(libFile))) {
                        // 如果资源下载成功则重启服务器
                        restartServer();
                    }
                    return;
                }
            }
        }
        // 如果是 Forge 服务端
        if (forge) {
            // 检查 TabooLib5 是否已经被加载
            if (Bukkit.getPluginManager().getPlugin("TabooLib5") == null) {
                Bukkit.getConsoleSender().sendMessage("§f[TabooLib] §7检测到当前为 Forge 服务端, 主运行库将以插件模式启动.");
                // 将 TabooLib 通过插件方式加载到服务端
                LoadByPlugin();
            }
        } else {
            // 检查 TabooLib5 是否被当做插件放入插件文件夹
            File pluginFile = checkLibPlugin();
            if (pluginFile != null) {
                pluginFile.delete();
                restartDuplicate(pluginFile.getName());
                return;
            }
            // 检查 TabooLib 是否已经被加载
            if (!isLoaded()) {
                // 将 TabooLib 通过 Bukkit.class 类加载器加载至内存中供其他插件使用
                // 并保证在热重载过程中不会被 Bukkit 卸载
                Loader.addPath(libFile);
                // 初始化 TabooLib 主类
                if (Loader.forName("io.izzel.taboolib.TabooLib", true, Bukkit.class.getClassLoader()) == null) {
                    Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c主运行库未完成初始化, 插件停止加载.");
                    initFailed = true;
                }
            }
        }
        // 清理临时文件
        deepDelete(new File("plugins/TabooLib/temp"));
    }

    private static boolean downloadFile() {
        Bukkit.getConsoleSender().sendMessage("§f[TabooLib] §7正在下载资源文件...");
        String[] newVersion = getNewVersion();
        if (newVersion == null || !downloadFile(newVersion[2], file(libFile))) {
            disablePlugin();
            return false;
        }
        return true;
    }

    private static boolean disablePlugin() {
        try {
            if (Bukkit.getOnlinePlayers().size() > 0) {
                Arrays.stream(MESSAGE[1]).forEach(message -> Bukkit.getConsoleSender().sendMessage(message));
                initFailed = true;
                return false;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Arrays.stream(MESSAGE[0]).forEach(message -> Bukkit.getConsoleSender().sendMessage(message));
        try {
            Thread.sleep(5000L);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        initFailed = true;
        return true;
    }

    private static boolean restartServer() {
        try {
            if (Bukkit.getOnlinePlayers().size() > 0) {
                Arrays.stream(MESSAGE[3]).forEach(message -> Bukkit.getConsoleSender().sendMessage(message));
                initFailed = true;
                return false;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Arrays.stream(MESSAGE[2]).forEach(message -> Bukkit.getConsoleSender().sendMessage(message));
        try {
            Thread.sleep(5000L);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        initFailed = true;
        Bukkit.shutdown();
        return true;
    }

    private static void restartDuplicate(String name) {
        Arrays.stream(MESSAGE[4]).forEach(message -> Bukkit.getConsoleSender().sendMessage(message.replace("{name}", name)));
        try {
            Thread.sleep(5000L);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        initFailed = true;
        Bukkit.shutdown();
    }

    /**
     * I/O 相关工具
     */
    private static String readFully(InputStream inputStream, Charset charset) throws IOException {
        return new String(readFully(inputStream), charset);
    }

    /**
     * I/O 相关工具
     */
    private static byte[] readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buf)) > 0) {
            stream.write(buf, 0, len);
        }
        return stream.toByteArray();
    }

    /**
     * I/O 相关工具
     */
    private static String readFromURL(String in, String def) {
        return Optional.ofNullable(readFromURL(in)).orElse(def);
    }

    /**
     * I/O 相关工具
     */
    private static String readFromURL(String in) {
        try (InputStream inputStream = new URL(in).openStream(); BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            return new String(readFully(bufferedInputStream));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    /**
     * I/O 相关工具
     */
    private static boolean downloadFile(String in, File file) {
        try (InputStream inputStream = new URL(in).openStream(); BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            toFile(bufferedInputStream, file);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

    /**
     * I/O 相关工具
     */
    private static File toFile(String in, File file) {
        try (FileWriter fileWriter = new FileWriter(file); BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(in);
            bufferedWriter.flush();
        } catch (Exception ignored) {
        }
        return file;
    }

    /**
     * I/O 相关工具
     */
    private static File toFile(InputStream inputStream, File file) {
        try (FileOutputStream fos = new FileOutputStream(file); BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                bos.write(buf, 0, len);
            }
            bos.flush();
        } catch (Exception ignored) {
        }
        return file;
    }

    /**
     * I/O 相关工具
     */
    private static File file(File file) {
        if (!file.exists()) {
            folder(file);
            try {
                file.createNewFile();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return file;
    }

    /**
     * I/O 相关工具
     */
    private static File folder(File file) {
        if (!file.exists()) {
            String filePath = file.getPath();
            int index = filePath.lastIndexOf(File.separator);
            String folderPath;
            File folder;
            if ((index >= 0) && (!(folder = new File(filePath.substring(0, index))).exists())) {
                folder.mkdirs();
            }
        }
        return file;
    }

    /**
     * I/O 相关工具
     */
    private static void deepDelete(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
            return;
        }
        for (File file1 : Objects.requireNonNull(file.listFiles())) {
            deepDelete(file1);
        }
        file.delete();
    }

    /**
     * md5 相关工具
     */
    private static String getFileMD5(File file) {
        try(FileInputStream fileInputStream = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fileInputStream.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, length);
            }
            byte[] md5Bytes  = digest.digest();
            return new BigInteger(1, md5Bytes).toString(16);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return "";
    }

    public static boolean isInitFailed() {
        return initFailed;
    }

    public static boolean isForge() {
        return forge;
    }

    public static BasePlugin getPlugin() {
        return plugin;
    }

    public static File getLibFile() {
        return libFile;
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Version {

        double value();

    }

    static class Loader extends URLClassLoader {

        static MethodHandles.Lookup lookup;
        static Unsafe UNSAFE;

        static {
            try {
                Field field = Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                UNSAFE = (Unsafe) field.get(null);
                Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
                Object lookupBase = UNSAFE.staticFieldBase(lookupField);
                long lookupOffset = UNSAFE.staticFieldOffset(lookupField);
                lookup = (MethodHandles.Lookup) UNSAFE.getObject(lookupBase, lookupOffset);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        public Loader(java.net.URL[] urls) {
            super(urls);
        }

        /**
         * 将文件读取至内存中
         * 读取后不会随着插件的卸载而卸载
         * 请在执行前判断是否已经被读取
         * 防止出现未知错误
         */
        public static void addPath(File file) {
            try {
                Field ucp = Bukkit.class.getClassLoader().getClass().getDeclaredField("ucp");
                long ucpOffset = UNSAFE.objectFieldOffset(ucp);
                Object urlClassPath = UNSAFE.getObject(Bukkit.class.getClassLoader(), ucpOffset);
                MethodHandle methodHandle = lookup.findVirtual(urlClassPath.getClass(), "addURL", MethodType.methodType(void.class, java.net.URL.class));
                methodHandle.invoke(urlClassPath, file.toURI().toURL());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        public static Class forName(String name, boolean initialize, ClassLoader loader) {
            try {
                return Class.forName(name, initialize, loader);
            } catch (Throwable ignored) {
                return null;
            }
        }
    }
}
