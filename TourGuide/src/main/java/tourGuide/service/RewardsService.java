package tourGuide.service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tourGuide.util.GpsUtil;

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
	}

	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}


	public List<User> calculateRewardsForAllUsers(List<User> userList) {
		getAttractions();

		List<User> listToReturn = new ArrayList<>();
		ExecutorService service = Executors.newFixedThreadPool(30);
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

	public void calculateRewards(User user) {
			List <VisitedLocation> userLocations = new ArrayList <>();

			if (user != null && user.getVisitedLocations().size() != 0) {
				userLocations.addAll(user.getVisitedLocations());
			} else {
				userLocations.add(new VisitedLocation(new UUID(123L, 12L), new Location(12330, 1233), new Date()));
			}

			Iterator <VisitedLocation> iterator = userLocations.iterator();
			while (iterator.hasNext()) {
				VisitedLocation visitedLocation = iterator.next();
				double longitude = visitedLocation.location.longitude;
				if (user.getUserRewards().stream().noneMatch(r -> map.containsKey(r.attraction.longitude))) {
					Attraction attraction = map.get(longitude);
					if (attraction != null) {
						if (nearAttraction(visitedLocation, attraction)) {
							user.addUserReward(new UserReward(visitedLocation, map.get(longitude), getRewardPoints(map.get(longitude), user)));
						}
					}
				}
			}

	}


	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}

	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
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
