package dragon.compiler.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import dragon.compiler.data.BasicBlock;
import dragon.compiler.data.DominatorTreeNode;
import dragon.compiler.data.Instruction;

public class DominatorTreeConstructor {
    
    public static DominatorTreeNode dtRoot;
    HashMap<Integer, LinkedList<Instruction>> cseHM;
    
    public DominatorTreeConstructor(){
        dtRoot = new DominatorTreeNode(ControlFlowGraph.getFirstBlock());
    }
    
    public void build(){
        HashSet<BasicBlock> visited = new HashSet<BasicBlock>();
        visited.add(dtRoot.block);
        buildDominatorTree(dtRoot, visited);
        cseHM = new HashMap<Integer, LinkedList<Instruction>>();
    }
    
    private void buildDominatorTree(DominatorTreeNode root, HashSet<BasicBlock> visited){
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
    
    public void indexedByOpcode(DominatorTreeNode root){
        if(root == null)
            return;
        
        for(Instruction instr : root.block.getInstructions()){
            int op = instr.getOperator();
            LinkedList<Instruction> head;
            if(!cseHM.containsKey(op)){
                head = new LinkedList<Instruction>();
            }else{
                head = cseHM.get(op);
            }
            head.add(instr);
        }
        
        for(DominatorTreeNode child : root.children){
            indexedByOpcode(child);
        }
    }
}
