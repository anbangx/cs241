package dragon.compiler.data;

import java.util.ArrayList;

public class Function {
    
    private int funcIdent;
    private ArrayList<Integer> localVariables;
    private ArrayList<Integer> globalVariables;
    
    public Function(int funcIdent){
        this.funcIdent= funcIdent;
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
