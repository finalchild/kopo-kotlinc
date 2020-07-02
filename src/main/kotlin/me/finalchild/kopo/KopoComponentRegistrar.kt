package me.finalchild.kopo

import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class KopoComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        ClassBuilderInterceptorExtension.registerExtension(
                project,
                KopoClassGenerationInterceptor()
        )
    }
}
