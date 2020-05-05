package koak

import java.lang.System.exit

class AST(allCodeStr: String) {

    private val innerCode : String = allCodeStr
    private val allNodes : MutableList<Node> = mutableListOf()
    private var position : Int = 0
    var errorDef = false

    init {
        if (Koak.debug)
            println(allCodeStr)
        run()
    }

    private fun run() {
        /*if (!innerCode.startsWith(ParseEnum.EXTERN.representation) &&
                !innerCode.startsWith(ParseEnum.DEFINITION.representation) &&
                !innerCode.startsWith(ParseEnum.EXPR.representation)) {
            errorDef = true
            return
        }*/
        position = this.getNext(0)
        while (!this.isDone()) {
            val rule = this.getRule()
            allNodes.add(createNode(this.position, this.position + getRuleLength() - 1, rule))
            position = this.getNext(this.position + getRuleLength())
        }
    }

    private fun createNode(indexLeft : Int, indexRight : Int, rule : String) : Node {
        val nodeLeft = Node(this.getPart(indexLeft, false))
        val nodeRight = Node(this.getPart(indexRight, true))
        val token = cleanString(rule)
        val node = Node(token, nodeLeft, nodeRight)
        return (node)
    }

    private fun cleanString(toChange : String) : String {
        var str : String = toChange.replace(Regex(pattern = "[\\n\\r]{2,}"), "R")
        str = str.replace(Regex(pattern = "[\\t]"), " ")
        str = str.replace(Regex(pattern = "^\\s+|\\s+\$|\\s+(?=\\s)"), "")
        if (str.isEmpty())
            str = " "
        return (str)
    }

    private fun avoidCommentary(toAvoid : Char, realPos : Int) : Int {
        var pos = realPos
        var checked = false
        if (pos < innerCode.length && innerCode[pos] == toAvoid)
        {
            while (pos < innerCode.length) {
                if (innerCode[pos] == '\n' || innerCode[pos] == '\r')
                    checked = true
                if (checked && isAlphaNumeric(innerCode[pos]))
                    break
                if (checked && innerCode[pos] == toAvoid)
                    pos = avoidCommentary(toAvoid, pos)
                else
                    pos++
            }
        }
        return (pos)
    }

    private fun getPart(index : Int, where : Boolean) : String {
        var move = ""
        var tmpPos: Int = if (where) index + 1 else index - 1
        var lock = tmpPos < innerCode.length && tmpPos >= 0 &&
                (innerCode[tmpPos] == '\'' || innerCode[tmpPos] == '\"')

        if (where)
            tmpPos = avoidCommentary('#', tmpPos)
        while (tmpPos < innerCode.length && tmpPos >= 0 &&
                (isAlphaNumeric(innerCode[tmpPos]) || lock)) {
            move += innerCode[tmpPos]
            tmpPos = if (where) tmpPos + 1 else tmpPos - 1
            if (tmpPos < innerCode.length && tmpPos >= 0 &&
                (innerCode[tmpPos] == '\'' || innerCode[tmpPos] == '\"')) {
                lock = !lock
                if (!lock)
                    move += innerCode[tmpPos]
            }
        }
        return (if (where) move else move.reversed())
    }

    private fun getRuleLength() : Int {
        var pos : Int = position
        while (pos < innerCode.length && !isAlphaNumeric(innerCode[pos])
                && innerCode[pos] != '#'  && innerCode[pos] != '\'' && innerCode[pos] != '\"') {
            pos++
        }
        return (pos - position)
    }

    private fun getRule() : String {
        var move = ""
        var pos : Int = position
        while (pos < innerCode.length && !isAlphaNumeric(innerCode[pos])
                && innerCode[pos] != '#' && innerCode[pos] != '\'' && innerCode[pos] != '\"') {
            move += innerCode[pos]
            pos++
        }
        if (avoidCommentary('#', pos) != pos)
            move += "R"
        return (move)
    }

    private fun getNext(posNext : Int) : Int {
        var pos = avoidCommentary('#', posNext)
        var lock = pos < innerCode.length && (innerCode[pos] == '\'' || innerCode[pos] == '\"')
        while (pos < innerCode.length && (isAlphaNumeric(innerCode[pos]) || lock))
        {
            pos++
            if (pos < innerCode.length && (innerCode[pos] == '\'' || innerCode[pos] == '\"')) {
                lock = !lock
                if (!lock)
                    pos++
            }
        }
        return (pos)
    }

    fun getAllNodes() : MutableList<Node> {
        return (this.allNodes)
    }

    private fun isDone() : Boolean {
        return (this.position >= this.innerCode.length)
    }

    companion object {
        fun isDigit(c: Char): Boolean {
            return c in '0'..'9'
        }

        private fun isAlpha(c: Char): Boolean {
            return c in 'a'..'z' ||
                    c in 'A'..'Z' ||
                    c == '_' ||
                    c == '.'
        }

        fun isAlphaNumeric(c: Char): Boolean {
            return isAlpha(c) || isDigit(c)
        }
    }
}