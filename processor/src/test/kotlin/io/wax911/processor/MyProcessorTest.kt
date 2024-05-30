package io.wax911.processor

import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.KotlinCompilation
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.*

@OptIn(ExperimentalCompilerApi::class)
class MyProcessorTest {

    @Test
    fun `test ksp processor`() {
        val source = SourceFile.kotlin(
            name = "MyItem.kt",
            contents = """
            package io.wax911.items

            import io.wax911.annotation.Param

            object MyItem {
                @Param
                data class Id(val id: String)
                
                @Param
                data class Description(val description: String)
            }
            """.trimIndent())

        val result = KspTestUtil.compile(
            sourceFiles = listOf(source),
            symbolProcessorProviders = listOf(MyProcessorProvider()),
        )

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFiles = KspTestUtil.getSourcesFromResult(result)
        assertTrue(generatedFiles.isNotEmpty(), "`generatedFiles` cannot be empty, make sure that files are being written")

        val generatedFile = generatedFiles.find { it.name == "MyItemParam.kt" }
        assertNotNull(generatedFile, "No file matching `MyItemParam.kt` exists in `generatedFiles`")

        @Suppress("RedundantVisibilityModifier")
        @Language("kotlin")
        val expectedContent = """
        package io.wax911.items

        import kotlin.String
        
        public object MyItemParam {
          public const val ID: String = "io.wax911.items.MyItem.Id"
        
          public const val DESCRIPTION: String = "io.wax911.items.MyItem.Description"
        }
        """.trimIndent()

        assertEquals(expectedContent, generatedFile.readText().trim())
    }
}