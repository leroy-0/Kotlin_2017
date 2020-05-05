package koak

enum class TokenEnum constructor(val representation : String, val value : Int, val dir : Boolean) {
    UNDEFINED("", -1, false),

    EQUAL("=",10, true),
    PLUS_EQUAL("+=",10, true),
    MINUS_EQUAL("-=",10, true),
    STAR_EQUAL("*=",10, true),
    SLASH_EQUAL("/=",10, true),
    MODULO_EQUAL("%=",10, true),
    DOUBLE_LOWER_EQUAL("<<=",10, true),
    DOUBLE_GREATER_EQUAL(">>=",10, true),
    AND_EQUAL("&=",10, true),
    CHEVRON_EQUAL("^=",10, true),
    PIPE_EQUAL("|=",10, true),

    // Logical
    PIPE_PIPE("||",20, false),
    AND_AND("&&",30, false),

    // Binary
    PIPE("|",40, false),
    CHEVRON("^",50, false),
    AND("&",60, false),
    DOUBLE_LOWER("<<",90, false),
    DOUBLE_GREATER(">>",90, false),

    // Comparison
    NOT("!",10, false),
    NOT_EQUAL("!=",70, false),
    EQUAL_EQUAL("==",70, false),
    LESS("<",80, false),
    LESS_EQUAL("<=",80, false),
    GREATER(">",80, false),
    GREATER_EQUAL(">=",80, false),

    // Calculus
    MINUS("-",100, false),
    PLUS("+",100, false),
    SLASH("/",110, false),
    STAR("*",110, false),
    MODULO("%",110, false),
}