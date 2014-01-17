package dragon.compiler.parser;

import java.io.IOException;

import dragon.compiler.data.SyntaxFormatException;
import dragon.compiler.scanner.Scanner;
import dragon.compiler.scanner.Token;

public class Parser {

    private Scanner scanner;
    private Token token;

    public Parser(Scanner scanner) throws IOException{
        this.scanner = scanner;
        this.token = next();
    }

    public Token next() throws IOException{
        return scanner.getNextToken();
    }
    
    public void parse() throws IOException, SyntaxFormatException{
        computation();
    }
    
    private void computation() throws IOException, SyntaxFormatException{
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
                        throwFormatException(". expected in computation");
                } else
                    throwFormatException("} expected in computation");
            } else
                throwFormatException("{ expected in computation");
        } else
            throwFormatException("main expected in computation");
    }

    private void designator() throws IOException, SyntaxFormatException{
        if (token == Token.IDENTIRIER) {
            next();
            while (token == Token.BEGIN_BRACKET) {
                next();
                expression();
                if (token == Token.END_BRACKET)
                    next();
                else
                    throwFormatException("[ expected after ]");
            }
        } else {
            throwFormatException("identifier expected in designator");
        }
    }

    private void factor() throws IOException, SyntaxFormatException{
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
                throwFormatException("( expected after )");
        } else if (token == Token.CALL) {
            funcCall();
        } else
            throwFormatException("not a valid factor!");
    }

    private void term() throws IOException, SyntaxFormatException{
        factor();
        while (token == Token.TIMES || token == Token.DIVIDE) {
            next();
            factor();
        }
    }

    private void expression() throws IOException, SyntaxFormatException{
        term();
        while (token == Token.PLUS || token == Token.MINUS) {
            next();
            term();
        }
    }

    private boolean isRelOp() throws IOException, SyntaxFormatException{
        return token == Token.EQL || token == Token.NEQ || token == Token.LSS || token == Token.LEQ
                || token == Token.GRE || token == Token.GEQ;
    }

    private void relation() throws IOException, SyntaxFormatException{
        expression();
        if (isRelOp()) {
            next();
            expression();
        } else
            throwFormatException("relOp expected in relation");
    }

    private void assignment() throws IOException, SyntaxFormatException{
        if (token == Token.LET) {
            next();
            designator();
            if (token == Token.BECOMETO) {
                next();
                expression();
            } else {
                throwFormatException("<- expected in assignment");
            }
        } else
            throwFormatException("let expected in assignment");
    }

    private void funcCall() throws IOException, SyntaxFormatException{
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
                        throwFormatException(") expected in funcCall");
                }
            } else
                throwFormatException("identifier expected in funCall");
        }
    }

    private void ifStatement() throws IOException, SyntaxFormatException{
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
                    throwFormatException("fi expected in ifStatement");
            } else
                throwFormatException("then expected in ifStatement");
        } else
            throwFormatException("if expected in ifStatement");
    }

    private void whileStatement() throws IOException, SyntaxFormatException{
        if (token == Token.WHILE) {
            next();
            relation();
            if (token == Token.DO) {
                next();
                statSequence();
                if (token == Token.OD) {
                    next();
                } else
                    throwFormatException("od expected in whileStatement");
            } else
                throwFormatException("do expected in whileStatement");
        } else
            throwFormatException("while expected in whileStatement");
    }

    private void returnStatement() throws IOException, SyntaxFormatException{
        if (token == Token.RETURN) {
            next();
            if (token == Token.IDENTIRIER) { // expression -> term -> factor -> designator -> identifier
                expression();
            }
        } else
            throwFormatException("return expected in returnStatement");
    }

    private boolean isStatement() {
        return token == Token.LET || token == Token.CALL || token == Token.IF || token == Token.WHILE
                || token == Token.RETURN;
    }

    private void statement() throws IOException, SyntaxFormatException{
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
            throwFormatException("not a valid statement");
    }

    private void statSequence() throws IOException, SyntaxFormatException{
        statement();
        while (token == Token.SEMICOMA) {
            next();
            statement();
        }
    }

    private void typeDecl() throws IOException, SyntaxFormatException{
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
                    throwFormatException("number expected in array declare");
                if (token == Token.END_BRACKET) {
                    next();
                } else
                    throwFormatException("] is expected in array declare");
            }
            if (!delDimension)
                throwFormatException("no dimension information in array declare!");
        } else
            throwFormatException("not a valid typeDecl");
    }

    private void varDecl() throws IOException, SyntaxFormatException{
        typeDecl();
        if (token == Token.IDENTIRIER) {
            next();
            while (token == Token.COMMA) {
                next();
                if (token == Token.IDENTIRIER)
                    next();
                else
                    throwFormatException("identifier expeced in varDecl");
            }
            if (token == Token.SEMICOMA) {
                next();
            } else
                throwFormatException("; expected in varDecl");
        } else
            throwFormatException("not a valid varDecl");
    }

    private void funcDecl() throws IOException, SyntaxFormatException{
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
                        throwFormatException("; expeced after funcBody in funcDecl");
                } else
                    throwFormatException("; expeced after identifier in funcDecl");
            } else
                throwFormatException("identifier expected after function/procedure in funcDecl");
        } else
            throwFormatException("not a valid funcDecl");
    }

    private void formalParam() throws IOException, SyntaxFormatException{
        if (token == Token.BEGIN_PARENTHESIS) {
            if (token == Token.IDENTIRIER) {
                next();
                while (token == Token.COMMA) {
                    next();
                    if (token == Token.IDENTIRIER) {
                        next();
                    } else
                        throwFormatException("identifier expeced after , in formalParam");
                }
            }
            if (token == Token.END_PARENTHESIS) {
                next();
            } else
                throwFormatException(") expected in formalParam");
        } else
            throwFormatException("not a valid formalParam");
    }
 
    private void funcBody() throws IOException, SyntaxFormatException {
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
                throwFormatException("} expected after { in funcBody");
        } else
            throwFormatException("{ expected in funcBody");
    }
    
    private void throwFormatException(String string)
            throws SyntaxFormatException {
        string = "Parser error: Line " + scanner.getLineNumber() + ": " + string;
        throw new SyntaxFormatException(string);
    }
}
