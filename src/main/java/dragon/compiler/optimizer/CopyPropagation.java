package dragon.compiler.optimizer;

import java.util.HashMap;
import java.util.Map;

import dragon.compiler.data.DominatorTreeNode;
import dragon.compiler.data.Instruction;
import dragon.compiler.data.Result;

public class CopyPropagation {
    
//    private HashMap<Result, Integer> originalNameToInstrId; // identId -> instr#
//    private HashMap<Result, Integer> originalNameToConstant; // identId -> constant
    
    public CopyPropagation(){
    }
    
    public void optimize(DominatorTreeNode root){
        optimizeUtil(root, new HashMap<Result, Integer>(), new HashMap<Result, Integer>());
    }
    
    public void optimizeUtil(DominatorTreeNode root, HashMap<Result, Integer> originalNameToInstrId,
            HashMap<Result, Integer> originalNameToConstant){
        if(root == null)
            return;
        
        for(Instruction instr : root.block.getInstructions()){
            int instrId;
            if(instr.isReadAssignment()){
                instrId = instr.getSelfPC();
                Instruction next = root.block.getNextInstruction(instr); 
                Result readY = new Result(); instr.setResult1(readY);
                readY.kind = next.getResult2().kind;
                readY.address = next.getResult2().address;
                readY.ssa = next.getResult2().ssa;
                Result y = next.getResult2();
                originalNameToInstrId.put(y, instrId);
                // mark next instr as deleted
                next.deleted = true;
            } else if(instr.isConstantAssignment()){
                int constant = instr.getResult1().value;
                Result result = instr.getResult2();
                originalNameToConstant.put(result, constant);
                // mark constant assignment as deleted
                instr.deleted = true;
            } else if(instr.isInstructionAssignment()){
                instrId = instr.getResult1().instrId;
                Result result = instr.getResult2();
                originalNameToInstrId.put(result, instrId);
                // mark instruction assignment as deleted
                instr.deleted = true;
            } else if(instr.isVariableAssignment()){
                Result left = instr.getResult1();
                Result right = instr.getResult2();
                if(originalNameToConstant.containsKey(left)){
                    int constant = originalNameToConstant.get(left);
                    originalNameToConstant.put(right, constant);
                    //mark instr as deleted
                    instr.deleted = true;
                }else{
                    instrId = originalNameToInstrId.get(left);
                    originalNameToInstrId.put(right, instrId);
                    // mark instr as deleted
                    instr.deleted = true;
                    instr.targetInstrId = instrId;
                }
            } else{ // if(instr.isOtherAssignment())
                Result left = instr.getResult1();
                Result right = instr.getResult2();
                if(left != null && left.isVariable()){
                    if(originalNameToConstant.containsKey(left)){
                        int constant = originalNameToConstant.get(left);
                        // put constant in result
                        left.kind = Result.Type.constant;
                        left.value = constant;
                    }else if(originalNameToInstrId.containsKey(left)){
                        instrId = originalNameToInstrId.get(left);
                        // change result type to instr and assign instr#
                        left.kind = Result.Type.instr;
                        left.instrId = instrId;
                    }
                }
                if(right != null && right.isVariable()){
                    if(originalNameToConstant.containsKey(right)){
                        int constant = originalNameToConstant.get(right);
                        // put constant in result
                        right.kind = Result.Type.constant;
                        right.value = constant;
                    }else if(originalNameToInstrId.containsKey(right)){
                        instrId = originalNameToInstrId.get(right);
                        // change result type to instr and assign instr#
                        right.kind = Result.Type.instr;
                        right.instrId = instrId;
                    }
                }
            }
        }
        
        for(Map.Entry<Integer, Instruction> entry : root.block.getPhiFuncs().entrySet()){
            Instruction oldInstr = entry.getValue();
            Result left = new Result(entry.getKey(), oldInstr, true);
            Result right = new Result(entry.getKey(), oldInstr, false);
            int instrId;
            if(originalNameToConstant.containsKey(left)){
                int constant = originalNameToConstant.get(left);
                // put constant in result
                left.kind = Result.Type.constant;
                left.value = constant;
                oldInstr.setResult1(left);
            }else if(originalNameToInstrId.containsKey(left)){
                instrId = originalNameToInstrId.get(left);
                // change result type to instr and assign instr#
                left.kind = Result.Type.instr;
                left.instrId = instrId;
                oldInstr.leftRepresentedByInstrId = true;
                oldInstr.setResult1(left);
            }
            
            if(originalNameToConstant.containsKey(right)){
                int constant = originalNameToConstant.get(right);
                // put constant in result
                right.kind = Result.Type.constant;
                right.value = constant;
                oldInstr.setResult2(right);
            }else if(originalNameToInstrId.containsKey(right)){
                instrId = originalNameToInstrId.get(right);
                // change result type to instr and assign instr#
                right.kind = Result.Type.instr;
                right.instrId = instrId;
                oldInstr.rightRepresentedByInstrId = true;
                oldInstr.setResult2(right);
            }
        }
        
        for(DominatorTreeNode child : root.children){
            optimizeUtil(child, new HashMap<Result, Integer>(originalNameToInstrId),
                    new HashMap<Result, Integer>(originalNameToConstant));
        }
    }
}
