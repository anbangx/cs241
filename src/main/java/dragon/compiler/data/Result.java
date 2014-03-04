package dragon.compiler.data;

import dragon.compiler.scanner.Scanner;

public class Result implements Comparable<Result>{
    public enum Type{
        unknown, constant, var, reg, condition, branch, instr
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
    public BasicBlock targetBlock; // the target block of the branch
    public int instrId;
    public boolean onlyMove = true;
    
    public Result(){
    }
    
    public Result(Result result){
    	if(result != null){
	    	this.kind = result.kind;
	    	this.value = result.value;
	    	this.address = result.address;
	    	this.ssa = result.ssa;
	    	this.regno = result.regno;
	    	this.fixuplocation = result.fixuplocation;
	    	this.cc = result.cc;
	    	this.targetBlock = result.targetBlock;
	    	this.instrId = result.instrId;
	    	this.onlyMove = result.onlyMove;
    	}
    }
    
    public Result(int ident, Instruction instr, boolean left){
        this.kind = Result.Type.var;
        if(left){
            this.address = ident; 
            this.ssa = instr.getSsa1();
        }else{
            this.address = ident;
            this.ssa = instr.getSsa2();
        }
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
            case instr:
            	this.kind = Type.instr;
            	this.instrId = input;
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
    
    public static Result makeBranch(BasicBlock targetBlock){
        Result x = new Result();
        x.kind = Result.Type.branch;
        x.targetBlock = targetBlock;
        return x;
    }
    
    
    public String toString(){
        StringBuilder sb = new StringBuilder("");
        switch(kind){
            case constant:
                sb.append(value);
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
                sb.append(targetBlock != null ? "[" + targetBlock.getId() + "]": "-1");
                break;
            case instr:
            	sb.append("(" + instrId + ")");
            	break;
            default:
                return "";
        }
        return sb.toString();
    }
    
    public boolean isIdent(int ident, int oldSSA){
    	return this.kind == Type.var && this.address == ident && this.ssa.getVersion() == oldSSA;  
    }

    public boolean isVariable(){
        return this.kind == Type.var;  
    }
    
    @Override
    public int compareTo(Result other) {
        if(this.kind != Type.var || other.kind != Type.var)
            try {
                throw new Exception("Only can compare var result!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        if(this.address == other.address && this.ssa == other.ssa)
            return 0;
        return -1;
    }
    
    @Override
    public int hashCode() {
        return this.address * 17 + this.ssa.hashCode() * 31;
    }
    
    public boolean equals(Object other){
        Result other2 = (Result)other;
        if(this.kind != Type.var || other2.kind != Type.var)
            try {
                throw new Exception("Only can compare var result!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        if(this.address == other2.address && this.ssa == other2.ssa)
            return true;
        return false;
    }

}
