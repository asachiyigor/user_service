package school.faang.user_service.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.faang.user_service.entity.Country;
import school.faang.user_service.repository.UserCountryRepository;

@Service
@RequiredArgsConstructor
public class UserCountryService {
    private final UserCountryRepository countryRepository;

    public synchronized Country createCountryIfNotExists(String title) {
        return countryRepository.findByNameCaseInsensitive(title)
                .orElseGet(() -> {
                    Country newCountry = new Country();
                    newCountry.setTitle(title);
                    return countryRepository.save(newCountry);
                });
    }
}