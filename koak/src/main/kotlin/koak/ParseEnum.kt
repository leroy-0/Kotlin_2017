package koak

enum class ParseEnum constructor(val representation : String) {

    UNDEFINED("undefined"),
    VARIABLE("var"),
    ASSIGNMENT("assignment"),
    CALL("call"),
    TMP_VALUE("tmpV"),

    // Function
    DEFINITION("def"),
    EXTERN("extern"),
    EXPR("expr"),
    MAIN("main"),

    // Types
    INT("int"),
    DOUBLE("double"),
    VOID("void"),
    CHAR("char"),
    STRING("string"),

    // Expressions
    IF("if"),
    THEN("then"),
    ELSE("else"),
    ELSE_IF("else if"),
    WHILE("while"),
    DO("do"),
    FOR("for"),
    IN("in"),
    RETURN("return"),
}
