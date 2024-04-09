package com.midas.app.services;

import com.midas.app.models.Account;
import java.util.List;

public interface AccountService {
  /**
   * createAccount creates a new account in the system and payment provider by initiating workflow.
   *
   * @param details is the details of the account to be created.
   * @return Account
   */
  Account createAccount(Account details);

  /**
   * getAccounts returns a list of accounts.
   *
   * @return List<Account>
   */
  List<Account> getAccounts();

  /**
   * updateAccount updates an existing account in the system and payment provider by initiating
   * workflow
   *
   * @param updatedDetails is the details of the account to be updated.
   * @return Account
   */
  Account updateAccount(Account updatedDetails);
}
