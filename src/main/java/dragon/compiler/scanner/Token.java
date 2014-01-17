package dragon.compiler.scanner;

public enum Token {
    
    /** keyword **/
    LET, CALL, IF, THEN, ELSE, FI,WHILE, DO, OD, RETURN,
    VAR, ARRAY, FUNCTION, PROCEDURE, MAIN,
    
    /** designator **/
    BECOMETO, // <-
    
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
    IDENTIRIER, // LETTER
    EOF, // End of file
    ERROR;
    
    public static Token getKeywordFromString(String s){
        switch(s){
            case "let":
                return LET;
            case "call":
                return CALL;
            case "if":
                return IF;
            case "then":
                return THEN;
            case "else":
                return ELSE;
            case "fi":
                return FI;
            case "while":
                return WHILE;
            case "do":
                return DO;
            case "od":
                return OD;
            case "return":
                return RETURN;
            case "var":
                return VAR;
            case "array":
                return ARRAY;
            case "function":
                return FUNCTION;
            case "procedure":
                return PROCEDURE;
            case "main":
                return MAIN;
        }
        return null;
    }
}
