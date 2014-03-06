package dragon.compiler.data;

import dragon.compiler.parser.ControlFlowGraph;

public class Instruction {
    enum Type {
        PHI,
        NON_PHI;
    }

    private static int pc = 0;
    private int selfPC;
    public static final int neg = 0;
    public static final int add = 1;
    public static final int sub = 2;
    public static final int mul = 3;
    public static final int div = 4;
    public static final int cmp = 5;

    public static final int adda = 6;
    public static final int load = 7;
    public static final int store = 8;
    public static final int move = 9;
    public static final int phi = 10;

    public static final int end = 11;
    public static final int bra = 12;
    public static final int bne = 13;
    public static final int beq = 14;
    public static final int ble = 15;
    public static final int blt = 16;
    public static final int bge = 17;
    public static final int bgt = 18;

    public static final int read = 19;
    public static final int write = 20;
    public static final int wln = 21;

    public static final int push = 22;
    public static final int pop = 23;
    public static final int subroutine = 24;
    public static final int retrn = 25;

    public String verbose(int opCode) {
        switch (opCode) {
            case neg:
                return "neg";
            case add:
                return "add";
            case sub:
                return "sub";
            case mul:
                return "mul";
            case div:
                return "div";
            case cmp:
                return "cmp";
            case adda:
                return "adda";
            case load:
                return "load";
            case store:
                return "store";
            case move:
                return "move";
            case phi:
                return "phi";
            case end:
                return "end";
            case bra:
                return "bra";
            case bne:
                return "bne";
            case beq:
                return "beq";
            case ble:
                return "ble";
            case blt:
                return "blt";
            case bge:
                return "bge";
            case bgt:
                return "bgt";
            case read:
                return "read";
            case write:
                return "write";
            case wln:
                return "wln";
            default:
                return "";
        }
    }

    private int operator;
    private Type kind;
    private Result result1 = null;
    private Result result2 = null;
    private SSA ssa1;
    private SSA ssa2;
    private String var;
    private boolean leftLatestUpdated;
    public boolean deleted = false;
    public boolean leftRepresentedByInstrId = false;
    public boolean rightRepresentedByInstrId = false;
    public int targetInstrId;

    public Instruction(int op, Result result1, Result result2) {
        this.operator = op;
        this.result1 = result1;
        this.result2 = result2;
        this.kind = Type.NON_PHI;
        this.selfPC = pc;
        Instruction.pc++;
        ControlFlowGraph.allInstructions.add(this);
    }

    public Instruction(int op, SSA ssa1, SSA ssa2) {
        this.operator = op;
        this.ssa1 = ssa1;
        this.ssa2 = ssa2;
        this.kind = Type.PHI;
        this.selfPC = pc;
        Instruction.pc++;
        ControlFlowGraph.allInstructions.add(this);
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("");
        sb.append(selfPC + ": ");
        sb.append(verbose(this.operator) + " ");
        if (kind == Type.NON_PHI) {
            if (this.operator >= bra && this.operator <= bgt) {
                sb.append(result2.toString());
            } else {
                sb.append(result1 != null ? result1.toString() + " " : "");
                sb.append(result2 != null ? result2.toString() : "");
            }
        } else {
            String var1 = "";
            String var2 = "";
            if (leftRepresentedByInstrId) {
                var1 = "(" + this.getResult1().instrId + ")";
            } else{
                var1 = this.var + "_" + ssa1.toString();
            }
            if(rightRepresentedByInstrId){
                var2 = "(" + this.getResult2().instrId + ")";
            } else{
                var2 = this.var + "_" + ssa2.toString();
            }
            sb.append(var + "_" + selfPC + " " + var1 + " " + var2);
        }
        if (deleted) {
            sb.append("(deleted)");
        }
        return sb.toString();
    }

    public boolean isReadAssignment() {
        return operator == Instruction.read;
    }

    public boolean isConstantAssignment() {
        return operator == Instruction.move && result1.kind == Result.Type.constant && result2.kind == Result.Type.var;
    }

    public boolean isInstructionAssignment() {
        return operator == Instruction.move && result1.kind == Result.Type.instr && result2.kind == Result.Type.var;
    }

    public boolean isVariableAssignment() {
        return operator == Instruction.move && result1.kind == Result.Type.var && result2.kind == Result.Type.var;
    }
    
    public boolean isExpressionOp(){
        return operator >= Instruction.neg && operator <= Instruction.div;
    }
    
    public int getExpressionOp(){
        return operator;
    }

    public boolean isOtherAssignment() {
        return (operator >= Instruction.add && operator <= Instruction.cmp || operator >= Instruction.bne
                && operator >= Instruction.bgt)
                && result1.kind == Result.Type.var && result2.kind == Result.Type.var;
    }

    public int getIdent() {
        return result1.address;
    }

    public int getLeftIdent() {
        return result1.address;
    }

    public int getRightIdent() {
        return result2.address;
    }

    public static int getPC() {
        return pc;
    }

    public static void setPC(int pc) {
        Instruction.pc = pc;
    }

    public int getSelfPC() {
        return selfPC;
    }

    public void setSelfPC(int selfPC) {
        this.selfPC = selfPC;
    }

    public Result getResult1() {
        return result1;
    }

    public void setResult1(Result result1) {
        this.result1 = result1;
    }

    public Result getResult2() {
        return result2;
    }

    public void setResult2(Result result2) {
        this.result2 = result2;
    }

    public SSA getSsa1() {
        return ssa1;
    }

    public void setSsa1(SSA ssa1) {
        this.ssa1 = ssa1;
    }

    public SSA getSsa2() {
        return ssa2;
    }

    public void setSsa2(SSA ssa2) {
        this.ssa2 = ssa2;
    }

    public int getOperator() {
        return operator;
    }

    public void setOperator(int operator) {
        this.operator = operator;
    }

    public boolean isLeftLatestUpdated() {
        return leftLatestUpdated;
    }

    public void setLeftLatestUpdated(boolean leftLatestUpdated) {
        this.leftLatestUpdated = leftLatestUpdated;
    }

}
