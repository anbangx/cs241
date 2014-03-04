package dragon.compiler.optimizer;

import java.util.HashMap;
import java.util.Map;

import dragon.compiler.data.DominatorTreeNode;
import dragon.compiler.data.Instruction;
import dragon.compiler.data.Result;

public class CopyPropagation {
    
    private HashMap<Result, Integer> originalNameToInstrId; // identId -> instr#
    
    public CopyPropagation(){
        originalNameToInstrId = new HashMap<Result, Integer>();
    }
    
    public void optimize(DominatorTreeNode root){
        if(root == null)
            return;
        
        for(Instruction instr : root.block.getInstructions()){
            int instrId;
            if(instr.isReadAssignment()){
                instrId = instr.getSelfPC();
                Result result = instr.getResult1();
                originalNameToInstrId.put(result, instrId);
            } else if(instr.isConstantAssignment()){
                instrId = instr.getSelfPC();
                Result result = instr.getResult2();
                originalNameToInstrId.put(result, instrId);
            } else if(instr.isInstructionAssignment()){
                instrId = instr.getResult1().instrId;
                Result result = instr.getResult2();
                originalNameToInstrId.put(result, instrId);
            } else if(instr.isVariableAssignment()){
                Result left = instr.getResult1();
                Result right = instr.getResult2();
                instrId = originalNameToInstrId.get(left);
                originalNameToInstrId.put(right, instrId);
                // mark instr as deleted
                instr.deleted = true;
                instr.targetInstrId = instrId;
            } else if(instr.isOtherAssignment()){
                Result left = instr.getResult1();
                Result right = instr.getResult2();
                if(left.isVariable()){
                    instrId = originalNameToInstrId.get(left);
                    // change result type to instr and assign instr#
                    left.kind = Result.Type.instr;
                    left.instrId = instrId;
                }
                if(right.isVariable()){
                    instrId = originalNameToInstrId.get(right);
                    // change result type to instr and assign instr#
                    right.kind = Result.Type.instr;
                    right.instrId = instrId;
                }
            }
        }
        
        for(Map.Entry<Integer, Instruction> entry : root.block.getPhiFuncs().entrySet()){
            Instruction oldInstr = entry.getValue();
            Result left = new Result(entry.getKey(), oldInstr, true);
            Result right = new Result(entry.getKey(), oldInstr, false);
            int instrId;
            if(originalNameToInstrId.containsKey(left)){
                instrId = originalNameToInstrId.get(left);
                // change result type to instr and assign instr#
                left.kind = Result.Type.instr;
                left.instrId = instrId;
                oldInstr.leftRepresentedByInstrId = true;
                oldInstr.setResult1(left);
            }
            
            if(originalNameToInstrId.containsKey(right)){
                instrId = originalNameToInstrId.get(right);
                // change result type to instr and assign instr#
                right.kind = Result.Type.instr;
                right.instrId = instrId;
                oldInstr.rightRepresentedByInstrId = true;
                oldInstr.setResult2(right);
            }
        }
        
        for(DominatorTreeNode child : root.children){
            optimize(child);
        }
    }
}
