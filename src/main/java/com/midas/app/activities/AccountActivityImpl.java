package com.midas.app.activities;

import com.midas.app.models.Account;
import com.midas.app.providers.payment.CreateAccount;
import com.midas.app.providers.payment.PaymentProviderFactory;
import com.midas.app.repositories.AccountRepository;
import com.midas.app.workflows.CreateAccountWorkflow;
import com.midas.app.workflows.UpdateAccountWorkflow;
import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@ActivityImpl(taskQueues = {CreateAccountWorkflow.QUEUE_NAME, UpdateAccountWorkflow.QUEUE_NAME})
@RequiredArgsConstructor
@Component
public class AccountActivityImpl implements AccountActivity {

  private final PaymentProviderFactory paymentProviderFactory;
  private final AccountRepository accountRepository;

  /**
   * saveAccount saves an account in the data store.
   *
   * @param account is the account to be saved
   * @return Account
   */
  @Override
  public Account saveAccount(Account account) {
    return accountRepository.save(account);
  }

  /**
   * createPaymentAccount creates a payment account in the system or provider.
   *
   * @param account is the account to be created
   * @return Account
   */
  @Override
  public Account createPaymentAccount(Account account) {
    CreateAccount accountDetails = new CreateAccount();
    // Copy properties values from Account object to CreateAccount object
    BeanUtils.copyProperties(account, accountDetails);
    return paymentProviderFactory.create(account.getProviderType()).createAccount(accountDetails);
  }

  /**
   * updatePaymentAccount updates a payment account in payment provider system.
   *
   * @param account is the account to be updated
   */
  @Override
  public void updatePaymentAccount(Account account) {
    CreateAccount accountDetails = new CreateAccount();
    // Copy properties values from Account object to CreateAccount object
    BeanUtils.copyProperties(account, accountDetails);
    accountDetails.setUserId(account.getId().toString());
    paymentProviderFactory
        .create(account.getProviderType())
        .updateAccount(accountDetails, account.getProviderId());
  }
}
