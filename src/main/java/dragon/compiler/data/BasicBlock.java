package dragon.compiler.data;

import java.util.ArrayList;
import java.util.List;

import dragon.compiler.parser.ControlFlowGraph;
import dragon.compiler.parser.VariableManager;

public class BasicBlock {
	public enum Type{
		IF, ELSE, JOIN, NONE;
	}
    private int id;
    private Type kind;
    private List<Instruction> instructions;
    private PhiFuncManager phiFuncManager;
    
    private BasicBlock directSuccessor; // used in if and while
    private BasicBlock elseSuccessor;
    // used in if-else statement for traversing CFG
    private BasicBlock joinSuccessor;
    
    // used in while statement
    private BasicBlock backSuccessor;
    
    public BasicBlock(Type kind) {
        this.id = ControlFlowGraph.blocks.size() + 1;
        this.kind = kind;
        ControlFlowGraph.blocks.add(this);
        instructions = new ArrayList<Instruction>();
        phiFuncManager = new PhiFuncManager();
    }
    
    public void createPhiFunction(int ident){
    	phiFuncManager.createPhiInstruction(ident, VariableManager.getLastVersionSSA(ident));
    }
    
    public void updatePhiFunction(int ident, SSA newSSA, Type blockType){
    	phiFuncManager.updatePhiInstruction(ident, newSSA, blockType == Type.IF ? 
    			PhiFuncManager.Update_Type.LEFT : PhiFuncManager.Update_Type.RIGHT);
    }
    
    public void generateIntermediateCode(int op, Result result1, Result result2) {
        instructions.add(new Instruction(op, result1, result2));
    }
    
    public PhiFuncManager getPhiFuncManager() {
		return phiFuncManager;
	}

	public void setPhiFuncManager(PhiFuncManager phiFuncManager) {
		this.phiFuncManager = phiFuncManager;
	}

	public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }
    
    public Type getKind() {
		return kind;
	}

	public void setKind(Type kind) {
		this.kind = kind;
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
    
    public BasicBlock makeDirectSuccessor(){
        BasicBlock ifSuccessor = new BasicBlock(Type.IF);
        this.directSuccessor = ifSuccessor;
        return ifSuccessor;
    }
    
    public BasicBlock makeElseSuccessor(){
        BasicBlock elseSuccessor = new BasicBlock(Type.ELSE);
        this.elseSuccessor = elseSuccessor;
        return elseSuccessor;
    }
    
    public BasicBlock getIfSuccessor() {
        return directSuccessor;
    }

    public void setIfSuccessor(BasicBlock ifSuccessor) {
        this.directSuccessor = ifSuccessor;
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
    
    public BasicBlock getBackSuccessor() {
        return backSuccessor;
    }

    public void setBackSuccessor(BasicBlock backSuccessor) {
        this.backSuccessor = backSuccessor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
}
