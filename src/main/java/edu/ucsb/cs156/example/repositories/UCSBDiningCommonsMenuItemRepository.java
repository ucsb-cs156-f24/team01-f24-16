package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;

import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * The UCSBDiningCommonsMenuItemRepository is a repository for UCSBDiningCommonsMenuItem entities
 */

@Repository
public interface UCSBDiningCommonsMenuItemRepository extends CrudRepository<UCSBDiningCommonsMenuItem, Long> {
    // /**
    //  * This method returns all UCSBDate entities with a given quarterYYYYQ.
    //  * @param quarterYYYYQ quarter in the format YYYYQ (e.g. 20241 for Winter 2024, 20242 for Spring 2024, 20243 for Summer 2024, 20244 for Fall 2024)
    //  * @return all UCSBDate entities with a given quarterYYYYQ
    //  */
    // Iterable<UCSBDate> findAllByQuarterYYYYQ(String quarterYYYYQ);
  }


