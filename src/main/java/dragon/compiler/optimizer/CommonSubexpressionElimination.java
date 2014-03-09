package dragon.compiler.optimizer;

import java.util.HashMap;
import java.util.Map;

import dragon.compiler.data.DominatorTreeNode;
import dragon.compiler.data.Expression;
import dragon.compiler.data.Instruction;
import dragon.compiler.data.Result;

public class CommonSubexpressionElimination {

    // {opcode : {exp, instrId}}
    private HashMap<Integer, HashMap<Expression, Integer>> hM;

    public CommonSubexpressionElimination() {
        hM = new HashMap<Integer, HashMap<Expression, Integer>>();
        for (int i = Instruction.neg; i <= Instruction.div; i++) {
            hM.put(i, new HashMap<Expression, Integer>());
        }
    }

    public void optimize(DominatorTreeNode root) {
        optimizeUtil(root, new HashMap<Integer, Integer>());
    }
    
    public void optimizeUtil(DominatorTreeNode root, HashMap<Integer, Integer> replaceInstr) {
        if (root == null)
            return;

        for (Instruction instr : root.block.getInstructions()) {
            if(instr.deleted)
                continue;
            // replace instrId
            Result left = instr.getResult1();
            Result right = instr.getResult2();
            if(left != null && left.kind == Result.Type.instr && replaceInstr.containsKey(left.instrId)){
                left.instrId = replaceInstr.get(left.instrId);
            }
            if(right != null && right.kind == Result.Type.instr && replaceInstr.containsKey(right.instrId)){
                right.instrId = replaceInstr.get(right.instrId);
            }
            
            if (instr.isExpressionOp()) {
                int opcode = instr.getExpressionOp();
                HashMap<Expression, Integer> exp2Id = hM.get(opcode);
                Expression exp = new Expression(instr.getResult1(), instr.getResult2());
                if (!exp2Id.containsKey(exp)) {
                    exp2Id.put(exp, instr.getSelfPC());
                    // mark next instr as replace instr#
                    Instruction next = root.block.getNextInstruction(instr);
                    if(!next.deleted && next.getExpressionOp() == Instruction.move)
                        replaceNextInstructionId(root.block.getNextInstruction(instr), instr.getSelfPC());
                } else {
                    int instrId = exp2Id.get(exp);
                    // mark instr as deleted
                    instr.deleted = true;
                    replaceNextInstructionId(root.block.getNextInstruction(instr), instrId);
                    // add replaced instrId to map
                    replaceInstr.put(instr.getSelfPC(), instrId);
                }
            }
        }
        
        for(Map.Entry<Integer, Instruction> entry : root.block.getPhiFuncs().entrySet()){
            Instruction instr = entry.getValue();
            Result left = instr.getResult1();
            Result right = instr.getResult2();
            if(left != null && left.kind == Result.Type.instr && replaceInstr.containsKey(left.instrId)){
                left.instrId = replaceInstr.get(left.instrId);
            }
            if(right != null && right.kind == Result.Type.instr && replaceInstr.containsKey(right.instrId)){
                right.instrId = replaceInstr.get(right.instrId);
            }
        }

        for (DominatorTreeNode child : root.children) {
            optimizeUtil(child, new HashMap<Integer, Integer>(replaceInstr));
        }
    }
    
    public void replaceNextInstructionId(Instruction nextInstruction, int replaceId){
        nextInstruction.kind = Instruction.Type.REPLACE;
        nextInstruction.targetInstrId = replaceId;
    }
}
