package com.midas.app.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import com.midas.generated.model.AccountDto;
import com.midas.generated.model.ProviderTypeEnumDto;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import okhttp3.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StringUtils;

@ActiveProfiles("default")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountControllerIT {

  private static final String FIRST_NAME = "Chandler";
  private static final String UPDATED_FIRST_NAME = "Joey";
  private static final String LAST_NAME = "Bing";
  private static final String UPDATED_LAST_NAME = "Tribianni";
  private static final String EMAIL = "cbing@friends.com";
  private static final String UPDATED_EMAIL = "chandlerb@friends.com";

  @LocalServerPort private int port;

  @Autowired private StripeClient stripeClient;

  private Gson gson;
  private OkHttpClient okHttpClient;
  private String PROVIDER_ID;
  private String BASE_URL;
  private UUID ACCOUNT_ID;

  private static final MediaType JSON = MediaType.get("application/json");

  @BeforeAll
  public void setup() {
    BASE_URL = "http://localhost:" + port + "/accounts";
    okHttpClient =
        new OkHttpClient()
            .newBuilder()
            .connectTimeout(2L, TimeUnit.MINUTES)
            .readTimeout(2L, TimeUnit.MINUTES)
            .build();
    gson =
        new GsonBuilder()
            .registerTypeAdapter(
                OffsetDateTime.class,
                (JsonDeserializer<OffsetDateTime>)
                    (json, type, context) ->
                        OffsetDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_DATE_TIME))
            .create();
  }

  @AfterAll
  public void tearDown() throws StripeException {
    if (StringUtils.hasText(PROVIDER_ID)) {
      stripeClient.customers().delete(PROVIDER_ID);
    }
  }

  @Test
  @Order(1)
  void testCreateUserAccountSuccess() throws IOException {
    File jsonFile = new ClassPathResource("createAccount.json").getFile();
    String requestBody = Files.readString(jsonFile.toPath());

    // Create new account
    Request createAccountRequest =
        new Request.Builder().url(BASE_URL).post(RequestBody.create(requestBody, JSON)).build();

    AccountDto account;
    try (Response response = okHttpClient.newCall(createAccountRequest).execute()) {
      assertEquals(201, response.code());
      account = gson.fromJson(response.body().string(), AccountDto.class);

      assertNotNull(account.getProviderId());
      PROVIDER_ID = account.getProviderId();
      ACCOUNT_ID = account.getId();

      // assert account values
      assertEquals(FIRST_NAME, account.getFirstName());
      assertEquals(LAST_NAME, account.getLastName());
      assertEquals(EMAIL, account.getEmail());
      assertEquals(ProviderTypeEnumDto.STRIPE, account.getProviderType());
    }
  }

  @Test
  @Order(2)
  void testCreateAccountFailureForCreatingNewAccountWithSameEmail() throws IOException {
    File jsonFile = new ClassPathResource("createAccount.json").getFile();
    String requestBody = Files.readString(jsonFile.toPath());

    // Create new account
    Request createAccountRequest =
        new Request.Builder().url(BASE_URL).post(RequestBody.create(requestBody, JSON)).build();

    try (Response response = okHttpClient.newCall(createAccountRequest).execute()) {
      assertEquals(409, response.code());
    }
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "creatAccountWithoutFirstName.json",
        "creatAccountWithoutLastName.json",
        "creatAccountWithoutEmail.json",
        "creatAccountWithoutProviderType.json"
      })
  @Order(3)
  void testCreateUserAccountFailureForMissingRequiredProperties(String fileName)
      throws IOException {
    File jsonFile = new ClassPathResource(fileName).getFile();
    String requestBody = Files.readString(jsonFile.toPath());

    // Create new account
    Request createAccountRequest =
        new Request.Builder().url(BASE_URL).post(RequestBody.create(requestBody, JSON)).build();

    try (Response response = okHttpClient.newCall(createAccountRequest).execute()) {
      assertEquals(400, response.code());
    }
  }

  @Test
  @Order(4)
  void testGetAllUserAccountsSuccess() throws IOException {
    // Verify account being created via calling get accounts API
    Request getAllAccountsRequest = new Request.Builder().url(BASE_URL).get().build();

    try (Response response = okHttpClient.newCall(getAllAccountsRequest).execute()) {
      assertEquals(200, response.code());

      List<AccountDto> accountDtoList =
          gson.fromJson(response.body().string(), new TypeToken<>() {});
      assertEquals(1, accountDtoList.size());

      // assert account values
      assertEquals(ACCOUNT_ID, accountDtoList.getFirst().getId());
      assertEquals(FIRST_NAME, accountDtoList.getFirst().getFirstName());
      assertEquals(LAST_NAME, accountDtoList.getFirst().getLastName());
      assertEquals(EMAIL, accountDtoList.getFirst().getEmail());
      assertEquals(ProviderTypeEnumDto.STRIPE, accountDtoList.getFirst().getProviderType());
      assertEquals(PROVIDER_ID, accountDtoList.getFirst().getProviderId());
    }
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "updateAccountWithFirstName.json",
        "updateAccountWithLastName.json",
        "updateAccountWithEmail.json"
      })
  @Order(5)
  void testUpdateAccountSuccess(String fileName) throws IOException {
    File jsonFile = new ClassPathResource(fileName).getFile();
    String requestBody = Files.readString(jsonFile.toPath());

    // Create new account
    Request createAccountRequest =
        new Request.Builder()
            .url(BASE_URL + "/" + ACCOUNT_ID)
            .patch(RequestBody.create(requestBody, JSON))
            .build();

    AccountDto account;
    try (Response response = okHttpClient.newCall(createAccountRequest).execute()) {
      assertEquals(200, response.code());
      account = gson.fromJson(response.body().string(), AccountDto.class);

      // assert account values
      assertEquals(ACCOUNT_ID, account.getId());
      if (fileName.contains("FirstName")) {
        assertEquals(UPDATED_FIRST_NAME, account.getFirstName());
      }
      if (fileName.contains("LastName")) {
        assertEquals(UPDATED_LAST_NAME, account.getLastName());
      }
      if (fileName.contains("Email")) {
        assertEquals(UPDATED_EMAIL, account.getEmail());
      }
      assertEquals(ProviderTypeEnumDto.STRIPE, account.getProviderType());
      assertEquals(PROVIDER_ID, account.getProviderId());
    }
  }
}
