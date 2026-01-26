package com.digitalwallet.platform.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.digitalwallet.platform.dto.RegisterRequest;
import com.digitalwallet.platform.dto.TransactionRequest;
import com.digitalwallet.platform.dto.TransferRequest;
import com.digitalwallet.platform.model.Wallet;
import com.digitalwallet.platform.repository.UserRepository;
import com.digitalwallet.platform.repository.WalletRepository;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class TransactionControllerIntegrationTest extends AbstractIntegrationTest {

  @Autowired private UserRepository userRepository;

  @Autowired private WalletRepository walletRepository;

  @Autowired
  private com.digitalwallet.platform.repository.TransactionRepository transactionRepository;

  private String senderToken;
  private String receiverWalletNumber;

  @BeforeEach
  void setUp() {
    super.setUp();
    transactionRepository.deleteAll();
    walletRepository.deleteAll();
    userRepository.deleteAll();

    // Register sender
    RegisterRequest senderRequest = new RegisterRequest();
    senderRequest.setEmail("sender@example.com");
    senderRequest.setPassword("Password123!");
    senderRequest.setFirstName("Sender");
    senderRequest.setLastName("User");
    senderRequest.setPhone("1111111111");

    senderToken =
        given()
            .contentType(ContentType.JSON)
            .body(senderRequest)
            .when()
            .post("/auth/register")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .path("token");

    userRepository.findByEmail("sender@example.com").get().getId();

    // Register receiver
    RegisterRequest receiverRequest = new RegisterRequest();
    receiverRequest.setEmail("receiver@example.com");
    receiverRequest.setPassword("Password123!");
    receiverRequest.setFirstName("Receiver");
    receiverRequest.setLastName("User");
    receiverRequest.setPhone("2222222222");

    given()
        .contentType(ContentType.JSON)
        .body(receiverRequest)
        .when()
        .post("/auth/register")
        .then()
        .statusCode(HttpStatus.CREATED.value());

    Long receiverUserId = userRepository.findByEmail("receiver@example.com").get().getId();
    Wallet receiverWallet = walletRepository.findByUserId(receiverUserId).get();
    receiverWalletNumber = receiverWallet.getWalletNumber();

    // Deposit money to sender
    TransactionRequest depositRequest = new TransactionRequest();
    depositRequest.setAmount(new BigDecimal("5000.00"));
    depositRequest.setCurrency("USD");

    given()
        .header("Authorization", "Bearer " + senderToken)
        .contentType(ContentType.JSON)
        .body(depositRequest)
        .when()
        .post("/wallet/deposit")
        .then()
        .statusCode(HttpStatus.OK.value());
  }

  @Test
  @DisplayName("Should transfer money between wallets")
  void shouldTransferMoneyBetweenWallets() {
    TransferRequest transferRequest = new TransferRequest();
    transferRequest.setReceiverWalletNumber(receiverWalletNumber);
    transferRequest.setAmount(new BigDecimal("500.00"));
    transferRequest.setCurrency("USD");
    transferRequest.setDescription("Test transfer");

    given()
        .header("Authorization", "Bearer " + senderToken)
        .contentType(ContentType.JSON)
        .body(transferRequest)
        .when()
        .post("/transactions/transfer")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo("COMPLETED"))
        .body("amount", equalTo(500.00f))
        .body("receiverWallet", equalTo(receiverWalletNumber));
  }

  @Test
  @DisplayName("Should reject transfer with insufficient balance")
  void shouldRejectTransferWithInsufficientBalance() {
    TransferRequest transferRequest = new TransferRequest();
    transferRequest.setReceiverWalletNumber(receiverWalletNumber);
    transferRequest.setAmount(new BigDecimal("100000.00"));
    transferRequest.setCurrency("USD");
    transferRequest.setDescription("Large transfer");

    given()
        .header("Authorization", "Bearer " + senderToken)
        .contentType(ContentType.JSON)
        .body(transferRequest)
        .when()
        .post("/transactions/transfer")
        .then()
        .statusCode(not(HttpStatus.OK.value()));
  }

  @Test
  @DisplayName("Should reject transfer to non-existent wallet")
  void shouldRejectTransferToNonExistentWallet() {
    TransferRequest transferRequest = new TransferRequest();
    transferRequest.setReceiverWalletNumber("INVALID_WALLET");
    transferRequest.setAmount(new BigDecimal("100.00"));
    transferRequest.setCurrency("USD");
    transferRequest.setDescription("Invalid transfer");

    given()
        .header("Authorization", "Bearer " + senderToken)
        .contentType(ContentType.JSON)
        .body(transferRequest)
        .when()
        .post("/transactions/transfer")
        .then()
        .statusCode(not(HttpStatus.OK.value()));
  }

  @Test
  @DisplayName("Should require authentication for transfer")
  void shouldRequireAuthenticationForTransfer() {
    TransferRequest transferRequest = new TransferRequest();
    transferRequest.setReceiverWalletNumber(receiverWalletNumber);
    transferRequest.setAmount(new BigDecimal("100.00"));
    transferRequest.setCurrency("USD");
    transferRequest.setDescription("Unauthorized transfer");

    given()
        .contentType(ContentType.JSON)
        .body(transferRequest)
        .when()
        .post("/transactions/transfer")
        .then()
        .statusCode(anyOf(is(HttpStatus.UNAUTHORIZED.value()), is(HttpStatus.FORBIDDEN.value())));
  }
}
