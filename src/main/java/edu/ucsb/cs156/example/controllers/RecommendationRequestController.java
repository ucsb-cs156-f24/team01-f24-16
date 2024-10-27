package edu.ucsb.cs156.example.controllers;
import edu.ucsb.cs156.example.entities.UCSBRecommendationRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBRecommendationRequestRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
 * This is a REST controller for RecommendationRequest
 */


@Tag(name = "RecommendationRequest")
@RequestMapping("/api/recommendationRequest")
@RestController
@Slf4j
public class RecommendationRequestController extends ApiController {

    @Autowired
    UCSBRecommendationRequestRepository  ucsbRecommendationRequestRepository; 

        /**
     * List all UCSB dates
     * 
     * @return an iterable of UCSBDate
     */
    @Operation(summary= "List all ucsb recommendation requests")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/all")
    public Iterable<UCSBRecommendationRequest> allUCSBRecommendationRequestRepository() {
        Iterable<UCSBRecommendationRequest> requests = ucsbRecommendationRequestRepository.findAll();
        return requests;   
    }



    /**
     * Create a new rec
     * 
     * 
     * @param requesterEmail the name of the requestor : string 
     * @param professorEmail the name of the professor : string 
     * @param explanation what the request details : string
     * @param dateRequested date requestor sent request : iso format, e.g. YYYY-mm-ddTHH:MM:SS
     * @param dateNeeded date requestor needs request finished by : iso format, e.g. YYYY-mm-ddTHH:MM:SS
     * @param done the truth value of it being finished or not : bool
     * @return the saved recommendation request
     */
    @Operation(summary= "Create a recommendation request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/post")
    public UCSBRecommendationRequest postRecommendationRequest
    (
            @Parameter(name="requesterEmail") @RequestParam String requesterEmail,
            @Parameter(name="professorEmail") @RequestParam String professorEmail,
            @Parameter(name="explanation") @RequestParam String explanation,
            @Parameter(name="done") @RequestParam Boolean done,
            @Parameter(name="dateRequested", description="date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("dateRequested") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateRequested,
            @Parameter(name="dateNeeded", description="date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("dateNeeded") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateNeeded
    )
 
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        log.info("dateRequested={}", dateRequested);
        log.info("dateNeeded={}", dateNeeded);

        UCSBRecommendationRequest ucsbRecommendationRequest = new UCSBRecommendationRequest();
        ucsbRecommendationRequest.setRequesterEmail(requesterEmail);
        ucsbRecommendationRequest.setProfessorEmail(professorEmail);
        ucsbRecommendationRequest.setExplanation(explanation);
        ucsbRecommendationRequest.setDateRequested(dateRequested);
        ucsbRecommendationRequest.setDateNeeded(dateNeeded);
        ucsbRecommendationRequest.setDone(done);

        UCSBRecommendationRequest saveducsbRecommendationRequest = ucsbRecommendationRequestRepository.save(ucsbRecommendationRequest);

        return saveducsbRecommendationRequest;
    }

        /**
     * Get a single item on the menu by id
     * 
     * @param id the id of the menu item
     * @return a menu item
     */
    @Operation(summary= "Get a single recommendation by id")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBRecommendationRequest getById(
            @Parameter(name="id") @RequestParam Long id) {
                UCSBRecommendationRequest ucsbRecommendationRequest = ucsbRecommendationRequestRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBRecommendationRequest.class, id));

        return ucsbRecommendationRequest;
    }
    
}
