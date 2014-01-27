package dragon.compiler.parser;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import dragon.compiler.data.BasicBlock;
import dragon.compiler.data.Instruction;


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
            printNode(block);
        }
        writer.println("}");
        writer.close();
    }
    
    private void printNode(BasicBlock block) {
        writer.println("node: {");
        writer.println("title: \"" + block.getId() + "\"");
        writer.println("label: \"" + block.getId() + "[");
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
