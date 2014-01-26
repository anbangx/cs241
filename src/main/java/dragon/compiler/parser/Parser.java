package dragon.compiler.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dragon.compiler.data.ControlFlowGraph;
import dragon.compiler.data.Function;
import dragon.compiler.data.Instruction;
import dragon.compiler.data.Result;
import dragon.compiler.data.SyntaxFormatException;
import dragon.compiler.data.Token;
import dragon.compiler.scanner.Scanner;

public class Parser {

    private Scanner scanner;
    private Token token;

    private IntermediateCodeGenerator icGen;
    private ControlFlowGraph cfg;

    public Parser(String path) throws IOException {
        this.scanner = new Scanner(path);
        this.icGen = new IntermediateCodeGenerator();
        this.cfg = new ControlFlowGraph();
    }

    public void moveToNextToken() throws IOException {
        token = scanner.getNextToken();
    }

    public void parse() throws Throwable {
        scanner.open();
        moveToNextToken();
        computation();
        scanner.close();
    }

    private void computation() throws Throwable {
        if (token == Token.MAIN) {
            moveToNextToken();
            while (token == Token.VAR || token == Token.ARRAY) {
                varDecl(null); // pass null as function because it is main
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

    private Result designator() throws IOException, SyntaxFormatException {
        Result x = new Result();
        List<Result> dimensions = new ArrayList<Result>();
        if (token == Token.IDENTIFIER) {
            x.set(Result.Type.var, scanner.id); x.setSSA(Instruction.getPC());
            moveToNextToken();
            while (token == Token.BEGIN_BRACKET) {
                moveToNextToken();
                dimensions.add(expression());
                if (token == Token.END_BRACKET)
                    moveToNextToken();
                else
                    throwFormatException("[ expected after ]");
            }
        } else {
            throwFormatException("identifier expected in designator");
        }
        if (dimensions.isEmpty())
            return x;
        else
            return x; // TODO should be an array
    }

    private boolean isExpression() {
        return token == Token.IDENTIFIER || token == Token.NUMBER || token == Token.BEGIN_PARENTHESIS
                || token == Token.CALL;
    }

    private Result factor() throws IOException, SyntaxFormatException {
        Result x = new Result();
        if (token == Token.IDENTIFIER) {
            x = designator();
        } else if (token == Token.NUMBER) {
            x.set(Result.Type.constant, scanner.val);
            moveToNextToken();
        } else if (token == Token.BEGIN_PARENTHESIS) {
            moveToNextToken();
            x = expression();
            if (token == Token.END_PARENTHESIS)
                moveToNextToken();
            else
                throwFormatException("( expected after )");
        } else if (token == Token.CALL) {
            funcCall();
        } else
            throwFormatException("not a valid factor!");

        return x;
    }

    private Result term() throws IOException, SyntaxFormatException {
        Result x = factor();
        while (token == Token.TIMES || token == Token.DIVIDE) {
            Token op = token;
            moveToNextToken();
            icGen.computeArithmeticOp(op, x, factor());
        }

        return x;
    }

    private Result expression() throws IOException, SyntaxFormatException {
        Result x = term();
        while (token == Token.PLUS || token == Token.MINUS) {
            Token op = token;
            moveToNextToken();
            icGen.computeArithmeticOp(op, x, term()); // x = x +/- y
        }

        return x;
    }

    private boolean isRelOp() throws IOException, SyntaxFormatException {
        return token == Token.EQL || token == Token.NEQ || token == Token.LSS || token == Token.LEQ
                || token == Token.GRE || token == Token.GEQ;
    }

    private Result relation() throws IOException, SyntaxFormatException {
        Result condition = null;
        Result left = expression();
        if (isRelOp()) {
            Token op = token;
            moveToNextToken();
            Result right = expression();
            icGen.computeCmpOp(Instruction.cmp, left, right);
            condition = new Result();
            condition.kind = Result.Type.condition;
            condition.cc = op;
            condition.fixuplocation = 0;
        } else
            throwFormatException("relOp expected in relation");

        return condition;
    }

    private void assignment() throws Throwable {
        if (token == Token.LET) {
            moveToNextToken();
            Result variable = designator();
            if (token == Token.BECOMETO) {
                moveToNextToken();
                Result assignedValue = expression();
                icGen.assign(variable, assignedValue);
            } else {
                throwFormatException("<- expected in assignment");
            }
        } else
            throwFormatException("let expected in assignment");
    }

    private void funcCall() throws IOException, SyntaxFormatException {
        Result x = new Result();
        if (token == Token.CALL) {
            moveToNextToken();
            if (token == Token.IDENTIFIER) {
                x.set(Result.Type.var, scanner.id); // x is a func ident in funcCall
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

    private void ifStatement() throws Throwable {
        if (token == Token.IF) {
            moveToNextToken();
            Result follow = new Result();
            follow.fixuplocation = 0;
            Result x = relation();
            icGen.condNegBraFwd(x);
            if (token == Token.THEN) {
                moveToNextToken();
                statSequence();
                if (token == Token.ELSE) {
                    moveToNextToken();
                    icGen.unCondBraFwd(follow);
                    icGen.fixup(x.fixuplocation);
                    statSequence();
                } else {
                    icGen.fixup(x.fixuplocation);
                }
                if (token == Token.FI) {
                    moveToNextToken();
                    icGen.fixAll(follow.fixuplocation);
                } else
                    throwFormatException("fi expected in ifStatement");
            } else
                throwFormatException("then expected in ifStatement");
        } else
            throwFormatException("if expected in ifStatement");
    }

    private void whileStatement() throws Throwable {
        if (token == Token.WHILE) {
            moveToNextToken();
            int loopLocation = Instruction.getPC();
            Result x = relation();
            icGen.condNegBraFwd(x);
            if (token == Token.DO) {
                moveToNextToken();
                statSequence();
                icGen.block.generateIntermediateCode(Instruction.bra, null, Result.makeBranch(loopLocation));
                icGen.fixup(x.fixuplocation);
                if (token == Token.OD) {
                    moveToNextToken();
                } else
                    throwFormatException("od expected in whileStatement");
            } else
                throwFormatException("do expected in whileStatement");
        } else
            throwFormatException("while expected in whileStatement");
    }

    private void returnStatement() throws IOException, SyntaxFormatException {
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

    private void statement() throws Throwable {
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

    private void statSequence() throws Throwable {
        statement();
        while (token == Token.SEMICOMA) {
            moveToNextToken();
            statement();
        }
    }

    private void typeDecl() throws IOException, SyntaxFormatException {
        if (token == Token.VAR) {
            // TODO
            moveToNextToken();
        } else if (token == Token.ARRAY) {
            moveToNextToken();
            boolean delDimension = false;
            while (token == Token.BEGIN_BRACKET) {
                delDimension = true;
                moveToNextToken();
                Result x = new Result(); // x is a dimension in typeDecl
                if (token == Token.NUMBER) {
                    x.set(Result.Type.constant, scanner.val);
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

    private void varDecl(Function function) throws Throwable {
        Result x = null;
        typeDecl();
        if (token == Token.IDENTIFIER) {
            x = new Result();
            x.set(Result.Type.var, scanner.id); // x is varName in varDecl
            /** declare variable **/
            icGen.declareVariable(x, function);
            moveToNextToken();
            while (token == Token.COMMA) {
                moveToNextToken();
                if (token == Token.IDENTIFIER){ // multiple varNames
                    x = new Result();
                    x.set(Result.Type.var, scanner.id); // x is varName in varDecl
                    /** declare variable **/
                    icGen.declareVariable(x, function);
                    moveToNextToken();
                }
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

    private void funcDecl() throws Throwable {
        if (token == Token.FUNCTION || token == Token.PROCEDURE) {
            moveToNextToken();
            if (token == Token.IDENTIFIER) {
                Result x = new Result();
                x.set(Result.Type.var, scanner.id);
                /** declare function **/
                Function function = icGen.declareFunction(x);
                moveToNextToken();
                if (token == Token.BEGIN_PARENTHESIS) { // formalParam
                    formalParam(function);
                }
                if (token == Token.SEMICOMA) {
                    moveToNextToken();
                    funcBody(function);
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

    private Result formalParam(Function function) throws Throwable {
        Result x = new Result();
        if (token == Token.BEGIN_PARENTHESIS) {
            moveToNextToken();
            if (token == Token.IDENTIFIER) { // x is paramName in formalParam
                x.set(Result.Type.var, scanner.id);
                /** declare variable **/
                icGen.declareVariable(x, function);
                moveToNextToken();
                while (token == Token.COMMA) {
                    moveToNextToken();
                    if (token == Token.IDENTIFIER) { // multiple paramNames
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

        return x;
    }

    private void funcBody(Function function) throws Throwable {
        while (token == Token.VAR || token == Token.ARRAY) {
            varDecl(function);
        }
        if (token == Token.BEGIN_BRACE) {
            moveToNextToken();
            if (isStatement()) { // statSequence -> statement
                statSequence();
            }
            if (token == Token.END_BRACE) {
                moveToNextToken();
            } else
                throwFormatException("} expected after { in funcBody");
        } else
            throwFormatException("{ expected in funcBody");
    }

    private void throwFormatException(String string) throws SyntaxFormatException {
        string = "Parser error: Line " + scanner.getLineNumber() + ": " + string;
        throw new SyntaxFormatException(string);
    }

    public static void main(String[] args) throws Throwable {
        Parser ps = new Parser("src/test/resources/testprogs/self/if.txt");
        ps.parse();
        ps.icGen.printIntermediateCode();
    }
}
