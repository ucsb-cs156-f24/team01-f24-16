package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBRecommendationRequest;
import edu.ucsb.cs156.example.repositories.UCSBRecommendationRequestRepository;

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
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

        @MockBean
        UCSBRecommendationRequestRepository ucsbRecommendationRequestRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/recommendationrequest/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/recommendationRequest/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "ADMIN" })
        @Test
        public void logged_in_admin_can_get_all() throws Exception {
                MvcResult result = mockMvc.perform(get("/api/recommendationRequest/all"))
                                .andExpect(status().is(200)).andReturn(); // logged
        }

        @Test
        @WithMockUser(roles = { "ADMIN" })
        public void logged_in_admin_can_get_all_returns_correct_data() throws Exception {
                LocalDateTime testDateNeeded = LocalDateTime.of(2024, 11, 23, 20, 40, 10);
                UCSBRecommendationRequest newRequest1 = UCSBRecommendationRequest.builder()
                                .requesterEmail("user1@example.com")
                                .professorEmail("prof1@example.com")
                                .explanation("Need%20this%20for%20graduate%20application.")
                                .dateRequested(testDateNeeded)
                                .dateNeeded(testDateNeeded.plusDays(3))
                                .done(false)
                                .build();

                UCSBRecommendationRequest newRequest2 = UCSBRecommendationRequest.builder()
                                .requesterEmail("user2@example.com")
                                .professorEmail("prof2@example.com")
                                .explanation("Need%20this%20for%20graduate%20application.")
                                .dateRequested(testDateNeeded)
                                .dateNeeded(testDateNeeded.plusDays(4))
                                .done(false)
                                .build();

                ArrayList<UCSBRecommendationRequest> mockRequests = new ArrayList<>();
                mockRequests.addAll(Arrays.asList(newRequest1, newRequest2));

                when(ucsbRecommendationRequestRepository.findAll()).thenReturn(mockRequests);

                MvcResult result = mockMvc.perform(get("/api/recommendationRequest/all"))
                                .andExpect(status().isOk())
                                .andExpect(content().json(mapper.writeValueAsString(mockRequests)))
                                .andReturn();

                // assert
                verify(ucsbRecommendationRequestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(mockRequests);
                String responseString = result.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/recommendationRequest?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/recommendationRequest/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/recommendationRequest/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_post_new_recommendation_request() throws Exception {
                LocalDateTime now = LocalDateTime.now();

                LocalDateTime testDateNeeded = LocalDateTime.of(2024, 11, 23, 20, 40, 10);
                UCSBRecommendationRequest newRequest = UCSBRecommendationRequest.builder()
                                .requesterEmail("user@example.com")
                                .professorEmail("prof@example.com")
                                .explanation("Need%20this%20for%20graduate%20application.")
                                .dateRequested(now)
                                .dateNeeded(testDateNeeded)
                                .done(true)
                                .build();

                when(ucsbRecommendationRequestRepository.save(eq(newRequest))).thenReturn(newRequest);

                String req1 = "/api/recommendationRequest/post?requesterEmail=user@example.com&professorEmail=prof@example.com&explanation=Need%20this%20for%20graduate%20application.&dateRequested=";
                req1 = req1 + now.toString();

                String reqf = req1 + "&dateNeeded=2024-11-23T20:40:10&done=true";

                System.out.println("Final URL: " + reqf);

                // act
                MvcResult response = mockMvc.perform(post(reqf).with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                System.out.println("Responce, " + response);

                when(ucsbRecommendationRequestRepository.save(eq(newRequest))).thenReturn(newRequest);

                verify(ucsbRecommendationRequestRepository, times(1)).save(newRequest);

                // assert

                String expectedJson = mapper.writeValueAsString(newRequest);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
                UCSBRecommendationRequest request = UCSBRecommendationRequest.builder()
                                .requesterEmail("user@example.com")
                                .professorEmail("prof@example.com")
                                .explanation("Need this for graduate application.")
                                .dateRequested(LocalDateTime.now())
                                .dateNeeded(LocalDateTime.now().plusDays(30))
                                .done(false)
                                .build();

                when(ucsbRecommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.of(request));

                MvcResult response = mockMvc.perform(get("/api/recommendationRequest?id=7"))
                                .andExpect(status().isOk()).andReturn();

                verify(ucsbRecommendationRequestRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(request);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

            // arrange

            when(ucsbRecommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(get("/api/recommendationRequest?id=7"))
                            .andExpect(status().isNotFound()).andReturn();

            // assert

            verify(ucsbRecommendationRequestRepository, times(1)).findById(eq(7L));
            Map<String, Object> json = responseToJson(response);
            assertEquals("EntityNotFoundException", json.get("type"));
            assertEquals("UCSBRecommendationRequest with id 7 not found", json.get("message"));
    }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_ucsbdate() throws Exception {
                // arrange

                UCSBRecommendationRequest recommendationRequest = UCSBRecommendationRequest.builder()
                                .requesterEmail("user@example.com")
                                .professorEmail("prof@example.com")
                                .explanation("Need this for graduate application.")
                                .dateRequested(LocalDateTime.now())
                                .dateNeeded(LocalDateTime.now().plusDays(30))
                                .done(false)
                                .id(68)
                                .build();

                UCSBRecommendationRequest recommendationRequestEdited = UCSBRecommendationRequest.builder()
                                .requesterEmail("John@example.com")
                                .professorEmail("Zuckerberg@example.com")
                                .explanation("Need this for Stanford application.")
                                .dateRequested(LocalDateTime.now())
                                .dateNeeded(LocalDateTime.now().plusDays(31))
                                .done(true)
                                .id(67)
                                .build();

                String requestBody = mapper.writeValueAsString(recommendationRequestEdited);

                when(ucsbRecommendationRequestRepository.findById(eq(67L)))
                                .thenReturn(Optional.of(recommendationRequest));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/recommendationRequest?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbRecommendationRequestRepository, times(1)).findById(67L);
                verify(ucsbRecommendationRequestRepository, times(1)).save(recommendationRequestEdited); // should be
                                                                                                         // saved with
                                                                                                         // correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_ucsbdate_that_does_not_exist() throws Exception {
                // arrange

                UCSBRecommendationRequest ucsbEditedrecommendationRequest = UCSBRecommendationRequest.builder()
                                .requesterEmail("John@example.com")
                                .professorEmail("Zuckerberg@example.com")
                                .explanation("Need this for Stanford application.")
                                .dateRequested(LocalDateTime.now())
                                .dateNeeded(LocalDateTime.now().plusDays(31))
                                .done(true)
                                .id(67)
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbEditedrecommendationRequest);

                when(ucsbRecommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/recommendationRequest?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbRecommendationRequestRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBRecommendationRequest with id 67 not found", json.get("message"));

        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_recommendationRequest() throws Exception {
                // arrange

                UCSBRecommendationRequest ucsbRecommendationRequest = UCSBRecommendationRequest.builder()
                                .requesterEmail("John@example.com")
                                .professorEmail("Zuckerberg@example.com")
                                .explanation("Need this for Stanford application.")
                                .dateRequested(LocalDateTime.now())
                                .dateNeeded(LocalDateTime.now().plusDays(31))
                                .done(true)
                                //.id(15) // might need for testing
                                .build();

                when(ucsbRecommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.of(ucsbRecommendationRequest));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/recommendationRequest?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbRecommendationRequestRepository, times(1)).findById(15L);
                verify(ucsbRecommendationRequestRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBRecommendationRequest with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_ucsbdate_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbRecommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/recommendationRequest?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbRecommendationRequestRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBRecommendationRequest with id 15 not found", json.get("message"));
        }

        // @WithMockUser(roles = { "USER" })
        // @Test
        // public void
        // test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws
        // Exception {
        // when(ucsbRecommendationRequestRepository.findById(123L)).thenReturn(Optional.empty());

        // mockMvc.perform(get("/api/recommendationRequest?id=123"))
        // .andExpect(status().isNotFound())
        // .andReturn();

        // verify(ucsbRecommendationRequestRepository, times(1)).findById(123L);
        // }

        // @WithMockUser(roles = { "ADMIN", "USER" })
        // @Test
        // public void adminCanCreateRecommendationRequestAndSetDone() throws Exception
        // {
        // LocalDateTime now = LocalDateTime.now();
        // UCSBRecommendationRequest newRequest = UCSBRecommendationRequest.builder()
        // .requesterEmail("user@example.com")
        // .professorEmail("prof@example.com")
        // .explanation("Need%20this%20for%20graduate%20application.")
        // .dateRequested(now)
        // .dateNeeded(now.plusDays(1))
        // .done(true) // Directly test both true and false states
        // .build();

        // String req1 =
        // "/api/recommendationRequest/post?requesterEmail=user@example.com&professorEmail=prof@example.com&explanation=Need%20this%20for%20graduate%20application.&dateRequested=";
        // req1 = req1 + now.toString();

        // String reqf = req1 + "&dateNeeded=2024-11-23T20:40:10&done=false";

        // System.out.println("Final URL: " + reqf);

        // when(ucsbRecommendationRequestRepository.save(eq(newRequest))).thenReturn(newRequest);
        // mockMvc.perform(post(reqf)
        // .with(csrf()))
        // .andExpect(status().isOk())
        // .andExpect((jsonPath("$.done").equals(false))); // Direct comparison without
        // using is()
        // verify(ucsbRecommendationRequestRepository, times(1)).save(newRequest);
        // }

}