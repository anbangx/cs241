package dragon.compiler.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
        ArrayList<SSA> ssaList = getSsaMap().get(id);
        return ssaList.get(ssaList.size() - 1);
    }
    
    public static void addSSA(int id, int ssa){
    	addSSA(id, new SSA(ssa));
    }
    
    public static void addSSA(int id, SSA ssa){
        ArrayList<SSA> ssaList = null;
        if(getSsaMap().containsKey(id)){
            ssaList = getSsaMap().get(id);
        } else{
            ssaList = new ArrayList<SSA>();
            getSsaMap().put(id, ssaList);
        }
        ssaList.add(ssa);
    }

    public static HashMap<Integer, ArrayList<SSA>> getSsaMap() {
        return ssaMap;
    }
    
    public static HashMap<Integer, ArrayList<SSA>> deepCopySSAMap(){
        HashMap<Integer, ArrayList<SSA>> newSSAMap = new HashMap<Integer, ArrayList<SSA>>();
        for(Map.Entry<Integer, ArrayList<SSA>> entry : ssaMap.entrySet()){
            newSSAMap.put(entry.getKey(), new ArrayList<SSA>(entry.getValue()));
        }
        return newSSAMap;
    }

    public static void setSsaMap(HashMap<Integer, ArrayList<SSA>> ssaMap) {
        VariableManager.ssaMap = ssaMap;
    }

}
