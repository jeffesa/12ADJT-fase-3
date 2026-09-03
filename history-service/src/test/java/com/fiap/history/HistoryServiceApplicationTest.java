package com.fiap.history;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HistoryServiceApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void shouldExposeGraphiqlUi() throws Exception {
        mockMvc.perform(get("/graphiql"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void shouldRequireAuthenticationForGraphQlEndpoint() throws Exception {
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"{ appointmentsByPatient(patientId: \\\"00000000-0000-0000-0000-000000000001\\\") { id } }\"}"))
                .andExpect(status().isForbidden());
    }
}
