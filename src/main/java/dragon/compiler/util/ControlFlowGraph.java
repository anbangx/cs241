package dragon.compiler.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dragon.compiler.data.BasicBlock;
import dragon.compiler.data.Function;
import dragon.compiler.data.Instruction;
import dragon.compiler.data.Result;
import dragon.compiler.data.SSA;

public class ControlFlowGraph {
    // private BasicBlock firstBlock;
    //
    // public ControlFlowGraph(){
    // firstBlock = new BasicBlock();
    // }
    //
    // public BasicBlock getFirstBlock() {
    // return firstBlock;
    // }
    //
    // public void setFirstBlock(BasicBlock firstBlock) {
    // this.firstBlock = firstBlock;
    // }

    private static BasicBlock firstBlock;
    public static ArrayList<BasicBlock> blocks;
    public static HashMap<BasicBlock.Type, PhiFuncManager.Update_Type> phiFuncUpdateType;

    public static HashMap<Integer, Function> existedFunctions;

    public static HashMap<Instruction, ArrayList<Instruction>> xPreDefUseChains;
    public static HashMap<Instruction, ArrayList<Instruction>> yPreDefUseChains;

    public static ArrayList<Instruction> allInstructions;

    public ControlFlowGraph() {
        blocks = new ArrayList<BasicBlock>();
        firstBlock = new BasicBlock(BasicBlock.Type.NONE);
        phiFuncUpdateType = new HashMap<BasicBlock.Type, PhiFuncManager.Update_Type>();
        phiFuncUpdateType.put(BasicBlock.Type.IF, PhiFuncManager.Update_Type.LEFT);
        phiFuncUpdateType.put(BasicBlock.Type.ELSE, PhiFuncManager.Update_Type.RIGHT);
        phiFuncUpdateType.put(BasicBlock.Type.DO, PhiFuncManager.Update_Type.RIGHT);

        existedFunctions = new HashMap<Integer, Function>();

        xPreDefUseChains = new HashMap<Instruction, ArrayList<Instruction>>();
        yPreDefUseChains = new HashMap<Instruction, ArrayList<Instruction>>();
        allInstructions = new ArrayList<Instruction>();
    }

    public static BasicBlock getFirstBlock() {
        return firstBlock;
    }

    public ArrayList<BasicBlock> getBlocks() {
        return blocks;
    }

    public static Instruction findInstruction(int index) {
        for (BasicBlock block : blocks) {
            if (block.findInstruction(index) != null)
                return block.findInstruction(index);
        }
        return null;
    }

    public static void printIntermediateCode() {
        for (BasicBlock block : blocks) {
            System.out.println("Block_" + block.getId() + "[");
            for (Map.Entry<Integer, Instruction> entry : block.getPhiFuncManager().getPhiFuncs().entrySet())
                System.out.println(entry.toString());
            for (Instruction i : block.getInstructions())
                System.out.println(i.toString());
            System.out.println("]");
        }
    }

    public static void updateDefUseChain(Result left, Result right) {
        ArrayList<Instruction> useInstructions = null;
        Instruction curInstr = null;
        if (left.kind == Result.Type.var) {
            curInstr = allInstructions.get(left.instrId);
            Instruction leftLastUse = allInstructions.get(left.ssa.getVersion());
            if (xPreDefUseChains.containsKey(leftLastUse)) {
                useInstructions = xPreDefUseChains.get(leftLastUse);
            } else {
                useInstructions = new ArrayList<Instruction>();
                xPreDefUseChains.put(leftLastUse, useInstructions);
            }
            useInstructions.add(curInstr);
        }
        if (right.kind == Result.Type.var) {
            Instruction rightLastUse = allInstructions.get(right.ssa.getVersion());

            if (yPreDefUseChains.containsKey(rightLastUse)) {
                useInstructions = yPreDefUseChains.get(rightLastUse);
            } else {
                useInstructions = new ArrayList<Instruction>();
                yPreDefUseChains.put(rightLastUse, useInstructions);
            }
            useInstructions.add(curInstr);
        }
    }

    public static void updateXPreDefUseChains(SSA ssaDef, Instruction use) {
        updateXPreDefUseChains(ControlFlowGraph.allInstructions.get(ssaDef.getVersion()), use);
    }

    public static void updateYPreDefUseChains(SSA ssaDef, Instruction use) {
        updateYPreDefUseChains(ControlFlowGraph.allInstructions.get(ssaDef.getVersion()), use);
    }

    public static void updateXPreDefUseChains(Instruction def, Instruction use) {
        ArrayList<Instruction> useInstructions = null;
        if (xPreDefUseChains.containsKey(def)) {
            useInstructions = xPreDefUseChains.get(def);
        } else {
            useInstructions = new ArrayList<Instruction>();
            xPreDefUseChains.put(def, useInstructions);
        }
        useInstructions.add(use);
    }

    public static void updateYPreDefUseChains(Instruction def, Instruction use) {
        ArrayList<Instruction> useInstructions = null;
        if (yPreDefUseChains.containsKey(def)) {
            useInstructions = xPreDefUseChains.get(def);
        } else {
            useInstructions = new ArrayList<Instruction>();
            yPreDefUseChains.put(def, useInstructions);
        }
        useInstructions.add(use);
    }

}
