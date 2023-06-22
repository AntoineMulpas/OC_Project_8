package tourGuide.service;

import org.springframework.stereotype.Service;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;

@Service
public class UserPreferencesService {

    public User setUserPreferences(User user, UserPreferences userPreferences) {
        user.setUserPreferences(userPreferences);
        return user;
    }

}
