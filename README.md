# Kotlin Multiplatform CodeBlock w/Builder

Common-Only multiplatform hacked from KotlinPoet CodeBlock.

## Setup

Use by adding the dependency from Maven Central in your build.gradle.kts

```
implementation("com.baremetalcloud:common-codeblock:0.5")
```

## Usage

```kotlin
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
            """.trimIndent(), source.trim())
```

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.
Please make sure to update tests as appropriate.