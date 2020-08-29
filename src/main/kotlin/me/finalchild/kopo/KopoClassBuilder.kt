package me.finalchild.kopo

import org.jetbrains.kotlin.codegen.AbstractClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.inline.SourceMapper
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor

class KopoClassBuilder(val delegate: ClassBuilder)
    : AbstractClassBuilder.Concrete(KopoClassVisitor(delegate)) {

    override fun defineClass(origin: PsiElement?, version: Int, access: Int, name: String, signature: String?, superName: String, interfaces: Array<out String>) {
        (visitor as KopoClassVisitor).origin = origin
        super.defineClass(origin, version, access, name, signature, superName, interfaces)
    }

    override fun newField(origin: JvmDeclarationOrigin, access: Int, name: String, desc: String, signature: String?, value: Any?): FieldVisitor {
        (visitor as KopoClassVisitor).fieldOrigins.add(origin)
        return super.newField(origin, access, name, desc, signature, value)
    }

    override fun newMethod(origin: JvmDeclarationOrigin, access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        (visitor as KopoClassVisitor).methodOrigins.add(origin)
        return super.newMethod(origin, access, name, desc, signature, exceptions)
    }

    override fun visitSMAP(smap: SourceMapper, backwardsCompatibleSyntax: Boolean) {
        (visitor as KopoClassVisitor).smap = smap
        (visitor as KopoClassVisitor).backwardsCompatibleSyntax = backwardsCompatibleSyntax
        super.visitSMAP(smap, backwardsCompatibleSyntax)
    }
}
