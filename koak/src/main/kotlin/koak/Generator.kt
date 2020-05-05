package koak

import me.tomassetti.kllvm.*
import java.text.FieldPosition

class Generator(_parser : Parser) {

    private val externs : MutableList<FunctionDeclaration> = mutableListOf()
    private val functions : MutableList<FunctionBuilder> = mutableListOf()
    private val module = ModuleBuilder()
    private var mainFunction = module.createMainFunction()
    private val parser : Parser = _parser
    private var tempValue : Int = 0

    init {
        run()
    }

    private fun run() {
        for (block in parser.getBlocks()) {
            if (Koak.debug)
                block.print()
            createBlock(block)
        }
        mainFunction?.addInstruction(ReturnInt(0))
    }

    fun getOutput() : String {
        return (module.IRCode())
    }

    private fun getType(type : ParseEnum): Type {
        return when (type) {
            ParseEnum.VOID -> (VoidType)
            ParseEnum.INT -> (I32Type)
            ParseEnum.DOUBLE -> (CustomType("double"))
            ParseEnum.CHAR -> (I32Type)
            ParseEnum.STRING -> (Pointer(I8Type))
            else -> {
                (VoidType)
            }
        }
    }

    private fun getParameters(parameters : MutableList<Param>) : List<Type> {
        val realParams : MutableList<Type> = mutableListOf()
        parameters.mapTo(realParams) { getType(it.getType()) }
        return (realParams)
    }

    private fun getInternFunc(value : String) : FunctionBuilder? {
        functions
                .filter { it.name == value }
                .forEach { return (it) }
        return (null)
    }

    private fun getExternFunc(value : String) : FunctionDeclaration? {
        externs
                .filter { it.name == value }
                .forEach { return (it) }
        return (null)
    }

    class VoidConst : Value {
        override fun type() = VoidType

        override fun IRCode(): String = "*"
    }

    private fun removeTrash(value : String) : String {
        var newVal = value.replace("\'", "")
        newVal = newVal.replace("\"", "")
        return (newVal)
    }

    private fun getValue(block : Block, value : Param?) : Value {
        if (value != null && value.getName().isNotEmpty()) {
            val type = if (value.getType() == ParseEnum.VARIABLE)
                getParamType(block, value.getName())
            else
                getType(value.getType())
            if (type != null)
                return when (value.getType()) {
                    ParseEnum.INT -> IntConst(value.getName().toInt(), type)
                    ParseEnum.CHAR -> IntConst(removeTrash(value.getName())[0].toInt(), type)
                    ParseEnum.DOUBLE -> FloatConst(value.getName().toFloat(), type)
                    ParseEnum.STRING -> StringReference(mainFunction?.stringConstForContent(removeTrash(value.getName())) as StringConst)
                    ParseEnum.VARIABLE -> LocalValueRef(getParam(block, value.getName()).toString(), type)
                    else -> {
                        LocalValueRef(value.getName(), type)
                    }
                }
        }
        return (LocalValueRef("", CustomType("")))
    }

    private fun getFunc(block : Block, funcName : String?, valueRight : Param?) : Instruction? {
        val extern = getExternFunc(funcName.toString())
        val intern = getInternFunc(funcName.toString())
        if (valueRight == null || valueRight.getName() == "")
        {
            if (extern != null)
                return(CallWithBitCast(extern))
            else if (intern != null)
                return(Call(intern.returnType, intern.name, VoidConst()))
        }
        else
        {
            val value = getValue(block, valueRight)
            if (extern != null)
                return (CallWithBitCast(extern, value))
            else if (intern != null)
                return (Call(intern.returnType, intern.name, value))
        }
        return (null)
    }

    private fun createInsideFunc(block : Block, function : FunctionBuilder) {
        for (component in block.getComponents()) {
            if (component.getTokenEnum() == TokenEnum.UNDEFINED)
            {
                val instruct = getFunc(block, block.getConfig().getName(), component.getValueRight())
                if (instruct != null)
                function.addInstruction(instruct)
            }
        }
    }

    private fun getParam(block : Block, value : String?) : String? {
        for ((index, parameter) in block.getParameters().withIndex()) {
            if (parameter.getName() == value)
                return (index.toString())
        }
        return (null)
    }

    private fun getParamType(block : Block, value : String?) : Type? {
        for ((index, parameter) in block.getParameters().withIndex()) {
            if (parameter.getName() == value)
                return (getType(parameter.getType()))
        }
        return (null)
    }

    private fun getInstruction(block : Block, component : Component, valueLeft : Param?, valueRight : Param?, isFirst : Boolean, name : String) : Instruction? {
        var instruction: Instruction? = null
        val valueL: Value = if (isFirst) getValue(block, valueLeft) else LocalValueRef(name, getType(valueLeft?.getType() as ParseEnum))
        if (component.getTokenEnum() == TokenEnum.PLUS) {
            if (block.getReturnType() == ParseEnum.DOUBLE)
                instruction = FloatAddition(valueL, getValue(block, valueRight))
            else if (block.getReturnType() == ParseEnum.INT)
                instruction = IntAddition(valueL, getValue(block, valueRight))
        }
        return (instruction)
    }

    private fun getComparisonType(token : TokenEnum?) : ComparisonType? {
        return when (token) {
            TokenEnum.EQUAL_EQUAL -> (ComparisonType.Equal)
            TokenEnum.NOT_EQUAL -> (ComparisonType.NotEqual)
            else -> {
                (null)
            }
        }
    }

    private fun checkChildrenBlock(component: Component, blockToCreate: BlockBuilder, block: Block) {
        for (childComponent in component.getChildren()) {
            if (childComponent.getChildren().isNotEmpty())
                checkChildrenBlock(childComponent, blockToCreate, block)
            if (childComponent.getParseToken() == null && childComponent.getTokenEnum() == TokenEnum.UNDEFINED &&
                    childComponent.getValueLeft() != null && childComponent.getValueLeft()?.getType() == ParseEnum.INT &&
                    childComponent.getValueLeft()?.getName()?.isNotEmpty() == true) {
                blockToCreate.addInstruction(ReturnInt(childComponent.getValueLeft()?.getName()?.toInt() as Int))
            }
            else if (childComponent.getValueLeft()?.getType() == ParseEnum.CALL)
            {
                val instruct = getFunc(block, childComponent.getValueLeft()?.getName(), childComponent.getValueRight())
                if (instruct != null)
                    blockToCreate.addInstruction(instruct)
            }
        }
    }

    private fun createBlock(name : String, block: Block, function: FunctionBuilder, components : MutableList<Component>, parseEnum: ParseEnum) : BlockBuilder {
        val blockToCreate = function.createBlock(name)

        for (component in components) {
            if (component.getParseToken() == parseEnum)
            {
                checkChildrenBlock(component, blockToCreate, block)
            }
        }
        return (blockToCreate)
    }

    private fun createCalls(component: Component, block: Block, function: FunctionBuilder) {
        var name : String = component.getChildren().elementAt(0).getValueLeft()?.getName().toString()
        var first = true
        for (childComponent in component.getChildren()) {
//            val valueL = Param(name, block.getReturnType())
            val instruction = getInstruction(block, childComponent, childComponent.getValueLeft(), childComponent.getValueRight(), first, name)
            if (instruction != null) {
                name = function.tempValue(instruction).name
            }
            first = false
        }
        function.addInstruction(Return(LocalValueRef(name, getType(block.getReturnType()))))
        tempValue++
    }

    private fun createIfElse(component: Component, block: Block, function: FunctionBuilder) {
        val comparisonType = getComparisonType(component.getTokenEnum())

        if (comparisonType != null)
        {
            val ok = createBlock("okParams" + tempValue, block, function, block.getComponents(), ParseEnum.IF)
            val ko = createBlock("koParams" + tempValue, block, function, block.getComponents(), ParseEnum.ELSE)

            if (block.getReturnType() == ParseEnum.VOID)
            {
                ok.addInstruction(ReturnVoid())
                ko.addInstruction(ReturnVoid())
            }

            val comparisonResult = function.tempValue(Comparison(comparisonType,
                    getValue(block, component.getValueLeft()), getValue(block, component.getValueRight())))
            function.addInstruction(IfInstruction(comparisonResult.reference(), ok, ko))
            tempValue++
        }
    }

    private fun createReturn(block : Block, function: FunctionBuilder) {
        if (block.getReturnType() == ParseEnum.UNDEFINED)
            block.setRetType(ParseEnum.VOID)

        for (component in block.getComponents()) {
            if (component.getParseToken() == null && component.getTokenEnum() == TokenEnum.UNDEFINED &&
                    component.getChildren().isNotEmpty())
                createCalls(component, block, function)
            else if (component.getParseToken() == ParseEnum.IF)
                createIfElse(component, block, function)
        }
    }

    private fun createBlock(block : Block) {

        when {
            block.getConfig().getType() == ParseEnum.EXTERN -> {
                var varArgs = false
                if (block.getConfig().getName() == "printf")
                    varArgs = true
                val func = FunctionDeclaration(block.getConfig().getName(), getType(block.getReturnType()),
                        getParameters(block.getParameters()), varargs = varArgs)
                externs.add(func)
                module.addDeclaration(func)
            }
            block.getConfig().getType() == ParseEnum.DEFINITION -> {
                val function = module.createFunction(block.getConfig().getName(), getType(block.getReturnType()),
                        getParameters(block.getParameters()))
                createInsideFunc(block, function)
                createReturn(block, function)
                functions.add(function)
            }
            block.getConfig().getType() == ParseEnum.MAIN -> {
                if (block.getReturnType() == ParseEnum.UNDEFINED) {
                    val instruct = getFunc(block, block.getConfig().getName(), block.getParameters()[0])
                    if (instruct != null)
                        mainFunction?.addInstruction(instruct)
                }
            }
            block.getConfig().getType() == ParseEnum.UNDEFINED ->
            {
                println("Type is undefined")
            }
        }
    }

    companion object {
        fun getSampleOutput() : String {
            val exitCodeOk = 0
            val exitWrongParams = 1
            val nbParamsExpected = 2
            val stringType = Pointer(I8Type)

            val module = ModuleBuilder()
            val mainFunction = module.createMainFunction()

            val atoiDeclaration = FunctionDeclaration("atoi", I32Type, listOf(), varargs = true)
            module.addDeclaration(atoiDeclaration)
            module.addDeclaration(FunctionDeclaration("printf", I32Type, listOf(stringType), varargs = true))

            val okParamsBlock = mainFunction.createBlock("okParams")
            val koParamsBlock = mainFunction.createBlock("koParams")

            val comparisonResult = mainFunction.tempValue(Comparison(ComparisonType.Equal,
                    mainFunction.paramReference(0), IntConst(nbParamsExpected + 1, I32Type)))
            mainFunction.addInstruction(IfInstruction(comparisonResult.reference(), okParamsBlock, koParamsBlock))

            // OK Block : convert to int, sum, and print
            val aAsStringPtr = okParamsBlock.tempValue(GetElementPtr(stringType, mainFunction.paramReference(1), IntConst(1, I64Type)))
            val aAsString = okParamsBlock.load(aAsStringPtr.reference())
            val aAsInt = okParamsBlock.tempValue(CallWithBitCast(atoiDeclaration, aAsString))
            val bAsStringPtr = okParamsBlock.tempValue(GetElementPtr(stringType, mainFunction.paramReference(1), IntConst(2, I64Type)))
            val bAsString = okParamsBlock.load(bAsStringPtr.reference())
            val bAsInt = okParamsBlock.tempValue(CallWithBitCast(atoiDeclaration, bAsString))
            val sum = okParamsBlock.tempValue(IntAddition(aAsInt.reference(), bAsInt.reference()))
            okParamsBlock.addInstruction(Printf(mainFunction.stringConstForContent("Result: %d\n").reference(), sum.reference()))
            okParamsBlock.addInstruction(ReturnInt(exitCodeOk))

            // KO Block : error message and exit
            koParamsBlock.addInstruction(Printf(mainFunction.stringConstForContent("Please specify two arguments").reference()))
            koParamsBlock.addInstruction(ReturnInt(exitWrongParams))

            return (module.IRCode())
        }
    }
}