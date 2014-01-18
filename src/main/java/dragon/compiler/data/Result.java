package dragon.compiler.data;

public class Result {
    enum Type{
        constant,
        var,
        reg,
        condition
    }
    
    public Type kind; // const, var, reg, cond
    public int value; // value if it is a constant  
    public int address; // address if it is a variable
    public int regno; // register number if it is a reg 

}
