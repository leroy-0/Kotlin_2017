package koak

import java.io.File

class Koak {

    private val compiler : Compiler = Compiler()
    private var streamInput : String = ""

    companion object {
        var debug : Boolean = false
    }

    private fun convertToLLVM(inputStr : String) : String {
        val ast = AST(inputStr)
        if (ast.errorDef)
            return ("Error Type Definitions")
        val parser = Parser(ast)
        val generator = Generator(parser)
        /*if (!parser.typeParams)
            return ("Error when parsing type parameters")
        if (parser.tokeLogicalError)
            return ("Error when parsing inside function (Logical error)")
        if (parser.returnError)
            return ("Error when parsing return type")
        if (!parser.parentheses)
            return ("Error when parsing parentheses")
        if (!parser.endInsideFunc)
            return ("Error when  parsing inside function (cannot find ';' at the end of the function)")*/
        return (generator.getOutput())
    }

    private fun getOutputLLVM(file : String) : String? {
        if (file.endsWith(".koak") || file.endsWith(".ll")) {
            val f = File(file)
            if (f.exists() && !f.isDirectory) {
                val inputString = f.inputStream().bufferedReader().use { it.readText() }
                return if (file.endsWith(".koak")) {
                    (convertToLLVM(inputString))
                } else
                    (inputString)
            } else
                println("Error: specified file is invalid")
        }
        else
            println("Error: specified file isn't a valid .koak script")
        return (null)
    }

    fun run(_debug : Boolean)
    {
        Koak.debug = _debug
        println("KOAK, compiler/interpreter #Version Epibros-0.0.1")
        while (true)
        {
            print("ready> ")
            val value : String? = readLine()
            if (value != null && value.isNotEmpty()) {
                if (value == "quit")
                    break
                when {
                    value == "clear" -> streamInput = ""
                    value == "sample" -> println(Generator.getSampleOutput())
                    value.contains("import") -> {
                        if (value.indexOf(" ") != -1) {
                            val file = value.substring(value.indexOf(" ") + 1, value.length)
                            val output = getOutputLLVM(file)
                            if (output != null)
                            {
                                println(output)
                                compiler.run(output, null)
                            }
                        }
                    }
                    else -> {
                        streamInput += value
                        val output = convertToLLVM(streamInput)
                        println(output)
                        if (output.contains("@main"))
                            compiler.run(output, null)
                    }
                }
            }
        }
    }

    fun run(file: String) {
        val output = getOutputLLVM(file)
        val filename: String = file.substringBeforeLast(".")
        if (output != null) {
            if (file.endsWith(".ll"))
                compiler.run(filename, output, false)
            else
                compiler.run(filename, output, true)
        }
    }
}