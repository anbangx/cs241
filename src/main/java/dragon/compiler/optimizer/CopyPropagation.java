package dragon.compiler.optimizer;

import java.util.HashMap;

import dragon.compiler.data.DominatorTreeNode;
import dragon.compiler.data.Instruction;

public class CopyPropagation {
    
    private HashMap<Integer, Integer> originalNameToInstrId; // identId -> instr#
    
    public CopyPropagation(){
        originalNameToInstrId = new HashMap<Integer, Integer>();
    }
    
    public void optimize(DominatorTreeNode root){
        if(root == null)
            return;
        
        for(Instruction instr : root.block.getInstructions()){
            int instrId;
            if(instr.isReadAssignment()){
                instrId = instr.getSelfPC();
                int ident = instr.getIdent();
                originalNameToInstrId.put(ident, instrId);
            } else if(instr.isConstantAssignment()){
                instrId = instr.getSelfPC();
                int ident = instr.getRightIdent();
                originalNameToInstrId.put(ident, instrId);
            } else if(instr.isVariableAssignment()){
                int leftIdent = instr.getLeftIdent();
                int rightIdent = instr.getRightIdent();
                instrId = originalNameToInstrId.get(leftIdent);
                originalNameToInstrId.put(rightIdent, instrId);
                // mark instr as deleted
                instr.deleted = true;
                instr.targetInstrId = instrId;
            }
        }
        for(DominatorTreeNode child : root.children){
            optimize(child);
        }
    }
}
