package com.digitalwallet.platform.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.digitalwallet.platform.dto.RegisterRequest;
import com.digitalwallet.platform.dto.TransactionRequest;
import com.digitalwallet.platform.repository.TransactionRepository;
import com.digitalwallet.platform.repository.UserRepository;
import com.digitalwallet.platform.repository.WalletRepository;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class WalletControllerIntegrationTest extends AbstractIntegrationTest {

  @Autowired private UserRepository userRepository;

  @Autowired private WalletRepository walletRepository;

  @Autowired private TransactionRepository transactionRepository;

  private String authToken;

  @BeforeEach
  void cleanUp() {
    transactionRepository.deleteAll();
    walletRepository.deleteAll();
    userRepository.deleteAll();

    // Register a user and get token for authenticated tests
    RegisterRequest request = new RegisterRequest();
    request.setEmail("wallet-test@example.com");
    request.setPassword("Password123!");
    request.setFirstName("Wallet");
    request.setLastName("Tester");
    request.setPhone("9876543210");

    authToken =
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/auth/register")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .path("token");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/auth/register")
        .then()
        .extract()
        .path("user.id");

    // Re-register to get userId (first call already created)
    // userId = userRepository.findByEmail("wallet-test@example.com").get().getId();
  }

  @Test
  @DisplayName("Should get wallet info for authenticated user")
  void shouldGetWalletInfoForAuthenticatedUser() {
    given()
        .header("Authorization", "Bearer " + authToken)
        .when()
        .get("/wallet")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("walletNumber", notNullValue())
        .body("balance", notNullValue());
  }

  @Test
  @DisplayName("Should reject unauthenticated wallet access")
  void shouldRejectUnauthenticatedWalletAccess() {
    given()
        .when()
        .get("/wallet")
        .then()
        .statusCode(anyOf(is(HttpStatus.UNAUTHORIZED.value()), is(HttpStatus.FORBIDDEN.value())));
  }

  @Test
  @DisplayName("Should deposit money successfully")
  void shouldDepositMoneySuccessfully() {
    TransactionRequest depositRequest = new TransactionRequest();
    depositRequest.setAmount(new BigDecimal("500.00"));
    depositRequest.setCurrency("USD");

    given()
        .header("Authorization", "Bearer " + authToken)
        .contentType(ContentType.JSON)
        .body(depositRequest)
        .when()
        .post("/wallet/deposit")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo("COMPLETED"));
  }

  @Test
  @DisplayName("Should reject deposit with invalid amount")
  void shouldRejectDepositWithInvalidAmount() {
    TransactionRequest depositRequest = new TransactionRequest();
    depositRequest.setAmount(new BigDecimal("-100.00"));
    depositRequest.setCurrency("USD");

    given()
        .header("Authorization", "Bearer " + authToken)
        .contentType(ContentType.JSON)
        .body(depositRequest)
        .when()
        .post("/wallet/deposit")
        .then()
        .statusCode(not(HttpStatus.OK.value()));
  }

  @Test
  @DisplayName("Should withdraw money successfully after deposit")
  void shouldWithdrawMoneySuccessfully() {
    // First deposit
    TransactionRequest depositRequest = new TransactionRequest();
    depositRequest.setAmount(new BigDecimal("1000.00"));
    depositRequest.setCurrency("USD");

    given()
        .header("Authorization", "Bearer " + authToken)
        .contentType(ContentType.JSON)
        .body(depositRequest)
        .when()
        .post("/wallet/deposit")
        .then()
        .statusCode(HttpStatus.OK.value());

    // Then withdraw
    TransactionRequest withdrawRequest = new TransactionRequest();
    withdrawRequest.setAmount(new BigDecimal("200.00"));
    withdrawRequest.setCurrency("USD");

    given()
        .header("Authorization", "Bearer " + authToken)
        .contentType(ContentType.JSON)
        .body(withdrawRequest)
        .when()
        .post("/wallet/withdraw")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo("COMPLETED"));
  }

  @Test
  @DisplayName("Should reject withdrawal exceeding balance")
  void shouldRejectWithdrawalExceedingBalance() {
    TransactionRequest withdrawRequest = new TransactionRequest();
    withdrawRequest.setAmount(new BigDecimal("10000.00"));
    withdrawRequest.setCurrency("USD");

    given()
        .header("Authorization", "Bearer " + authToken)
        .contentType(ContentType.JSON)
        .body(withdrawRequest)
        .when()
        .post("/wallet/withdraw")
        .then()
        .statusCode(not(HttpStatus.OK.value()));
  }
}
