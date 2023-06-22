package tourGuide;

import gpsUtil.location.Attraction;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tourGuide.user.User;
import tourGuide.util.GpsUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class TestPerformance {
	
	/*
	 * A note on performance improvements:
	 *     
	 *     The number of users generated for the high volume tests can be easily adjusted via this method:
	 *     
	 *     		InternalTestHelper.setInternalUserNumber(100000);
	 *     
	 *     
	 *     These tests can be modified to suit new solutions, just as long as the performance metrics
	 *     at the end of the tests remains consistent. 
	 * 
	 *     These are performance metrics that we are trying to hit:
	 *     
	 *     highVolumeTrackLocation: 100,000 users within 15 minutes:
	 *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
	 *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */


	/* TODO: Change internal user number up to 100_000

	 */

	@Test
	public void highVolumeTrackLocation() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(100_000);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		List <User> allUsers = tourGuideService.getAllUsers();
		
	    StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		tourGuideService.trackAllUsersLocation(allUsers);

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	/* TODO: Refactor method below.
	 */
	
	@Test
	public void highVolumeGetRewards() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		// Users should be incremented up to 100,000, and test finishes within 20 minutes
		InternalTestHelper.setInternalUserNumber(100_000);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		
	    Attraction attraction = gpsUtil.getAttractions().get(0);
		//List <User> allUsers = tourGuideService.getAllUsers();
		//allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

		UserService userService = new UserService();
		List <User> allUsers = userService.addVisitedLocationsForEachUser(tourGuideService.getAllUsers(), attraction);

		System.out.println(TimeUnit.MILLISECONDS.toMinutes(stopWatch.getTime()));
		System.out.println("Done adding visited location.");

		List <User> users = rewardsService.calculateRewardsForAllUsers(allUsers);
		System.out.println(TimeUnit.MILLISECONDS.toMinutes(stopWatch.getTime()));
		System.out.println("Done calculating rewards.");


		stopWatch.stop();
		System.out.println("Finished: starting to assert. " + TimeUnit.MILLISECONDS.toMinutes(stopWatch.getTime()));
		tourGuideService.tracker.stopTracking();

		for(User user : users) {
			assertTrue(user.getUserRewards().size() > 0);
		}


		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
	
}
