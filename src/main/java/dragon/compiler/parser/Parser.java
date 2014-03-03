package dragon.compiler.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import dragon.compiler.data.BasicBlock;
import dragon.compiler.data.Function;
import dragon.compiler.data.Instruction;
import dragon.compiler.data.Result;
import dragon.compiler.data.SSA;
import dragon.compiler.data.SyntaxFormatException;
import dragon.compiler.data.Token;
import dragon.compiler.scanner.Scanner;

public class Parser {

    private Scanner scanner;
    private Token token;

    private IntermediateCodeGenerator icGen;

    public Parser(String path) throws IOException {
        this.scanner = new Scanner(path);
        this.icGen = new IntermediateCodeGenerator();
        new ControlFlowGraph();
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
                varDecl(ControlFlowGraph.getFirstBlock(), null); // pass null as
                                                                 // function
                                                                 // because
                                                                 // it is
                                                                 // main
            }
            while (token == Token.FUNCTION || token == Token.PROCEDURE) {
                funcDecl();
            }
            if (token == Token.BEGIN_BRACE) {
                moveToNextToken();
                BasicBlock lastBlock = statSequence(ControlFlowGraph.getFirstBlock(), null, null);
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

    private Result designator(BasicBlock curBlock, ArrayList<BasicBlock> joinBlocks) throws IOException,
            SyntaxFormatException {
        Result x = new Result();
        List<Result> dimensions = new ArrayList<Result>();
        if (token == Token.IDENTIFIER) {
            /** use def-use chain to find last version of x **/
            x.set(Result.Type.var, scanner.id); // set variable version to
                                                // instructionId
            moveToNextToken();
            while (token == Token.BEGIN_BRACKET) {
                moveToNextToken();
                dimensions.add(expression(curBlock, joinBlocks));
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

    private Result factor(BasicBlock curBlock, ArrayList<BasicBlock> joinBlocks) throws IOException,
            SyntaxFormatException {
        Result x = new Result();
        if (token == Token.IDENTIFIER) {
            x = designator(curBlock, joinBlocks);
            //			if (joinBlock != null) {
            //				joinBlock.createPhiFunction(x.address, curBlock);
            //				x.ssa = joinBlock.findLastVersionSSAFromJoinblock(x.address);
            //			} else
            x.ssa = VariableManager.getLastVersionSSA(x.address);
        } else if (token == Token.NUMBER) {
            x.set(Result.Type.constant, scanner.val);
            moveToNextToken();
        } else if (token == Token.BEGIN_PARENTHESIS) {
            moveToNextToken();
            x = expression(curBlock, joinBlocks);
            if (token == Token.END_PARENTHESIS)
                moveToNextToken();
            else
                throwFormatException("( expected after )");
        } else if (token == Token.CALL) {
            x = funcCall(curBlock, joinBlocks);
        } else
            throwFormatException("not a valid factor!");

        return x;
    }

    private Result term(BasicBlock curBlock, ArrayList<BasicBlock> joinBlocks) throws IOException,
            SyntaxFormatException {
        Result x = factor(curBlock, joinBlocks);
        while (token == Token.TIMES || token == Token.DIVIDE) {
            Token op = token;
            moveToNextToken();

            Result y = factor(curBlock, joinBlocks);
            x.onlyMove = false;
            x.instrId = Instruction.getPC();
            icGen.computeArithmeticOp(curBlock, op, x, y);

            // update def use chain
            ControlFlowGraph.updateDefUseChain(x, y);
        }

        return x;
    }

    private Result expression(BasicBlock curBlock, ArrayList<BasicBlock> joinBlocks) throws IOException,
            SyntaxFormatException {
        Result x = term(curBlock, joinBlocks);
        while (token == Token.PLUS || token == Token.MINUS) {
            Token op = token;
            moveToNextToken();

            Result y = term(curBlock, joinBlocks);
            x.onlyMove = false;
            x.instrId = Instruction.getPC();
            icGen.computeArithmeticOp(curBlock, op, x, y); // x = x +/- y

            // update def use chain
            ControlFlowGraph.updateDefUseChain(x, y);
        }
        if (!x.onlyMove)
            x.kind = Result.Type.instr;
        return x;
    }

    private boolean isRelOp() throws IOException, SyntaxFormatException {
        return token == Token.EQL || token == Token.NEQ || token == Token.LSS || token == Token.LEQ
                || token == Token.GRE || token == Token.GEQ;
    }

    private Result relation(BasicBlock curBlock, ArrayList<BasicBlock> joinBlocks) throws IOException,
            SyntaxFormatException {
        Result condition = null;
        Result left = expression(curBlock, joinBlocks);
        if (isRelOp()) {
            Token op = token;
            moveToNextToken();
            Result right = expression(curBlock, joinBlocks);
            icGen.computeCmpOp(curBlock, Instruction.cmp, left, right);
            condition = new Result();
            condition.kind = Result.Type.condition;
            condition.cc = op;
            condition.fixuplocation = 0;
        } else
            throwFormatException("relOp expected in relation");

        return condition;
    }

    private void assignment(BasicBlock curBlock, ArrayList<BasicBlock> joinBlocks) throws Throwable {
        if (token == Token.LET) {
            moveToNextToken();
            Result variable = designator(curBlock, joinBlocks);
            // create phi func in joinBlock if it exists
            if (joinBlocks != null && joinBlocks.size() > 0) {
                joinBlocks.get(joinBlocks.size() - 1).createPhiFunction(variable.address);
            }
            if (token == Token.BECOMETO) {
                moveToNextToken();
                // assignedValue should be a instrution#
                Result assignedValue = expression(curBlock, joinBlocks);
                if (joinBlocks != null)
                    icGen.assign(curBlock, joinBlocks.get(joinBlocks.size() - 1), variable, assignedValue);
                else
                    icGen.assign(curBlock, null, variable, assignedValue);
            } else {
                throwFormatException("<- expected in assignment");
            }
        } else
            throwFormatException("let expected in assignment");
    }

    private Result funcCall(BasicBlock curBlock, ArrayList<BasicBlock> joinBlocks) throws IOException,
            SyntaxFormatException {
        Result x = new Result();
        Function func = null;
        if (token == Token.CALL) {
            moveToNextToken();
            if (token == Token.IDENTIFIER) {
                x.set(Result.Type.var, scanner.id); // x is a func ident in
                                                    // funcCall
                // assign value to parameters
                func = ControlFlowGraph.existedFunctions.get(x.address);
                moveToNextToken();

                int index = 0;
                if (token == Token.BEGIN_PARENTHESIS) {
                    moveToNextToken();
                    Result y = new Result();
                    if (isExpression()) { // expression -> term -> factor ->
                                          // designator -> identifier
                        y = expression(curBlock, joinBlocks);
                        if (x.address >= 3)
                            icGen.generateAssignParameterOp(curBlock, y, func.getParameters().get(index++));

                        while (token == Token.COMMA) {
                            moveToNextToken();
                            y = expression(curBlock, joinBlocks);
                            if (x.address >= 3)
                                icGen.generateAssignParameterOp(curBlock, y, func.getParameters().get(index++));
                        }
                    }
                    if (token == Token.END_PARENTHESIS) {
                        moveToNextToken();
                    } else
                        throwFormatException(") expected in funcCall");
                    // make bra to func call
                    if (x.address < 3)
                        icGen.generateBasicIoOp(curBlock, x.address, y);
                    else {
                        Result branch = Result.makeBranch(ControlFlowGraph.existedFunctions.get(x.address)
                                .getFirstFuncBlock());
                        curBlock.generateIntermediateCode(Instruction.bra, null, branch);
                    }
                } else {
                    // make bra to func call
                    if (x.address < 3)
                        icGen.generateBasicIoOp(curBlock, x.address, null);
                    else {
                        Result branch = Result.makeBranch(ControlFlowGraph.existedFunctions.get(x.address)
                                .getFirstFuncBlock());
                        curBlock.generateIntermediateCode(Instruction.bra, null, branch);
                    }
                }
            } else
                throwFormatException("identifier expected in funCall");
        }
        return x.address < 3 ? null : func.getReturnInstr();
    }

    private void linkJoinBlock(BasicBlock curBlock, BasicBlock ifLastBlock, BasicBlock elseLastBlock,
            BasicBlock joinBlock) {
        ifLastBlock.setJoinSuccessor(joinBlock);

        if (elseLastBlock != null) {
            elseLastBlock.setJoinSuccessor(joinBlock);
        } else {
            curBlock.setElseSuccessor(joinBlock);
        }
    }

    private BasicBlock ifStatement(BasicBlock curBlock, ArrayList<BasicBlock> joinBlocks, Function function)
            throws Throwable {
        HashMap<Integer, ArrayList<SSA>> ssaMap = VariableManager.deepCopySSAMap();
        if (token == Token.IF) {
            moveToNextToken();
            Result follow = new Result();
            follow.fixuplocation = 0;
            Result x = relation(curBlock, null);

            BasicBlock joinBlock = new BasicBlock(BasicBlock.Type.IF_JOIN);
            curBlock.setJoinSuccessor(joinBlock);

            icGen.condNegBraFwd(curBlock, x);

            BasicBlock ifLastBlock = null;
            BasicBlock elseLastBlock = null;

            if (token == Token.THEN) {
                moveToNextToken();
                if (joinBlocks == null)
                    joinBlocks = new ArrayList<BasicBlock>();
                joinBlocks.add(joinBlock);
                ifLastBlock = statSequence(curBlock.makeIfSuccessor(), joinBlocks, function);
                joinBlocks.remove(joinBlocks.size() - 1);
                if (token == Token.ELSE) {
                    VariableManager.setSsaMap(ssaMap);
                    moveToNextToken();
                    icGen.unCondBraFwd(ifLastBlock, follow);
                    BasicBlock elseBlock = curBlock.makeElseSuccessor();
                    icGen.fixup(x.fixuplocation, elseBlock); // set target of
                                                             // NegBraFwd
                    joinBlocks.add(joinBlock);
                    elseLastBlock = statSequence(elseBlock, joinBlocks, function);
                    joinBlocks.remove(joinBlocks.size() - 1);
                } else {
                    icGen.fixup(x.fixuplocation, joinBlock);
                }
                if (token == Token.FI) {
//                    joinBlocks.add(joinBlock);
                    moveToNextToken();
                    icGen.fixAll(follow.fixuplocation, joinBlock); // set follow 
                                                                   // as target
                                                                   // for all
                                                                   // if and
                                                                   // elseif
                    linkJoinBlock(curBlock, ifLastBlock, elseLastBlock, joinBlock);
                    
                    // update phi existed func occur in the previous join blocks
                    updatePhiFuncsOccurInPreviousJoinBlocks(curBlock, ifLastBlock, elseLastBlock, joinBlock, ssaMap);
                    
                    // create non-existed phi funcs occur in the previous join blocks
                    createPhiFuncsOccurInPreviousIfJoinBlocks(curBlock, ifLastBlock, elseLastBlock, joinBlock, ssaMap);
                    
                    VariableManager.setSsaMap(ssaMap);
                    
                    // update all values to be results of phi functions
                    updateReferenceForPhiVarInJoinBlock(joinBlock);

                    return joinBlock;
                } else
                    throwFormatException("fi expected in ifStatement");
            } else
                throwFormatException("then expected in ifStatement");
        } else
            throwFormatException("if expected in ifStatement");
        throw new Exception("Programmer error in ifStatement!");
    }
    
    public void updatePhiFuncsOccurInPreviousJoinBlocks(BasicBlock curBlock, BasicBlock ifLastBlock,
            BasicBlock elseLastBlock, BasicBlock joinBlock, HashMap<Integer, ArrayList<SSA>> ssaMap){
        HashMap<Integer, Instruction> leftPhiFuncs = ifLastBlock.getPhiFuncsFromStartBlock(curBlock);
        updateValuesInPhiFuncForSuccessorBlock(leftPhiFuncs, joinBlock, true);
        if (elseLastBlock != null){
            HashMap<Integer, Instruction> rightPhiFuncs = elseLastBlock.getPhiFuncsFromStartBlock(curBlock);
            updateValuesInPhiFuncForSuccessorBlock(rightPhiFuncs, joinBlock, false);
        }else{
            for (Integer phiVar : joinBlock.getPhiFuncs().keySet()) {
                joinBlock.updatePhiFunction(phiVar, ssaMap.get(phiVar).get(ssaMap.get(phiVar).size() - 1), PhiFuncManager.Update_Type.RIGHT);
            }
        }
    }
    
    public void createPhiFuncsOccurInPreviousIfJoinBlocks(BasicBlock curBlock, BasicBlock ifLastBlock,
            BasicBlock elseLastBlock, BasicBlock joinBlock, HashMap<Integer, ArrayList<SSA>> ssaMap) throws SyntaxFormatException{
        HashSet<Integer> phiVars = new HashSet<Integer>();
        HashSet<Integer> ifPhiVars = ifLastBlock.getPhiVars(curBlock);
        phiVars.addAll(ifPhiVars);
        HashSet<Integer> elsePhiVars = null;
        if(elseLastBlock != null){
            elsePhiVars = elseLastBlock.getPhiVars(curBlock);
            phiVars.addAll(elsePhiVars);
        }
        HashSet<Integer> curPhiVars = joinBlock.getPhiVars();
        for(Integer phiVar : phiVars){
            if(!curPhiVars.contains(phiVar)){
                joinBlock.createPhiFunction(phiVar);
                if(ifPhiVars.contains(phiVar))
                    joinBlock.updatePhiFunction(phiVar, ifLastBlock.findLastSSA(phiVar, curBlock), PhiFuncManager.Update_Type.LEFT);
                if(elseLastBlock != null && elsePhiVars.contains(phiVar))
                    joinBlock.updatePhiFunction(phiVar, elseLastBlock.findLastSSA(phiVar, curBlock), PhiFuncManager.Update_Type.RIGHT);
                else
                    joinBlock.updatePhiFunction(phiVar, ssaMap.get(phiVar).get(ssaMap.get(phiVar).size() - 1), PhiFuncManager.Update_Type.RIGHT);
            }
        }
    }
    
    public void updateReferenceForPhiVarInJoinBlock(BasicBlock joinBlock) {
        for (Map.Entry<Integer, Instruction> entry : joinBlock.getPhiFuncs().entrySet()) {
            VariableManager.addSSA(entry.getKey(), entry.getValue().getSelfPC());
        }
    }

    private BasicBlock whileStatement(BasicBlock curBlock, ArrayList<BasicBlock> joinBlocks, Function function)
            throws Throwable {
        HashMap<Integer, ArrayList<SSA>> ssaMap = VariableManager.deepCopySSAMap();
        if (token == Token.WHILE) {
            moveToNextToken();

            BasicBlock innerJoinBlock = curBlock.makeInnerJoinSuccessor();
            curBlock = innerJoinBlock;

            Result x = relation(curBlock, null);
            icGen.condNegBraFwd(curBlock, x);

            BasicBlock doLastBlock = null;
            if (token == Token.DO) {
                moveToNextToken();
                BasicBlock startBlock = curBlock.makeDoSuccessor();
                if (joinBlocks == null)
                    joinBlocks = new ArrayList<BasicBlock>();
                joinBlocks.add(innerJoinBlock);
                doLastBlock = statSequence(startBlock, joinBlocks, function);
                doLastBlock.generateIntermediateCode(Instruction.bra, null, Result.makeBranch(curBlock));
                
                // update values in the phi function of doLastBlock
                updateReferenceForPhiVarInLoopBody(innerJoinBlock, startBlock, doLastBlock);

                BasicBlock followBlock = curBlock.makeElseSuccessor();
                icGen.fixup(x.fixuplocation, followBlock);

                // link loop block back to condition
                doLastBlock.setBackSuccessor(curBlock);
                
                joinBlocks.remove(joinBlocks.size() - 1);
                for (BasicBlock jB : joinBlocks){
                    updateValuesInPhiFuncForSuccessorBlock(innerJoinBlock.getPhiFuncs(), jB, false);
                }
                
                // create non-existed phi funcs occur in the previous join blocks
                createPhiFuncsOccurInPreviousWhileJoinBlocks(curBlock, doLastBlock, innerJoinBlock, ssaMap);
                
                VariableManager.setSsaMap(ssaMap);
                // update all values to be results of phi functions
                updateReferenceForPhiVarInJoinBlock(innerJoinBlock);

                // curBlock.setDirectPredecessor(doLastBlock);

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
    
    public void createPhiFuncsOccurInPreviousWhileJoinBlocks(BasicBlock curBlock, BasicBlock doLastBlock,
            BasicBlock joinBlock, HashMap<Integer, ArrayList<SSA>> ssaMap) throws SyntaxFormatException{
        HashSet<Integer> phiVars = new HashSet<Integer>();
        phiVars.addAll(doLastBlock.getPhiVars(curBlock));
        HashSet<Integer> curPhiVars = joinBlock.getPhiVars();
        for(Integer phiVar : phiVars){
            if(!curPhiVars.contains(phiVar)){
                joinBlock.createPhiFunction(phiVar);
                joinBlock.updatePhiFunction(phiVar, doLastBlock.findLastSSA(phiVar, curBlock), PhiFuncManager.Update_Type.RIGHT);
                joinBlock.updatePhiFunction(phiVar, ssaMap.get(phiVar).get(ssaMap.get(phiVar).size() - 1), PhiFuncManager.Update_Type.LEFT);
            }
        }
    }
    
    public void updateValuesInPhiFuncForSuccessorBlock(HashMap<Integer, Instruction> phifuncs, BasicBlock joinBlock,
            boolean fromLeft) {
        if (fromLeft) {
            for (Map.Entry<Integer, Instruction> entry1 : phifuncs.entrySet()) {
                for (Map.Entry<Integer, Instruction> entry2 : joinBlock.getPhiFuncs().entrySet()) {
                    if (entry1.getKey() == entry2.getKey())
                        entry2.getValue().setSsa1(new SSA(entry1.getValue().getSelfPC()));
                }
            }
        } else {
            for (Map.Entry<Integer, Instruction> entry1 : phifuncs.entrySet()) {
                for (Map.Entry<Integer, Instruction> entry2 : joinBlock.getPhiFuncs().entrySet()) {
                    if (entry1.getKey() == entry2.getKey())
                        entry2.getValue().setSsa2(new SSA(entry1.getValue().getSelfPC()));
                }
            }
        }
    }

    public void updateReferenceForPhiVarInLoopBody(BasicBlock innerJoinBlock, BasicBlock startBlock,
            BasicBlock doLastBlock) throws Throwable {
        for (Map.Entry<Integer, Instruction> entry : innerJoinBlock.getPhiFuncs().entrySet())
            innerJoinBlock.updateVarReferenceToPhi(entry.getKey(), entry.getValue().getSsa1().getVersion(), entry
                    .getValue().getSelfPC(), startBlock, doLastBlock);
    }

    private Result returnStatement(BasicBlock curBlock, ArrayList<BasicBlock> joinBlocks) throws IOException,
            SyntaxFormatException {
        Result x = new Result();
        if (token == Token.RETURN) {
            moveToNextToken();
            if (isExpression()) { // expression -> term -> factor -> designator
                                  // -> identifier
                x = expression(curBlock, joinBlocks);
            }
        } else
            throwFormatException("return expected in returnStatement");
        return x;
    }

    private boolean isStatement() {
        return token == Token.LET || token == Token.CALL || token == Token.IF || token == Token.WHILE
                || token == Token.RETURN;
    }

    private BasicBlock statement(BasicBlock curBlock, ArrayList<BasicBlock> joinBlocks, Function function)
            throws Throwable {
        if (token == Token.LET) { // assignment
            assignment(curBlock, joinBlocks);
            return curBlock;
        } else if (token == Token.CALL) { // funcCall
            funcCall(curBlock, joinBlocks);
            return curBlock;
        } else if (token == Token.IF) { // ifStatement
            return ifStatement(curBlock, joinBlocks, function);
        } else if (token == Token.WHILE) { // whileStatement
            return whileStatement(curBlock, joinBlocks, function);
        } else if (token == Token.RETURN) { // returnStatement
            Result x = returnStatement(curBlock, joinBlocks);
            icGen.generateReturnOp(curBlock, x, function);
            return curBlock;
        } else
            throwFormatException("not a valid statement");
        throw new Exception("Programmer error in statement!");
    }

    private BasicBlock statSequence(BasicBlock curBlock, ArrayList<BasicBlock> joinBlocks, Function function)
            throws Throwable {
        BasicBlock lastBlock = statement(curBlock, joinBlocks, function);
        while (token == Token.SEMICOMA) {
            moveToNextToken();
            lastBlock = statement(lastBlock, joinBlocks, function);
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
                if (token == Token.IDENTIFIER) { // multiple varNames
                    x = new Result();
                    x.set(Result.Type.var, scanner.id); // x is varName in
                                                        // varDecl
                    /** declare variable **/
                    icGen.declareVariable(curBlock, x, function);
                    moveToNextToken();
                } else
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

    private void formalParam(Function function) throws Throwable {
        Result x = null;
        if (token == Token.BEGIN_PARENTHESIS) {
            moveToNextToken();
            if (token == Token.IDENTIFIER) { // x is paramName in formalParam
                x = new Result();
                x.set(Result.Type.var, scanner.id);
                /** declare variable **/
                icGen.declareVariable(function.getFirstFuncBlock(), x, function);
                /** add to function parameters **/
                function.getParameters().add(x);
                moveToNextToken();
                while (token == Token.COMMA) {
                    moveToNextToken();
                    if (token == Token.IDENTIFIER) { // multiple paramNames
                        x = new Result();
                        x.set(Result.Type.var, scanner.id);
                        /** declare variable **/
                        icGen.declareVariable(function.getFirstFuncBlock(), x, function);
                        /** add to function parameters **/
                        function.getParameters().add(x);
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

    private void funcBody(Function function) throws Throwable {
        while (token == Token.VAR || token == Token.ARRAY) {
            varDecl(function.getFirstFuncBlock(), function);
        }
        if (token == Token.BEGIN_BRACE) {
            moveToNextToken();
            if (isStatement()) { // statSequence -> statement
                statSequence(function.getFirstFuncBlock(), null, function);
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
        String testprog = "test010";
        Parser ps = new Parser("src/test/resources/testprogs/while/" + testprog + ".txt");
        ps.parse();
        ControlFlowGraph.printIntermediateCode();
        VCGPrinter printer = new VCGPrinter(testprog);
        printer.printCFG();

        System.out.println(ControlFlowGraph.xPreDefUseChains);
        System.out.println(ControlFlowGraph.yPreDefUseChains);
    }
}
