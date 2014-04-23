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
  * Class for defining the initialisation data for all tasks.
  *
  * @author H W Yau
  * @version $Revision: 1.10 $ $Date: 1999/02/16 18:52:53 $
  */
public class ToInitAllTasks implements java.io.Serializable {
  private final String name;
  private final int startDate;
  private final int endDate;
  private final double dTime;
  private final int returnDefinition;
  private final double expectedReturnRate;
  private final double volatility;
  private final int nTimeSteps;
  private final double pathStartValue;

  /**
    * Another constructor, slightly easier to use by having slightly
    * fewer arguments.  Makes use of the "ReturnPath" object to
    * accomplish this.
    *
    * @param obj Object used to define the instance variables which
    *            should be carried over to this object.
    * @param nTimeSteps The number of time steps which the Monte
    *                   Carlo generator should make.
    * @param pathStartValue The stock price value to use at the start of each
    *        Monte Carlo simulation path.
    * @exception DemoException thrown if there is a problem accessing the
    *                          instance variables from the target objetct.
    */
  public ToInitAllTasks(ReturnPath obj, int nTimeSteps, double pathStartValue) 
  throws DemoException {
    //
    // Instance variables defined in the PathId object.
    this.name      = obj.get_name();
    this.startDate = obj.get_startDate();
    this.endDate   = obj.get_endDate();
    this.dTime     = obj.get_dTime();
    //
    // Instance variables defined in ReturnPath object.
    this.returnDefinition   = obj.get_returnDefinition();
    this.expectedReturnRate = obj.get_expectedReturnRate();
    this.volatility         = obj.get_volatility();
    this.nTimeSteps         = nTimeSteps;
    this.pathStartValue     = pathStartValue;
  }

  /**
    * Accessor method for private instance variable <code>name</code>.
    *
    * @return Value of instance variable <code>name</code>.
    */
  public String get_name() {
    return(this.name);
  }
  /**
    * Accessor method for private instance variable <code>startDate</code>.
    *
    * @return Value of instance variable <code>startDate</code>.
    */
  public int get_startDate() {
    return(this.startDate);
  }
  /**
    * Accessor method for private instance variable <code>endDate</code>.
    *
    * @return Value of instance variable <code>endDate</code>.
    */
  public int get_endDate() {
    return(this.endDate);
  }
  /**
    * Accessor method for private instance variable <code>dTime</code>.
    *
    * @return Value of instance variable <code>dTime</code>.
    */
  public double get_dTime() {
    return(this.dTime);
  }
  /**
    * Accessor method for private instance variable <code>returnDefinition</code>.
    *
    * @return Value of instance variable <code>returnDefinition</code>.
    */
  public int get_returnDefinition() {
    return(this.returnDefinition);
  }
  /**
    * Accessor method for private instance variable <code>expectedReturnRate</code>.
    *
    * @return Value of instance variable <code>expectedReturnRate</code>.
    */
  public double get_expectedReturnRate() {
    return(this.expectedReturnRate);
  }
  /**
    * Accessor method for private instance variable <code>volatility</code>.
    *
    * @return Value of instance variable <code>volatility</code>.
    */
  public double get_volatility() {
    return(this.volatility);
  }
  /**
    * Accessor method for private instance variable <code>nTimeSteps</code>.
    *
    * @return Value of instance variable <code>nTimeSteps</code>.
    */
  public int get_nTimeSteps() {
    return(this.nTimeSteps);
  }
  /**
    * Accessor method for private instance variable <code>pathStartValue</code>.
    *
    * @return Value of instance variable <code>pathStartValue</code>.
    */
  public double get_pathStartValue() {
    return(this.pathStartValue);
  }
  //------------------------------------------------------------------------
}
