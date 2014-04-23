/**************************************************************************
*                                                                         *
*         Java Grande Forum Benchmark Suite - Thread Version 1.0          *
*                                                                         *
*                            produced by                                  *
*                                                                         *
*                  Java Grande Benchmarking Project                       *
*                                                                         *
*                                at                                       *
*                                                                         *
*                Edinburgh Parallel Computing Centre                      *
*                                                                         *
*                email: epcc-javagrande@epcc.ed.ac.uk                     *
*                                                                         *
*      Original version of this code by Hon Yau (hwyau@epcc.ed.ac.uk)     *
*                                                                         *
*      This version copyright (c) The University of Edinburgh, 2001.      *
*                         All rights reserved.                            *
*                                                                         *
**************************************************************************/


package section3.montecarlo;

import java.util.Vector;

/**
  * Code, a test-harness for invoking and driving the Applications
  * Demonstrator classes.
  *
  * <p>To do:
  * <ol>
  *   <li>Very long delay prior to connecting to the server.</li>
  *   <li>Some text output seem to struggle to get out, without
  *       the user tapping ENTER on the keyboard!</li>
  * </ol>
  *
  * @author H W Yau
  * @version $Revision: 1.12 $ $Date: 1999/02/16 19:13:38 $
  */
public class AppDemo extends Universal {
  //------------------------------------------------------------------------
  // Instance variables.
  //------------------------------------------------------------------------

    
  public double JGFavgExpectedReturnRateMC =0.0;
  /**
    * Directory in which to find the historical rates.
    */
  private final String dataDirname;
  /**
    * Name of the historical rate to model.
    */
  private final String dataFilename;
  /**
    * The number of time-steps which the Monte Carlo simulation should
    * run for.
    */
  private int nTimeStepsMC=0;
  /**
    * The number of Monte Carlo simulations to run.
    */
  private int nRunsMC=0;
  private final int nthreads;
  /**
    * The default duration between time-steps, in units of a year.
    */
  private final double dTime = 1.0/365.0;

  public Vector<ToTask> tasks;
  public Vector<ToResult> results;

  public AppDemo(  
  String dataDirname, String dataFilename, int nTimeStepsMC, 
  int nRunsMC, int nthreads) {
    this.dataDirname    = dataDirname;
    this.dataFilename   = dataFilename;
    this.nTimeStepsMC   = nTimeStepsMC;
    this.nRunsMC        = nRunsMC;
    this.nthreads       = nthreads;
    set_prompt("AppDemo> ");
    set_DEBUG(true);
  }
  /**
    * Single point of contact for running this increasingly bloated
    * class.  Other run modes can later be defined for whether a new rate
    * should be loaded in, etc.
    * Note that if the <code>hostname</code> is set to the string "none",
    * then the demonstrator runs in purely serial mode.
    */

  /**
    * Initialisation and Run methods.
    */

    PriceStock psMC;
    double pathStartValue = 100.0;


    public static ToInitAllTasks initAllTasks = null;

    public void initSerial() { 
    try{
      //
      // Measure the requested path rate.
      RatePath rateP = new RatePath(dataDirname, dataFilename);
      ReturnPath returnP = rateP.getReturnCompounded();
      returnP.estimatePath();
      //
      // Now prepare for MC runs.
      initAllTasks = new ToInitAllTasks(returnP, nTimeStepsMC, 
      pathStartValue);
      //
      // Now create the tasks.
      initTasks();
      //
    } catch( DemoException demoEx ) {
      dbgPrintln(demoEx.toString());
      System.exit(-1);
    }
  }

  public void runThread() {
    results = new Vector<ToResult>(nRunsMC);

       Runnable thobjects[] = new Runnable [nthreads];
       Thread th[] = new Thread [nthreads];

        for(int i=1;i<nthreads;i++) {
            thobjects[i] = new AppDemoThread(i,nRunsMC);
            th[i] = new Thread(thobjects[i]);
            th[i].start();
        }

        thobjects[0] = new AppDemoThread(0,nRunsMC);
        thobjects[0].run();


        for(int i=1;i<nthreads;i++) {
            try {
                th[i].join();
            }
            catch (InterruptedException e) {}
        }      

  }

  public void processSerial() {
      //
      // Process the results.
    try {
      processResults();
    } catch( DemoException demoEx ) {
      dbgPrintln(demoEx.toString());
      System.exit(-1);
    }
  }
  //------------------------------------------------------------------------
  /**
    * Generates the parameters for the given Monte Carlo simulation.
    */
  private void initTasks() {
    tasks = new Vector<ToTask>(nRunsMC);
    for( int i=0; i < nRunsMC; i++ ) {
      String header="MC run "+String.valueOf(i);
      ToTask task = new ToTask(header, (long)i*11);
      tasks.addElement(task);
    }
  }
  /**
    * Method for doing something with the Monte Carlo simulations.
    * It's probably not mathematically correct, but shall take an average over
    * all the simulated rate paths.
    *
    * @exception DemoException thrown if there is a problem with reading in
    *            any values.
    */
  private void processResults() throws DemoException{
    double avgExpectedReturnRateMC = 0.0;
    ToResult returnMC;
    if( nRunsMC != results.size() ) {
      errPrintln("Fatal: TaskRunner managed to finish with no all the results gathered in!");
      System.exit(-1);
    }
    //
    // Create an instance of a RatePath, for accumulating the results of the
    // Monte Carlo simulations.
    RatePath avgMCrate = new RatePath(nTimeStepsMC, "MC", 19990109, 19991231, dTime);
    for( int i=0; i < nRunsMC; i++ ) {
      // First, create an instance which is supposed to generate a
      // particularly simple MC path.
      returnMC = results.elementAt(i);
      avgMCrate.inc_pathValue(returnMC.get_pathValue());
      avgExpectedReturnRateMC += returnMC.get_expectedReturnRate();
    } // for i;
    avgMCrate.inc_pathValue(1.0/(nRunsMC));
    avgExpectedReturnRateMC /= nRunsMC;
    JGFavgExpectedReturnRateMC = avgExpectedReturnRateMC;

//    dbgPrintln("Average over "+nRunsMC+": expectedReturnRate="+
//    avgExpectedReturnRateMC+" volatility="+avgVolatilityMC + JGFavgExpectedReturnRateMC);
  }
  //
  //------------------------------------------------------------------------


class AppDemoThread implements Runnable {

    int id,nRunsMC;

    public AppDemoThread(int id,int nRunsMC) {
        this.id = id;
        this.nRunsMC=nRunsMC;

    }


    public void run() {

         PriceStock ps;
        // Now do the computation.


        int ilow, iupper, slice;

        slice = (nRunsMC + nthreads-1)/nthreads;

        ilow = id*slice;
        iupper = (id+1)*slice;
        if (id==nthreads-1) iupper=nRunsMC;

        for( int iRun=ilow; iRun < iupper; iRun++ ) {
        ps = new PriceStock();
        ps.setInitAllTasks(AppDemo.initAllTasks);
        ps.setTask(tasks.elementAt(iRun));
        ps.run();
            results.addElement(ps.getResult());
        }
    }
}

}
