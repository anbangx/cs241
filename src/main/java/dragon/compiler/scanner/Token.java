package dragon.compiler.scanner;

public enum Token {
    
    /** keyword **/
    LET, CALL, IF, THEN, ELSE, FI,WHILE, DO, OD, RETURN,
    VAR, ARRAY, FUNCTION, PROCEDURE, MAIN,
    
    /** designator **/
    DESIGNATOR, // <-
    
    /** Operator **/
    PLUS, MINUS, TIMES, DIVIDE, // +,-,*,/
    
    /** Comparison **/
    EQL, NEQ, LSS, GRE, LEQ, GEQ, // ==, !=, <, >, <=, >=
    
    /** Punctuation **/
    PERIOD, COMMA, SEMICOMA, COLON, // . , ; :
    
    /** Block **/
    BEGIN_PARENTHESIS, END_PARENTHESIS, // (, )
    BEGIN_BRACKET, END_BRACKET, // [, ]
    BEGIN_BRACE, END_BRACE, // {, }
    
    /** Others **/
    NUMBER, // 0 ~ 9
    IDENTIRIER // LETTER
}
