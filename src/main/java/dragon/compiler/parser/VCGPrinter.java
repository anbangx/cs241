package dragon.compiler.parser;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import dragon.compiler.data.BasicBlock;
import dragon.compiler.data.DominatorTreeNode;
import dragon.compiler.data.Instruction;
import dragon.compiler.scanner.Scanner;


public class VCGPrinter {
    
    private PrintWriter writer;
    
    public VCGPrinter(String outputName){
        try{
            writer = new PrintWriter(new FileWriter("vcg/" + outputName + ".vcg"));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void printCFG() {
        writer.println("graph: { title: \"Control Flow Graph\"");
        writer.println("layoutalgorithm: dfs");
        writer.println("manhattan_edges: yes");
        writer.println("smanhattan_edges: yes");
        for(BasicBlock block : ControlFlowGraph.blocks) {
            printCFGNode(block);
        }
        writer.println("}");
        writer.close();
    }
    
    public void printDominantTree(){
        writer.println("graph: { title: \"Dominant Tree\"");
        writer.println("layoutalgorithm: dfs");
        writer.println("manhattan_edges: yes");
        writer.println("smanhattan_edges: yes");
        
        printDominantTreeUtil(DominatorTreeConstructor.dtRoot);
        
        writer.println("}");
        writer.close();
    }
    
    private void printDominantTreeUtil(DominatorTreeNode root){
        if(root == null)
            return;
        printDTNode(root);
        for(DominatorTreeNode child : root.children)
            printDominantTreeUtil(child);
    }
    
    private void printCFGNode(BasicBlock block) {
        writer.println("node: {");
        writer.println("title: \"" + block.getId() + "\"");
        writer.println("label: \"" + block.getId() + "[");
        for(Map.Entry<Integer, Instruction> entry : block.getPhiFuncManager().getPhiFuncs().entrySet()){
        	String var = Scanner.existIdents.get(entry.getKey());
        	Instruction instr = entry.getValue();
        	instr.setVar(var);
        	this.printInstruction(entry.getValue());
        }
        for(Instruction inst : block.getInstructions()) {
            this.printInstruction(inst);
        }
        writer.println("]\"");
        writer.println("}");
        
        if(block.getIfSuccessor() != null) {
            printEdge(block.getId(), block.getIfSuccessor().getId());
        }
        
        if(block.getElseSuccessor() != null) {
            printEdge(block.getId(), block.getElseSuccessor().getId());
        }
        
        if(block.getBackSuccessor() != null) {
            printEdge(block.getId(), block.getBackSuccessor().getId());
        }
        
        if(block.getJoinSuccessor() != null && block.getIfSuccessor() == null) {
            printEdge(block.getId(), block.getJoinSuccessor().getId());
        }
    }
    
    private void printDTNode(DominatorTreeNode node) {
        writer.println("node: {");
        writer.println("title: \"" + node.block.getId() + "\"");
        writer.println("label: \"" + node.block.getId() + "[");
        for(Map.Entry<Integer, Instruction> entry : node.block.getPhiFuncManager().getPhiFuncs().entrySet()){
            String var = Scanner.existIdents.get(entry.getKey());
            Instruction instr = entry.getValue();
            instr.setVar(var);
            this.printInstruction(entry.getValue());
        }
        for(Instruction inst : node.block.getInstructions()) {
            this.printInstruction(inst);
        }
        writer.println("]\"");
        writer.println("}");
        
        for(DominatorTreeNode child : node.children){
            printEdge(node.block.getId(), child.block.getId());
        }
    }
    
    public void printEdge(int sourceId, int targetId){
        writer.println("edge: { sourcename: \"" + sourceId + "\"");
        writer.println("targetname: \"" + targetId + "\"");
        writer.println("color: blue");
        writer.println("}");
    }
    
    public void printInstruction(Instruction instruction){
        writer.println(instruction);
    }
}
