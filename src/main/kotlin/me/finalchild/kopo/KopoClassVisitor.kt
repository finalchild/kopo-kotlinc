package me.finalchild.kopo

import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.tree.*

class KopoClassVisitor(val original: ClassVisitor) : ClassVisitor(Opcodes.ASM7, ClassNode()) {
    override fun visitEnd() {
        super.visitEnd()
        val node = cv as ClassNode
        run {
            if (node.access != Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL or Opcodes.ACC_SUPER || node.superName != "org/bukkit/plugin/java/JavaPlugin") {
                return@run
            }
            val instanceVariable = node.fields.find { field ->
                field.access == Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL or Opcodes.ACC_STATIC && field.name == "INSTANCE" && field.desc == "L${node.name};"
            } ?: return@run
            val instanceInit = node.methods.filter { method ->
                method.name == "<init>" && method.desc == "()V"
            }
            val objectInit = instanceInit.ifEmpty {
                listOf(MethodNode(Opcodes.ACC_PRIVATE, "<init>", "()V", null, null))
            }.takeIf {
                it.size == 1
            }?.first()?.takeIf { method ->
                method.access == Opcodes.ACC_PRIVATE
            } ?: return@run

            val classInit = node.methods.find { method ->
                method.name == "<clinit>" && method.desc == "()V" && (node.version < 51 || (node.access and Opcodes.ACC_STATIC) != 0)
            } ?: return@run

            instanceVariable.access = Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC
            objectInit.access = Opcodes.ACC_PUBLIC
            objectInit.instructions.iterator().asSequence().filter { insnNode ->
                insnNode.opcode == Opcodes.RETURN
            }.forEach { returnInsn ->
                objectInit.instructions.insertBefore(returnInsn, VarInsnNode(Opcodes.ALOAD, 0))
                objectInit.instructions.insertBefore(returnInsn, FieldInsnNode(Opcodes.PUTSTATIC, node.name, "INSTANCE", "L${node.name};"))
            }
            classInit.instructions.iterator().forEach { insnNode ->
                if (insnNode.opcode == Opcodes.NEW && (insnNode as TypeInsnNode).desc == node.name) {
                    classInit.instructions.set(insnNode, InsnNode(Opcodes.ACONST_NULL))
                } else if (insnNode.opcode == Opcodes.INVOKESPECIAL && (insnNode as MethodInsnNode).owner == node.name && insnNode.name == "<init>" && insnNode.desc == "()V") {
                    classInit.instructions.set(insnNode, InsnNode(Opcodes.POP))
                }
            }
        }
        node.accept(original)
    }
}
