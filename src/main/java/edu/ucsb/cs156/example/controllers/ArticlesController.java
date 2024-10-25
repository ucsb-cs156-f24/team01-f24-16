package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.time.LocalDateTime;

/**
 * This is a REST controller for Articles
 */

@Tag(name = "Articles", description = "Endpoints for managing articles")
@RequestMapping("/api/articles")
@RestController
@Slf4j
public class ArticlesController extends ApiController {

    @Autowired
    ArticlesRepository ArticlesRepository;

    /**
     * List all articles
     * 
     * @return an iterable of Articles
     */
    @Operation(summary= "List all articles", description = "Retrieve all articles from the database")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Articles> allArticles() {
        Iterable<Articles> articles = ArticlesRepository.findAll();
        return articles;
    }

    /**
     * Create a new article
     * 
     * @param title the title of the article
     * @param url the URL of the article
     * @param explanation the explanation of the article
     * @param email the associated email
     * @param dateAdded the date the article was added
     * @return the saved Article
     */
    @Operation(summary= "Create a new article", description = "Create a new article by providing the necessary details")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Articles postArticle(
            @Parameter(description = "Title of the article", example = "First Article") @RequestParam String title,
            @Parameter(description = "URL of the article", example = "https://first.com") @RequestParam String url,
            @Parameter(description = "Explanation of the article", example = "This is a sample explanation.") @RequestParam String explanation,
            @Parameter(description = "Email associated with the article", example = "first@example.com") @RequestParam String email,
            @Parameter(description = "Date when the article was added", example = "2024-10-23T00:00:00") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateAdded) 
            throws JsonProcessingException {

        log.info("dateAdded={}", dateAdded);

        Articles article = new Articles();
        article.setTitle(title);
        article.setUrl(url);
        article.setExplanation(explanation);
        article.setEmail(email);
        article.setDateAdded(dateAdded);

        Articles savedArticle = ArticlesRepository.save(article);
        
        return savedArticle;        
    }


     /**
     * Get a single article by id
     * 
     * @param id the id of the article
     * @return an article
     */
    @Operation(summary= "Get a single article", description = "Retrieve a single article by providing the ID")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Articles getById(
            @Parameter(name="id") @RequestParam Long id) {
        Articles article = ArticlesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

        return article;
    }

    /**
     * Update a single article
     * 
     * @param id       id of the article to update
     * @param incoming the new article object
     * @return the updated article object
     */
    @Operation(summary= "Update a single article", description = "Update a single article by providing the ID and the new article data")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Articles updateArticle(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid Articles incoming) {

        Articles article = ArticlesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

        article.setTitle(incoming.getTitle());
        article.setUrl(incoming.getUrl());
        article.setExplanation(incoming.getExplanation());
        article.setEmail(incoming.getEmail());
        article.setDateAdded(incoming.getDateAdded());

        ArticlesRepository.save(article);

        return article;
    }

    /**
     * Delete an article
     * 
     * @param id the id of the article to delete
     * @return a message indicating the article was deleted
     */
    @Operation(summary= "Delete a single article", description = "Delete a single article by providing the ID")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteArticle(
            @Parameter(name="id") @RequestParam Long id) {
        Articles article = ArticlesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

        ArticlesRepository.delete(article);
        return genericMessage("Article with id %s deleted".formatted(id));
    }

}
