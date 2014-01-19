package dragon.compiler.data;

public class ControlFlowGraph {
    private BasicBlock firstBlock;

    public ControlFlowGraph(){
        firstBlock = new BasicBlock();
    }
    
    public BasicBlock getFirstBlock() {
        return firstBlock;
    }

    public void setFirstBlock(BasicBlock firstBlock) {
        this.firstBlock = firstBlock;
    }
    
}
