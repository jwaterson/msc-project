package instrumentation;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;


/**
 * Agent class
 *
 * @author Josh Waterson
 */
public class Agent {

    static final long START_TIME = System.nanoTime();
    static final String BASE_APP_DIR = "java/instrumentation/";

    public static void premain(String args, Instrumentation instrumentation) {
        if (args == null) {
            // main-class was not passed as argument to Agent. Shouldn't ever occur.
            throw new RuntimeException();
        }

        instrumentation.addTransformer((new ClassFileTransformer() {
            /**
             * Transforms the given class file and returns a new replacement class file.
             * This method is invoked when the {@link Module Module} bearing {@link
             * ClassFileTransformer#transform(Module, ClassLoader, String, Class, ProtectionDomain, byte[])
             * transform} is not overridden.
             *
             * @param loader              the defining loader of the class to be transformed,
             *                            may be {@code null} if the bootstrap loader
             * @param className           the name of the class in the internal form of fully
             *                            qualified class and interface names as defined in
             *                            <i>The Java Virtual Machine Specification</i>.
             *                            For example, <code>"java/util/List"</code>.
             * @param classBeingRedefined if this is triggered by a redefine or retransform,
             *                            the class being redefined or retransformed;
             *                            if this is a class load, {@code null}
             * @param protectionDomain    the protection domain of the class being defined or redefined
             * @param classfileBuffer     the input byte buffer in class file format - must not be modified
             * @return a well-formed class file buffer (the result of the transform),
             * or {@code null} if no transform is performed
             * @throws IllegalClassFormatException if the input does not represent a well-formed class file
             * @implSpec The default implementation returns null.
             * @revised 9
             */
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

                ClassNode cn = new ClassNode(ASM9);
                ClassReader cr = new ClassReader(classfileBuffer);
                cr.accept(cn, 0);
                boolean shutdownToBeAdded = false;

                for (MethodNode mn : cn.methods) {

                    if (mn.name.equals("main") && args.equals(className)) {
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
                                    "(Ljava/lang/Thread;Lapplication/ThreadMarker;)V"));

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

        }));

    }

}

