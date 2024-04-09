package com.midas.app.providers.external.stripe;

import com.midas.app.exceptions.ApiException;
import com.midas.app.mappers.Mapper;
import com.midas.app.models.Account;
import com.midas.app.providers.payment.CreateAccount;
import com.midas.app.providers.payment.PaymentProvider;
import com.midas.generated.model.ProviderTypeEnumDto;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Getter
public class StripePaymentProvider implements PaymentProvider {
  private final Logger logger = LoggerFactory.getLogger(StripePaymentProvider.class);

  private final StripeClient stripeClient;

  /** providerName is the name of the payment provider */
  @Override
  public String providerName() {
    return "stripe";
  }

  /**
   * createAccount creates a new account in Stripe
   *
   * @param details is the details of the account to be created.
   * @return Account
   */
  @Override
  public Account createAccount(CreateAccount details) {
    CustomerCreateParams customerCreateParams =
        CustomerCreateParams.builder()
            .setName(details.getFirstName() + " " + details.getLastName())
            .setEmail(details.getEmail())
            .build();
    Customer customer;
    try {
      customer = stripeClient.customers().create(customerCreateParams);
    } catch (StripeException e) {
      logger.error("Exception occurred while creating customer at {} ", providerName(), e);
      throw new ApiException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          String.format("Could not create customer at provider %s", providerName()));
    }

    logger.info(
        "account for email {} created in payment provider {} with providerId {}",
        details.getEmail(),
        providerName(),
        customer.getId());

    return Mapper.toAccount(customer, ProviderTypeEnumDto.fromValue(providerName()));
  }

  /**
   * updateAccount updates an existing account in Stripe
   *
   * @param details is the details of the account to be updated.
   */
  @Override
  public void updateAccount(CreateAccount details, String customerId) {
    CustomerUpdateParams customerUpdateParamsParams =
        CustomerUpdateParams.builder()
            .setName(details.getFirstName() + " " + details.getLastName())
            .setEmail(details.getEmail())
            .build();
    try {
      stripeClient.customers().update(customerId, customerUpdateParamsParams);
    } catch (StripeException e) {
      logger.error("Exception occurred while creating customer at {} ", providerName(), e);
      throw new ApiException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          String.format("Could not create customer at provider %s", providerName()));
    }

    logger.info(
        "account for userId {} updated in payment provider {} and providerId {}",
        details.getUserId(),
        providerName(),
        customerId);
  }
}
