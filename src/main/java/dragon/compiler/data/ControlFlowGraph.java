package dragon.compiler.data;

import java.util.ArrayList;

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
    
    private ArrayList<BasicBlock> blocks;
    
    public ControlFlowGraph(){
        this.blocks = new ArrayList<BasicBlock>();
    }

    public ArrayList<BasicBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(ArrayList<BasicBlock> blocks) {
        this.blocks = blocks;
    }
    
}
