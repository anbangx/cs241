package dragon.compiler.generator;

import java.util.HashMap;

import dragon.compiler.data.BasicBlock;
import dragon.compiler.data.Function;
import dragon.compiler.data.Instruction;
import dragon.compiler.data.Result;
import dragon.compiler.data.Token;
import dragon.compiler.optimizer.RegisterAllocator;
import dragon.compiler.util.ControlFlowGraph;
import dragon.compiler.util.VariableManager;

public class IntermediateCodeGenerator {

	public final int InputNum = 0;
	public final int OutputNum = 1;
	public final int OutputNewLine = 2;

	private RegisterAllocator registerAllocator;
	private HashMap<Token, Integer> arithmeticOpCode;
	private HashMap<Token, Integer> negatedBranchOpCode;
	private HashMap<Integer, Integer> basicIoOpCode;

	public IntermediateCodeGenerator() {
		this.registerAllocator = new RegisterAllocator();
		this.arithmeticOpCode = new HashMap<Token, Integer>();
		this.negatedBranchOpCode = new HashMap<Token, Integer>();
		this.basicIoOpCode = new HashMap<Integer, Integer>();

		arithmeticOpCode.put(Token.PLUS, Instruction.add);
		arithmeticOpCode.put(Token.MINUS, Instruction.sub);
		arithmeticOpCode.put(Token.TIMES, Instruction.mul);
		arithmeticOpCode.put(Token.DIVIDE, Instruction.div);

		negatedBranchOpCode.put(Token.EQL, Instruction.bne);
		negatedBranchOpCode.put(Token.NEQ, Instruction.beq);
		negatedBranchOpCode.put(Token.LSS, Instruction.bge);
		negatedBranchOpCode.put(Token.LEQ, Instruction.bgt);
		negatedBranchOpCode.put(Token.GRE, Instruction.ble);
		negatedBranchOpCode.put(Token.GEQ, Instruction.blt);

		basicIoOpCode.put(InputNum, Instruction.read);
		basicIoOpCode.put(OutputNum, Instruction.write);
		basicIoOpCode.put(OutputNewLine, Instruction.wln);
	}

	public void load(Result x) {
		if (x.kind == Result.Type.constant) {
			x.kind = Result.Type.reg;
			if (x.value == 0)
				x.regno = 0;
			else {
				x.regno = registerAllocator.allocateReg();
				// TODO put into assembler format, but not addi
				// generateIntermediateCode(Instruction);
			}
		} else if (x.kind == Result.Type.var) {
			x.kind = Result.Type.reg;
			x.regno = registerAllocator.allocateReg();
			// generateIntermediateCode(Instruction.load, x.regno, );
		}
	}

	public void computeArithmeticOp(BasicBlock curBlock, Token op, Result x,
			Result y) {
		if (x.kind == Result.Type.constant && y.kind == Result.Type.constant) {
			switch (arithmeticOpCode.get(op)) {
			case Instruction.cmp:
				break;
			case Instruction.add:
				x.value += y.value;
				break;
			case Instruction.sub:
				x.value -= y.value;
				break;
			case Instruction.mul:
				x.value *= y.value;
				break;
			case Instruction.div:
				x.value /= y.value;
				break;
			}
		} else {
			// load(x);
			if (y.kind == Result.Type.constant) {
				curBlock.generateIntermediateCode(arithmeticOpCode.get(op), x,
						y);
			} else {
				// load(y);
				curBlock.generateIntermediateCode(arithmeticOpCode.get(op), x,
						y);
				registerAllocator.deAllocate(y.regno);
			}
		}
	}

	public void computeCmpOp(BasicBlock curBlock, int opCode, Result left,
			Result right) {
		curBlock.generateIntermediateCode(opCode, left, right);
	}

	public Instruction generateBasicIoOp(BasicBlock curBlock, int ioOp, Result x) {
		return curBlock.generateIntermediateCode(basicIoOpCode.get(ioOp), x, null);
	}

	public void declareVariable(BasicBlock curBlock, Result x, Function function)
			throws Throwable {
		if (x.kind != Result.Type.var)
			throw new Exception("The type of x should be var!");
		int varIndent = x.address;
		x.setSSA(Instruction.getPC());
		VariableManager.addSSA(x.address, x.ssa);

		if (function != null) {
			// add ident to local variable of the function
			function.getLocalVariables().add(varIndent);
		} else {
			// add ident to global variable
			VariableManager.addGlobalVariable(varIndent);
		}

		Result defaultConstant = Result.makeConstant(0);
		curBlock.generateIntermediateCode(Instruction.move, defaultConstant, x);
	}

	public Function declareFunction(Result x) throws Exception {
		if (x.kind != Result.Type.var)
			throw new Exception("The type of x should be var!");
		int funcIdent = x.address;
		if (!ControlFlowGraph.existedFunctions.containsKey(funcIdent)) {
			Function func = new Function(funcIdent);
			ControlFlowGraph.existedFunctions.put(funcIdent, func);
			return func;
		} else {
			System.out.println("Function name duplicates!");
			return null;
		}
	}

	public void assign(BasicBlock curBlock, BasicBlock joinBlock,
			Result variable, Result assignedValue) throws Throwable {
		if (variable.kind == Result.Type.constant) {
			throw new Exception("left Result cannot be constant");
		}
		variable.setSSA(Instruction.getPC());
		VariableManager.addSSA(variable.address, variable.ssa);
		curBlock.generateIntermediateCode(Instruction.move, assignedValue,
				variable);

		// update phi function
		if (joinBlock != null) {
			joinBlock.updatePhiFunction(variable.address, variable.ssa,
					curBlock.getKind());
		}
		
		// update def-use chain
		
	}
	
	public void generateReturnOp(BasicBlock curBlock, Result x, Function function){
		Result curInstr = new Result();
		curInstr.set(Result.Type.instr, Instruction.getPC());
		curBlock.generateIntermediateCode(Instruction.move, x, curInstr);
		// connect function with return Instruction#
		function.setReturnInstr(curInstr);
	}
	
	public void generateAssignParameterOp(BasicBlock curBlock, Result value, Result parameter){
		curBlock.generateIntermediateCode(Instruction.move, value, parameter);
	}

	public void condNegBraFwd(BasicBlock curBlock, Result x) {
		x.fixuplocation = Instruction.getPC();
		curBlock.generateIntermediateCode(negatedBranchOpCode.get(x.cc), x,
				Result.makeBranch(null));
	}

	public void unCondBraFwd(BasicBlock curBlock, Result follow) {
		Result branch = Result.makeBranch(null);
		branch.fixuplocation = follow.fixuplocation;
		curBlock.generateIntermediateCode(Instruction.bra, null, branch);
		follow.fixuplocation = Instruction.getPC() - 1;
	}

	public void fixup(int loc, BasicBlock targetBlock) {
		ControlFlowGraph.findInstruction(loc).getResult2().targetBlock = targetBlock;
	}

	public void fixAll(int loc, BasicBlock joinBlock) {
		while (loc != 0) {
			int next = ControlFlowGraph.findInstruction(loc).getResult2().fixuplocation;
			fixup(loc, joinBlock);
			loc = next;
		}
	}

}
