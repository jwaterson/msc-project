package instrumentation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import usercode.MultithreadedPrinter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ThreadRecorderTransformerTest {

    public String className;

    private static class TestClassLoader extends ClassLoader {
        private final String className;

        public TestClassLoader(String className) {
            super();
            this.className = className;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            byte[] transformedCode = null;
            if (name.equals(className)) {
                try {
                    transformedCode = ThreadRecorderTransformer.instrument(name, Files.readAllBytes(Path.of(name)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return transformedCode == null ? super.loadClass(name)
                    : defineClass(name, transformedCode, 0, transformedCode.length);
        }
    }


    @BeforeEach
    void setUp() {
        className = "build/classes/java/main/usercode/MultithreadedPrinter.class";
    }

    @AfterEach
    void tearDown() {
        className = null;
    }

    @Test
    void byteCodeIsAltered() {
        try {
            assertFalse(Arrays.equals(Files.readAllBytes(Path.of(className)),
                    ThreadRecorderTransformer.instrument(className, Files.readAllBytes(Path.of(className)))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Test
//    void instrumentMultiThreadedPrinter() {
//        ClassLoader classLoader = new TestClassLoader(className);
//        try {
//            MultithreadedPrinter o = (MultithreadedPrinter) classLoader.loadClass(className).getConstructor().newInstance();
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}