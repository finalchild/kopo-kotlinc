package me.finalchild.kopo

import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.codegen.ClassBuilderMode
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin

class KopoClassGenerationInterceptor : ClassBuilderInterceptorExtension {
    override fun interceptClassBuilderFactory(
        interceptedFactory: ClassBuilderFactory,
        bindingContext: BindingContext,
        diagnostics: DiagnosticSink
    ): ClassBuilderFactory
            = object: ClassBuilderFactory {
        override fun getClassBuilderMode(): ClassBuilderMode =
            interceptedFactory.classBuilderMode
        override fun newClassBuilder(origin: JvmDeclarationOrigin): ClassBuilder =
            KopoClassBuilder(interceptedFactory.newClassBuilder(origin))
        override fun asText(builder: ClassBuilder?): String =
            interceptedFactory.asText((builder as KopoClassBuilder).delegate)
        override fun asBytes(builder: ClassBuilder): ByteArray =
            interceptedFactory.asBytes((builder as KopoClassBuilder).delegate)
        override fun close() = interceptedFactory.close()
    }
}
