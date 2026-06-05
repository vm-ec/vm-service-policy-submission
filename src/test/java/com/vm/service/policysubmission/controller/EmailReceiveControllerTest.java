package com.vm.service.policysubmission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vm.service.policysubmission.dto.BedrockAgentResponse;
import com.vm.service.policysubmission.dto.ContextEmailRequest;
import com.vm.service.policysubmission.dto.EmailRequest;
import com.vm.service.policysubmission.service.EmailReceiveService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EmailReceiveController.class)
class EmailReceiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmailReceiveService emailReceiveService;

    @Test
    void process_shouldForwardRequestPayloadToServiceAndReturnAck() throws Exception {
        EmailRequest request = new EmailRequest();
        request.setSubject("FNOL - Vehicle Accident");
        request.setBody("Policy POL-12345 accident details");
        request.setSender("insured@example.com");
        request.setRecipients(List.of("claims@example.com"));
        request.setQueueId("q-1001");
        request.setTimestamp(Instant.parse("2026-06-05T12:00:00Z"));
        request.setAttachmentPaths(List.of("s3://bucket/a.pdf", "s3://bucket/b.jpg"));
        ContextEmailRequest contextRequest = new ContextEmailRequest();
        contextRequest.setContext(request);

        BedrockAgentResponse serviceResponse = BedrockAgentResponse.builder()
                .success(true)
                .statusCode(200)
                .sessionId("session-123")
                .message("ok")
                .build();

        when(emailReceiveService.pushToBedrock(any(EmailRequest.class))).thenReturn(serviceResponse);

        mockMvc.perform(post("/claims-fnol/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contextRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.agentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.sessionId").value("session-123"));

        ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailReceiveService).pushToBedrock(captor.capture());

        EmailRequest forwarded = captor.getValue();
        assertEquals(request.getSubject(), forwarded.getSubject());
        assertEquals(request.getBody(), forwarded.getBody());
        assertEquals(request.getSender(), forwarded.getSender());
        assertEquals(request.getQueueId(), forwarded.getQueueId());
        assertEquals(request.getRecipients(), forwarded.getRecipients());
        assertEquals(request.getAttachmentPaths(), forwarded.getAttachmentPaths());
        assertEquals(request.getTimestamp(), forwarded.getTimestamp());
    }
}

