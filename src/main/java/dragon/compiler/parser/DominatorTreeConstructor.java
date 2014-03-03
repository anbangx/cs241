package dragon.compiler.parser;

import java.util.HashSet;

import dragon.compiler.data.BasicBlock;
import dragon.compiler.data.DominatorTreeNode;

public class DominatorTreeConstructor {
    
    public static DominatorTreeNode root;
    
    public DominatorTreeConstructor(){
        root = new DominatorTreeNode(ControlFlowGraph.getFirstBlock());
        HashSet<BasicBlock> visited = new HashSet<BasicBlock>();
        visited.add(root.block);
        buildDominatorTree(root, visited);
    }
    
    public void buildDominatorTree(DominatorTreeNode root, HashSet<BasicBlock> visited){
        if(root == null)
            return;
        
        if(root.block.getIfSuccessor() != null && !visited.contains(root.block.getIfSuccessor())){
            DominatorTreeNode child = new DominatorTreeNode(root.block.getIfSuccessor());
            root.children.add(child);
            visited.add(root.block.getIfSuccessor());
        }
        if(root.block.getElseSuccessor() != null && !visited.contains(root.block.getElseSuccessor())){
            DominatorTreeNode child = new DominatorTreeNode(root.block.getElseSuccessor());
            root.children.add(child);
            visited.add(root.block.getElseSuccessor());
        }
        if(root.block.getJoinSuccessor() != null && !visited.contains(root.block.getJoinSuccessor())){
            DominatorTreeNode child = new DominatorTreeNode(root.block.getJoinSuccessor());
            root.children.add(child);
            visited.add(root.block.getJoinSuccessor());
        }
    }
}
