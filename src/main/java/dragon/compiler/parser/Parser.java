package dragon.compiler.parser;

import java.io.IOException;

import dragon.compiler.data.SyntaxFormatException;
import dragon.compiler.data.Token;
import dragon.compiler.scanner.Scanner;

public class Parser {

    private Scanner scanner;
    private Token token;

    public Parser(String path) throws IOException{
        this.scanner = new Scanner(path);
    }

    public void moveToNextToken() throws IOException{
        token = scanner.getNextToken();
    }
    
    public void parse() throws IOException, SyntaxFormatException{
        scanner.open();
        moveToNextToken();
        computation();
        scanner.close();
    }
    
    private void computation() throws IOException, SyntaxFormatException{
        if (token == Token.MAIN) {
            moveToNextToken();
            while (token == Token.VAR || token == Token.ARRAY) {
                varDecl();
            }
            while (token == Token.FUNCTION || token == Token.PROCEDURE) {
                funcDecl();
            }
            if (token == Token.BEGIN_BRACE) {
                moveToNextToken();
                statSequence();
                if (token == Token.END_BRACE) {
                    moveToNextToken();
                    if (token == Token.PERIOD) {
                        moveToNextToken();
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
        if (token == Token.IDENTIFIER) {
            moveToNextToken();
            while (token == Token.BEGIN_BRACKET) {
                moveToNextToken();
                expression();
                if (token == Token.END_BRACKET)
                    moveToNextToken();
                else
                    throwFormatException("[ expected after ]");
            }
        } else {
            throwFormatException("identifier expected in designator");
        }
    }
    
    private boolean isExpression(){
        return token == Token.IDENTIFIER || token == Token.NUMBER || token == Token.BEGIN_PARENTHESIS
                || token == Token.CALL;
    }
    
    private void factor() throws IOException, SyntaxFormatException{
        if (token == Token.IDENTIFIER) {
            designator();
        } else if (token == Token.NUMBER) {
            // TODO return a val here
            moveToNextToken();
        } else if (token == Token.BEGIN_PARENTHESIS) {
            moveToNextToken();
            expression();
            if (token == Token.END_PARENTHESIS)
                moveToNextToken();
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
            moveToNextToken();
            factor();
        }
    }

    private void expression() throws IOException, SyntaxFormatException{
        term();
        while (token == Token.PLUS || token == Token.MINUS) {
            moveToNextToken();
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
            moveToNextToken();
            expression();
        } else
            throwFormatException("relOp expected in relation");
    }

    private void assignment() throws IOException, SyntaxFormatException{
        if (token == Token.LET) {
            moveToNextToken();
            designator();
            if (token == Token.BECOMETO) {
                moveToNextToken();
                expression();
            } else {
                throwFormatException("<- expected in assignment");
            }
        } else
            throwFormatException("let expected in assignment");
    }

    private void funcCall() throws IOException, SyntaxFormatException{
        if (token == Token.CALL) {
            moveToNextToken();
            if (token == Token.IDENTIFIER) {
                moveToNextToken();
                if (token == Token.BEGIN_PARENTHESIS) {
                    moveToNextToken();
                    if (isExpression()) { // expression -> term -> factor -> designator -> identifier
                        expression();
                        while (token == Token.COMMA) {
                            moveToNextToken();
                            expression();
                        }
                    }
                    if (token == Token.END_PARENTHESIS) {
                        moveToNextToken();
                    } else
                        throwFormatException(") expected in funcCall");
                }
            } else
                throwFormatException("identifier expected in funCall");
        }
    }

    private void ifStatement() throws IOException, SyntaxFormatException{
        if (token == Token.IF) {
            moveToNextToken();
            relation();
            if (token == Token.THEN) {
                moveToNextToken();
                statSequence();
                if (token == Token.ELSE) {
                    moveToNextToken();
                    statSequence();
                }
                if (token == Token.FI) {
                    moveToNextToken();
                } else
                    throwFormatException("fi expected in ifStatement");
            } else
                throwFormatException("then expected in ifStatement");
        } else
            throwFormatException("if expected in ifStatement");
    }

    private void whileStatement() throws IOException, SyntaxFormatException{
        if (token == Token.WHILE) {
            moveToNextToken();
            relation();
            if (token == Token.DO) {
                moveToNextToken();
                statSequence();
                if (token == Token.OD) {
                    moveToNextToken();
                } else
                    throwFormatException("od expected in whileStatement");
            } else
                throwFormatException("do expected in whileStatement");
        } else
            throwFormatException("while expected in whileStatement");
    }

    private void returnStatement() throws IOException, SyntaxFormatException{
        if (token == Token.RETURN) {
            moveToNextToken();
            if (isExpression()) { // expression -> term -> factor -> designator -> identifier
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
            moveToNextToken();
            statement();
        }
    }

    private void typeDecl() throws IOException, SyntaxFormatException{
        if (token == Token.VAR) {
            // TODO
            moveToNextToken();
        } else if (token == Token.ARRAY) {
            moveToNextToken();
            boolean delDimension = false;
            while (token == Token.BEGIN_BRACKET) {
                delDimension = true;
                moveToNextToken();
                if (token == Token.NUMBER) {
                    // TODO return val
                    moveToNextToken();
                } else
                    throwFormatException("number expected in array declare");
                if (token == Token.END_BRACKET) {
                    moveToNextToken();
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
        if (token == Token.IDENTIFIER) {
            moveToNextToken();
            while (token == Token.COMMA) {
                moveToNextToken();
                if (token == Token.IDENTIFIER)
                    moveToNextToken();
                else
                    throwFormatException("identifier expeced in varDecl");
            }
            if (token == Token.SEMICOMA) {
                moveToNextToken();
            } else
                throwFormatException("; expected in varDecl");
        } else
            throwFormatException("not a valid varDecl");
    }

    private void funcDecl() throws IOException, SyntaxFormatException{
        if (token == Token.FUNCTION || token == Token.PROCEDURE) {
            moveToNextToken();
            if (token == Token.IDENTIFIER) {
                moveToNextToken();
                if (token == Token.BEGIN_PARENTHESIS) { // formalParam
                    formalParam();
                }
                if (token == Token.SEMICOMA) {
                    moveToNextToken();
                    funcBody();
                    if (token == Token.SEMICOMA) {
                        moveToNextToken();
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
            moveToNextToken();
            if (token == Token.IDENTIFIER) {
                moveToNextToken();
                while (token == Token.COMMA) {
                    moveToNextToken();
                    if (token == Token.IDENTIFIER) {
                        moveToNextToken();
                    } else
                        throwFormatException("identifier expeced after , in formalParam");
                }
            }
            if (token == Token.END_PARENTHESIS) {
                moveToNextToken();
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
            moveToNextToken();
            if(isStatement()){ // statSequence -> statement
                statSequence();
            }
            if(token == Token.END_BRACE){
                moveToNextToken();
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
    
    public static void main(String[] args) throws Exception {
        Parser ps = new Parser("src/test/resources/testprogs/test014.txt");
        ps.parse();
    }
}
