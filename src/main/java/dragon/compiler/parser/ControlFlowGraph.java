package dragon.compiler.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    
    private static BasicBlock firstBlock;
    public static ArrayList<BasicBlock> blocks;
    
    public ControlFlowGraph(){
        blocks = new ArrayList<BasicBlock>();
        firstBlock = new BasicBlock(BasicBlock.Type.NONE);
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
    
    public static Instruction findInstruction(int index){
        for(BasicBlock block : blocks){
            if(block.findInstruction(index) != null)
                return block.findInstruction(index);
        }
        return null;
    }
    
    public void printIntermediateCode() {
        for(BasicBlock block : blocks){
            System.out.println("Block_" + block.getId() + "[");
            for(Map.Entry<Integer, Instruction> entry : block.getPhiFuncManager().getPhiFuncs().entrySet())
                System.out.println(entry.toString());
            for(Instruction i : block.getInstructions())
                System.out.println(i.toString());
            System.out.println("]");
        }
    }
}
