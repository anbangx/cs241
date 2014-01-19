package dragon.compiler.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import dragon.compiler.data.BasicBlock;
import dragon.compiler.data.Function;
import dragon.compiler.data.Instruction;
import dragon.compiler.data.Result;
import dragon.compiler.data.Token;
import dragon.compiler.register.RegisterAllocator;

public class IntermediateCodeGenerator {

    BasicBlock bb;

    private RegisterAllocator regAllocator;
    private HashMap<Token, Integer> opCode;
    private HashSet<Integer> existedFunctions;
    
    public IntermediateCodeGenerator() {
        bb = new BasicBlock();
        this.opCode = new HashMap<Token, Integer>();
        regAllocator = new RegisterAllocator();
        existedFunctions = new HashSet<Integer>();

        opCode.put(Token.TIMES, Instruction.mul);
        opCode.put(Token.DIVIDE, Instruction.div);
        opCode.put(Token.PLUS, Instruction.add);
        opCode.put(Token.MINUS, Instruction.sub);
    }

    public void compute(Token op, Result x, Result y) {
        if (x.kind == Result.Type.constant && y.kind == Result.Type.constant) {
            switch (op) {
                case PLUS:
                    x.value += y.value;
                    break;
                case MINUS:
                    x.value -= y.value;
                    break;
                case TIMES:
                    x.value *= y.value;
                    break;
                case DIVIDE:
                    x.value /= y.value;
                    break;
            }
        } else {
            regAllocator.load(x);
            if (y.kind == Result.Type.constant) {
                bb.generateIntermediateCode(opCode.get(op), x, y);
            } else {
                regAllocator.load(y);
                bb.generateIntermediateCode(opCode.get(op), x, y);
                regAllocator.deAllocate(y.regno);
            }
        }
    }

    public void assign(Result x, Result y) throws Throwable {
        if (x.kind == Result.Type.constant) {
            throw new Exception("left Result cannot be constant");
        }
        bb.generateIntermediateCode(Instruction.move, x, y);
        //look up the constant table, if exists the same constant, use previous ssa
        //        if(y.kind == Result.Type.constant) {
        //            if(!VariableManager.constantExist(y.val)) {
        //                VariableManager.addAssignment(Instruction.getPC(), x);
        //                VariableManager.addConstant(y.val, x.ssa);
        //            } else {
        //                y.ssa = VariableManager.getSSAOfConstant(y.val);
        //                VariableManager.addAssignment(Instruction.getPC(), x);
        //            }
        //        } else {
        //            VariableManager.addAssignment(Instruction.getPC(), x);
        //        }
    }

    public void declareVariable(int varIndent, Function function) {
        if(function != null){
            // add ident to local variable of the function
            function.getLocalVariables().add(varIndent);
        } else{
            // add ident to global variable
            VariableManager.addGlobalVariable(varIndent);
        }
    }

    public Function declareFunction(int funcIdent) {
        if(!existedFunctions.contains(funcIdent)){
            existedFunctions.add(funcIdent);
            return new Function(funcIdent);
        } else{
            System.out.println("Function name duplicates!");
            return null;
        }
    }

    public void printIntermediateCode() {
        for (Instruction instruction : bb.getInstructions()) {
            System.out.println(instruction.toString());
        }
    }
}
