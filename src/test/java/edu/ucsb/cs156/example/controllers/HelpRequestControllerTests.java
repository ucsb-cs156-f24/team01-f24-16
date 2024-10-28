package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase {

        @MockBean
        HelpRequestRepository helpRequestRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/helprequest/admin/all (ALL)
        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/helprequest/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/helprequest/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void logged_in_admins_can_get_all() throws Exception {
                mockMvc.perform(get("/api/helprequest/all"))
                                .andExpect(status().is(200)); // admin
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_helprequests() throws Exception {

                // arrange
                LocalDateTime reqTime1 = LocalDateTime.parse("2024-10-02T00:00:00");

                HelpRequest helpRequest1 = HelpRequest.builder()
                                .requesterEmail("ttnguyen@ucsb.edu")
                                .teamId("F24-16")
                                .tableOrBreakoutRoom("Table 16")
                                .explanation("Needs help with jpa03")
                                .solved(true)
                                .requestTime(reqTime1)
                                .build();

                LocalDateTime reqTime2 = LocalDateTime.parse("2024-10-02T00:00:00");

                HelpRequest helpRequest2 = HelpRequest.builder()
                                .requesterEmail("mockemail@ucsb.edu")
                                .teamId("F24-01")
                                .tableOrBreakoutRoom("Table 1")
                                .explanation("Needs help with jpa02")
                                .solved(true)
                                .requestTime(reqTime2)
                                .build();

                ArrayList<HelpRequest> expectedHelpReqs = new ArrayList<>();
                expectedHelpReqs.addAll(Arrays.asList(helpRequest1, helpRequest2));

                when(helpRequestRepository.findAll()).thenReturn(expectedHelpReqs);

                // act
                MvcResult response = mockMvc.perform(get("/api/helprequest/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(helpRequestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedHelpReqs);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // tests for POST

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_helprequest() throws Exception {
                // arrange

                LocalDateTime reqTime1 = LocalDateTime.parse("2024-10-02T00:00:00");

                HelpRequest helpRequest1 = HelpRequest.builder()
                                .requesterEmail("ttnguyen@ucsb.edu")
                                .teamId("F24-16")
                                .tableOrBreakoutRoom("Table_16")
                                .explanation("Needs_help_with_jpa03")
                                .solved(true)
                                .requestTime(reqTime1)
                                .build();

                when(helpRequestRepository.save(eq(helpRequest1))).thenReturn(helpRequest1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/helprequest/post?requesterEmail=ttnguyen@ucsb.edu&teamId=F24-16&tableOrBreakoutRoom=Table_16&explanation=Needs_help_with_jpa03&solved=true&requestTime=2024-10-02T00:00:00")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).save(helpRequest1);
                String expectedJson = mapper.writeValueAsString(helpRequest1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Authorization tests for GET

        /* FROM CHECKLIST TASK ; 
                In HelpRequestController.java there is code for an
                endpoint GET /api/HelpRequest?id=123 endpoint
                that returns the JSON of the database record with id 123 if it
                exists, or a 404 and the error message id 123 not found if it
                does not.
        */

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/helprequest?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime reqTime = LocalDateTime.parse("2024-10-31T00:00:00");

                HelpRequest helpRequest = HelpRequest.builder()
                                .requesterEmail("ttnguyen@ucsb.edu")
                                .teamId("F24-16")
                                .tableOrBreakoutRoom("Table_16")
                                .explanation("Needs_help_with_jpa03")
                                .solved(true)
                                .requestTime(reqTime)
                                .build();

                when(helpRequestRepository.findById(eq(123L))).thenReturn(Optional.of(helpRequest));

                // act
                MvcResult response = mockMvc.perform(get("/api/helprequest?id=123"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(helpRequestRepository, times(1)).findById(eq(123L));
                String expectedJson = mapper.writeValueAsString(helpRequest);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(helpRequestRepository.findById(eq(123L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/helprequest?id=123"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(helpRequestRepository, times(1)).findById(eq(123L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("HelpRequest with id 123 not found", json.get("message"));
        }

        // Authorization tests for /api/helprequest/post (POST)
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/helprequest/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/helprequest/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Admin tests for PUT (edit) 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_helprequest() throws Exception {
                // arrange

                LocalDateTime reqTime1 = LocalDateTime.parse("2024-10-31T00:00:00");
                LocalDateTime reqTime2 = LocalDateTime.parse("2023-01-13T00:00:00");

                HelpRequest helpRequestOrig = HelpRequest.builder()
                                .requesterEmail("mockuser@email.com")
                                .teamId("F24-01")
                                .tableOrBreakoutRoom("Table_01")
                                .explanation("Needs_help_with_jpa01")
                                .solved(false)
                                .requestTime(reqTime1)
                                .build();

                HelpRequest helpRequestEdited = HelpRequest.builder()
                                .requesterEmail("ttnguyen@ucsb.edu")
                                .teamId("F24-16")
                                .tableOrBreakoutRoom("Table_16")
                                .explanation("Needs_help_with_jpa03")
                                .solved(true)
                                .requestTime(reqTime2)
                                .build();

                String requestBody = mapper.writeValueAsString(helpRequestEdited);

                when(helpRequestRepository.findById(eq(123L))).thenReturn(Optional.of(helpRequestOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/helprequest?id=123")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).findById(123L);
                verify(helpRequestRepository, times(1)).save(helpRequestEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_helprequest_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime reqTime1 = LocalDateTime.parse("2000-01-01T00:00:00");

                HelpRequest editedHelpRequest = HelpRequest.builder()
                                .requesterEmail("ttnguyen@ucsb.edu")
                                .teamId("F24-16")
                                .tableOrBreakoutRoom("Table_16")
                                .explanation("Needs_help_with_jpa03")
                                .solved(true)
                                .requestTime(reqTime1)
                                .build();

                String requestBody = mapper.writeValueAsString(editedHelpRequest);

                when(helpRequestRepository.findById(eq(123L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/helprequest?id=123")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).findById(123L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("HelpRequest with id 123 not found", json.get("message"));

        }
}