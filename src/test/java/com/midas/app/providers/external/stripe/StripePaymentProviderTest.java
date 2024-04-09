package com.midas.app.providers.external.stripe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.midas.app.exceptions.ApiException;
import com.midas.app.models.Account;
import com.midas.app.providers.payment.CreateAccount;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StripePaymentProviderTest {

  private static final String FIRST_NAME = "testFirstName";
  private static final String LAST_NAME = "testLastName";
  private static final String EMAIL = "testFirstName";
  private static final String PROVIDER_ID = "testCustomerId";

  @Mock private StripeClient stripeClient;

  @Mock private CustomerService customerService;

  @InjectMocks private StripePaymentProvider stripePaymentProvider;

  @Test
  void testCreateAccountSuccess() throws StripeException {
    CreateAccount testCreateAccount = buildCreateAccount();
    Customer customer = buildCustomer();

    when(stripeClient.customers()).thenReturn(customerService);
    when(customerService.create(any(CustomerCreateParams.class))).thenReturn(customer);

    Account newAccount = stripePaymentProvider.createAccount(testCreateAccount);

    assertEquals(PROVIDER_ID, newAccount.getProviderId());
    assertEquals(FIRST_NAME, newAccount.getFirstName());
    assertEquals(LAST_NAME, newAccount.getLastName());
    assertEquals(EMAIL, newAccount.getEmail());
  }

  @Test
  void testCreateAccountFailure() throws StripeException {
    CreateAccount testCreateAccount = buildCreateAccount();
    Customer customer = buildCustomer();

    when(stripeClient.customers()).thenReturn(customerService);
    when(customerService.create(any(CustomerCreateParams.class)))
        .thenThrow(
            new StripeException("Error occurred", "testRequestId", "401", 401) {
              @Override
              public String getMessage() {
                return super.getMessage();
              }
            });

    assertThrows(ApiException.class, () -> stripePaymentProvider.createAccount(testCreateAccount));
  }

  @ParameterizedTest
  @ValueSource(strings = {FIRST_NAME, LAST_NAME, EMAIL})
  void testUpdateAccountSuccess(String input) throws StripeException {
    CreateAccount testCreateAccount = new CreateAccount();
    if (input.contains("FirstName")) {
      testCreateAccount.setFirstName(input);
    } else if (input.contains("LastName")) {
      testCreateAccount.setLastName(input);
    } else {
      testCreateAccount.setEmail(input);
    }

    when(stripeClient.customers()).thenReturn(customerService);
    when(customerService.update(any(String.class), any(CustomerUpdateParams.class)))
        .thenReturn(buildCustomer());

    stripePaymentProvider.updateAccount(testCreateAccount, PROVIDER_ID);

    verify(customerService, times(1)).update(any(String.class), any(CustomerUpdateParams.class));
  }

  @Test
  void testUpdateAccountFailure() throws StripeException {
    CreateAccount testCreateAccount = new CreateAccount();
    testCreateAccount.setEmail(EMAIL);

    when(stripeClient.customers()).thenReturn(customerService);
    when(customerService.update(any(String.class), any(CustomerUpdateParams.class)))
        .thenThrow(
            new StripeException("Error occurred", "testRequestId", "401", 401) {
              @Override
              public String getMessage() {
                return super.getMessage();
              }
            });

    assertThrows(
        ApiException.class,
        () -> stripePaymentProvider.updateAccount(testCreateAccount, PROVIDER_ID));
  }

  private CreateAccount buildCreateAccount() {
    CreateAccount testCreateAccount = new CreateAccount();
    testCreateAccount.setFirstName(FIRST_NAME);
    testCreateAccount.setLastName(LAST_NAME);
    testCreateAccount.setEmail(EMAIL);
    return testCreateAccount;
  }

  private Customer buildCustomer() {
    Customer customer = new Customer();
    customer.setId(PROVIDER_ID);
    customer.setName(FIRST_NAME + " " + LAST_NAME);
    customer.setEmail(EMAIL);
    return customer;
  }
}
