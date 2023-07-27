package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import tourGuide.user.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {


    /*
    public List <User> addVisitedLocationsForEachUser(List<User> userList, Attraction attraction) {
        List<User> listToReturn = new ArrayList <>();
        ExecutorService service = Executors.newFixedThreadPool(15);
        try {
        for (User user : userList) {
            service.execute(() -> {
                user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
                listToReturn.add(user);
            });
        }
            service.shutdown();
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return listToReturn;
    }

     */


}
