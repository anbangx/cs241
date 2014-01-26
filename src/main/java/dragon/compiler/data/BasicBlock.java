package dragon.compiler.data;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {

    private List<Instruction> instructions;
    
    private BasicBlock ifSuccessor;
    private BasicBlock elseSuccessor;
    //used in if-else statement for traversing CFG
    private BasicBlock joinSuccessor;
    
    public BasicBlock() {
        instructions = new ArrayList<Instruction>();
    }

    public void generateIntermediateCode(int op, Result result1, Result result2) {
        instructions.add(new Instruction(op, result1, result2));
    }
    
    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }
    
    public Instruction findInstruction(int index){
        for(Instruction i : instructions){
            if(i.getSelfPC() == index)
                return i;
        }
        return null;
    }
    
    public String toString(){
        StringBuilder sb = new StringBuilder("");
        for(Instruction i : instructions){
            sb.append(i + "\n");
        }
        return sb.toString();
    }
    
    public BasicBlock makeIfSuccessor(){
        BasicBlock ifSuccessor = new BasicBlock();
        this.ifSuccessor = ifSuccessor;
        return ifSuccessor;
    }
    
    public BasicBlock makeElseSuccessor(){
        BasicBlock elseSuccessor = new BasicBlock();
        this.elseSuccessor = elseSuccessor;
        return elseSuccessor;
    }
    
    public BasicBlock getIfSuccessor() {
        return ifSuccessor;
    }

    public void setIfSuccessor(BasicBlock ifSuccessor) {
        this.ifSuccessor = ifSuccessor;
    }

    public BasicBlock getElseSuccessor() {
        return elseSuccessor;
    }

    public void setElseSuccessor(BasicBlock elseSuccessor) {
        this.elseSuccessor = elseSuccessor;
    }

    public BasicBlock getJoinSuccessor() {
        return joinSuccessor;
    }

    public void setJoinSuccessor(BasicBlock joinSuccessor) {
        this.joinSuccessor = joinSuccessor;
    }
    
}
