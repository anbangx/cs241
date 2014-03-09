package dragon.compiler.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import dragon.compiler.util.ControlFlowGraph;
import dragon.compiler.util.PhiFuncManager;
import dragon.compiler.util.VariableManager;
import dragon.compiler.util.PhiFuncManager.Update_Type;

public class BasicBlock {
    public enum Type {
        IF,
        ELSE,
        DO,
        IF_JOIN,
        WHILE_JOIN,
        NONE;
    }

    private int id;
    private Type kind;
    private List<Instruction> instructions;
    private PhiFuncManager phiFuncManager;

    private BasicBlock directSuccessor; // used in if and while
    private BasicBlock elseSuccessor;
    // used in if-else statement for traversing CFG
    private BasicBlock joinSuccessor;

    private BasicBlock predecessor;

    // used in while statement
    private BasicBlock backSuccessor;
    
    public BasicBlock(Type kind) {
        this.id = ControlFlowGraph.blocks.size() + 1;
        this.kind = kind;
        ControlFlowGraph.blocks.add(this);
        instructions = new ArrayList<Instruction>();
        phiFuncManager = new PhiFuncManager();
        directSuccessor = null;
        elseSuccessor = null;
        joinSuccessor = null;
        predecessor = null;
    }

    public Instruction createPhiFunction(int ident) throws SyntaxFormatException {
        return phiFuncManager.createPhiInstruction(ident, VariableManager.getLastVersionSSA(ident));
    }

    public void updatePhiFunction(int ident, SSA newSSA, Type blockType) {
        phiFuncManager.updatePhiInstruction(ident, newSSA, ControlFlowGraph.phiFuncUpdateType.get(blockType));
    }
    
    public void updatePhiFunction(int ident, SSA newSSA, Update_Type updateType) {
        phiFuncManager.updatePhiInstruction(ident, newSSA, updateType);
    }
    
    public void updateVarReferenceToPhi(int ident, int oldSSA, int newSSA, BasicBlock startBlock, BasicBlock endBlock)
            throws SyntaxFormatException {
        updateVarReferenceInJoinBlock(ident, oldSSA, newSSA);
        updateVarReferenceInLoopBody(startBlock, endBlock, ident, oldSSA, newSSA);
    }

    public void updateVarReferenceInJoinBlock(int ident, int oldSSA, int newSSA) throws SyntaxFormatException {
        if (kind != Type.WHILE_JOIN)
            throw new SyntaxFormatException("updateVarReferenceToPhi can only be called in the WHOLE_JOIN!");
        // update cond statement
        Instruction cond = findCondInstruction(ident);
        if (cond != null) {
            if (cond.getResult1().address == ident)
                cond.getResult1().setSSA(newSSA);
            else
                cond.getResult2().setSSA(newSSA);
        }
    }

    public void updateVarReferenceInLoopBody(BasicBlock startBlock, BasicBlock endBlock, int ident, int oldSSA,
            int newSSA) {
        HashSet<BasicBlock> visited = new HashSet<BasicBlock>();
        Queue<BasicBlock> q = new LinkedList<BasicBlock>();
        q.add(startBlock);
        while (!q.isEmpty()) {
            BasicBlock curBlock = q.poll();
            visited.add(curBlock);
            updateVarReferenceInBasicBlock(curBlock, ident, oldSSA, newSSA);

            if (curBlock == endBlock)
                continue;
            Queue<BasicBlock> newSuccessors = curBlock.getSuccessors();
            for (BasicBlock successor : newSuccessors) {
                if (!visited.contains(successor))
                    q.add(successor);
            }
        }
    }

    public void updateVarReferenceInBasicBlock(BasicBlock block, int ident, int oldSSA, int newSSA) {
        for (Instruction i : block.getInstructions()) {
            if (i.getResult1() != null && i.getResult1().isIdent(ident, oldSSA))
                i.getResult1().setSSA(newSSA);
            if (i.getResult2() != null && i.getResult2().isIdent(ident, oldSSA))
                i.getResult2().setSSA(newSSA);
        }
    }

    public SSA findLastVersionSSAFromJoinblock(int ident) throws SyntaxFormatException {
        if (kind != Type.IF_JOIN && kind != Type.WHILE_JOIN)
            throw new SyntaxFormatException("findLastVersionSSAFromJoinblock is called only in the join block!");
        Instruction instr = phiFuncManager.getPhiFuncs().get(ident);
        return instr.isLeftLatestUpdated() ? instr.getSsa1() : instr.getSsa2();
    }

    public Instruction generateIntermediateCode(int op, Result result1, Result result2) {
        Instruction instr = new Instruction(op, result1 == null ? null : new Result(result1), result2 == null ? null
                : new Result(result2));
        instructions.add(instr);
        return instr;
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

    public Instruction findInstruction(int index) {
        for (Instruction i : instructions) {
            if (i.getSelfPC() == index)
                return i;
        }
        return null;
    }

    public Instruction findCondInstruction(int ident) {
        for (Instruction i : instructions) {
            if (i.getOperator() == Instruction.cmp
                    && (i.getResult1().address == ident || i.getResult2().address == ident))
                return i;
        }
        return null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("");
        for (Map.Entry<Integer, Instruction> entry : phiFuncManager.getPhiFuncs().entrySet())
            sb.append(entry.toString() + "\n");
        for (Instruction i : instructions) {
            sb.append(i + "\n");
        }
        return sb.toString();
    }

    public BasicBlock makeIfSuccessor() {
        BasicBlock ifSuccessor = new BasicBlock(Type.IF);
        this.directSuccessor = ifSuccessor;
        ifSuccessor.predecessor = this;
        return ifSuccessor;
    }

    public BasicBlock makeElseSuccessor() {
        BasicBlock elseSuccessor = new BasicBlock(Type.ELSE);
        this.elseSuccessor = elseSuccessor;
        elseSuccessor.predecessor = this;
        return elseSuccessor;
    }

    public BasicBlock makeDoSuccessor() {
        BasicBlock doSuccessor = new BasicBlock(Type.DO);
        this.directSuccessor = doSuccessor;
        doSuccessor.predecessor = this;
        return doSuccessor;
    }

    public BasicBlock makeInnerJoinSuccessor() {
        BasicBlock directSuccessor = new BasicBlock(Type.WHILE_JOIN);
        this.directSuccessor = directSuccessor;
        return directSuccessor;
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

    public BasicBlock getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(BasicBlock predecessor) {
        this.predecessor = predecessor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public HashMap<Integer, Instruction> getPhiFuncs() {
        return phiFuncManager.getPhiFuncs();
    }

    public HashMap<Integer, Instruction> getPhiFuncsFromStartBlock(BasicBlock startBlock) {
        HashMap<Integer, Instruction> hM = new HashMap<Integer, Instruction>();
        BasicBlock cur = this;
        while (cur != null && cur != startBlock) {
            hM.putAll(cur.getPhiFuncs());
            cur = cur.getPredecessor();
        }
        return hM;
    }

    public Queue<BasicBlock> getSuccessors() {
        Queue<BasicBlock> successors = new LinkedList<BasicBlock>();
        if (directSuccessor != null)
            successors.add(directSuccessor);
        if (elseSuccessor != null && !successors.contains(elseSuccessor))
            successors.add(elseSuccessor);
        if (joinSuccessor != null && !successors.contains(joinSuccessor))
            successors.add(joinSuccessor);
        return successors;
    }

    public HashSet<Integer> getPhiVars() {
        HashSet<Integer> vars = new HashSet<Integer>();
        vars.addAll(this.getPhiFuncs().keySet());
        return vars;
    }

    public HashSet<Integer> getPhiVars(BasicBlock startBlock) {
        HashSet<Integer> vars = new HashSet<Integer>();
        BasicBlock cur = this;
        while (cur != null && cur != startBlock) {
            vars.addAll(cur.getPhiFuncs().keySet());
            cur = cur.getPredecessor();
        }
        return vars;
    }
    
    public SSA findLastSSA(int ident, BasicBlock startBlock){
        BasicBlock cur = this;
        while (cur != null && cur != startBlock) {
            for(Map.Entry<Integer, Instruction> entry : cur.getPhiFuncs().entrySet()){
                if(entry.getKey() == ident){
                    return new SSA(entry.getValue().getSelfPC());
                }
            }
            cur = cur.getPredecessor();
        }
        return null;
    }
    
    public void assignNewSSA(int ident, SSA ssa, SSA newSSA, BasicBlock startBlock){
        BasicBlock cur = this;
        while (cur != null) {
            for(Map.Entry<Integer, Instruction> entry : cur.getPhiFuncs().entrySet()){
                if(entry.getKey() == ident && entry.getValue().getSsa1() == ssa){
                    entry.getValue().setSsa1(newSSA);
                }
            }
            HashSet<Instruction> instrs = startBlock.getAllDoInstructions();
            for(Instruction instr : instrs){
                if(instr.getResult1() != null && instr.getResult1().address == ident && instr.getResult1().ssa == ssa){
                    instr.getResult1().ssa = newSSA;
                }
                if(instr.getResult1() != null && instr.getResult2().address == ident && instr.getResult2().ssa == ssa){
                    instr.getResult2().ssa = newSSA;
                }
            }
            if(cur == startBlock)
                break;
            cur = cur.getPredecessor();
        }
        return;
    }
    
    public HashSet<Instruction> getAllDoInstructionsUtil(BasicBlock block){
        HashSet<Instruction> instrs = new HashSet<Instruction>();
        if(block == null)
            return instrs;
        instrs.addAll(block.getInstructions());
        if(block.directSuccessor != null)
            instrs.addAll(getAllDoInstructionsUtil(block.directSuccessor));
        if(block.elseSuccessor != null)
            instrs.addAll(getAllDoInstructionsUtil(block.elseSuccessor));
        if(block.joinSuccessor != null)
            instrs.addAll(getAllDoInstructionsUtil(block.joinSuccessor));
        return instrs;
    }
    
    public HashSet<Instruction> getAllDoInstructions(){
        if(this.kind != Type.WHILE_JOIN)
            return null;
        return getAllDoInstructionsUtil(this);
    }
    
    public Instruction getNextInstruction(Instruction instr){
        for(int i = 0; i < instructions.size(); i++){
            if(instructions.get(i) == instr)
                return  instructions.get(i + 1);
        }
        return null;
    }
}
