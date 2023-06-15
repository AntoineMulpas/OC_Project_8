package tourGuide;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import rewardCentral.RewardCentral;
import tourGuide.service.RewardsService;
import tourGuide.util.GpsUtil;

@Configuration
public class TourGuideModule {
	
	@Bean
	public GpsUtil getGpsUtil() {
		return new GpsUtil();
	}
	
	@Bean
	public RewardsService getRewardsService() {
		return new RewardsService(getGpsUtil(), getRewardCentral());
	}
	
	@Bean
	public RewardCentral getRewardCentral() {
		return new RewardCentral();
	}
	
}
