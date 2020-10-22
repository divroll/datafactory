package com.divroll.datafactory.exceptions;

import com.divroll.datafactory.conditions.EntityCondition;

public class UnsatisfiedConditionException extends DataFactoryException {
  public UnsatisfiedConditionException(EntityCondition condition) {
   super("The condition " + condition.getClass().getName() + " was not satisfied");
  }
}
