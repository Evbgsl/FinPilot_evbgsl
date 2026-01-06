package com.evbgsl.finpilot.service;

import static org.junit.jupiter.api.Assertions.*;

import com.evbgsl.finpilot.infra.FileStorage;
import com.evbgsl.finpilot.infra.UserStorage;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AuthServiceTest {

  @TempDir Path tempDir;

  @Test
  void registerAndLogin_success() {
    // given
    Path walletsDir = tempDir.resolve("wallets");
    Path usersFile = tempDir.resolve("users.json");

    FileStorage walletStorage = new FileStorage(walletsDir);
    UserStorage userStorage = new UserStorage(usersFile);

    AuthService auth = new AuthService(walletStorage, userStorage);

    // when
    auth.register("u1", "1");
    auth.login("u1", "1");

    // then
    assertNotNull(auth.getCurrentUser());
    assertEquals("u1", auth.getCurrentUser().login());
    assertNotNull(auth.getCurrentWallet());
  }

  @Test
  void loginFailsForWrongPassword() {
    Path walletsDir = tempDir.resolve("wallets");
    Path usersFile = tempDir.resolve("users.json");

    FileStorage walletStorage = new FileStorage(walletsDir);
    UserStorage userStorage = new UserStorage(usersFile);

    AuthService auth = new AuthService(walletStorage, userStorage);

    auth.register("u1", "1");

    assertThrows(IllegalArgumentException.class, () -> auth.login("u1", "wrong"));
  }

  @Test
  void logoutClearsSession() {
    Path walletsDir = tempDir.resolve("wallets");
    Path usersFile = tempDir.resolve("users.json");

    FileStorage walletStorage = new FileStorage(walletsDir);
    UserStorage userStorage = new UserStorage(usersFile);

    AuthService auth = new AuthService(walletStorage, userStorage);

    auth.register("u1", "1");
    auth.login("u1", "1");
    auth.logout();

    assertNull(auth.getCurrentUser());
    assertNull(auth.getCurrentWallet());
  }
}
