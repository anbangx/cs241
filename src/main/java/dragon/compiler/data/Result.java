package dragon.compiler.data;

import dragon.compiler.scanner.Scanner;

public class Result {
    public enum Type{
        unknown, constant, var, reg, condition, branch
    }
//    public static final int unknown = 0;
//    public static final int constant = 1;
//    public static final int var = 2;//variable
//    public static final int reg = 3;
//    public static final int condition = 4;//temporary variable to store the result of comparison
    
    public Type kind; // const, var, reg, cond
    public int value; // value if it is a constant  
    public int address; // address if it is a variable
    public SSA ssa; // ssa version if it is a variable
    public int regno; // register number if it is a reg 
    public int fixuplocation;
    public Token cc;
//    public BasicBlock bb; // the target block of the branch
    public int targetLine;
    
    public Result(){
    }
    
    public void set(Type type, int input){
        switch(type){
            case constant:
                this.kind = Type.constant;
                this.value = input;
                break;
            case var:
                this.kind = Type.var;
                this.address = input;
                break;
            case reg:
                this.kind = Type.reg;
                this.regno = input;
                break;
            default:
                break;
        }
    }
    
    public void setSSA(int version){
        this.ssa = new SSA(version);
    }
    
    // value if it is a constant; address if it is a variable; register number if it is a reg 
    public int getInfo(){
        switch(kind){
            case constant:
                return value;
            case var:
                return address;
            case reg:
                return regno;
            default:
                return -1;
        }
    }
    
    public static Result makeConstant(int value){
        Result x = new Result();
        x.set(Result.Type.constant, value);
        return x;
    }
    
    public static Result makeBranch(int targetLine){
        Result x = new Result();
        x.kind = Result.Type.branch;
        x.targetLine = targetLine;
        return x;
    }
    
    public int getTargetLine() {
        return targetLine;
    }

    public void setTargetLine(int targetLine) {
        this.targetLine = targetLine;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("");
        switch(kind){
            case constant:
                sb.append("#" + value);
                break;
            case var:
                sb.append(Scanner.existIdents.get(address) + "_" + ssa.getVersion());
                break;
            case reg:
                sb.append("r" + regno);
                break;
            case condition:
                sb.append(fixuplocation);
                break;
            case branch:
                sb.append(targetLine);
                break;
            default:
                return "";
        }
        return sb.toString();
    }
}
