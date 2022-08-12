package jarifier;

import java.io.IOException;
import java.util.jar.JarOutputStream;

public class JarDriver {
    public static void main(String[] args) throws IOException {
        JarTool tool = new JarTool();
        String base = System.getProperty("user.dir");
        tool.startManifest();
        tool.setMainClass("Example");

        JarOutputStream target = tool.openJar("HelloWorld.jar");

        tool.addFile(target, System.getProperty("user.dir") + "\\src",
                System.getProperty("user.dir") + "\\out\\production\\PROJECT\\Example.class");
        target.close();
    }
}
