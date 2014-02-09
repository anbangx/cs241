package dragon.compiler.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dragon.compiler.parser.ControlFlowGraph;
import dragon.compiler.parser.PhiFuncManager;
import dragon.compiler.parser.VariableManager;

public class BasicBlock {
	public enum Type {
		IF, ELSE, DO, IF_JOIN, WHILE_JOIN, NONE;
	}

	private int id;
	private Type kind;
	private List<Instruction> instructions;
	private PhiFuncManager phiFuncManager;

	private BasicBlock directSuccessor; // used in if and while
	private BasicBlock elseSuccessor;
	// used in if-else statement for traversing CFG
	private BasicBlock joinSuccessor;

	// used in while statement
	private BasicBlock backSuccessor;

	public BasicBlock(Type kind) {
		this.id = ControlFlowGraph.blocks.size() + 1;
		this.kind = kind;
		ControlFlowGraph.blocks.add(this);
		instructions = new ArrayList<Instruction>();
		phiFuncManager = new PhiFuncManager();
	}

	public void createPhiFunction(int ident, BasicBlock curBlock)
			throws SyntaxFormatException {
		phiFuncManager.createPhiInstruction(ident,
				VariableManager.getLastVersionSSA(ident));
	}

	public void updatePhiFunction(int ident, SSA newSSA, Type blockType) {
		phiFuncManager.updatePhiInstruction(ident, newSSA,
				ControlFlowGraph.phiFuncUpdateType.get(blockType));
	}

	public void updateVarReferenceToPhi(int ident, int oldSSA, int newSSA,
			BasicBlock curBlock) throws SyntaxFormatException {
		if (kind != Type.WHILE_JOIN)
			throw new SyntaxFormatException(
					"updateVarReferenceToPhi can only be called in the WHOLE_JOIN!");
		// update cond statement
		Instruction cond = findCondInstruction(ident);
		if (cond.getResult1().address == ident)
			cond.getResult1().setSSA(newSSA);
		else
			cond.getResult2().setSSA(newSSA);

		// update loop body
		if (curBlock.kind != Type.DO)
			throw new SyntaxFormatException(
					"updateVarReferenceToPhi can only be called in the LOOP BODY!");
		for (Instruction i : curBlock.getInstructions()) {
			if (i.getResult1() != null && i.getResult1().isIdent(ident, oldSSA))
				i.getResult1().setSSA(newSSA);
			if (i.getResult2() != null && i.getResult2().isIdent(ident, oldSSA))
				i.getResult2().setSSA(newSSA);
		}
	}

	public SSA findLastVersionSSAFromJoinblock(int ident)
			throws SyntaxFormatException {
		if (kind != Type.IF_JOIN && kind != Type.WHILE_JOIN)
			throw new SyntaxFormatException(
					"findLastVersionSSAFromJoinblock is called only in the join block!");
		Instruction instr = phiFuncManager.getPhiFuncs().get(ident);
		return instr.getSsa2();
	}

	public void generateIntermediateCode(int op, Result result1, Result result2) {
		instructions.add(new Instruction(op, result1, result2));
	}

	public PhiFuncManager getPhiFuncManager() {
		return phiFuncManager;
	}

	public void setPhiFuncManager(PhiFuncManager phiFuncManager) {
		this.phiFuncManager = phiFuncManager;
	}

	public List<Instruction> getInstructions() {
		return instructions;
	}

	public void setInstructions(List<Instruction> instructions) {
		this.instructions = instructions;
	}

	public Type getKind() {
		return kind;
	}

	public void setKind(Type kind) {
		this.kind = kind;
	}

	public Instruction findInstruction(int index) {
		for (Instruction i : instructions) {
			if (i.getSelfPC() == index)
				return i;
		}
		return null;
	}

	public Instruction findCondInstruction(int ident) {
		for (Instruction i : instructions) {
			if (i.getOperator() == Instruction.cmp
					&& (i.getResult1().address == ident || i.getResult2().address == ident))
				return i;
		}
		return null;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("");
		for (Map.Entry<Integer, Instruction> entry : phiFuncManager
				.getPhiFuncs().entrySet())
			sb.append(entry.toString() + "\n");
		for (Instruction i : instructions) {
			sb.append(i + "\n");
		}
		return sb.toString();
	}

	public BasicBlock makeIfSuccessor() {
		BasicBlock ifSuccessor = new BasicBlock(Type.IF);
		this.directSuccessor = ifSuccessor;
		return ifSuccessor;
	}

	public BasicBlock makeElseSuccessor() {
		BasicBlock elseSuccessor = new BasicBlock(Type.ELSE);
		this.elseSuccessor = elseSuccessor;
		return elseSuccessor;
	}

	public BasicBlock makeDoSuccessor() {
		BasicBlock doSuccessor = new BasicBlock(Type.DO);
		this.directSuccessor = doSuccessor;
		return doSuccessor;
	}

	public BasicBlock makeInnerJoinSuccessor() {
		BasicBlock directSuccessor = new BasicBlock(Type.WHILE_JOIN);
		this.directSuccessor = directSuccessor;
		return directSuccessor;
	}

	public BasicBlock getIfSuccessor() {
		return directSuccessor;
	}

	public void setIfSuccessor(BasicBlock ifSuccessor) {
		this.directSuccessor = ifSuccessor;
	}

	public BasicBlock getElseSuccessor() {
		return elseSuccessor;
	}

	public void setElseSuccessor(BasicBlock elseSuccessor) {
		this.elseSuccessor = elseSuccessor;
	}

	public BasicBlock getJoinSuccessor() {
		return joinSuccessor;
	}

	public void setJoinSuccessor(BasicBlock joinSuccessor) {
		this.joinSuccessor = joinSuccessor;
	}

	public BasicBlock getBackSuccessor() {
		return backSuccessor;
	}

	public void setBackSuccessor(BasicBlock backSuccessor) {
		this.backSuccessor = backSuccessor;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public HashMap<Integer, Instruction> getPhiFuncs() {
		return phiFuncManager.getPhiFuncs();
	}

}
