package dragon.compiler.parser;

import dragon.compiler.scanner.Scanner;
import dragon.compiler.scanner.Token;

public class Parser {

    private Scanner scanner;
    private Token token;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
        this.token = next();
    }

    public Token next() {
        return scanner.getNextToken();
    }
    
    public void parse(){
        computation();
    }
    
    private void computation() {
        if (token == Token.MAIN) {
            next();
            while (token == Token.VAR || token == Token.ARRAY) {
                varDecl();
            }
            while (token == Token.FUNCTION || token == Token.PROCEDURE) {
                funcDecl();
            }
            if (token == Token.BEGIN_BRACE) {
                next();
                statSequence();
                if (token == Token.END_BRACE) {
                    next();
                    if (token == Token.PERIOD) {
                        next();
                    } else
                        printSyntaxError(token.toString()
                                + " is not a valid ., which is required in the end of computation!");
                } else
                    printSyntaxError(token.toString() + " is not a valid }, which is required in computation!");
            } else
                printSyntaxError(token.toString() + " is not a valid {, which is required in computation!");
        } else
            printSyntaxError(token.toString()
                    + " is not a valid Main, which is required to be the first token in computation!");
    }

    private void designator() {
        if (token == Token.IDENTIRIER) {
            next();
            while (token == Token.BEGIN_BRACKET) {
                next();
                expression();
                if (token == Token.END_BRACKET)
                    next();
                else
                    printSyntaxError("[ should be followed by ]");
            }
        } else {
            printSyntaxError(token.toString()
                    + " is not a valid IDENTIRIER, which is required to be the first token in designator!");
        }
    }

    private void factor() {
        if (token == Token.IDENTIRIER) {
            designator();
        } else if (token == Token.NUMBER) {
            // TODO return a val here
        } else if (token == Token.BEGIN_PARENTHESIS) {
            next();
            expression();
            if (token == Token.END_PARENTHESIS)
                next();
            else
                printSyntaxError("( should be followed by )");
        } else if (token == Token.CALL) {
            funcCall();
        } else
            printSyntaxError(token.toString() + " is not a valid factor!");
    }

    private void term() {
        factor();
        while (token == Token.TIMES || token == Token.DIVIDE) {
            next();
            factor();
        }
    }

    private void expression() {
        term();
        while (token == Token.PLUS || token == Token.MINUS) {
            next();
            term();
        }
    }

    private boolean isRelOp() {
        return token == Token.EQL || token == Token.NEQ || token == Token.LSS || token == Token.LEQ
                || token == Token.GRE || token == Token.GEQ;
    }

    private void relation() {
        expression();
        if (isRelOp()) {
            next();
            expression();
        } else
            printSyntaxError(token.toString()
                    + " is not a valid relOp, which is required to be followed by expression in relation!");
    }

    private void assignment() {
        if (token == Token.LET) {
            next();
            designator();
            if (token == Token.DESIGNATOR) {
                next();
                expression();
            } else {
                printSyntaxError(token.toString()
                        + " is not a valid Token.DESIGNATOR, which is required to be followed by designator"
                        + " in assignment!");
            }
        } else
            printSyntaxError(token.toString()
                    + " is not a valid Token.LET, which is required to be the first token in assignment!");
    }

    private void funcCall() {
        if (token == Token.CALL) {
            next();
            if (token == Token.IDENTIRIER) {
                next();
                if (token == Token.BEGIN_PARENTHESIS) {
                    next();
                    if (token == Token.IDENTIRIER) { // expression -> term -> factor -> designator -> identifier
                        expression();
                        while (token == Token.COMMA) {
                            next();
                            expression();
                        }
                    }
                    if (token == Token.END_PARENTHESIS) {
                        next();
                    } else
                        printSyntaxError(token.toString()
                                + " is not a valid ), which is required to be followed by ( in funcCall!");
                }
            } else
                printSyntaxError(token.toString()
                        + " is not a valid Token.IDENTIRIER, which is required to be the first token in funcCall!");
        }
    }

    private void ifStatement() {
        if (token == Token.IF) {
            next();
            relation();
            if (token == Token.THEN) {
                next();
                statSequence();
                if (token == Token.ELSE) {
                    next();
                    statSequence();
                }
                if (token == Token.FI) {
                    next();
                } else
                    printSyntaxError(token.toString()
                            + " is not a valid Token.FI, which is required to be the last token in ifStatement!");
            } else
                printSyntaxError(token.toString()
                        + " is not a valid Token.THEN, which is required to be followed by if in ifStatement!");
        } else
            printSyntaxError(token.toString()
                    + " is not a valid Token.IF, which is required to be the first token in ifStatement!");
    }

    private void whileStatement() {
        if (token == Token.WHILE) {
            next();
            relation();
            if (token == Token.DO) {
                next();
                statSequence();
                if (token == Token.OD) {
                    next();
                } else
                    printSyntaxError(token.toString()
                            + " is not a valid Token.OD, which is required to be the last token in whileStatement!");
            } else
                printSyntaxError(token.toString()
                        + " is not a valid Token.DO, which is required to be followed by while in whileStatement!");
        } else
            printSyntaxError(token.toString()
                    + " is not a valid Token.WHILE, which is required to be the first token in whileStatement!");
    }

    private void returnStatement() {
        if (token == Token.RETURN) {
            next();
            if (token == Token.IDENTIRIER) { // expression -> term -> factor -> designator -> identifier
                expression();
            }
        } else
            printSyntaxError(token.toString()
                    + " is not a valid Token.RETURN, which is required to be the first token in returnStatement!");
    }

    private boolean isStatement() {
        return token == Token.LET || token == Token.CALL || token == Token.IF || token == Token.WHILE
                || token == Token.RETURN;
    }

    private void statement() {
        if (token == Token.LET) { // assignment
            assignment();
        } else if (token == Token.CALL) { // funcCall
            funcCall();
        } else if (token == Token.IF) { // ifStatement
            ifStatement();
        } else if (token == Token.WHILE) { // whileStatement
            whileStatement();
        } else if (token == Token.RETURN) { // returnStatement
            returnStatement();
        } else
            printSyntaxError(token.toString()
                    + " is not a valid statement token, which is required to be the first token in statement!");
    }

    private void statSequence() {
        statement();
        while (token == Token.SEMICOMA) {
            next();
            statement();
        }
    }

    private void typeDecl() {
        if (token == Token.VAR) {
            // TODO
        } else if (token == Token.ARRAY) {
            boolean delDimension = false;
            while (token == Token.BEGIN_BRACKET) {
                delDimension = true;
                next();
                if (token == Token.NUMBER) {
                    // TODO return val
                } else
                    printSyntaxError(token.toString()
                            + " is not a valid number, which is required for an array in typeDel!");
                if (token == Token.END_BRACKET) {
                    next();
                } else
                    printSyntaxError(token.toString()
                            + " is not a valid ], which is required to be followed by [ for an array in typeDel!");
            }
            if (!delDimension)
                printSyntaxError("no dimension information in array declare!");
        } else
            printSyntaxError(token.toString()
                    + " is not a valid var or array, which is required to be the first token in typeDel!");
    }

    private void varDecl() {
        typeDecl();
        if (token == Token.IDENTIRIER) {
            next();
            while (token == Token.COMMA) {
                next();
                if (token == Token.IDENTIRIER)
                    next();
                else
                    printSyntaxError(token.toString()
                            + " is not a valid Token.IDENTIRIER, which is required to be followed by , in varDel!");
            }
            if (token == Token.SEMICOMA) {
                next();
            } else
                printSyntaxError(token.toString()
                        + " is not a valid Token.SEMICOMA, which is required to be the last token in varDel!");
        } else
            printSyntaxError(token.toString()
                    + " is not a valid Token.IDENTIRIER, which is required to be followed by typeDel in varDel!");
    }

    private void funcDecl() {
        if (token == Token.FUNCTION || token == Token.PROCEDURE) {
            next();
            if (token == Token.IDENTIRIER) {
                next();
                if (token == Token.BEGIN_BRACE) { // formalParam
                    formalParam();
                }
                if (token == Token.SEMICOMA) {
                    next();
                    funcBody();
                    if (token == Token.SEMICOMA) {
                        next();
                    } else
                        printSyntaxError(token.toString()
                                + " is not a valid ;, which is required to be followed by funcBody in funcDecl!");
                } else
                    printSyntaxError(token.toString()
                            + " is not a valid ;, which is required to be ahead of funcBody in funcDecl!");
            } else
                printSyntaxError(token.toString()
                        + " is not a valid Token.IDENTIRIER, which is required to be followed by function or "
                        + "procedure in funcDel!");
        } else
            printSyntaxError(token.toString()
                    + " is not a valid Token.FUNCTION or Token.PROCEDURE, which is required to be the first token "
                    + "in funcDel!");
    }

    private void formalParam() {
        if (token == Token.BEGIN_PARENTHESIS) {
            if (token == Token.IDENTIRIER) {
                next();
                while (token == Token.COMMA) {
                    next();
                    if (token == Token.IDENTIRIER) {
                        next();
                    } else
                        printSyntaxError(token.toString()
                                + " is not a valid Token.IDENTIRIER, which is required to be followed by , as non-first"
                                + " parameter in formalParam!");
                }
            }
            if (token == Token.BEGIN_PARENTHESIS) {
                next();
            } else
                printSyntaxError(token.toString()
                        + " is not a valid ), which is required to be the last token in formalParam!");
        } else
            printSyntaxError(token.toString()
                    + " is not a valid (, which is required to be the first token in formalParam!");
    }

    private void funcBody() {
        while(token == Token.VAR || token == Token.ARRAY){
            varDecl();
        }
        if(token == Token.BEGIN_BRACE){
            if(isStatement()){ // statSequence -> statement
                statSequence();
            }
            if(token == Token.END_BRACE){
                next();
            } else
                printSyntaxError(token.toString()
                        + " is not a valid }, which is required to be the last token in funcBody!");
        } else
            printSyntaxError(token.toString()
                    + " is not a valid {, which is required to be the first token followed by main in funcBody!");
    }

    private void printSyntaxError(String errMsg) {
        scanner.printSyntaxError(errMsg);
    }
}
