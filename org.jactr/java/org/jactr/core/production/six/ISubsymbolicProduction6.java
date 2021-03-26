package org.jactr.core.production.six;

import org.jactr.core.production.IProduction;
/*
 * default logging
 */
import org.jactr.core.production.ISubsymbolicProduction;

public interface ISubsymbolicProduction6 extends ISubsymbolicProduction
{

  static public final String EXPECTED_UTILITY_PARAM = "ExpectedUtility";

  static public final String UTILITY_PARAM          = "Utility";

  static public final String REWARD_PARAM           = "Reward";

  static public final String PARENT_PRODUCTION_PARAM = "ParentProduction";

  /**
   * return the computed expected utility
   * @return
   */
  public double getExpectedUtility();
  
  public void setExpectedUtility(double utility);

  /**
   * return the predefined utility of the production
   * 
   * @return
   */
  public double getUtility();

  public void setUtility(double utility);

  /**
   * return the reward value associated with this production or Double.NaN if
   * there is no reward explicitly defined for this production
   * 
   * @return
   */
  public double getReward();

  public void setReward(double reward);

  /**
   * null if hand coded production, otherwise its the first parent from
   * compilation
   *
   * @return
   */
  public IProduction getPrimaryParent();

  public void setPrimaryParent(IProduction production);
}
