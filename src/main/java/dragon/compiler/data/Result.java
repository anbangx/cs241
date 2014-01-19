package dragon.compiler.data;

public class Result {
    public enum Type{
        unknown, constant, var, reg, condition
    }
//    public static final int unknown = 0;
//    public static final int constant = 1;
//    public static final int var = 2;//variable
//    public static final int reg = 3;
//    public static final int condition = 4;//temporary variable to store the result of comparison
    
    public Type kind; // const, var, reg, cond
    public int value; // value if it is a constant  
    public int address; // address if it is a variable
    public int regno; // register number if it is a reg 
    
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
    
    public String toString(){
        StringBuilder sb = new StringBuilder("");
        switch(kind){
            case constant:
                sb.append("const: " + value);
                break;
            case var:
                sb.append("var: " + address);
                break;
            case reg:
                sb.append("reg: " + regno);
                break;
            default:
                return "";
        }
        return sb.toString();
    }
}
