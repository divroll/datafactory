package com.divroll.datafactory.conditions;

import com.divroll.datafactory.exceptions.UnsatisfiedConditionException;
import javax.annotation.Nullable;
import jetbrains.exodus.entitystore.Entity;

public class HasBeenLikedCondition implements CustomCondition {
  @Override public void execute(Entity entityInContext) throws UnsatisfiedConditionException {
    Comparable likes = entityInContext.getProperty("likes");
    if(likes == null || !Integer.class.isAssignableFrom(likes.getClass())) {
      throw new UnsatisfiedConditionException(this);
    }
  }

  @Nullable @Override public EntityCondition intersect() {
    return null;
  }

  @Nullable @Override public EntityCondition union() {
    return null;
  }

  @Nullable @Override public EntityCondition minus() {
    return null;
  }

  @Nullable @Override public EntityCondition concat() {
    return null;
  }
}
