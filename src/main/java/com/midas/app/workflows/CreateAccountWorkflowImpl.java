package com.midas.app.workflows;

import com.midas.app.activities.AccountActivity;
import com.midas.app.models.Account;
import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import org.slf4j.Logger;

@WorkflowImpl(taskQueues = CreateAccountWorkflow.QUEUE_NAME)
public class CreateAccountWorkflowImpl implements CreateAccountWorkflow {
  private final Logger logger = Workflow.getLogger(UpdateAccountWorkflowImpl.class);

  private final AccountActivity accountActivity;

  public CreateAccountWorkflowImpl() {
    this.accountActivity =
        Workflow.newActivityStub(
            AccountActivity.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofMinutes(5)).build());
  }

  /**
   * createAccount creates a new account in the system and payment provider.
   *
   * @param details is the details of the account to be created.
   * @return Account
   */
  @Override
  public Account createAccount(Account details) {
    Account account = accountActivity.createPaymentAccount(details);
    Account dbAccount = accountActivity.saveAccount(account);
    logger.info(
        "successfully created account for email {} accountId {} providerType {} providerId {}",
        dbAccount.getEmail(),
        dbAccount.getId(),
        account.getProviderType(),
        account.getProviderId());
    return dbAccount;
  }
}
