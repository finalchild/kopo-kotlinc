package me.finalchild.kopo.kotlinc

import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.codegen.ClassBuilderMode
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.ClassReader
import org.jetbrains.org.objectweb.asm.ClassWriter

class KopoClassBuilderFactory(val delegate: ClassBuilderFactory) : ClassBuilderFactory {
    override fun getClassBuilderMode(): ClassBuilderMode =
            delegate.classBuilderMode

    override fun newClassBuilder(origin: JvmDeclarationOrigin): ClassBuilder =
            delegate.newClassBuilder(origin)

    override fun asText(classBuilder: ClassBuilder?): String =
            delegate.asText(classBuilder)

    override fun asBytes(classBuilder: ClassBuilder?): ByteArray {
        val writer = object : ClassWriter(COMPUTE_MAXS or COMPUTE_FRAMES) {
            override fun getCommonSuperClass(type1: String, type2: String) = "java/lang/Object"
        }
        val visitor = KopoClassVisitor(writer)
        val reader = ClassReader(delegate.asBytes(classBuilder))
        reader.accept(visitor, 0)
        return writer.toByteArray()
    }

    override fun close() =
            delegate.close()
}
