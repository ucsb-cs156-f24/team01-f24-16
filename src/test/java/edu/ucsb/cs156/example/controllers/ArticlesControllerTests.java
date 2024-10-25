package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

    @MockBean
    ArticlesRepository articleRepository;  // Use the correct field name

    @MockBean
    UserRepository userRepository;

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/articles/all"))
                .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/articles/all"))
                .andExpect(status().isOk()); // logged in users can get all
    }

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/articles/post"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/articles/post"))
                .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_articles() throws Exception {

        // Arrange
        LocalDateTime dateAdded1 = LocalDateTime.parse("2024-10-23T00:00:00");
        Articles article1 = Articles.builder()
                .id(1L)
                .title("First Article")
                .url("https://first.com")
                .explanation("This is the first article.")
                .email("first@example.com")
                .dateAdded(dateAdded1)
                .build();

        LocalDateTime dateAdded2 = LocalDateTime.parse("2024-10-24T00:00:00");
        Articles article2 = Articles.builder()
                .id(2L)
                .title("Second Article")
                .url("https://second.com")
                .explanation("This is the second article.")
                .email("second@example.com")
                .dateAdded(dateAdded2)
                .build();

        ArrayList<Articles> expectedArticles = new ArrayList<>();
        expectedArticles.addAll(Arrays.asList(article1, article2));

        when(articleRepository.findAll()).thenReturn(expectedArticles);  // Corrected call

        // Act
        MvcResult response = mockMvc.perform(get("/api/articles/all"))
                .andExpect(status().isOk()).andReturn();

        // Assert
        verify(articleRepository, times(1)).findAll();  // Corrected call
        String expectedJson = mapper.writeValueAsString(expectedArticles);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN" })
    @Test
    public void an_admin_user_can_post_a_new_article() throws Exception {

        // Arrange
        LocalDateTime dateAdded = LocalDateTime.parse("2024-10-23T00:00:00");
        Articles article = Articles.builder()
                .id(1L)
                .title("First Article")
                .url("https://first.com")
                .explanation("This is the first article.")
                .email("first@example.com")
                .dateAdded(dateAdded)
                .build();

        when(articleRepository.save(eq(article))).thenReturn(article);  // Corrected call

        // Act
        MvcResult response = mockMvc.perform(
                post("/api/articles/post")
                        .param("id", "1")
                        .param("title", "First Article")
                        .param("url", "https://first.com")
                        .param("explanation", "This is the first article.")
                        .param("email", "first@example.com")
                        .param("dateAdded", "2024-10-23T00:00:00")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // Assert
        verify(articleRepository, times(1)).save(article);  // Corrected call
        String expectedJson = mapper.writeValueAsString(article);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }
}