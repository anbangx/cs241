package dragon.compiler.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import dragon.compiler.data.BasicBlock;
import dragon.compiler.data.Instruction;

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

	public static HashMap<Integer, BasicBlock> existedFunctions;

	public ControlFlowGraph() {
		blocks = new ArrayList<BasicBlock>();
		firstBlock = new BasicBlock(BasicBlock.Type.NONE);
		phiFuncUpdateType = new HashMap<BasicBlock.Type, PhiFuncManager.Update_Type>();
		phiFuncUpdateType.put(BasicBlock.Type.IF,
				PhiFuncManager.Update_Type.LEFT);
		phiFuncUpdateType.put(BasicBlock.Type.ELSE,
				PhiFuncManager.Update_Type.RIGHT);
		phiFuncUpdateType.put(BasicBlock.Type.DO,
				PhiFuncManager.Update_Type.RIGHT);

		existedFunctions = new HashMap<Integer, BasicBlock>();
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
			for (Map.Entry<Integer, Instruction> entry : block
					.getPhiFuncManager().getPhiFuncs().entrySet())
				System.out.println(entry.toString());
			for (Instruction i : block.getInstructions())
				System.out.println(i.toString());
			System.out.println("]");
		}
	}
}
