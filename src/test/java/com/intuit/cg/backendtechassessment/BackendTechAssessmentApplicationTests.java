package com.intuit.cg.backendtechassessment;

import com.intuit.cg.backendtechassessment.models.Bid;
import com.intuit.cg.backendtechassessment.models.Buyer;
import com.intuit.cg.backendtechassessment.models.Project;
import com.intuit.cg.backendtechassessment.models.Seller;
import com.intuit.cg.backendtechassessment.repository.*;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Unit tests for job Marketplace logic. H2 database repositories were used. Another approach is to create and setup
 * Mock objects for the Repositories and inject them into jobMarketplaceService object.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class BackendTechAssessmentApplicationTests {
	private static Logger log = LoggerFactory.getLogger(BackendTechAssessmentApplicationTests.class);
	private static final int DEADLINE_DELAY_SECONDS = 3;

	@Autowired
	private JobMarketplaceService jobMarketplaceService;

	private LocalDateTime deadline = null;
	private Buyer buyer = null, buyer2 = null;
	private Seller seller1 = null, seller2 = null, seller3 = null;
	private Project projectOriginal;
	private EntityManager em;

	@PersistenceContext
	public void setEntityManager(EntityManager em) {
		log.debug("setEntityManager() ");
		this.em = em;
	}

	@Before
	public void setUp() {
		deadline = jobMarketplaceService.calculateDeadline(0, DEADLINE_DELAY_SECONDS);
		buyer = jobMarketplaceService.addBuyer("ABC");
		buyer2 = jobMarketplaceService.addBuyer("DEF");

		seller1 = jobMarketplaceService.addSeller("Seller1a");
		seller2 = jobMarketplaceService.addSeller("Seller2a");
		seller3 = jobMarketplaceService.addSeller("Seller3a");

		projectOriginal = jobMarketplaceService.createProject(seller1.getId(),"project 1", 100, deadline);
	}

	@After
	public void clear() {
		this.em.clear();
	}

	@Test
	public void contextLoads() {
	}

	private void dumpBids() {
		List<Bid> bids = jobMarketplaceService.getBids();
		if (bids != null) {
			for (Bid bid : bids) {
				log.info("dumpBids() bid {}", bid);
			}
		}
	}

	protected void delay(int second) {
		try {
			Thread.sleep(second * 1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	//check final minimum bid
	protected Project checkFinalMinimumBid(String label, long projectId) {
		//wait until after project bid deadline to get project. Otherwise, bid status will still be NONE
		delay(DEADLINE_DELAY_SECONDS +1);
		Project project =jobMarketplaceService.getProjectById2(projectId);
		if(project != null) {
			log.info("{} minBid={} minBidId={} bidStatus={}", label, project.getMinBid(), project.getMinBidId(), project.getBidStatus());
		}
		return project;
	}

	/** test to create 2 projects and retrieve projects. also test calculate minimum bid with no bid */
	@Test
	public void testGetProjectById() {
		log.info("testGetProjectById() create project 2");
		Project project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testGetProjectById() get project id {} got id {}", projectOriginal.getId(), project.getId());
		assert (projectOriginal.getId() == project.getId());

		log.info("testGetProjectById() create project 2");
		Project projectSecond = jobMarketplaceService.createProject(seller2.getId(),"project 2", 200, deadline);
		Project project2 = jobMarketplaceService.getProjectById2(projectSecond.getId());
		log.info("testGetProjectById() get project id {} got id {}", projectSecond.getId(), project2.getId());
		assert (projectSecond.getId() == project2.getId());
		assert (project2.getBidStatus() == Project.BidStatus.NONE); //before final calculation at deadline

		project2 = checkFinalMinimumBid("testGetProjectById()", projectSecond.getId());
		assert (project2.getBidStatus() == Project.BidStatus.MINIMUM_NOT_FOUND);
	}

	/** test to pick lowest bid from 2 bids */
	@Test
	public void testProjectAndBids() {
		log.info("testProjectAndBids() create project");
		Project project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectAndBids() minBid={} bidStatus={}", project.getMinBid(), project.getBidStatus());

		jobMarketplaceService.addNewBid(10, projectOriginal.getId(), buyer.getId(),false, 0);
		project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectAndBids() after bid of 10 minBid={} bidStatus={}", project.getMinBid(), project.getBidStatus());

		jobMarketplaceService.addNewBid(5, projectOriginal.getId(), buyer2.getId(),false, 0);
		project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectAndBids() after bid of 5 minBid={} bidStatus={}", project.getMinBid(), project.getBidStatus());

		dumpBids();

		project = checkFinalMinimumBid("testProjectAndBids()", projectOriginal.getId());
		assert(project.getMinBid() == 5);
	}



	@Test
	public void testProjectAndLateBid() {
		log.info("testProjectAndLateBid()");
		LocalDateTime deadline = jobMarketplaceService.calculateDeadline(-1, 0); //use expired deadline

		Project projectOriginal = jobMarketplaceService.createProject(seller1.getId(),"project 1", 100, deadline);
		Project project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectAndLateBid() minBid={} bidStatus={}", project.getMinBid(), project.getBidStatus());

		//check if new bid will be rejected
		boolean lateBidExceptionFound = false;
		try {
			jobMarketplaceService.addNewBid(10, projectOriginal.getId(), buyer.getId(),false, 0);
		} catch (IllegalArgumentException e) {
			log.error("testProjectAndLateBid() error={}", e.getMessage());
			if (e.getMessage().indexOf("bid after deadline") >= 0) {
				lateBidExceptionFound = true;
			}
		}
		assert(lateBidExceptionFound);

		dumpBids();

		//check final minimum bid
		//no need for delay, current time already late
		project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectAndLateBid() minBid again={} bidStatus={}", project.getMinBid(), project.getBidStatus());
		assert(project.getBidStatus() == Project.BidStatus.MINIMUM_NOT_FOUND);
	}

	@Test
	public void testProjectAndBidTooHigh() {
		log.info("testProjectAndBidTooHigh()");

		Project project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectAndBidTooHigh() minBid={} bidStatus={}", project.getMinBid(), project.getBidStatus());

		jobMarketplaceService.addNewBid(10000, projectOriginal.getId(), buyer.getId(),false, 0);

		dumpBids();

		project = checkFinalMinimumBid("testProjectAndBidTooHigh()", projectOriginal.getId());
		assert(project.getBidStatus() == Project.BidStatus.MINIMUM_TOO_HIGH);
	}

	@Test
	public void testProjectAndMinBidFound() {
		log.info("testProjectAndMinBidFound()");
		jobMarketplaceService.addNewBid(10, projectOriginal.getId(), buyer.getId(),false, 0);

		dumpBids();

		Project project = checkFinalMinimumBid("testProjectAndMinBidFound()", projectOriginal.getId());
		assert(project.getBidStatus() == Project.BidStatus.MINIMUM_FOUND);
	}


	/** test to pick lowest bid from 2 bids and 1 auto bids */
	@Test
	public void testProjectBidsAnd1AutoBid() {
		log.info("testProjectBidsAnd1AutoBid()");
		Project project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectBidsAnd1AutoBid() minBid={} bidStatus={}", project.getMinBid(), project.getBidStatus());

		jobMarketplaceService.addNewBid(10, projectOriginal.getId(), buyer.getId(),false, 0);
		project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectBidsAnd1AutoBid() after bid of 10 minBid={} bidStatus={}", project.getMinBid(), project.getBidStatus());

		jobMarketplaceService.addNewBid(5, projectOriginal.getId(), buyer2.getId(), false, 0);
		project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectBidsAnd1AutoBid() after bid of 5 minBid={} bidStatus={}", project.getMinBid(), project.getBidStatus());

		jobMarketplaceService.addNewBid(5, projectOriginal.getId(), buyer2.getId(), true, 1);
		project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectBidsAnd1AutoBid() after bid of 5 and 1 minBid={} minBidId={} bidStatus={}",
				project.getMinBid(), project.getMinBidId(), project.getBidStatus());

		dumpBids();

		project = checkFinalMinimumBid("testProjectBidsAnd1AutoBid()", projectOriginal.getId());
		assert(project.getMinBid() == 4);
	}

	/** test to pick lowest bid from 2 bids and 2 auto bids */
	@Test
	public void testProjectBidsAnd2AutoBids() {
		log.info("testProjectBidsAnd2AutoBids()");
		Project project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectBidsAnd2AutoBids() minBid={} bidStatus={}", project.getMinBid(), project.getBidStatus());

		jobMarketplaceService.addNewBid(10, projectOriginal.getId(), buyer.getId(),false, 0);
		project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectBidsAnd2AutoBids() after bid of 10 minBid={} bidStatus={}", project.getMinBid(), project.getBidStatus());

		jobMarketplaceService.addNewBid(5, projectOriginal.getId(), buyer2.getId(), false, 0);
		project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectBidsAnd2AutoBids() after bid of 5 minBid={} bidStatus={}", project.getMinBid(), project.getBidStatus());

		jobMarketplaceService.addNewBid(5, projectOriginal.getId(), buyer2.getId(), true, 1);
		project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectBidsAnd2AutoBids() after bid of 5 and 1 minBid={} minBidId={} bidStatus={}",
				project.getMinBid(), project.getMinBidId(), project.getBidStatus());
		jobMarketplaceService.addNewBid(5, projectOriginal.getId(), buyer2.getId(), true, 4);
		project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectBidsAnd2AutoBids() after bid of 5 and 4 minBid={} minBidId={} bidStatus={}",
				project.getMinBid(), project.getMinBidId(), project.getBidStatus());

		dumpBids();

		project = checkFinalMinimumBid("testProjectBidsAnd2AutoBids()", projectOriginal.getId());
		assert(project.getMinBid() == 3);
	}

	@Test
	public void testProject1FixedBidAnd1AutoBid() {
		log.info("testProjectBidsAnd1AutoBid()");
		Project project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectBidsAnd1AutoBid() minBid={} bidStatus={}", project.getMinBid(), project.getBidStatus());

		jobMarketplaceService.addNewBid(3, projectOriginal.getId(), buyer.getId(),false, 0);
		project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectBidsAnd1AutoBid() after bid of 6 minBid={} bidStatus={}", project.getMinBid(), project.getBidStatus());

		jobMarketplaceService.addNewBid(5, projectOriginal.getId(), buyer2.getId(), true, 4);
		project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectBidsAnd1AutoBid() after bid of 5 and 1 minBid={} minBidId={} bidStatus={}",
				project.getMinBid(), project.getMinBidId(), project.getBidStatus());

		dumpBids();

		project = checkFinalMinimumBid("testProjectBidsAnd1AutoBid()", projectOriginal.getId());
		assert(project.getMinBid() == 3);
	}

	@Test
	public void testProject1FixedBidAnd2AutoBids() {
		log.info("testProject1FixedBidAnd2AutoBids()");
		Project project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProject1FixedBidAnd2AutoBids() minBid={} bidStatus={}", project.getMinBid(), project.getBidStatus());

		jobMarketplaceService.addNewBid(3, projectOriginal.getId(), buyer.getId(),false, 0);
		project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProject1FixedBidAnd2AutoBids() after bid of 10 minBid={} bidStatus={}", project.getMinBid(), project.getBidStatus());

		jobMarketplaceService.addNewBid(5, projectOriginal.getId(), buyer2.getId(), true, 4);
		project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProject1FixedBidAnd2AutoBids() after bid of 5 and 1 minBid={} minBidId={} bidStatus={}",
				project.getMinBid(), project.getMinBidId(), project.getBidStatus());

		jobMarketplaceService.addNewBid(6, projectOriginal.getId(), buyer2.getId(), true, 4);
		project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectBidsAnd2AutoBids() after bid of 5 and 4 minBid={} minBidId={} bidStatus={}",
				project.getMinBid(), project.getMinBidId(), project.getBidStatus());

		dumpBids();

		project = checkFinalMinimumBid("testProject1FixedBidAnd2AutoBids()", projectOriginal.getId());
		assert(project.getMinBid() == 3);
	}

	@Test
	public void testProjectNoFixedBidAnd1AutoBids() {
		log.info("testProjectNoFixedBidAnd1AutoBids()");
		Project project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectNoFixedBidAnd1AutoBids() minBid={} bidStatus={}", project.getMinBid(), project.getBidStatus());

		jobMarketplaceService.addNewBid(5, projectOriginal.getId(), buyer2.getId(), true, 4);
		project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectNoFixedBidAnd1AutoBids() after bid of 5 and 4 minBid={} minBidId={} bidStatus={}",
				project.getMinBid(), project.getMinBidId(), project.getBidStatus());

		dumpBids();

		project = checkFinalMinimumBid("testProjectNoFixedBidAnd1AutoBids()", projectOriginal.getId());
		//min bid = 5 as there is only 1 bid, no need to cut down amount.
		assert(project.getMinBid() == 5);
	}

	@Test
	public void testProjectNoFixedBidAnd2AutoBids() {
		log.info("testProject1FixedBidAnd2AutoBids()");
		Project project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProject1FixedBidAnd2AutoBids() minBid={} bidStatus={}", project.getMinBid(), project.getBidStatus());

		jobMarketplaceService.addNewBid(5, projectOriginal.getId(), buyer2.getId(), true, 4);
		project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProject1FixedBidAnd2AutoBids() after bid of 5 and 4 minBid={} minBidId={} bidStatus={}",
				project.getMinBid(), project.getMinBidId(), project.getBidStatus());

		jobMarketplaceService.addNewBid(6, projectOriginal.getId(), buyer2.getId(), true, 2);
		project = jobMarketplaceService.getProjectById2(projectOriginal.getId());
		log.info("testProjectBidsAnd2AutoBids() after bid of 6 and 42 minBid={} minBidId={} bidStatus={}",
				project.getMinBid(), project.getMinBidId(), project.getBidStatus());

		dumpBids();

		project = checkFinalMinimumBid("testProject1FixedBidAnd2AutoBids()", projectOriginal.getId());
		assert(project.getMinBid() == 3);
	}
}