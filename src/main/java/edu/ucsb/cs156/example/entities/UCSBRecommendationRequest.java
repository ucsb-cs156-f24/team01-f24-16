package edu.ucsb.cs156.example.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "ucsbrecommendationrequests")
public class UCSBRecommendationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String requesterEmail;
    private String professorEmail;
    private String explanation;
    private LocalDateTime dateRequested;
    private LocalDateTime dateNeeded;
    private boolean done;
    
}