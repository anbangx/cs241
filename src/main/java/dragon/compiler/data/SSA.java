package dragon.compiler.data;

public class SSA {
    
    private int version;
    
    public SSA(int version){
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    
}
