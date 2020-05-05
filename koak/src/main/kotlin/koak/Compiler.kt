package koak

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Scanner

class Compiler {

    private fun generateFilename() : String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
        val filename : String = "tmp_" + current.format(formatter)
        return (filename)
    }

    private fun generateCommand(command : String, arguments : Array<String>?) : String {
        if (arguments == null)
            return command
        var ret = command
        for (value in arguments) {
            ret += " $value"
        }
        return (ret)
    }

    private fun execCommand(outstr : String, arguments : Array<String>?) : Boolean {
        var ret = false
        val cmd = generateCommand(outstr, arguments)
        val executable = Runtime.getRuntime().exec(cmd)
        Scanner(executable.inputStream).use {
            while (it.hasNextLine()) {
                println(it.nextLine())
                ret = true
            }
            ret = true
        }
        Scanner(executable.errorStream).use {
            while (it.hasNextLine()) {
                println(it.nextLine())
                ret = false
            }
        }
        return (ret)
    }

    fun run(outstr: String, arguments : Array<String>? ) {
        val filename = generateFilename()

        val llfile = createTempFile("$filename", ".ll")
        llfile.deleteOnExit()
        llfile.printWriter().use {
            out -> out.println(outstr)
        }
        print(">>> ")
        execCommand("lli " + llfile.absolutePath, arguments)
    }

    fun run(filename: String, outstr: String, isInKoak : Boolean) {
        val llfile = createTempFile("$filename", ".ll")
        llfile.deleteOnExit()
        llfile.printWriter().use {
            out -> out.println(outstr)
        }
        var filepathObject = llfile.absolutePath.substring(0, llfile.absolutePath.length- 3)
        filepathObject += ".o"
        if (execCommand("llc", arrayOf("-filetype=obj", llfile.absolutePath))) {
            execCommand("ld", arrayOf("-dynamic-linker",
                    "/lib64/ld-linux-x86-64.so.2", "/usr/lib/x86_64-linux-gnu/crt1.o",
                    "/usr/lib/x86_64-linux-gnu/crti.o", "/usr/lib/x86_64-linux-gnu/crtn.o",
                    "-lc", "$filepathObject"))
        }
    }
}