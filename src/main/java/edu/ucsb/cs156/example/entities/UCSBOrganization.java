package edu.ucsb.cs156.example.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 
 * This is a JPA entity that represents a UCSBOrganization
 * 
 * A UCSBOrganization is an organizations at UCSB
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "ucsborganization")

public class UCSBOrganization {
  @Id
 
  private String orgCode;
  private String orgTranslationShort;
  private String orgTranslation;
  @Getter private boolean inactive;
}