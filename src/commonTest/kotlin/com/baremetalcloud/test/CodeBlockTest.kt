package com.baremetalcloud.test

import com.baremetalcloud.codeblock.*
import kotlin.test.Test
import kotlin.test.assertEquals


class CodeBlockTest {

    @Test
    fun sample() {
        val innerBlock = CodeBlock.builder()
        innerBlock.beginControlFlow("innerFun")
        innerBlock.addStatement("innerStatement")
        innerBlock.endControlFlow()
        val block = CodeBlock.builder()
        block.beginControlFlow("someFun")
        block.add(innerBlock.build())
        block.endControlFlow()
        val source = block.build().toString()
        assertEquals("""
            someFun {
              innerFun {
                innerStatement
              }
            }
            """.trimIndent(), source.trim()
        )
    }
}



