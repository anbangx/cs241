package dragon.compiler.parser;

import java.util.ArrayList;

import dragon.compiler.data.BasicBlock;
import dragon.compiler.data.Instruction;

public class ControlFlowGraph {
//    private BasicBlock firstBlock;
//
//    public ControlFlowGraph(){
//        firstBlock = new BasicBlock();
//    }
//    
//    public BasicBlock getFirstBlock() {
//        return firstBlock;
//    }
//
//    public void setFirstBlock(BasicBlock firstBlock) {
//        this.firstBlock = firstBlock;
//    }
    
    private BasicBlock firstBlock;
    private ArrayList<BasicBlock> blocks;
    
    public ControlFlowGraph(){
        firstBlock = new BasicBlock();
        this.blocks = new ArrayList<BasicBlock>();
    }
    
    public BasicBlock getFirstBlock() {
        return firstBlock;
    }


    public void setFirstBlock(BasicBlock firstBlock) {
        this.firstBlock = firstBlock;
    }

    public ArrayList<BasicBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(ArrayList<BasicBlock> blocks) {
        this.blocks = blocks;
    }
    
    public void printIntermediateCode() {
        for (Instruction instruction : firstBlock.getInstructions()) {
            System.out.println(instruction.toString());
        }
    }
}
