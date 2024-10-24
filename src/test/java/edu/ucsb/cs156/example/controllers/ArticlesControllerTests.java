package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.eq;
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



    @Test
    public void logged_out_users_cannot_get_article_by_id() throws Exception {
        mockMvc.perform(get("/api/articles?id=7"))
                .andExpect(status().is(403)); // logged out users can't get articles by id
    }

    // Authorization tests for /api/articles/put and delete can be added if needed

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_article_by_id_when_it_exists() throws Exception {
        // arrange
        LocalDateTime dateAdded1 = LocalDateTime.parse("2022-01-01T00:00:00");

        Articles article = Articles.builder()
                .id(7L)
                .title("First Article")
                .url("https://first.com")
                .explanation("This is the first article.")
                .email("first@example.com")
                .dateAdded(dateAdded1)
                .build();

        when(articleRepository.findById(eq(7L))).thenReturn(Optional.of(article));

        // act
        MvcResult response = mockMvc.perform(get("/api/articles?id=7"))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(articleRepository, times(1)).findById(eq(7L));
        String expectedJson = mapper.writeValueAsString(article);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_cannot_get_article_when_id_does_not_exist() throws Exception {
        // arrange
        when(articleRepository.findById(eq(7L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(get("/api/articles?id=7"))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(articleRepository, times(1)).findById(eq(7L));
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("Articles with id 7 not found", json.get("message")); // Corrected message to plural "Articles"
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_article() throws Exception {
        // arrange
        LocalDateTime dateAdded1 = LocalDateTime.parse("2022-01-01T00:00:00");
        LocalDateTime newDateAdded = LocalDateTime.parse("2022-12-31T00:00:00");

        Articles originalArticle = Articles.builder()
                .id(67L)
                .title("Original Article")
                .url("https://original.com")
                .explanation("This is the original content.")
                .email("original@example.com")
                .dateAdded(dateAdded1)
                .build();

        Articles editedArticle = Articles.builder()
                .id(67L)
                .title("Edited Article")
                .url("https://edited.com")
                .explanation("This is the edited content.")
                .email("edited@example.com")
                .dateAdded(newDateAdded) // Retain the same dateAdded
                .build();

        String requestBody = mapper.writeValueAsString(editedArticle);

        when(articleRepository.findById(eq(67L))).thenReturn(Optional.of(originalArticle));

        // act
        MvcResult response = mockMvc.perform(
                        put("/api/articles?id=67")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .content(requestBody)
                                .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(articleRepository, times(1)).findById(67L);
        verify(articleRepository, times(1)).save(editedArticle); 
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);

        // Check that the new date is correctly saved
        assertEquals(newDateAdded, editedArticle.getDateAdded());
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_article_that_does_not_exist() throws Exception {
        // arrange
        LocalDateTime dateAdded1 = LocalDateTime.parse("2022-01-01T00:00:00");

        Articles editedArticle = Articles.builder()
                .id(67L)
                .title("Edited Article")
                .url("https://edited.com")
                .explanation("This is the edited content.")
                .email("edited@example.com")
                .dateAdded(dateAdded1)
                .build();

        String requestBody = mapper.writeValueAsString(editedArticle);

        when(articleRepository.findById(eq(67L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                        put("/api/articles?id=67")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .content(requestBody)
                                .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(articleRepository, times(1)).findById(67L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("Articles with id 67 not found", json.get("message")); // Corrected message to plural "Articles"
    }

}