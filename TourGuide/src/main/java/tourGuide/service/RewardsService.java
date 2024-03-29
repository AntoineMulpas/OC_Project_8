package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tourGuide.util.GpsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private final int           defaultProximityBuffer = 10;
	private       int           proximityBuffer = defaultProximityBuffer;
	private final int           attractionProximityRange = 200;
	private final GpsUtil       gpsUtil;
	private final RewardCentral rewardsCentral;
	private List<Attraction> attractions;



	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;

		getAttractions();
	}

	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}


	public List<User> calculateRewardsForAllUsers(List<User> userList) {
		List<User> listToReturn = new ArrayList<>();

		ExecutorService service = Executors.newFixedThreadPool(50);
		try {
		for (User user : userList) {
			service.execute(() -> {
				if (user != null) {
					listToReturn.add(calculateRewards(user));
				}
			});
		}
		service.shutdown();
		service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return listToReturn;
	}

	private void getAttractions() {
		attractions = gpsUtil.getAttractions();
	}


	/* TODO: Change usersLocation instantiation condition.

	 */

	public User calculateRewards(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();

		for(VisitedLocation visitedLocation : userLocations) {
			for(Attraction attraction : attractions) {
					if(user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
						if(nearAttraction(visitedLocation, attraction)) {
							user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
						}
					}
			}
		}
		return user;
	}


	public double isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location);
	}
	/*
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return (getDistance(attraction, location) < attractionProximityRange);
	}
	 */

	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return (getDistance(attraction, visitedLocation.location) < proximityBuffer);
	}

	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
		return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
	}

}
