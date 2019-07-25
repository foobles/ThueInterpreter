abstract class Rule(private val from: String) {
    protected abstract fun runSubst(): String

    fun substitute(str: String): String? {
        if (str.length < from.length) return null
        val matchIndices = mutableListOf<Int>()
        for (i in 0..str.length - from.length) {
            if (str.subSequence(i, i + from.length) == from) {
                matchIndices.add(i)
            }
        }
        return if (matchIndices.size> 0) {
            val index = matchIndices.random()
            str.replaceRange(index, index + from.length, runSubst())
        } else {
            null
        }
    }
}

class StandardRule(from: String, private val into: String) : Rule(from) {
    override fun runSubst() = into
}

class PrintRule(from: String, private val message: String) : Rule(from) {
    override fun runSubst(): String {
        if (message.isEmpty()) {
            println()
        } else {
            print(message)
        }
        return ""
    }
}

class ReadRule(from: String) : Rule(from) {
    override fun runSubst() = readLine() ?: ""
}


class ThueProg(private vararg val rules: Rule, var state: String) {
    fun run() {
        val ruleList = rules.toMutableList()
        var done = false
        while (!done) {
            done = true
            ruleList.shuffle()
            for (rule in ruleList) {
                val newState = rule.substitute(state)
                if (newState != null) {
                    state = newState
                    done = false
                    break
                }
            }
        }
    }
}

fun parseRule(str: String): Rule? {
    val sides = str.split("::=", limit = 2)
    if (sides.size != 2) return null
    val lhs = sides[0]
    val rhs = sides[1]
    return when {
        rhs == ":::" -> ReadRule(lhs)
        rhs.isNotEmpty() && rhs.first() == '~' -> PrintRule(lhs, rhs.drop(1))
        else -> StandardRule(lhs, rhs)
    }
}

enum class ParseState {
    DEF_RULES, DEF_START_STATE, END
}

fun parseProgram(prog: String): ThueProg? {
    var state = ParseState.DEF_RULES
    val rules = mutableListOf<Rule>()
    var startState: String? = null
    for (rawLine in prog.lines()) {
        val line = rawLine.trim(' ')
        if (line.isEmpty()) continue
        when (state) {
            ParseState.DEF_RULES -> {
                if (line != "::=") {
                    rules.add(parseRule(line) ?: return null)
                } else {
                    state = ParseState.DEF_START_STATE
                }
            }
            ParseState.DEF_START_STATE -> {
                startState = line
                state = ParseState.END
            }
            ParseState.END -> return null
        }
    }
    return ThueProg(*rules.toTypedArray(), state = startState ?: return null)
}

fun main() {
    val prog = parseProgram("""
        1_::=1++
        0_::=1
        01++::=10

        11++::=1++0

        _0::=_
        _1++::=10

        ::=

        _1111111111_
    """.trimIndent())
    prog?.run()
    println("FINAL STATE: ${prog?.state ?: ""}")
}