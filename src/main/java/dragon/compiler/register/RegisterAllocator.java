package dragon.compiler.register;

import java.util.HashMap;

import dragon.compiler.data.BasicBlock;
import dragon.compiler.data.Instruction;
import dragon.compiler.data.Result;


public class RegisterAllocator {

    private boolean[] registers;
    private final static int NUM_OF_REGS = 10;

    public RegisterAllocator() {
        this.registers = new boolean[NUM_OF_REGS];

        //cannot use reg0
        registers[0] = true;
        for (int i = 1; i < NUM_OF_REGS; i++) {
            this.registers[i] = false;
        }
    }

    public int allocateReg() {
        int i = 1;
        for (; i < NUM_OF_REGS; i++) {
            if (registers[i] == false) {
                registers[i] = true;
                return i;
            }
        }
        System.out.println("Exceed the maximum number of registers!");
        return -1;
    }

    public void deAllocate(int regno) {
        registers[regno] = false;
    }
    
    public void optimize(BasicBlock root) throws Exception{
        if(root == null)
            return;
        
        for(Instruction instr : root.getInstructions()){
            if(instr.deleted)
                continue;
            if(instr.isReadAssignment()){
                Result x = root.getNextInstruction(instr).getResult2();
                load(x);
                id2Regno.put(instr.getSelfPC(), x.regno);
            } else if(instr.isExpressionOp()){
                computeArithmeticOp(instr);
            } else if(instr.isWriteAssignment()){
                Result x = instr.getResult1();
                mapInstridToConstOrRegno(x);
            }
        }
        
        if(root.getIfSuccessor() != null) {
            optimize(root.getIfSuccessor());
        }
        
        if(root.getElseSuccessor() != null) {
            optimize(root.getElseSuccessor());
        }
        
        if(root.getJoinSuccessor() != null && root.getIfSuccessor() == null) {
            optimize(root.getJoinSuccessor());
        }
        
    }
    
    private HashMap<Integer, Integer> id2Const = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> id2Regno = new HashMap<Integer, Integer>();
    
    public void load(Result x) {
        if (x.kind == Result.Type.constant) {
            x.kind = Result.Type.reg;
            if (x.value == 0)
                x.regno = 0;
            else {
                x.regno = this.allocateReg();
            }
        } else if (x.kind == Result.Type.var) {
            x.kind = Result.Type.reg;
            x.regno = this.allocateReg();
        }
    }
    
    public void computeArithmeticOp(Instruction instr) throws Exception {
        Result x = instr.getResult1();
        Result y = instr.getResult2();
        if (x.kind == Result.Type.constant && y.kind == Result.Type.constant) {
            switch (instr.getOperator()) {
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
            id2Const.put(instr.getSelfPC(), x.value);
            instr.deleted = true;
        } else {
            if(x.kind == Result.Type.var){
                load(x);
            }else if(x.kind == Result.Type.instr){
                mapInstridToConstOrRegno(x);
            }
            if(y.kind == Result.Type.var){
                load(y);
                this.deAllocate(y.regno);
            }else if(y.kind == Result.Type.instr){
                mapInstridToConstOrRegno(y);
            }
            id2Regno.put(instr.getSelfPC(), x.regno);
        }
    }
    
    public void mapInstridToConstOrRegno(Result x) throws Exception{
        if(id2Const.containsKey(x.instrId)){
            x.kind = Result.Type.constant;
            x.value = id2Const.get(x.instrId);
        }else if(id2Regno.containsKey(x.instrId)){
            x.kind = Result.Type.reg;
            x.regno = id2Regno.get(x.instrId);
        }else
            throw new Exception("Programmer error!");
    }
}
