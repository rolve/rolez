package rolez.lang;

public class VoidTask extends Task<Void> {
    
    private final Runnable runnable;
    
    public VoidTask(Runnable runnable) {
        this.runnable = runnable;
    }
    
    @Override
    protected Void runRolez() {
        runnable.run();
        return null;
    }
}
