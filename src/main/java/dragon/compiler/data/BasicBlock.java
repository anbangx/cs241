package dragon.compiler.data;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {

    private List<Instruction> instructions;

    public BasicBlock() {
        instructions = new ArrayList<Instruction>();
    }

    public void generateIntermediateCode(int op, Result result1, Result result2) {
        instructions.add(new Instruction(op, result1, result2));
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

}
