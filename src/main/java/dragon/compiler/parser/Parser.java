package dragon.compiler.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dragon.compiler.data.BasicBlock;
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
                varDecl(cfg.getFirstBlock(), null); // pass null as function because it is main
            }
            while (token == Token.FUNCTION || token == Token.PROCEDURE) {
                funcDecl();
            }
            if (token == Token.BEGIN_BRACE) {
                moveToNextToken();
                BasicBlock lastBlock = statSequence(cfg.getFirstBlock(), null);
                if (token == Token.END_BRACE) {
                    moveToNextToken();
                    if (token == Token.PERIOD) {
                        moveToNextToken();
                        lastBlock.generateIntermediateCode(Instruction.end, null, null);
                    } else
                        throwFormatException(". expected in computation");
                } else
                    throwFormatException("} expected in computation");
            } else
                throwFormatException("{ expected in computation");
        } else
            throwFormatException("main expected in computation");
    }

    private Result designator(BasicBlock curBlock) throws IOException, SyntaxFormatException {
        Result x = new Result();
        List<Result> dimensions = new ArrayList<Result>();
        if (token == Token.IDENTIFIER) {
        	/** use def-use chain to find last version of x **/
            x.set(Result.Type.var, scanner.id); // set variable version to instructionId 
            moveToNextToken();
            while (token == Token.BEGIN_BRACKET) {
                moveToNextToken();
                dimensions.add(expression(curBlock));
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

    private Result factor(BasicBlock curBlock) throws IOException, SyntaxFormatException {
        Result x = new Result();
        if (token == Token.IDENTIFIER) {
            x = designator(curBlock);
            x.ssa = VariableManager.getLastVersionSSA(x.address);
        } else if (token == Token.NUMBER) {
            x.set(Result.Type.constant, scanner.val);
            moveToNextToken();
        } else if (token == Token.BEGIN_PARENTHESIS) {
            moveToNextToken();
            x = expression(curBlock);
            if (token == Token.END_PARENTHESIS)
                moveToNextToken();
            else
                throwFormatException("( expected after )");
        } else if (token == Token.CALL) {
            funcCall(curBlock);
        } else
            throwFormatException("not a valid factor!");

        return x;
    }

    private Result term(BasicBlock curBlock) throws IOException, SyntaxFormatException {
        Result x = factor(curBlock);
        while (token == Token.TIMES || token == Token.DIVIDE) {
            Token op = token;
            moveToNextToken();
            icGen.computeArithmeticOp(curBlock, op, x, factor(curBlock));
        }

        return x;
    }

    private Result expression(BasicBlock curBlock) throws IOException, SyntaxFormatException {
        Result x = term(curBlock);
        while (token == Token.PLUS || token == Token.MINUS) {
            Token op = token;
            moveToNextToken();
            icGen.computeArithmeticOp(curBlock, op, x, term(curBlock)); // x = x +/- y
        }

        return x;
    }

    private boolean isRelOp() throws IOException, SyntaxFormatException {
        return token == Token.EQL || token == Token.NEQ || token == Token.LSS || token == Token.LEQ
                || token == Token.GRE || token == Token.GEQ;
    }

    private Result relation(BasicBlock curBlock) throws IOException, SyntaxFormatException {
        Result condition = null;
        Result left = expression(curBlock);
        if (isRelOp()) {
            Token op = token;
            moveToNextToken();
            Result right = expression(curBlock);
            icGen.computeCmpOp(curBlock, Instruction.cmp, left, right);
            condition = new Result();
            condition.kind = Result.Type.condition;
            condition.cc = op;
            condition.fixuplocation = 0;
        } else
            throwFormatException("relOp expected in relation");

        return condition;
    }

    private void assignment(BasicBlock curBlock, BasicBlock joinBlock) throws Throwable {
        if (token == Token.LET) {
            moveToNextToken();
            Result variable = designator(curBlock);
            // create phi func in joinBlock if it exists
            if(joinBlock != null){
            	joinBlock.createPhiFunction(variable.address);
            }
            if (token == Token.BECOMETO) {
                moveToNextToken();
                Result assignedValue = expression(curBlock);
                icGen.assign(curBlock, variable, assignedValue);
            } else {
                throwFormatException("<- expected in assignment");
            }
        } else
            throwFormatException("let expected in assignment");
    }

    private void funcCall(BasicBlock curBlock) throws IOException, SyntaxFormatException {
        Result x = new Result();
        if (token == Token.CALL) {
            moveToNextToken();
            if (token == Token.IDENTIFIER) {
                x.set(Result.Type.var, scanner.id); // x is a func ident in funcCall
                moveToNextToken();
                if (token == Token.BEGIN_PARENTHESIS) {
                    moveToNextToken();
                    if (isExpression()) { // expression -> term -> factor -> designator -> identifier
                        expression(curBlock);
                        while (token == Token.COMMA) {
                            moveToNextToken();
                            expression(curBlock);
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

    
    private void linkJoinBlock(BasicBlock curBlock, BasicBlock ifLastBlock, BasicBlock elseLastBlock, BasicBlock joinBlock){
        ifLastBlock.setJoinSuccessor(joinBlock);
        
        if(elseLastBlock != null){
            elseLastBlock.setJoinSuccessor(joinBlock);
        } else {
            curBlock.setElseSuccessor(joinBlock);
        }
    }
    
    private BasicBlock ifStatement(BasicBlock curBlock) throws Throwable {
        if (token == Token.IF) {
            moveToNextToken();
            Result follow = new Result();
            follow.fixuplocation = 0;
            Result x = relation(curBlock);
            
            BasicBlock joinBlock = new BasicBlock();
            curBlock.setJoinSuccessor(joinBlock);
            
            icGen.condNegBraFwd(curBlock, x);
            
            BasicBlock ifLastBlock = null;
            BasicBlock elseLastBlock = null;
            
            if (token == Token.THEN) {
                moveToNextToken();
                ifLastBlock = statSequence(curBlock.makeDirectSuccessor(), joinBlock);
                if (token == Token.ELSE) {
                    moveToNextToken();
                    icGen.unCondBraFwd(ifLastBlock, follow);
                    BasicBlock elseBlock = curBlock.makeElseSuccessor();
                    icGen.fixup(x.fixuplocation, elseBlock); // set target of NegBraFwd
                    elseLastBlock = statSequence(elseBlock, joinBlock);
                } else {
                    icGen.fixup(x.fixuplocation, joinBlock);
                }
                if (token == Token.FI) {
                    moveToNextToken();
                    icGen.fixAll(follow.fixuplocation, joinBlock);  // set follow as target for all if and elseif
                    linkJoinBlock(curBlock, ifLastBlock, elseLastBlock, joinBlock);
                    
                    return joinBlock;
                } else
                    throwFormatException("fi expected in ifStatement");
            } else
                throwFormatException("then expected in ifStatement");
        } else
            throwFormatException("if expected in ifStatement");
        throw new Exception("Programmer error in ifStatement!");
    }

    private BasicBlock whileStatement(BasicBlock curBlock) throws Throwable {
        if (token == Token.WHILE) {
            moveToNextToken();
            
            BasicBlock innerJoinBlock = curBlock.makeDirectSuccessor();
            curBlock = innerJoinBlock;
            
            Result x = relation(curBlock);
            icGen.condNegBraFwd(curBlock, x);
            
            BasicBlock doLastBlock = null;
            if (token == Token.DO) {
                moveToNextToken();
                doLastBlock = statSequence(curBlock.makeDirectSuccessor(), innerJoinBlock);
                doLastBlock.generateIntermediateCode(Instruction.bra, null, Result.makeBranch(curBlock));
                
                BasicBlock followBlock = curBlock.makeElseSuccessor();
                icGen.fixup(x.fixuplocation, followBlock);
                
                //link loop block back to condition
                doLastBlock.setBackSuccessor(curBlock);
//                curBlock.setDirectPredecessor(doLastBlock);
                
                if (token == Token.OD) {
                    moveToNextToken();
                    return followBlock;
                } else
                    throwFormatException("od expected in whileStatement");
            } else
                throwFormatException("do expected in whileStatement");
        } else
            throwFormatException("while expected in whileStatement");
        throw new Exception("Programmer error!");
    }

    private void returnStatement(BasicBlock curBlock) throws IOException, SyntaxFormatException {
        if (token == Token.RETURN) {
            moveToNextToken();
            if (isExpression()) { // expression -> term -> factor -> designator -> identifier
                expression(curBlock);
            }
        } else
            throwFormatException("return expected in returnStatement");
    }

    private boolean isStatement() {
        return token == Token.LET || token == Token.CALL || token == Token.IF || token == Token.WHILE
                || token == Token.RETURN;
    }

    private BasicBlock statement(BasicBlock curBlock, BasicBlock joinBlock) throws Throwable {
        if (token == Token.LET) { // assignment
            assignment(curBlock, joinBlock);
            return curBlock;
        } else if (token == Token.CALL) { // funcCall
//            funcCall(curBlock);
            return null;
        } else if (token == Token.IF) { // ifStatement
            return ifStatement(curBlock);
        } else if (token == Token.WHILE) { // whileStatement
            return whileStatement(curBlock);
        } else if (token == Token.RETURN) { // returnStatement
            returnStatement(curBlock);
            return curBlock;
        } else
            throwFormatException("not a valid statement");
        throw new Exception("Programmer error in statement!");
    }

    private BasicBlock statSequence(BasicBlock curBlock, BasicBlock joinBlock) throws Throwable {
        BasicBlock lastBlock = statement(curBlock, joinBlock);
        while (token == Token.SEMICOMA) {
            moveToNextToken();
            lastBlock = statement(lastBlock, joinBlock);
        }
        return lastBlock;
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

    private void varDecl(BasicBlock curBlock, Function function) throws Throwable {
        Result x = null;
        typeDecl();
        if (token == Token.IDENTIFIER) {
            x = new Result();
            x.set(Result.Type.var, scanner.id); // x is varName in varDecl
            /** declare variable **/
            icGen.declareVariable(curBlock, x, function);
            moveToNextToken();
            while (token == Token.COMMA) {
                moveToNextToken();
                if (token == Token.IDENTIFIER){ // multiple varNames
                    x = new Result();
                    x.set(Result.Type.var, scanner.id); // x is varName in varDecl
                    /** declare variable **/
                    icGen.declareVariable(curBlock, x, function);
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
//                icGen.declareVariable(x, function);
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
            varDecl(function.getFuncBlock(), function);
        }
        if (token == Token.BEGIN_BRACE) {
            moveToNextToken();
            if (isStatement()) { // statSequence -> statement
//                statSequence();
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
        String testprog = "if";
        Parser ps = new Parser("src/test/resources/testprogs/self/" + testprog + ".txt");
        ps.parse();
        ps.cfg.printIntermediateCode();
        VCGPrinter printer = new VCGPrinter(testprog);
        printer.printCFG();
    }
}
