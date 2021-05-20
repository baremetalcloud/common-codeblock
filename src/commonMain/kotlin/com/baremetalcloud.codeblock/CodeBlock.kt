package com.baremetalcloud.codeblock

import kotlin.math.min

public data class CodeBlock(
    internal val formatParts: List<String> = listOf()
) {
    public fun isEmpty(): Boolean = formatParts.isEmpty()

    public fun isNotEmpty(): Boolean = !isEmpty()

    public companion object {
        public fun builder(): Builder = Builder()

    }

    public class Builder {
        public fun add(codeBlock: CodeBlock): Builder = apply {
            formatParts += codeBlock.formatParts
        }
        public fun isEmpty(): Boolean = formatParts.isEmpty()
        public fun indent(): Builder = apply {
            formatParts += "⇥"
        }

        public fun unindent(): Builder = apply {
            formatParts += "⇤"
        }

        public fun clear(): Builder = apply {
            formatParts.clear()
//            args.clear()
        }

        public fun isNotEmpty(): Boolean = !isEmpty()
        internal val formatParts = mutableListOf<String>()
        public fun addNamed(format: String, arguments: Map<String, *>): Builder = apply {
            var p = 0

            while (p < format.length) {
                val nextP = format.nextPotentialPlaceholderPosition(startIndex = p)
                if (nextP == -1) {
                    formatParts += format.substring(p, format.length)
                    break
                }

                if (p != nextP) {
                    formatParts += format.substring(p, nextP)
                    p = nextP
                }

                var matchResult: MatchResult? = null
                val colon = format.indexOf(':', p)
                if (colon != -1) {
                    val endIndex = min(colon + 2, format.length)
                    matchResult = NAMED_ARGUMENT.matchEntire(format.substring(p, endIndex))
                }
                if (matchResult != null) {
                    val argumentName = matchResult.groupValues[ARG_NAME]
                    require(arguments.containsKey(argumentName)) {
                        "Missing named argument for %$argumentName"
                    }
                    val formatChar = matchResult.groupValues[TYPE_NAME].first()
//                    addArgument(format, formatChar, arguments[argumentName])
                    formatParts += "%$formatChar"
                    p += matchResult.range.endInclusive + 1
                } else if (format[p].isSingleCharNoArgPlaceholder) {
                    formatParts += format.substring(p, p + 1)
                    p++
                } else {
                    require(p < format.length - 1) { "dangling % at end" }
                    require(format[p + 1].isMultiCharNoArgPlaceholder) {
                        "unknown format %${format[p + 1]} at ${p + 1} in '$format'"
                    }
                    formatParts += format.substring(p, p + 2)
                    p += 2
                }
            }
        }
        public fun add(format: String, vararg args: Any?): Builder = apply {
            var hasRelative = false
            var hasIndexed = false

            var relativeParameterCount = 0
            val indexedParameterCount = IntArray(args.size)

            var p = 0
            while (p < format.length) {
                if (format[p].isSingleCharNoArgPlaceholder) {
                    formatParts += format[p].toString()
                    p++
                    continue
                }

                if (format[p] != '%') {
                    var nextP = format.nextPotentialPlaceholderPosition(startIndex = p + 1)
                    if (nextP == -1) nextP = format.length
                    formatParts += format.substring(p, nextP)
                    p = nextP
                    continue
                }

                p++ // '%'.

                // Consume zero or more digits, leaving 'c' as the first non-digit char after the '%'.
                val indexStart = p
                var c: Char
                do {
                    require(p < format.length) { "dangling format characters in '$format'" }
                    c = format[p++]
                } while (c in '0'..'9')
                val indexEnd = p - 1

                // If 'c' doesn't take an argument, we're done.
                if (c.isMultiCharNoArgPlaceholder) {
                    require(indexStart == indexEnd) { "%% may not have an index" }
                    formatParts += "%$c"
                    continue
                }

                // Find either the indexed argument, or the relative argument. (0-based).
                val index: Int
                if (indexStart < indexEnd) {
                    index = format.substring(indexStart, indexEnd).toInt() - 1
                    hasIndexed = true
                    if (args.isNotEmpty()) {
                        indexedParameterCount[index % args.size]++ // modulo is needed, checked below anyway
                    }
                } else {
                    index = relativeParameterCount
                    hasRelative = true
                    relativeParameterCount++
                }

                require(index >= 0 && index < args.size) {
                    "index ${index + 1} for '${format.substring(
                        indexStart - 1,
                        indexEnd + 1
                    )}' not in range (received ${args.size} arguments)"
                }
                require(!hasIndexed || !hasRelative) { "cannot mix indexed and positional parameters" }

//                addArgument(format, c, args[index])

                formatParts += "%$c"
            }

            if (hasRelative) {
                require(relativeParameterCount >= args.size) {
                    "unused arguments: expected $relativeParameterCount, received ${args.size}"
                }
            }
            if (hasIndexed) {
                val unused = mutableListOf<String>()
                for (i in args.indices) {
                    if (indexedParameterCount[i] == 0) {
                        unused += "%" + (i + 1)
                    }
                }
                val s = if (unused.size == 1) "" else "s"
                require(unused.isEmpty()) { "unused argument$s: ${unused.joinToString(", ")}" }
            }
        }
        public fun build(): CodeBlock = CodeBlock(formatParts.toList())

    }
    override fun toString(): String = buildCodeString { emitCode(this@CodeBlock) }
}

internal fun String.nextPotentialPlaceholderPosition(startIndex: Int) = indexOfAny(charArrayOf('%', '«', '»', '⇥', '⇤'), startIndex)

private val NAMED_ARGUMENT = Regex("%([\\w_]+):([\\w]).*")
private val LOWERCASE = Regex("[a-z]+[\\w_]*")
private const val ARG_NAME = 1
private const val TYPE_NAME = 2
private val NO_ARG_PLACEHOLDERS = setOf("⇥", "⇤", "«", "»")
internal val EMPTY = CodeBlock(mutableListOf())
internal val Char.isMultiCharNoArgPlaceholder get() = this == '%'
internal val Char.isSingleCharNoArgPlaceholder get() = isOneOf('⇥', '⇤', '«', '»')
internal val String.isPlaceholder
    get() = (length == 1 && first().isSingleCharNoArgPlaceholder) ||
            (length == 2 && first().isMultiCharNoArgPlaceholder)

internal fun <T> T.isOneOf(t1: T, t2: T, t3: T? = null, t4: T? = null, t5: T? = null, t6: T? = null) =
    this == t1 || this == t2 || this == t3 || this == t4 || this == t5 || this == t6


private fun String.withOpeningBrace(): String {
    for (i in length - 1 downTo 0) {
        if (this[i] == '{') {
            return "$this\n"
        } else if (this[i] == '}') {
            break
        }
    }
    return "$this·{\n"
}

public fun CodeBlock.Builder.beginControlFlow(controlFlow: String, vararg args: Any?): CodeBlock.Builder = apply {
    add(controlFlow.withOpeningBrace(), *args)
    indent()
}

public fun CodeBlock.Builder.endControlFlow(): CodeBlock.Builder = apply {
    unindent()
    add("}\n")
}

public fun CodeBlock.Builder.addStatement(format: String, vararg args: Any?): CodeBlock.Builder = apply {
    add("«")
    add(format, *args)
    add("\n»")
}

