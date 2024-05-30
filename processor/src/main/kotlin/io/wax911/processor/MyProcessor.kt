package io.wax911.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.wax911.annotation.Param
import java.util.*

class MyProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private fun generateCompanionObject(classes: List<KSClassDeclaration>) {
        val packageName = classes.first().packageName.asString()
        val parentClassName = classes.first().parentDeclaration?.simpleName?.asString()

        if (parentClassName == null) {
            logger.exception(
                IllegalArgumentException("The annotated item in `$packageName` should belong in a parent class")
            )
        }

        val companionObjectName = "${parentClassName}Param"
        val fileSpecBuilder = FileSpec.builder(packageName, companionObjectName)
        val typeSpecBuilder = TypeSpec.objectBuilder(companionObjectName)

        classes.forEach { classDeclaration ->
            val className = classDeclaration.simpleName.asString().uppercase()
            val propertyValue = requireNotNull(classDeclaration.qualifiedName).asString()
            typeSpecBuilder.addProperty(
                PropertySpec.builder(className, String::class)
                    .addModifiers(KModifier.CONST)
                    .initializer("%S", propertyValue)
                    .build()
            )
        }

        fileSpecBuilder.addType(typeSpecBuilder.build())
        val fileSpec = fileSpecBuilder.build()

        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(
                aggregating = false,
                sources = classes.mapNotNull(
                    transform = KSClassDeclaration::containingFile
                ).toTypedArray()
            ),
            packageName = packageName,
            fileName = companionObjectName
        )

        file.writer().use { stream ->
            fileSpec.writeTo(stream)
            stream.flush()
        }
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val qualifier = requireNotNull(Param::class.qualifiedName)
        val symbols = resolver.getSymbolsWithAnnotation(qualifier)
            .filterIsInstance<KSClassDeclaration>()

        symbols
            .groupBy { it.parentDeclaration?.qualifiedName?.asString() }
            .map(transform = Map.Entry<String?, List<KSClassDeclaration>>::value)
            .forEach(action = ::generateCompanionObject)

        logger.info("Finished processing ${symbols.count()} symbols for $qualifier")

        return emptyList()
    }
}
