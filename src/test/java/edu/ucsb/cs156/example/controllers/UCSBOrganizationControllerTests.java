package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)

public class UCSBOrganizationControllerTests extends ControllerTestCase {
        @MockBean
        UCSBOrganizationRepository ucsbOrganizationRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/ucsborganization/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsborganization?orgCode=123"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/ucsborganization/post
        // (Perhaps should also have these for put and delete)
        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                UCSBOrganization organizations = UCSBOrganization.builder()
                                .orgCode("ZPR")
                                .orgTranslationShort("ZETA PHI RHO")
                                .orgTranslation("ZETA PHI RHO")
                                .inactive(true)
                                .build();

                when(ucsbOrganizationRepository.findById(eq("ZPR"))).thenReturn(Optional.of(organizations));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgCode=ZPR"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findById(eq("ZPR"));
                String expectedJson = mapper.writeValueAsString(organizations);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbOrganizationRepository.findById(eq("art"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgCode=art"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findById(eq("art"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBOrganization with id art not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

                // arrange

                UCSBOrganization osli = UCSBOrganization.builder()
                                .orgCode("OSLI")
                                .orgTranslationShort("STUDENT LIFE")
                                .orgTranslation("OFFICE OF STUDENT LIFE	")
                                .inactive(true)
                                .build();

                UCSBOrganization sky = UCSBOrganization.builder()
                                .orgCode("SKY")
                                .orgTranslationShort("SKYDIVING CLUB")
                                .orgTranslation("SKYDIVING CLUB AT UCSB")
                                .inactive(true)
                                .build();

                ArrayList<UCSBOrganization> expectedOrganizations = new ArrayList<>();
                expectedOrganizations.addAll(Arrays.asList(osli, sky));

                when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrganizations);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedOrganizations);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_organizations() throws Exception {
                // arrange

                UCSBOrganization ZPR = UCSBOrganization.builder()
                                .orgCode("ZPR")
                                .orgTranslationShort("ZETA PHI RHO")
                                .orgTranslation("ZETA PHI RHO")
                                .inactive(true)
                                .build();

                when(ucsbOrganizationRepository.save(eq(ZPR))).thenReturn(ZPR);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsborganization/post?orgCode=ZPR&orgTranslationShort=ZETA PHI RHO&orgTranslation=ZETA PHI RHO&inactive=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).save(ZPR);
                String expectedJson = mapper.writeValueAsString(ZPR);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // @WithMockUser(roles = { "ADMIN", "USER" })
        // @Test
        // public void admin_can_delete_a_organizations() throws Exception {
        //         // arrange

        //         UCSBOrganization sky = UCSBOrganization.builder()
        //                         .orgCode("SKY")
        //                         .orgTranslationShort("SKYDIVING CLUB")
        //                         .orgTranslation("SKYDIVING CLUB AT UCSB")
        //                         .inactive(true)
        //                         .build();

        //         when(ucsbOrganizationRepository.findById(eq("SKY"))).thenReturn(Optional.of(sky));

        //         // act
        //         MvcResult response = mockMvc.perform(
        //                         delete("/api/ucsborganization?orgCode=SKY")
        //                                         .with(csrf()))
        //                         .andExpect(status().isOk()).andReturn();

        //         // assert
        //         verify(ucsbOrganizationRepository, times(1)).findById("SKY");
        //         verify(ucsbOrganizationRepository, times(1)).delete(any());

        //         Map<String, Object> json = responseToJson(response);
        //         assertEquals("UCSBOrganization with id SKY deleted", json.get("message"));
        // }

        // @WithMockUser(roles = { "ADMIN", "USER" })
        // @Test
        // public void admin_tries_to_delete_non_existant_organizations_and_gets_right_error_message()
        //                 throws Exception {
        //         // arrange

        //         when(ucsbOrganizationRepository.findById(eq("ASL"))).thenReturn(Optional.empty());

        //         // act
        //         MvcResult response = mockMvc.perform(
        //                         delete("/api/ucsborganization?orgCode=ASL")
        //                                         .with(csrf()))
        //                         .andExpect(status().isNotFound()).andReturn();

        //         // assert
        //         verify(ucsbOrganizationRepository, times(1)).findById("ASL");
        //         Map<String, Object> json = responseToJson(response);
        //         assertEquals("UCSBOrganization with id ASL not found", json.get("message"));
        // }

        // @WithMockUser(roles = { "ADMIN", "USER" })
        // @Test
        // public void admin_can_edit_an_existing_organizations() throws Exception {
        //         // arrange

        //         UCSBOrganization KRCOrig = UCSBOrganization.builder()
        //                         .orgCode("KRC")
        //                         .orgTranslationShort("KOREAN RADIO CL")
        //                         .orgTranslation("KOREAN RADIO CLUB")
        //                         .inactive(false)
        //                         .build();

        //         UCSBOrganization KRCEdited = UCSBOrganization.builder()
        //                         .orgCode("KR")
        //                         .orgTranslationShort("KOR RAD")
        //                         .orgTranslation("KoreanRadio")
        //                         .inactive(true)
        //                         .build();

        //         String requestBody = mapper.writeValueAsString(KRCEdited);

        //         when(ucsbOrganizationRepository.findById(eq("KRC"))).thenReturn(Optional.of(KRCOrig));

        //         // act
        //         MvcResult response = mockMvc.perform(
        //                         put("/api/ucsborganization?orgCode=KRC")
        //                                         .contentType(MediaType.APPLICATION_JSON)
        //                                         .characterEncoding("utf-8")
        //                                         .content(requestBody)
        //                                         .with(csrf()))
        //                         .andExpect(status().isOk()).andReturn();

        //         // assert
        //         verify(ucsbOrganizationRepository, times(1)).findById("KRC");
        //         verify(ucsbOrganizationRepository, times(1)).save(KRCEdited); // should be saved with updated info
        //         String responseString = response.getResponse().getContentAsString();
        //         assertEquals(requestBody, responseString);
        // }

        // @WithMockUser(roles = { "ADMIN", "USER" })
        // @Test
        // public void admin_cannot_edit_organizations_that_does_not_exist() throws Exception {
        //         // arrange

        //         UCSBOrganization editedOrganizations = UCSBOrganization.builder()
        //                         .orgCode("KRC")
        //                         .orgTranslationShort("KOR RAD")
        //                         .orgTranslation("KOREAN RADIO CLUB")
        //                         .inactive(true)
        //                         .build();

        //         String requestBody = mapper.writeValueAsString(editedOrganizations);

        //         when(ucsbOrganizationRepository.findById(eq("KRC"))).thenReturn(Optional.empty());

        //         // act
        //         MvcResult response = mockMvc.perform(
        //                         put("/api/ucsborganization?orgCode=KRC")
        //                                         .contentType(MediaType.APPLICATION_JSON)
        //                                         .characterEncoding("utf-8")
        //                                         .content(requestBody)
        //                                         .with(csrf()))
        //                         .andExpect(status().isNotFound()).andReturn();

        //         // assert
        //         verify(ucsbOrganizationRepository, times(1)).findById("KRC");
        //         Map<String, Object> json = responseToJson(response);
        //         assertEquals("UCSBOrganization with id KRC not found", json.get("message"));

        // }
        
}