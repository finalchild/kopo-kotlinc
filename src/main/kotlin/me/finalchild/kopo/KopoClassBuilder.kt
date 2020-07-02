package me.finalchild.kopo

import org.jetbrains.kotlin.codegen.AbstractClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.org.objectweb.asm.ClassVisitor

class KopoClassBuilder(val delegate: ClassBuilder)
    : AbstractClassBuilder() {
    val visitor = KopoClassVisitor(delegate.visitor)
    override fun getVisitor(): ClassVisitor = visitor
}
