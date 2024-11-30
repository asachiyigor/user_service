package school.faang.user_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.faang.user_service.entity.Country;

import java.util.Optional;

@Repository
public interface UserCountryRepository extends CrudRepository<Country, Long> {

    @Query("SELECT c FROM Country c WHERE LOWER(c.title) = LOWER(:title)")
    Optional<Country> findByNameCaseInsensitive(@Param("title") String title);
}