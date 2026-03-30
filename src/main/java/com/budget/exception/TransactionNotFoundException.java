package com.budget.exception;

public class TransactionNotFoundException extends RuntimeException {
  public TransactionNotFoundException(String uuid) {
    super("Transaction not found: " + uuid);
  }
}
