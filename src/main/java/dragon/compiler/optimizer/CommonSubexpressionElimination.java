package dragon.compiler.optimizer;

import java.util.HashMap;

import dragon.compiler.data.DominatorTreeNode;
import dragon.compiler.data.Expression;
import dragon.compiler.data.Instruction;

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
        if (root == null)
            return;

        for (Instruction instr : root.block.getInstructions()) {
            if (instr.isExpressionOp()) {
                int opcode = instr.getExpressionOp();
                HashMap<Expression, Integer> exp2Id = hM.get(opcode);
                Expression exp = new Expression(instr.getResult1(), instr.getResult2());
                if (!exp2Id.containsKey(exp)) {
                    exp2Id.put(exp, instr.getSelfPC());
                } else {
                    int originalId = exp2Id.get(exp);
                    // mark instr as deleted
                    instr.deleted = true;
                    instr.targetInstrId = originalId;
                }
            }
        }

        for (DominatorTreeNode child : root.children) {
            optimize(child);
        }
    }
}
