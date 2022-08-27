package instrumentation;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static instrumentation.Agent.START_TIME;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * Custom ClassFileTransformer
 *
 * @author Josh Waterson
 */
public class ThreadObserver implements ClassFileTransformer {

    static final String BASE_APP_DIR = "instrumentation/";

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {

        if (loader == null) { // bootstrap loader caught by this check - will not load user's code
            return null;
        }

        if (className.startsWith(BASE_APP_DIR)) {
            return classfileBuffer;
        }

        URL url = loader.getResource("META-INF/MANIFEST.MF");
        if (url == null) {
            throw new RuntimeException("Can't locate manifest.");
        }

        String mainClass = null;
        try {
            Manifest manifest = new Manifest(url.openStream());
            Attributes attr = manifest.getMainAttributes();
            mainClass = attr.getValue("Main-Class").replace(".", "/");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mainClass == null) {
            throw new RuntimeException("No main class specified in Manifest.");
        }

        ClassNode cn = new ClassNode(ASM9);
        ClassReader cr = new ClassReader(classfileBuffer);
        cr.accept(cn, 0);
        boolean shutdownToBeAdded = false;

        for (MethodNode mn : cn.methods) {

            if (mn.name.equals("main") && mainClass.equals(className)) {
                shutdownToBeAdded = true;
            }

            InsnList insns = mn.instructions;

            if (insns.size() == 0) {
                continue;
            }

            int lineNum = -1, l1 = -1, l2 = -1;
            AbstractInsnNode node;
            int numAdded;
            for (int i = 0; i < insns.size(); i++) {
                node = insns.get(i);
                if (node instanceof LineNumberNode) {
                    lineNum = ((LineNumberNode) node).line;
                } else if (node instanceof LabelNode) {
                    if (l1 == -1) {
                        l1 = i;
                    } else {
                        l2 = i;
                    }
                } else if (node instanceof FrameNode) {
                    l1 = i;
                }

                if (lineNum > -1 && l1 < l2) {
                    InsnList addedInsns = new InsnList();

                    addedInsns.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Thread",
                            "currentThread", "()Ljava/lang/Thread;", false));
                    addedInsns.add(new TypeInsnNode(NEW, BASE_APP_DIR + "ThreadMarker"));
                    addedInsns.add(new InsnNode(DUP));
                    addedInsns.add(new MethodInsnNode(INVOKESTATIC, "java/lang/System",
                            "nanoTime", "()J", false));
                    addedInsns.add(new LdcInsnNode(START_TIME));
                    addedInsns.add(new InsnNode(LSUB));
                    addedInsns.add(new IntInsnNode(BIPUSH, lineNum));
                    addedInsns.add(new LdcInsnNode(className));
                    addedInsns.add(new MethodInsnNode(INVOKESPECIAL, BASE_APP_DIR + "ThreadMarker",
                            "<init>", "(JILjava/lang/String;)V"));
                    addedInsns.add(new MethodInsnNode(INVOKESTATIC, BASE_APP_DIR + "StackMapMediator",
                            "submitThreadMarker",
                            "(Ljava/lang/Thread;L" + BASE_APP_DIR + "ThreadMarker;)V"));

                    if (shutdownToBeAdded) {
                        addedInsns.add(new MethodInsnNode(INVOKESTATIC, BASE_APP_DIR + "StackMapMediator",
                                "shutdownThreadPool", "()V"));
                        shutdownToBeAdded = false;
                    }

                    numAdded = addedInsns.size();
                    insns.insert(insns.get(l1), addedInsns);
                    lineNum = -1;

                    i += numAdded - 1; // -1 to counteract i incrementing with next iteration
                    l1 = -1;
                    l2 = -1;

                }

            }

        }

        ClassWriter cw1 = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cn.accept(cw1);
        return cw1.toByteArray();

    }
}
