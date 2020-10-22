package com.divroll.datafactory.exceptions;

import com.divroll.datafactory.builders.DataFactoryEntity;
import jetbrains.exodus.ExodusException;

public class DataFactoryException extends ExodusException {
  public DataFactoryException(String message) {
    super(message);
  }
}
