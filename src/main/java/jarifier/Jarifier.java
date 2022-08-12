package jarifier;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;


public class Jarifier {

    public void run() throws IOException
    {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        JarOutputStream target = new JarOutputStream(new FileOutputStream("output.jar"), manifest);
        File inputDirectory = new File("inputDirectory");
        for (File nestedFile : Objects.requireNonNull(inputDirectory.listFiles()))
            add("", nestedFile, target);
        target.close();
    }

    private void add(String parents, File source, JarOutputStream target) throws IOException
    {
        BufferedInputStream in = null;
        try
        {
            String name = (parents + source.getName()).replace("\\", "/");

            if (source.isDirectory())
            {
                if (!name.isEmpty())
                {
                    if (!name.endsWith("/"))
                        name += "/";
                    JarEntry entry = new JarEntry(name);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }
                for (File nestedFile : Objects.requireNonNull(source.listFiles()))
                    add(name, nestedFile, target);
                return;
            }

            JarEntry entry = new JarEntry(name);
            entry.setTime(source.lastModified());
            target.putNextEntry(entry);
            in = new BufferedInputStream(new FileInputStream(source));

            byte[] buffer = new byte[1024];
            while (true)
            {
                int count = in.read(buffer);
                if (count == -1)
                    break;
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        }
        finally
        {
            if (in != null)
                in.close();
        }
    }

    public static void createJarFrom(String path, String className) {
        FileOutputStream fileOut;
        JarOutputStream jarOut;
        String base = System.getProperty("user.dir").replace("\\", "/"); // dir from which JVM was invoked
        String jarDir = base + "/out/artifacts/losingthethread";
        while (!(new File(jarDir).mkdirs())) { // returns false if couldn't complete
            jarDir += "_";
        }
        try {
            fileOut = new FileOutputStream(jarDir + "/losingthethread.jar");
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "test.usercode.BasicMultithreadedPrinting");
            jarOut = new JarOutputStream(fileOut, manifest);
            jarOut.putNextEntry(new ZipEntry(base + "/out/"));
            jarOut.putNextEntry(new ZipEntry(base + path + className));
            jarOut.write(Files.readAllBytes(Paths.get(base + path + className)));
            jarOut.closeEntry();
            jarOut.close();
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace(); //TODO: handle this properly
        }
    }

    public static void main(String[] args) {
        Jarifier.createJarFrom("/out/production/PROJECT/test/usercode/",
                "BasicMultithreadedPrinting.class");

    }
}
