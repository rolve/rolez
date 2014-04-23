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

/**
  * Wrapper code to invoke the Application demonstrator.
  *
  * @author H W Yau
  * @version $Revision: 1.19 $ $Date: 1999/02/16 19:10:02 $
  */
public class CallAppDemo {
    public int size;
    int datasizes[] = {10000,60000};
    int input[] = new int[2];
    int nthreads;
    AppDemo ap = null;
    
    public CallAppDemo(int nthreads) {
      this.nthreads = nthreads;
    }

    public void initialise () {

      input[0] = 1000;
      input[1] = datasizes[size];

      String dirName="Data";
      String filename="hitData";
      ap = new AppDemo(dirName, filename,
      (input[0]),(input[1]),nthreads);
      ap.initSerial();
    }

    public void runiters () {
      ap.runTasks();
    }
    public void presults () {
      ap.processSerial();
    }

}
