package dragon.compiler.data;

public class Expression {
    
    public Result operand1;
    public Result operand2;
    
    public Expression(Result operand1, Result operand2){
        this.operand1 = operand1;
        this.operand2 = operand2;
    }
    
    public String toString(){
        return operand1.address + "_" + operand1.ssa.getVersion() + " "
                + operand2.address + "_" + operand2.ssa.getVersion();
    }
    
    @Override
    public int hashCode() {
        int hashcode1 = operand1.kind == Result.Type.var ? 
                operand1.address * 17 + operand1.ssa.hashCode() * 31 : operand1.value * 61;
        int hashcode2 = operand2.kind == Result.Type.var ? 
                operand2.address * 43 + operand2.ssa.hashCode() * 59 : operand1.value * 61;
        return hashcode1 + hashcode2;
    }
    
    public boolean equals(Object other){
        Expression other2 = (Expression)other;
        if(this.operand1.kind != other2.operand1.kind
                || this.operand2.kind != other2.operand2.kind)
            return false;
        if(operand1.kind == Result.Type.var){
            if(this.operand1.address != other2.operand1.address 
                    || !this.operand1.ssa.equals(other2.operand1.ssa))
                    return false;
        }else if(operand1.kind == Result.Type.instr){
            if(this.operand1.instrId != other2.operand1.instrId)
                return false;
        }else{
            if(this.operand1.value != other2.operand1.value)
                return false;
        }
        if(operand2.kind == Result.Type.var){
            if(this.operand2.address != other2.operand2.address 
                    || !this.operand2.ssa.equals(other2.operand2.ssa))
                    return false;
        }else if(operand2.kind == Result.Type.instr){
            if(this.operand2.instrId != other2.operand2.instrId)
                return false;
        }else{
            if(this.operand2.value != other2.operand2.value)
                return false;
        }
        return true;
    }
}
