package dragon.compiler.data;

import java.util.ArrayList;

public class Function {
    
    private int funcIdent;
    private BasicBlock firstFuncBlock;
    private ArrayList<Integer> localVariables;
    private ArrayList<Integer> globalVariables;
    private ArrayList<Result> parameters;
    
    private Result returnInstr;
    
    public Function(int funcIdent){
        this.funcIdent= funcIdent;
        firstFuncBlock = new BasicBlock(BasicBlock.Type.NONE);
        localVariables = new ArrayList<Integer>();
        globalVariables = new ArrayList<Integer>();
        parameters = new ArrayList<Result>();
        returnInstr = new Result();
    }
    
    public void addLocalVariable(int localVariable){
        getLocalVariables().add(localVariable);
    }
    
    public void addGlobalVariable(int globalVariable){
        getGlobalVariables().add(globalVariable);
    }
    
    public int getFuncIdent() {
        return funcIdent;
    }

    public void setFuncIdent(int funcIdent) {
        this.funcIdent = funcIdent;
    }

    public BasicBlock getFirstFuncBlock() {
		return firstFuncBlock;
	}

	public void setFirstFuncBlock(BasicBlock firstFuncBlock) {
		this.firstFuncBlock = firstFuncBlock;
	}

	public ArrayList<Integer> getLocalVariables() {
        return localVariables;
    }

    public void setLocalVariables(ArrayList<Integer> localVariables) {
        this.localVariables = localVariables;
    }

    public ArrayList<Integer> getGlobalVariables() {
        return globalVariables;
    }

    public void setGlobalVariables(ArrayList<Integer> globalVariables) {
        this.globalVariables = globalVariables;
    }

	public ArrayList<Result> getParameters() {
		return parameters;
	}

	public void setParameters(ArrayList<Result> parameters) {
		this.parameters = parameters;
	}

	public Result getReturnInstr() {
		return returnInstr;
	}

	public void setReturnInstr(Result returnInstr) {
		this.returnInstr = returnInstr;
	}
    
}
