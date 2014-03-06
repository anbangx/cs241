package dragon.compiler.data;

public class Expression {
    
    public Result operand1;
    public Result operand2;
    
    public Expression(){
        
    }
    
    public Expression(Result operand1, Result operand2){
        this.operand1 = operand1;
        this.operand2 = operand2;
    }
}
