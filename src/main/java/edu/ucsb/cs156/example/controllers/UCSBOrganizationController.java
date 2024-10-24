package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * This is a REST controller for UCSBOrganization
 */

@Tag(name = "UCSBOrganization")
@RequestMapping("/api/ucsborganization")
@RestController
@Slf4j
public class UCSBOrganizationController extends ApiController {

    @Autowired
    UCSBOrganizationRepository ucsbOrganizationRepository;

    /**
     * THis method returns a list of all ucsborgnization.
     * @return a list of all ucsborgnization
     */

    @Operation(summary= "List all ucsb organizations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBOrganization> allOrganizations() {
        Iterable<UCSBOrganization> organizations = ucsbOrganizationRepository.findAll();
        return organizations;
    }

    /**
     * This method returns a single organizations.
     * @param orgCode code of the organizations
     * @return a single organizations
     */
    @Operation(summary= "Get a single organizations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBOrganization getById(
            @Parameter(name="orgCode") @RequestParam String orgCode) {
        UCSBOrganization organizations = ucsbOrganizationRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

        return organizations;
    }

    /**
     * This method creates a new organizations. Accessible only to users with the role "ROLE_ADMIN".
     * @param orgCode code of the organizations
     * @param orgTranslationShort name of organization translation short
     * @param orgTranslation name of organization translation
     * @param inactive whether or not the organizations is active
     * @return the save organizations
     */

    @Operation(summary= "Create a new organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBOrganization postOrganizations(
        @Parameter(name="orgCode") @RequestParam String orgCode,
        @Parameter(name="orgTranslationShort") @RequestParam String orgTranslationShort,
        @Parameter(name="orgTranslation") @RequestParam String orgTranslation,
        @Parameter(name="inactive") @RequestParam boolean inactive
        )
        {

        UCSBOrganization organizations = new UCSBOrganization();
        organizations.setOrgCode(orgCode);
        organizations.setOrgTranslationShort(orgTranslationShort);
        organizations.setOrgTranslation(orgTranslation);
        organizations.setInactive(inactive);

        UCSBOrganization savedOrganizations = ucsbOrganizationRepository.save(organizations);

        return savedOrganizations;
    }

    // /**
    //  * Delete a organizations. Accessible only to users with the role "ROLE_ADMIN".
    //  * @param orgCode code of the organizations
    //  * @return a message indiciating the organizations was deleted
    //  */
    // @Operation(summary= "Delete a UCSBOrganization")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    // @DeleteMapping("")
    // public Object deleteOrganizations(
    //         @Parameter(name="orgCode") @RequestParam String orgCode) {
    //     UCSBOrganization organizations = ucsbOrganizationRepository.findById(orgCode)
    //             .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

    //     ucsbOrganizationRepository.delete(organizations);
    //     return genericMessage("UCSBOrganization with id %s deleted".formatted(orgCode));
    // }

    // /**
    //  * Update a single organizations. Accessible only to users with the role "ROLE_ADMIN".
    //  * @param orgCode code of the organizations
    //  * @param incoming the new organizations contents
    //  * @return the updated organizations object
    //  */
    // @Operation(summary= "Update a single organizations")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    // @PutMapping("")
    // public UCSBOrganization updateOrganizations(
    //         @Parameter(name="orgCode") @RequestParam String orgCode,
    //         @RequestBody @Valid UCSBOrganization incoming) {

    //     UCSBOrganization organizations = ucsbOrganizationRepository.findById(orgCode)
    //             .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));


    //     organizations.setOrgCode(incoming.getOrgCode());  
    //     organizations.setOrgTranslationShort(incoming.getOrgTranslationShort());
    //     organizations.setOrgTranslation(incoming.getOrgTranslation());
    //     organizations.setInactive(incoming.getInactive());

    //     ucsbOrganizationRepository.save(organizations);

    //     return organizations;
    // }
}





