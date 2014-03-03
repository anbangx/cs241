package dragon.compiler.data;

import java.util.ArrayList;

public class DominatorTreeNode {
    
    public BasicBlock block;
    public ArrayList<DominatorTreeNode> children;
    
    public DominatorTreeNode(BasicBlock block){
        this.block = block;
        this.children = new ArrayList<DominatorTreeNode>();
    }

    public BasicBlock getBlock() {
        return block;
    }

    public void setBlock(BasicBlock block) {
        this.block = block;
    }

    public ArrayList<DominatorTreeNode> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<DominatorTreeNode> children) {
        this.children = children;
    }
    
    
}   
