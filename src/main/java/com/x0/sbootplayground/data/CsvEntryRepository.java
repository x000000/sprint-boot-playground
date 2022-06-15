package com.x0.sbootplayground.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface CsvEntryRepository extends CrudRepository<CsvEntry, Long> {

    @Query("from CsvEntry where code=:code")
    public CsvEntry findByCode(@Param("code") String code);

}
