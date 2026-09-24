package com.raynald.budget_tracker;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class ApiIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    // ---------- helpers ----------

    /** Registers a new user with a unique email, logs in, and returns their token. */
    private String registerAndLogin(String name) throws Exception {
        String email = name.toLowerCase() + "-" + UUID.randomUUID() + "@example.com";
        mvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON).content("""
                {"email":"%s","password":"password123","name":"%s"}""".formatted(email, name)))
                .andExpect(status().isCreated());

        String body = mvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON).content("""
                {"email":"%s","password":"password123"}""".formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private long createCategory(String token, String name) throws Exception {
        String body = mvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"%s\"}".formatted(name)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private void createExpense(String token, long categoryId, String amount, String date) throws Exception {
        mvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"categoryId":%d,"type":"EXPENSE","amount":%s,"date":"%s"}"""
                                .formatted(categoryId, amount, date)))
                .andExpect(status().isCreated());
    }

    // ---------- tests ----------

    @Test
    void requestsWithoutATokenAreRejected() throws Exception {
        mvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void registeringTheSameEmailTwiceReturnsConflict() throws Exception {
        String json = """
                {"email":"dup-%s@example.com","password":"password123","name":"Dup"}"""
                .formatted(UUID.randomUUID());
        mvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON).content(json))
                .andExpect(status().isConflict());
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        String email = "carol-" + UUID.randomUUID() + "@example.com";
        mvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON).content("""
                {"email":"%s","password":"password123","name":"Carol"}""".formatted(email)))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON).content("""
                {"email":"%s","password":"wrong-password"}""".formatted(email)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void invalidTransactionReturnsFieldErrors() throws Exception {
        String token = registerAndLogin("Dave");

        mvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"type\":\"EXPENSE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").value("Amount is required"))
                .andExpect(jsonPath("$.fieldErrors.date").value("Date is required"));
    }

    @Test
    void usersCannotSeeOrChangeEachOthersData() throws Exception {
        String alice = registerAndLogin("Alice");
        String bob = registerAndLogin("Bob");
        long aliceCategory = createCategory(alice, "Food");

        // Bob's list is empty, even though Alice has a category
        mvc.perform(get("/api/categories").header("Authorization", "Bearer " + bob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // Bob can't rename Alice's category: it looks like it doesn't exist
        mvc.perform(put("/api/categories/" + aliceCategory)
                        .header("Authorization", "Bearer " + bob)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Hacked\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void summaryTotalsTheMonthAndFlagsOverspending() throws Exception {
        String token = registerAndLogin("Erin");
        long food = createCategory(token, "Food");

        mvc.perform(put("/api/budgets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"categoryId":%d,"year":2026,"month":9,"amount":20.00}""".formatted(food)))
                .andExpect(status().isOk());

        createExpense(token, food, "15.00", "2026-09-10");
        createExpense(token, food, "10.00", "2026-09-20");
        createExpense(token, food, "100.00", "2026-10-01");  // October: must not be counted

        mvc.perform(get("/api/summary?year=2026&month=9").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpense").value(25.0))
                .andExpect(jsonPath("$.overBudgetCount").value(1))
                .andExpect(jsonPath("$.categories[0].categoryName").value("Food"))
                .andExpect(jsonPath("$.categories[0].remaining").value(-5.0))
                .andExpect(jsonPath("$.categories[0].percentUsed").value(125))
                .andExpect(jsonPath("$.categories[0].overBudget").value(true));
    }
}