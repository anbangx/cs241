package dragon.compiler.parser;

import java.util.HashMap;

import dragon.compiler.data.Instruction;
import dragon.compiler.data.SSA;

public class PhiFuncManager {

	public enum Update_Type{
		LEFT, RIGHT;
	}
	// <ident, instruction>
	private HashMap<Integer, Instruction> phiFuncs;

	public PhiFuncManager() {
		phiFuncs = new HashMap<Integer, Instruction>();
	}
	
	public Instruction createPhiInstruction(int ident, SSA lastSSA){
		Instruction instr = null;
		if(phiFuncs.containsKey(ident)){
			instr = phiFuncs.get(ident);
		} else{
			// create a new instruction
			instr = new Instruction(Instruction.phi, lastSSA, lastSSA);
			phiFuncs.put(ident, instr);
			
			ControlFlowGraph.updateXPreDefUseChains(lastSSA, instr);
			ControlFlowGraph.updateYPreDefUseChains(lastSSA, instr);
		}
		return instr;
	}
	
	public void updatePhiInstruction(int ident, SSA newSSA, Update_Type updateType){
		Instruction instr = phiFuncs.get(ident);
		if(updateType == Update_Type.LEFT){
			instr.setLeftLatestUpdated(true);
			instr.setSsa1(newSSA);
			ControlFlowGraph.updateXPreDefUseChains(newSSA, instr);
		}
		else{
			instr.setLeftLatestUpdated(false);
			instr.setSsa2(newSSA);
			ControlFlowGraph.updateYPreDefUseChains(newSSA, instr);
		}
	}

	public HashMap<Integer, Instruction> getPhiFuncs() {
		return phiFuncs;
	}

	public void setPhiFuncs(HashMap<Integer, Instruction> phiFuncs) {
		this.phiFuncs = phiFuncs;
	}
	
}
