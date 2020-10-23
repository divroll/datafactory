package com.divroll.datafactory.conditions;

import com.divroll.datafactory.exceptions.UnsatisfiedConditionException;
import jetbrains.exodus.entitystore.Entity;

public class HasBeenLikedCondition implements CustomCondition {
  @Override public void execute(Entity entityInContext) throws UnsatisfiedConditionException {
    Comparable likes = entityInContext.getProperty("likes");
    if(likes == null || !Integer.class.isAssignableFrom(likes.getClass())) {
      throw new UnsatisfiedConditionException(this);
    }
  }
}
