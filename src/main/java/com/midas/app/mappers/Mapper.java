package com.midas.app.mappers;

import com.midas.app.models.Account;
import com.midas.generated.model.AccountDto;
import com.midas.generated.model.ProviderTypeEnumDto;
import com.stripe.model.Customer;
import lombok.NonNull;

public class Mapper {
  // Prevent instantiation
  private Mapper() {}

  /**
   * toAccountDto maps an account to an account dto.
   *
   * @param account is the account to be mapped
   * @return AccountDto
   */
  public static AccountDto toAccountDto(@NonNull Account account) {
    var accountDto = new AccountDto();

    accountDto
        .id(account.getId())
        .firstName(account.getFirstName())
        .lastName(account.getLastName())
        .email(account.getEmail())
        .providerId(account.getProviderId())
        .providerType(account.getProviderType())
        .createdAt(account.getCreatedAt())
        .updatedAt(account.getUpdatedAt());

    return accountDto;
  }

  /**
   * toAccount maps an Stripe customer object and payment provider type to a Account object
   *
   * @param customer Stripe customer object
   * @param providerType Payment Provider type
   * @return Account
   */
  public static Account toAccount(@NonNull Customer customer, ProviderTypeEnumDto providerType) {
    String[] fullName = customer.getName().split(" ");
    return Account.builder()
        .firstName(fullName[0])
        .lastName(fullName[1])
        .email(customer.getEmail())
        .providerId(customer.getId())
        .providerType(providerType)
        .build();
  }
}
