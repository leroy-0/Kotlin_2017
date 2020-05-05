package koak

import java.lang.Character.*

object Lexer {
    internal var IdentifierStr: String = ""// Filled in if tok_identifier
    internal var NumVal: Double = 0.toDouble() // Filled in if tok_number
    internal// Skip any whitespace.
            // identifier: [a-zA-Z][a-zA-Z0-9]*
            // Number: [0-9.]+
            // Comment until end of line.
            // Check for end of file.  Don't eat the EOF.
            // Otherwise, just return the character as its ascii value.
    fun tok(): Int{
            var LastChar: Int = ' '.toInt()
            while (isWhitespace(LastChar)) {
                LastChar = System.`in`.read()
            }
            if (isLetter(LastChar)) {
                IdentifierStr = Character.toString(LastChar.toChar())
                while ((isDigit({LastChar = System.`in`.read(); LastChar}()) || isLetter(LastChar))){
                    IdentifierStr += Character.toString(LastChar.toChar())
                }
                if (IdentifierStr.equals("def"))
                    return Token.TOK_DEF.value
                if (IdentifierStr.equals("extern"))
                    return (Token.TOK_EXTERN.value)
                else
                    return (Token.TOK_IDENTIFIER.value)
            }
            if (isDigit(LastChar) || LastChar == '.'.toInt()) {
                var NumStr = ""
                do {
                    NumStr += LastChar
                    LastChar =  System.`in`.read()
                } while (isDigit(LastChar) || LastChar == '.'.toInt())
                NumVal = java.lang.Float.parseFloat(NumStr).toDouble()
                return Token.TOK_NUMBER.value
            }
            if (LastChar == '#'.toInt()) {
                do
                    LastChar = System.`in`.read()
                while (LastChar != '\u0000'.toInt() && LastChar != '\n'.toInt() && LastChar != '\r'.toInt())
                if (LastChar != '\u0000'.toInt())
                    return tok()
            }
            if (LastChar == '\u0000'.toInt())
                return Token.TOK_EOF.value
            val ThisChar = LastChar
            LastChar = System.`in`.read()
            return ThisChar
        }

    enum class Token private constructor(val value: Int) {
        TOK_EOF(-1),
        // commands
        TOK_DEF(-2),
        TOK_EXTERN(-3),
        // primary
        TOK_IDENTIFIER(-4),
        TOK_NUMBER(-5)
    }
}
