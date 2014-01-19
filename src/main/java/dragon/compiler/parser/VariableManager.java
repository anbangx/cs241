package dragon.compiler.parser;

import java.util.HashSet;

public class VariableManager {
    
    private static HashSet<Integer> globalVariables;
    
    public VariableManager(){
        globalVariables = new HashSet<Integer>();
    }
    
    public static void addGlobalVariable(int globalIdent){
        getGlobalVariables().add(globalIdent);
    }

    public static HashSet<Integer> getGlobalVariables() {
        return globalVariables;
    }

}
