import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;


/**
 * Agent class
 *
 * @author Josh Waterson
 */
public class Agent {

    public static void premain(String args, Instrumentation instrumentation) {
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
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                if (loader == null) { // bootstrap loader caught by this check - will not load user's code
                    return null;
                }
                ClassNode cn = new ClassNode(ASM9);
                ClassReader cr1 = new ClassReader(classfileBuffer);
                cr1.accept(cn, 0);

                String mapName = availableName(cn.fields);
                cn.fields.add(new FieldNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                        mapName, "Ljava/util/concurrent/ConcurrentHashMap;",
                        null, null));

                boolean synchronizedBlockPresent = false;
                long startTime = System.nanoTime();

                for (MethodNode mn : cn.methods) {
                    if ("<clinit>".equals(mn.name)) {
                        synchronizedBlockPresent = true;
                    }

                    InsnList insns = mn.instructions;

                    if (insns.size() == 0) {
                        continue;
                    }

                    int lineNum = -1;
                    int l1 = -1;
                    int l2 = -1;
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

                            addedInsns.add(new FieldInsnNode(GETSTATIC, className, mapName,
                                    "Ljava/util/concurrent/ConcurrentHashMap;"));
                            addedInsns.add(new LdcInsnNode(startTime));
                            addedInsns.add(new MethodInsnNode(INVOKESTATIC, "java/lang/System",
                                    "nanoTime", "()J", false));
                            addedInsns.add(new InsnNode(LSUB));
                            addedInsns.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Long",
                                    "valueOf", "(J)Ljava/lang/Long;", false));
                            addedInsns.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Thread",
                                    "currentThread", "()Ljava/lang/Thread;", false));
                            addedInsns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Thread",
                                    "getId", "()J", false));
                            addedInsns.add(new InvokeDynamicInsnNode("makeConcatWithConstants",
                                    "(J)Ljava/lang/String;",
                                    new Handle(H_INVOKESTATIC,
                                            "java/lang/invoke/StringConcatFactory",
                                            "makeConcatWithConstants",
                                            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
                                            false),
                                    "\u0001|" + lineNum + "|" + className));
                            addedInsns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/concurrent/ConcurrentHashMap",
                                    "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false));
                            addedInsns.add(new InsnNode(POP));

                            numAdded = addedInsns.size();
                            insns.insert(insns.get(l1), addedInsns);
                            lineNum = -1;

                            i += numAdded - 1; // -1 to counteract i incrementing with next iteration
                            l1 = -1;
                            l2 = -1;
                        }
                    }
                }

                if (!synchronizedBlockPresent) {
                    cn.methods.add(new MethodNode(ACC_STATIC, "<clinit>",
                            "()V", null, null));
                }

                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals("<clinit>")) {
                        // prepend map instantiation
                        InsnList insns = new InsnList();
                        insns.add(new LabelNode());
                        insns.add(new TypeInsnNode(NEW,
                                "java/util/concurrent/ConcurrentHashMap"));
                        insns.add(new InsnNode(DUP));
                        insns.add(new MethodInsnNode(INVOKESPECIAL,
                                "java/util/concurrent/ConcurrentHashMap",
                                "<init>",
                                "()V",
                                false));
                        insns.add(new FieldInsnNode(PUTSTATIC,
                                className,
                                mapName,
                                "Ljava/util/concurrent/ConcurrentHashMap;"));
                        if (!synchronizedBlockPresent) {
                            insns.add(new InsnNode(RETURN));
                            mn.maxStack = 2;
                        }
                        mn.instructions.insert(insns); // prepends new insns
                        break;
                    }
                }

                ClassWriter cw1 = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                cn.accept(cw1);
                return cw1.toByteArray();
            }

        }));

    }

    private static String availableName(List<FieldNode> fields) {
        int[] nums = fields.stream()
                .mapToInt(f -> f.name.length())
                .sorted()
                .toArray();

        boolean encountered;
        for (int i = 1; ; i++) {
            encountered = false;
            for (int n : nums) {
                if (n == i) {
                    encountered = true;
                    break;
                }
            }
            if (!encountered) {
                return "$".repeat(i);
            }
        }
    }

}

