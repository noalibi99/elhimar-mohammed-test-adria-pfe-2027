package me.medelhimar.bdcc.account.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void createsAndUpdatesAccountBalance() throws Exception {
        String body = mvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerReference":"customer-1","currency":"EUR","balance":100}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();
        JsonNode created = mapper.readTree(body);
        String url = "/api/accounts/" + created.get("id").asText();

        mvc.perform(patch(url + "/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"change\":-40}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(60));

        mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(60));

        mvc.perform(patch(url + "/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"change\":-61}"))
                .andExpect(status().isBadRequest());

        mvc.perform(patch(url + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BLOCKED\"}"))
                .andExpect(status().isOk());

        mvc.perform(patch(url + "/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"change\":10}"))
                .andExpect(status().isConflict());
    }

    @Test
    void missingAccountReturnsNotFound() throws Exception {
        mvc.perform(get("/api/accounts/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
