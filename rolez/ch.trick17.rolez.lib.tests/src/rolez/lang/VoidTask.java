package rolez.lang;

class VoidTask extends Task<Void> {
    private final Runnable runnable;
    
    public VoidTask(Runnable runnable) {
        super(new Object[]{}, new Object[]{});
        this.runnable = runnable;
    }
    
    @Override
    protected Void runRolez() {
        runnable.run();
        return null;
    }
}