package dragon.compiler.data;

import java.util.ArrayList;

public class Function {
    
    private int funcIdent;
    private BasicBlock funcBlock;
    private ArrayList<Integer> localVariables;
    private ArrayList<Integer> globalVariables;
    
    public Function(int funcIdent){
        this.funcIdent= funcIdent;
        funcBlock = new BasicBlock(BasicBlock.Type.NONE);
        localVariables = new ArrayList<Integer>();
        globalVariables = new ArrayList<Integer>();
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
    
    public BasicBlock getFuncBlock() {
        return funcBlock;
    }

    public void setFuncBlock(BasicBlock funcBlock) {
        this.funcBlock = funcBlock;
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
    
}
