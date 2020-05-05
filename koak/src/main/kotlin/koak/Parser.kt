package koak

class Parser(resAst : AST) {


    private val ast : AST = resAst
    private val blocks : MutableList<Block> = mutableListOf()
    private var positionNode : Int = 0
    private var positionBlock : Int = 0
    var parentheses = false
    var returnError = false
    var endInsideFunc = true
    var typeParams = false
    var tokeLogicalError = false

    init {
        run()
    }

    private fun run() {

        if (Koak.debug) {
            for (node in ast.getAllNodes()) {
                node.print()
            }
        }

        while (positionNode < ast.getAllNodes().size) {
            if (ast.getAllNodes()[positionNode].getToken() == " ") {
                if (ast.getAllNodes()[positionNode].getLeft()?.getToken() == ParseEnum.DEFINITION.representation)
                    blocks.add(createBlock(ParseEnum.DEFINITION))
                else if (ast.getAllNodes()[positionNode].getLeft()?.getToken() == ParseEnum.EXTERN.representation)
                    blocks.add(createBlock(ParseEnum.EXTERN))
            }
            else if (positionNode + 1 < ast.getAllNodes().size &&
                    ast.getAllNodes()[positionNode].getToken()?.contains(";") == true &&
                    ast.getAllNodes()[positionNode + 1].getToken()?.contains("(") == true)
                blocks.add(createBlock(ParseEnum.MAIN))

            positionNode++
        }
    }

    private fun getParameterType(toCompare: String?) : ParseEnum {
        var type : ParseEnum = ParseEnum.UNDEFINED
        when (toCompare) {
            ParseEnum.VOID.representation -> type = ParseEnum.VOID
            ParseEnum.DOUBLE.representation -> type = ParseEnum.DOUBLE
            ParseEnum.INT.representation -> type = ParseEnum.INT
            ParseEnum.CHAR.representation -> type = ParseEnum.CHAR
            ParseEnum.STRING.representation -> type = ParseEnum.STRING
        }
        return (type)
    }

    private fun guessParameter(value : String?, token : String?, isLeft : Boolean) : ParseEnum {
        val type : ParseEnum
        if (value != null && value.isNotEmpty())
        {
            type = if (value[0] == '\"')
                ParseEnum.STRING
            else if (value[0] == '\'')
                ParseEnum.CHAR
            else if (value.contains("."))
                ParseEnum.DOUBLE
            else {
                var tmpPos = 0
                while (tmpPos < value.length && AST.isDigit(value[tmpPos]))
                    tmpPos++
                if (tmpPos != value.length) {
                    if (token?.contains("(") == true && isLeft)
                        ParseEnum.CALL
                    else
                        ParseEnum.VARIABLE
                }
                else
                    ParseEnum.INT
            }
        }
        else
            type = ParseEnum.UNDEFINED
        return (type)
    }

    private fun addParam(block : Block, isCall : Boolean) {
        val tokenRight = getTokenParse(ast.getAllNodes()[positionNode].getRight()?.getToken())
        val tokenLeft = getTokenParse(ast.getAllNodes()[positionNode].getLeft()?.getToken())
        if (positionNode < ast.getAllNodes().size && ast.getAllNodes()[positionNode].getToken()?.contentEquals(":") == true &&
                (tokenRight != ParseEnum.UNDEFINED) && tokenLeft == ParseEnum.UNDEFINED) {
            typeParams = true
        }
        if (!isCall)
            block.addParameter(ast.getAllNodes()[positionNode].getLeft()?.getToken(),
                    getParameterType(ast.getAllNodes()[positionNode].getRight()?.getToken()))
        else if (ast.getAllNodes()[positionNode].getToken()?.contains("(") == false)
            block.addParameter(ast.getAllNodes()[positionNode].getLeft()?.getToken(),
                    guessParameter(ast.getAllNodes()[positionNode].getLeft()?.getToken(), ast.getAllNodes()[positionNode].getToken(), true))

        if (positionNode < ast.getAllNodes().size)
            positionNode++
        if (positionNode < ast.getAllNodes().size && ast.getAllNodes()[positionNode].getToken() == " ")
            positionNode++
        else if (positionNode < ast.getAllNodes().size && ast.getAllNodes()[positionNode].getToken()?.contains(")") == true && isCall)
            block.addParameter(ast.getAllNodes()[positionNode].getLeft()?.getToken(),
                    guessParameter(ast.getAllNodes()[positionNode].getLeft()?.getToken(), ast.getAllNodes()[positionNode].getToken(), true))
    }

    private fun createParams(block : Block) {
        var isCall = false
        if (positionNode < ast.getAllNodes().size && ast.getAllNodes()[positionNode].getToken()?.contains("()") == true)
            parentheses = true
        if (positionNode + 1 < ast.getAllNodes().size &&
                ast.getAllNodes()[positionNode + 1].getToken()?.contains(":") == false)
            isCall = true
        else
            positionNode++

        while (positionNode < ast.getAllNodes().size && ast.getAllNodes()[positionNode].getToken()?.contains(")") == false)
            addParam(block, isCall)

        if (positionNode < ast.getAllNodes().size && ast.getAllNodes()[positionNode].getToken()?.contentEquals("():") == true)
            parentheses = true
        if (positionNode < ast.getAllNodes().size && ast.getAllNodes()[positionNode].getToken()?.contentEquals("):") == true)
            parentheses = true

        if (isCall)
            positionNode--
    }

    private fun createReturnType(block : Block) {
        var hasParam = false
        if (ast.getAllNodes()[positionNode].getToken() == "):" || ast.getAllNodes()[positionNode].getToken() == ") :") {
            val tokenRet = getTokenParse(ast.getAllNodes()[positionNode].getRight()?.getToken())
            if (tokenRet != ParseEnum.UNDEFINED)
                block.setRetType(getParameterType(ast.getAllNodes()[positionNode].getRight()?.getToken()))
                else
                returnError = true
            hasParam = true
        }
        else
            parentheses = true
        positionNode++
        if (hasParam && ast.getAllNodes()[positionNode].getToken() == "R")
            positionNode++
    }

    private fun getToken(representation: String?) : TokenEnum {
        return TokenEnum.values()
                .firstOrNull { representation == it.representation }
                ?.let { (it) }
                ?: (TokenEnum.UNDEFINED)
    }

    private fun getTokenParse(representation: String?) : ParseEnum {
        return ParseEnum.values().firstOrNull { representation == it.representation }
                ?.let { (it) }
                ?: (ParseEnum.UNDEFINED)
    }

    private fun getComponentValues(parseEnum: ParseEnum?) : Component {
        positionNode++
        val pos : Int = positionNode
        var highest : TokenEnum = getToken(ast.getAllNodes()[pos].getToken())
        var tempValue : TokenEnum
        var savedPos : Int = pos

        while (positionNode < ast.getAllNodes().size &&
                ast.getAllNodes()[positionNode].getLeft()?.getToken() != ParseEnum.THEN.representation) {
            if (ast.getAllNodes()[positionNode].getToken() != " ") {
                tempValue = getToken(ast.getAllNodes()[positionNode].getToken())
                if (highest.value < tempValue.value) {
                    highest = tempValue
                    savedPos = positionNode
                }
            }
            positionNode++
        }
        val component = getInsideValues(savedPos, false)
        component.setParseEnum(parseEnum)
        positionNode++
        return (component)
    }

    private fun getInsideValues(pos : Int, isAlone : Boolean) : Component {
        checkToken(pos)
        val compoChild = Component()
        if (pos < ast.getAllNodes().size) {
            compoChild.setValueLeft(Param(ast.getAllNodes()[pos].getLeft()?.getToken(), guessParameter(ast.getAllNodes()[pos].getLeft()?.getToken(), ast.getAllNodes()[pos].getToken(), true)))
            if (!isAlone)
                compoChild.setValueRight(Param(ast.getAllNodes()[pos].getRight()?.getToken(), guessParameter(ast.getAllNodes()[pos].getRight()?.getToken(), ast.getAllNodes()[pos].getToken(), false)))
            compoChild.setTokenEnum(getToken(ast.getAllNodes()[pos].getToken()))
        }
        return (compoChild)
    }

    private fun checkToken(pos : Int)
    {
        if (pos < ast.getAllNodes().size) {
            //val token = getToken(ast.getAllNodes()[pos].getToken())
            //tokeLogicalError = !(tokeLogicalError == false && token != TokenEnum.UNDEFINED)
        }
    }

    private fun getExpressionType(valueLeft : String?, valueRight : String?): ParseEnum {
        return when (valueLeft) {
            ParseEnum.IF.representation -> (ParseEnum.IF)
            ParseEnum.ELSE.representation -> {
                if (valueRight == ParseEnum.IF.representation)
                    (ParseEnum.ELSE_IF)
                else
                    (ParseEnum.ELSE)
            }
            ParseEnum.WHILE.representation -> (ParseEnum.WHILE)
            ParseEnum.FOR.representation -> (ParseEnum.FOR)
            ParseEnum.DO.representation -> (ParseEnum.DO)
            else -> (ParseEnum.UNDEFINED)
        }
    }

    private fun setTypes(block : Block) {
        val compo : Component
        val tokenLeft = ast.getAllNodes()[positionNode].getLeft()?.getToken()
        val tokenRight = ast.getAllNodes()[positionNode].getRight()?.getToken()
        val type = getExpressionType(tokenLeft, tokenRight)

        if (type != ParseEnum.UNDEFINED) {
            if (type == ParseEnum.ELSE_IF)
                positionNode++
            if (type != ParseEnum.DO && type != ParseEnum.ELSE)
                compo = getComponentValues(type)
            else {
                compo = Component()
                compo.setParseEnum(type)
                positionNode++
                if (type == ParseEnum.DO)
                    positionNode++
            }

            var isAlone = false
            if (positionNode < ast.getAllNodes().size)
                 isAlone = getExpressionType(ast.getAllNodes()[positionNode].getRight()?.getToken(),
                         ast.getAllNodes()[positionNode].getLeft()?.getToken()) != ParseEnum.UNDEFINED

            compo.addChild(getInsideValues(positionNode, isAlone))
            if (!isAlone)
                positionNode++
            if (positionNode < ast.getAllNodes().size &&
                    ast.getAllNodes()[positionNode].getToken()?.contains(";") == true)
                positionNode--
        }
        else {
            compo = getInsideValues(positionNode, false)
            if (ast.getAllNodes().size > positionNode)
                positionNode++
            while (ast.getAllNodes().size > positionNode && getToken((ast.getAllNodes()[positionNode].getToken())) != TokenEnum.UNDEFINED) {
                compo.addChild(getInsideValues(positionNode, false))
                checkToken(positionNode)
                positionNode++
            }
            positionNode--
        }
        block.addComponent(compo)
    }

    private fun createInsideFunc(block : Block) {
        while (positionNode < ast.getAllNodes().size &&
                ast.getAllNodes()[positionNode].getLeft()?.getToken() != ParseEnum.DEFINITION.representation &&
                ast.getAllNodes()[positionNode].getLeft()?.getToken() != ParseEnum.EXTERN.representation &&
                ast.getAllNodes()[positionNode].getToken()?.contains(";") == false)
        {
            if (ast.getAllNodes()[positionNode].getToken()?.contains("") == true ||
                ast.getAllNodes()[positionNode].getToken()?.contains("R") == false ||
                    ast.getAllNodes()[positionNode].getToken()?.contains("(") == true)
                setTypes(block)
            positionNode++
        }
        positionNode--
    }

    private fun createBlock(type : ParseEnum) : Block {
        val defBlock = Block(ast.getAllNodes()[positionNode].getRight()?.getToken(), type)

        positionNode++

        if (positionNode < ast.getAllNodes().size && ast.getAllNodes()[positionNode].getToken()?.contains("(") == true)
            createParams(defBlock)

        if (type != ParseEnum.MAIN) {
            if (positionNode < ast.getAllNodes().size &&
                    (ast.getAllNodes()[positionNode].getToken() == ")" || ast.getAllNodes()[positionNode].getToken() == ")R" ||
                            ast.getAllNodes()[positionNode].getToken() == ") R" || ast.getAllNodes()[positionNode].getToken() == "):"
                            || ast.getAllNodes()[positionNode].getToken() == ") :"))
                createReturnType(defBlock)
            if (positionNode < ast.getAllNodes().size)
                createInsideFunc(defBlock)
        }

        positionBlock++
        return (defBlock)
    }

    fun getBlocks() : MutableList<Block> {
        return (blocks)
    }
}