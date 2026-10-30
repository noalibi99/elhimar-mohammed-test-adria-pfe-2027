package me.medelhimar.bdcc.account.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AccountAdaptersTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ToolCallbackProvider tools;

    @Test
    void graphqlCreatesUpdatesAndReadsAccounts() throws Exception {
        String created = graphql("""
                mutation { createAccount(customerReference: "graphql-1", currency: "EUR", balance: 100) { id status } }
                """);
        JsonNode result = mapper.readTree(created);
        assertThat(result.path("errors").isMissingNode()).isTrue();
        String id = result.path("data").path("createAccount").path("id").asText();
        assertThat(result.path("data").path("createAccount").path("status").asText()).isEqualTo("ACTIVE");

        mvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("query", """
                                mutation { adjustAccountBalance(id: "%s", change: -25) { balance } }
                                """.formatted(id)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.adjustAccountBalance.balance").value(75));

        mvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("query", """
                                query { account(id: "%s") { currency balance } }
                                """.formatted(id)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.account.balance").value(75));
    }

    @Test
    void mcpToolsAreRegisteredAndCallAccountService() {
        assertThat(Arrays.stream(tools.getToolCallbacks()).map(tool -> tool.getToolDefinition().name()))
                .containsExactlyInAnyOrder("getAccount", "listAccounts", "createAccount",
                        "changeAccountStatus", "adjustAccountBalance");

        ToolCallback create = Arrays.stream(tools.getToolCallbacks())
                .filter(tool -> tool.getToolDefinition().name().equals("createAccount"))
                .findFirst().orElseThrow();
        String result = create.call("""
                {"customerReference":"mcp-1","currency":"EUR","balance":50}
                """);
        assertThat(result).contains("mcp-1", "ACTIVE");
    }

    private String graphql(String query) throws Exception {
        return mvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("query", query))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }
}
