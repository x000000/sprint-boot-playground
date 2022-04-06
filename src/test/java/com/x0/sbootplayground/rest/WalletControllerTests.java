package com.x0.sbootplayground.rest;

import com.x0.sbootplayground.data.Wallet;
import com.x0.sbootplayground.data.WalletRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(WalletController.class)
@AutoConfigureJsonTesters
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class WalletControllerTests {

    private static final String BASE_URI = "/api/wallets";
    private static final String TEST_DATA = "/wallets.sql";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private WalletRepository repo;
    @Autowired
    private JacksonTester<Wallet> json;

    @Test
    void testWalletCreation() throws Exception {
        String dummyName    = "Valid";
        String dummySurname = "Credentials";
        String url = BASE_URI + "?name={0}&surname={0}";

        for (int i = 1; i <= 3; i++) {
            assertRequest(post(url, dummyName + i, dummySurname + i).accept(MediaType.APPLICATION_JSON), "" + i);
        }

        List<Wallet> list = StreamSupport.stream(repo.findAll().spliterator(), false).collect(Collectors.toList());
        assertEquals(3, list.size());

        for (Wallet wallet : list) {
            assertEquals(dummyName + wallet.getId(), wallet.getName());
            assertEquals(dummySurname + wallet.getId(), wallet.getSurname());
            assertEquals(0, wallet.getBalance());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "name", "surname" })
    void testEmptyParams(String param) throws Exception {
        String url = param.equals("name")
                ? BASE_URI + "?surname=some&name="
                : BASE_URI + "?surname=&name=some";
        assertRequestFail(post(url), "'" + param + "'");
    }

    @Sql({ TEST_DATA })
    @ParameterizedTest
    @CsvSource({ "/3,420.0", "/4,0.0", "/5,0.3" })
    void testBalance(String sfx, String resp) throws Exception {
        assertRequest(get(BASE_URI + sfx).accept(MediaType.APPLICATION_JSON), resp);
    }

    @Sql({ TEST_DATA })
    @ParameterizedTest
    @CsvSource({ "/3,430.0", "/4,10.0", "/5,10.3" })
    void testTopUp(String sfx, String resp) throws Exception {
        assertRequest(put(BASE_URI + sfx + "/topup?amount=10").accept(MediaType.APPLICATION_JSON), resp);
    }

    @Sql({ TEST_DATA })
    @ParameterizedTest
    @ValueSource(ints = { 0, -5 })
    void testTopUp(int amount) throws Exception {
        assertRequestFail(put(BASE_URI + "/3/topup?amount={0}", amount), "should be a positive number");
    }

    @Sql({ TEST_DATA })
    @ParameterizedTest
    @CsvSource({ "/3,413.0", "/1,6.0", "/2,0.0" })
    void testWithdraw(String sfx, String resp) throws Exception {
        assertRequest(put(BASE_URI + sfx + "/withdraw?amount=7").accept(MediaType.APPLICATION_JSON), resp);
    }

    @Sql({ TEST_DATA })
    @ParameterizedTest
    @ValueSource(ints = { 0, -5, 10 })
    void testWithdraw(int amount) throws Exception {
        String resp = amount > 0
                ? "Not enough funds"
                : "should be a positive number";
        assertRequestFail(put(BASE_URI + "/2/withdraw?amount={0}", amount), resp);
    }

    @Sql({ TEST_DATA })
    @ParameterizedTest
    @CsvSource({ "/3,/2,419.7", "/5,/2,0.0", "/1,/2,12.7" })
    void testTransfer(String from, String to, String resp) throws Exception {
        assertRequest(put(BASE_URI + from + "/transfer" + to + "?amount=0.3").accept(MediaType.APPLICATION_JSON), resp);
        assertEquals(7.3f, repo.findById(2L).orElseThrow().getBalance());
    }

    @Sql({ TEST_DATA })
    @ParameterizedTest
    @ValueSource(ints = { 0, -5, 10 })
    void testTransfer(int amount) throws Exception {
        String resp = amount > 0
                ? "Not enough funds"
                : "should be a positive number";
        assertRequestFail(put(BASE_URI + "/2/transfer/4?amount={0}", amount), resp);
    }

    @Sql({ TEST_DATA })
    @Test
    void testTransfer() throws Exception {
        assertRequestFail(put(BASE_URI + "/2/transfer/2?amount=1"), "Target wallet cannot be a source one");
    }


    private void assertRequest(RequestBuilder req, String resp) throws Exception {
        mvc.perform(req)
                .andExpect(status().isOk())
                .andExpect(content().string(resp));
    }

    private void assertRequestFail(RequestBuilder req, String resp) throws Exception {
        mvc.perform(req)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(Matchers.containsString(resp)));
    }
}
