package com.badrqaba.kbooster.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class UseCaseProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        environment.logger.warn("Provider created")
        return UseCaseProcessor(
            environment.codeGenerator,
            logger = environment.logger
        )
    }
}