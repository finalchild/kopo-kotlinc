package me.finalchild.kopo

import org.jetbrains.kotlin.codegen.optimization.common.asSequence
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.tree.*
import java.util.stream.Collectors

class KopoClassVisitor(val original: ClassVisitor) : ClassVisitor(Opcodes.ASM7, ClassNode()) {
    override fun visitEnd() {
        super.visitEnd()
        val clazz = cv as ClassNode
        run {
            if (clazz.access and Opcodes.ACC_PUBLIC == 0 || clazz.access and Opcodes.ACC_FINAL == 0 || clazz.superName != "org/bukkit/plugin/java/JavaPlugin") {
                return@run
            }

            val classInit = clazz.methods.find { method ->
                method.name == "<clinit>" && method.desc == "()V" && (clazz.version < 51 || (method.access and Opcodes.ACC_STATIC) != 0)
            } ?: return@run

            val instanceVariable = clazz.fields.find { field ->
                (field.access and Opcodes.ACC_PUBLIC != 0) && (field.access and Opcodes.ACC_FINAL != 0) && (field.access and Opcodes.ACC_STATIC != 0)
                        && field.name == "INSTANCE" && field.desc == "L${clazz.name};"
            } ?: return@run

            val objectInit = clazz.methods.filter { method ->
                method.name == "<init>" && method.desc == "()V"
            }.takeIf {
                it.size == 1
            }?.first()?.takeIf { method ->
                (method.access and Opcodes.ACC_PRIVATE) != 0
            } ?: return@run

            val methodsNamedKopoClinit = clazz.methods.stream().map { it.name }.filter { it.startsWith("\$kopo\$clinit") }.collect(Collectors.toSet())
            val kopoClinitName = generateSequence(0) { it + 1 }.map { no ->
                "\$kopo\$clinit" + if (no == 0) "" else "$$no"
            }.first { candidate ->
                !methodsNamedKopoClinit.contains(candidate)
            }
            val methodsNamedKopoInit = clazz.methods.stream().map { it.name }.filter { it.startsWith("\$kopo\$init") }.collect(Collectors.toSet())
            val kopoInitName = generateSequence(0) { it + 1 }.map { no ->
                "\$kopo\$init" + if (no == 0) "" else "$$no"
            }.first { candidate ->
                !methodsNamedKopoInit.contains(candidate)
            }

            val wrapperObjectInit = MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null).also { clazz.methods.add(it) }
            wrapperObjectInit.instructions.add(VarInsnNode(Opcodes.ALOAD, 0))
            wrapperObjectInit.instructions.add(InsnNode(Opcodes.DUP))
            wrapperObjectInit.instructions.add(MethodInsnNode(Opcodes.INVOKESPECIAL, "org/bukkit/plugin/java/JavaPlugin", "<init>", "()V", false))
            wrapperObjectInit.instructions.add(FieldInsnNode(Opcodes.PUTSTATIC, clazz.name, "INSTANCE", "L${clazz.name};"))
            wrapperObjectInit.instructions.add(MethodInsnNode(Opcodes.INVOKESTATIC, clazz.name, kopoClinitName, "()V", false))
            wrapperObjectInit.instructions.add(InsnNode(Opcodes.RETURN))

            classInit.access = classInit.access and Opcodes.ACC_PUBLIC.inv() and Opcodes.ACC_PROTECTED.inv() or Opcodes.ACC_PRIVATE or Opcodes.ACC_SYNTHETIC
            classInit.name = kopoClinitName
            classInit.instructions.asSequence().forEach { insn ->
                if (insn.opcode == Opcodes.NEW && (insn as TypeInsnNode).desc == clazz.name) {
                    classInit.instructions.set(insn, FieldInsnNode(Opcodes.GETSTATIC, clazz.name, "INSTANCE", "L${clazz.name};"))
                } else if (insn.opcode == Opcodes.INVOKESPECIAL && (insn as MethodInsnNode).owner == clazz.name && insn.name == "<init>" && insn.desc == "()V") {
                    classInit.instructions.set(insn, MethodInsnNode(Opcodes.INVOKEVIRTUAL, clazz.name, kopoInitName, "()V", false))
                }
            }
            clazz.fields.stream().filter { field ->
                field.access and Opcodes.ACC_STATIC != 0 && field.access and Opcodes.ACC_FINAL != 0
            }.forEach { field ->
                field.access = field.access and Opcodes.ACC_FINAL.inv()
            }

            objectInit.access = objectInit.access or Opcodes.ACC_SYNTHETIC
            objectInit.name = kopoInitName
            objectInit.instructions.asSequence().forEach { insn ->
                if (insn.opcode == Opcodes.INVOKESPECIAL && (insn as MethodInsnNode).owner == "org/bukkit/plugin/java/JavaPlugin" && insn.name == "<init>" && insn.desc == "()V") {
                    objectInit.instructions.set(insn, InsnNode(Opcodes.POP))
                }
            }
        }
        clazz.accept(original)
    }
}
