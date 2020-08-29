package me.finalchild.kopo

import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.inline.SourceMapper
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.*

class ClassBuilderClassVisitor(val cb: ClassBuilder, val origin: PsiElement?, val fieldOrigins: Iterator<JvmDeclarationOrigin>, val methodOrigins: Iterator<JvmDeclarationOrigin>, val smap: SourceMapper?, val backwardsCompatibleSyntax: Boolean) : ClassVisitor(Opcodes.ASM7, cb.visitor) {
    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?): Unit =
            cb.defineClass(origin, version, access, name, signature, superName!!, interfaces ?: emptyArray())

    override fun visitSource(source: String?, debug: String?): Unit =
            if (smap == null) {
                cb.visitSource(source ?: "", debug)
            } else {
                cb.visitSMAP(smap, backwardsCompatibleSyntax)
            }

    override fun visitOuterClass(owner: String, name: String?, descriptor: String?): Unit =
            cb.visitOuterClass(owner, name, descriptor)

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? =
            cb.newAnnotation(descriptor, visible)

    override fun visitInnerClass(name: String, outerName: String?, innerName: String?, access: Int): Unit =
            cb.visitInnerClass(name, outerName, innerName, access)

    override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor? =
            cb.newField(fieldOrigins.next(), access, name, descriptor, signature, value)

    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor =
            cb.newMethod(methodOrigins.next(), access, name, descriptor, signature, exceptions ?: emptyArray())

    override fun visitEnd() = cb.done()
}
