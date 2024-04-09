package com.midas.app.services;

import com.midas.app.exceptions.ApiException;
import com.midas.app.exceptions.ResourceAlreadyExistsException;
import com.midas.app.exceptions.ResourceNotFoundException;
import com.midas.app.models.Account;
import com.midas.app.repositories.AccountRepository;
import com.midas.app.workflows.CreateAccountWorkflow;
import com.midas.app.workflows.UpdateAccountWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
  private final Logger logger = Workflow.getLogger(AccountServiceImpl.class);

  private final WorkflowClient workflowClient;

  private final AccountRepository accountRepository;

  /**
   * createAccount creates a new account in the system or provider.
   *
   * @param details is the details of the account to be created.
   * @return Account
   */
  @Override
  public Account createAccount(Account details) {

    // check for already created account with same email
    accountRepository
        .findByEmail(details.getEmail())
        .ifPresent(
            account -> {
              throw new ResourceAlreadyExistsException(
                  String.format("Account with email %s is already present", details.getEmail()));
            });

    var options = buildWorkflowOptions(CreateAccountWorkflow.QUEUE_NAME, details.getEmail());

    logger.info("initiating workflow to create account for email: {}", details.getEmail());

    var workflow = workflowClient.newWorkflowStub(CreateAccountWorkflow.class, options);

    return workflow.createAccount(details);
  }

  /**
   * getAccounts returns a list of accounts.
   *
   * @return List<Account>
   */
  @Override
  public List<Account> getAccounts() {
    return accountRepository.findAll();
  }

  /**
   * updateAccount updates an existing account in the system and payment provider by initiating
   * workflow
   *
   * @param updatedDetails is the details of the account to be updated.
   * @return Account
   */
  @Override
  public Account updateAccount(Account updatedDetails) {
    // check for existing account with the given id
    var existingDetails =
        accountRepository
            .findById(updatedDetails.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

    updateExistingAccountObject(existingDetails, updatedDetails);

    var options =
        buildWorkflowOptions(UpdateAccountWorkflow.QUEUE_NAME, updatedDetails.getId().toString());
    logger.info("initiating workflow to update account for accountId {}", updatedDetails.getId());
    var workflow = workflowClient.newWorkflowStub(UpdateAccountWorkflow.class, options);

    return workflow.updateAccount(existingDetails);
  }

  /**
   * updateExistingAccountObject updates Account object fetched from database with the updated
   * account details
   *
   * @param existingAccount Account fetched from database
   * @param updatedDetails Account received in request
   */
  private void updateExistingAccountObject(Account existingAccount, Account updatedDetails) {
    if (StringUtils.hasText(updatedDetails.getFirstName())) {
      existingAccount.setFirstName(updatedDetails.getFirstName());
    } else if ((StringUtils.hasText(updatedDetails.getLastName()))) {
      existingAccount.setLastName(updatedDetails.getLastName());
    } else if ((StringUtils.hasText(updatedDetails.getEmail()))) {
      existingAccount.setEmail(updatedDetails.getEmail());
    }
  }

  /**
   * buildWorkflowOptions creates WorkflowOptions object from specified properties
   *
   * @param taskQueue name of the queue in which workflow should be initiated
   * @param workflowId id that will be used to uniquely identify workflow
   * @return WorkflowOptions
   */
  private WorkflowOptions buildWorkflowOptions(String taskQueue, String workflowId) {
    return WorkflowOptions.newBuilder()
        .setTaskQueue(taskQueue)
        .setRetryOptions(
            RetryOptions.newBuilder()
                .setMaximumAttempts(5)
                .setDoNotRetry(new String[] {ApiException.class.getName()})
                .build())
        .setWorkflowId(workflowId)
        .build();
  }
}
