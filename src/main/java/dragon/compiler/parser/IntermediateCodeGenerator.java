package dragon.compiler.parser;

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

    private RegisterAllocator registerAllocator;
    private HashSet<Integer> existedFunctions;
    private HashMap<Token, Integer> arithmeticOpCode;
    private HashMap<Token, Integer> negatedBranchOpCode;

    public IntermediateCodeGenerator() {
        this.bb = new BasicBlock();
        this.registerAllocator = new RegisterAllocator();
        this.existedFunctions = new HashSet<Integer>();
        this.arithmeticOpCode = new HashMap<Token, Integer>();
        this.negatedBranchOpCode = new HashMap<Token, Integer>();

        arithmeticOpCode.put(Token.PLUS, Instruction.add);
        arithmeticOpCode.put(Token.MINUS, Instruction.sub);
        arithmeticOpCode.put(Token.TIMES, Instruction.mul);
        arithmeticOpCode.put(Token.DIVIDE, Instruction.div);

        negatedBranchOpCode.put(Token.EQL, Instruction.bne);
        negatedBranchOpCode.put(Token.NEQ, Instruction.beq);
        negatedBranchOpCode.put(Token.LSS, Instruction.bge);
        negatedBranchOpCode.put(Token.LEQ, Instruction.bgt);
        negatedBranchOpCode.put(Token.GRE, Instruction.ble);
        negatedBranchOpCode.put(Token.GEQ, Instruction.blt);
    }

    public void load(Result x) {
        if (x.kind == Result.Type.constant) {
            x.kind = Result.Type.reg;
            if (x.value == 0)
                x.regno = 0;
            else {
                x.regno = registerAllocator.allocateReg();
                // TODO put into assembler format, but not addi
                //                generateIntermediateCode(Instruction);
            }
        } else if (x.kind == Result.Type.var) {
            x.kind = Result.Type.reg;
            x.regno = registerAllocator.allocateReg();
            //            generateIntermediateCode(Instruction.load, x.regno, );
        }
    }

    public void computeArithmeticOp(Token op, Result x, Result y) {
        if (x.kind == Result.Type.constant && y.kind == Result.Type.constant) {
            switch (arithmeticOpCode.get(op)) {
                case Instruction.cmp:
                    break;
                case Instruction.add:
                    x.value += y.value;
                    break;
                case Instruction.sub:
                    x.value -= y.value;
                    break;
                case Instruction.mul:
                    x.value *= y.value;
                    break;
                case Instruction.div:
                    x.value /= y.value;
                    break;
            }
        } else {
            load(x);
            if (y.kind == Result.Type.constant) {
                bb.generateIntermediateCode(arithmeticOpCode.get(op), x, y);
            } else {
                load(y);
                bb.generateIntermediateCode(arithmeticOpCode.get(op), x, y);
                registerAllocator.deAllocate(y.regno);
            }
        }
    }

    public void computeCmpOp(int opCode, Result left, Result right) {
        bb.generateIntermediateCode(opCode, left, right);
    }

    public void assign(Result variable, Result assignedValue) throws Throwable {
        if (variable.kind == Result.Type.constant) {
            throw new Exception("left Result cannot be constant");
        }
        bb.generateIntermediateCode(Instruction.move, assignedValue, variable);
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

    public void declareVariable(Result x, Function function) throws Throwable {
        if (x.kind != Result.Type.var)
            throw new Exception("The type of x should be var!");
        int varIndent = x.address;
        if (function != null) {
            // add ident to local variable of the function
            function.getLocalVariables().add(varIndent);
        } else {
            // add ident to global variable
            VariableManager.addGlobalVariable(varIndent);
        }

        Result defaultConstant = Result.makeConstant(0);
        bb.generateIntermediateCode(Instruction.move, defaultConstant, x);
    }

    public Function declareFunction(Result x) throws Exception {
        if (x.kind != Result.Type.var)
            throw new Exception("The type of x should be var!");
        int funcIdent = x.address;
        if (!existedFunctions.contains(funcIdent)) {
            existedFunctions.add(funcIdent);
            return new Function(funcIdent);
        } else {
            System.out.println("Function name duplicates!");
            return null;
        }
    }

    public void condNegBraFwd(Result x) {
        x.fixuplocation = Instruction.getPC();
        bb.generateIntermediateCode(negatedBranchOpCode.get(x.cc), x, Result.makeBranch(-1));
    }

    public void unCondBraFwd(Result follow) {
        Result branch = Result.makeBranch(-1);
        branch.fixuplocation = follow.fixuplocation;
        bb.generateIntermediateCode(Instruction.bra, null, branch);
        follow.fixuplocation = Instruction.getPC() - 1;
    }

    public void fixup(int loc) {
        bb.findInstruction(loc).getResult2().setTargetLine(Instruction.getPC());
    }

    public void fixAll(int loc) {
        while (loc != 0) {
            int next = bb.findInstruction(loc).getResult2().getTargetLine();
            fixup(loc);
            loc = next;
        }
    }

    public void printIntermediateCode() {
        for (Instruction instruction : bb.getInstructions()) {
            System.out.println(instruction.toString());
        }
    }
}
