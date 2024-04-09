package com.midas.app.workflows;

import com.midas.app.activities.AccountActivity;
import com.midas.app.models.Account;
import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import org.slf4j.Logger;

@WorkflowImpl(taskQueues = UpdateAccountWorkflow.QUEUE_NAME)
public class UpdateAccountWorkflowImpl implements UpdateAccountWorkflow {
  private final Logger logger = Workflow.getLogger(UpdateAccountWorkflowImpl.class);

  private final AccountActivity accountActivity;

  public UpdateAccountWorkflowImpl() {
    this.accountActivity =
        Workflow.newActivityStub(
            AccountActivity.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofMinutes(5)).build());
  }

  /**
   * updateAccount updates an existing account in the system and payment provider.
   *
   * @param details is the details of the account to be updated.
   * @return Account
   */
  @Override
  public Account updateAccount(Account details) {
    accountActivity.updatePaymentAccount(details);
    Account account = accountActivity.saveAccount(details);
    logger.info(
        "successfully updated account details for accountId {} providerType {} providerId {}",
        account.getId(),
        account.getProviderType(),
        account.getProviderId());
    return account;
  }
}
