package me.finalchild.kopo

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor

class KopoCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String
            = "kopo"
    override val pluginOptions: Collection<AbstractCliOption>
            = emptyList()
}
