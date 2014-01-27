package dragon.compiler.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import dragon.compiler.data.SSA;

public class VariableManager {
    
    private static HashSet<Integer> globalVariables = new HashSet<Integer>();
    private static HashMap<Integer, ArrayList<SSA>> ssaMap = new HashMap<Integer, ArrayList<SSA>>();
    
    public static void addGlobalVariable(int globalIdent){
        getGlobalVariables().add(globalIdent);
    }

    public static HashSet<Integer> getGlobalVariables() {
        return globalVariables;
    }
    
    public static SSA getLastVersionSSA(int id){
        ArrayList<SSA> ssaList = ssaMap.get(id);
        return ssaList.get(ssaList.size() - 1);
    }
    
    public static void addSSA(int id, SSA ssa){
        ArrayList<SSA> ssaList = null;
        if(ssaMap.containsKey(id)){
            ssaList = ssaMap.get(id);
        } else{
            ssaList = new ArrayList<SSA>();
            ssaMap.put(id, ssaList);
        }
        ssaList.add(ssa);
    }

}
