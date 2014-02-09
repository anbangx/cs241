package dragon.compiler.data;

import java.util.HashMap;

public class PhiFuncManager {

	// <ident, instruction>
	private HashMap<Integer, Instruction> phiFuncs;

	public PhiFuncManager() {
		phiFuncs = new HashMap<Integer, Instruction>();
	}
	
	public Instruction createPhiInstruction(int ident, SSA oldSSA){
		Instruction ans = null;
		if(phiFuncs.containsKey(ident)){
			ans = phiFuncs.get(ident);
		} else{
			// create a new instruction
			ans = new Instruction(Instruction.phi, oldSSA, oldSSA);
		}
		return ans;
	}
}
