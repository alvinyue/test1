package com.intuit.cg.backendtechassessment.repository;

import com.intuit.cg.backendtechassessment.models.Bid;
import com.intuit.cg.backendtechassessment.models.Buyer;
import com.intuit.cg.backendtechassessment.models.Project;
import com.intuit.cg.backendtechassessment.models.Seller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * assume time is measured with default timezone and locale
 *
 * Note: we might want to factor out min bid calculation so that the getProjects method can call it as well.
 *
 * Optimization: In getProjectById we used a query to get new bids that are added after last calculation to save
 * processing time
 *
 * Auto Bid:
 * calculate the final minimum bid only when the deadline is passed.
 * auto bid only requires the 2 lowest auto bid limits entries as they would define the lowest
 * when lowest fixed bid is included the final lowest can be found. Here are the cases
 *
 *    1. only fixed bids (no auto bid to process)
 *    2. fixed bids + 1 auto bid (either fix bid or auto bid is lowest, if auto bid's minimum is lower, auto bid wins)
 *    3. fixed bids + more than 1 auto bid (either fix bid or an auto bid is lowest. if fixed bid is lower than
 *       the minimum of the auto bids, fixed bid won. If auto bids minimum is lower than fixed bid, the 2 lowest
 *       auto bid minimum values will be used to define the minimum bid.
 *    4. only auto bids (a special case of case 3)
 */
@Service("jobMarketplaceService")
public class JobMarketplaceService {

    private static Logger log = LoggerFactory.getLogger(JobMarketplaceService.class);
    private static final long BID_OFFSET = 1; //value to reduce amount in each round of auto bid

    private ProjectRepository projectRepository;
    private BidRepository bidRepository;
    private BuyerRepository buyerRepository;
    private SellerRepository sellerRepository;

    public JobMarketplaceService(ProjectRepository projectRepository, BidRepository bidRepository,
                                 BuyerRepository buyerRepository, SellerRepository sellerRepository) {
        assert(projectRepository != null);
        assert(bidRepository != null);
        assert(buyerRepository != null);
        assert(sellerRepository != null);

        this.projectRepository = projectRepository;
        this.bidRepository = bidRepository;
        this.buyerRepository = buyerRepository;
        this.sellerRepository = sellerRepository;
    }

    /**
     * calculates current time offset by given min/second (for deadline usage).
     *
     * @param minuteOffset controls +/- no. of minutes offset from current time
     * @param secondOffset controls +/- no. of seconds offset from current time
     */
    public LocalDateTime calculateDeadline(int minuteOffset, int secondOffset) {
        LocalDateTime currentTime = LocalDateTime.now();
        return currentTime.plusMinutes(minuteOffset).plusSeconds(secondOffset);
    }

    /** load default buyer, seller, project data into DB */
    @Transactional
    public void init() {

        //default buyers
        Buyer buyer1 = addBuyer("Buyer1");
        Buyer buyer2 = addBuyer("Buyer2");
        Buyer buyer3 = addBuyer("Buyer3");

        //default sellers
        Seller seller1 = addSeller("Seller1");
        Seller seller2 = addSeller("Seller2");
        Seller seller3 = addSeller("Seller3");

        //default projects
        Project project1 = createProject(seller1.getId(), "Project 1", 100, LocalDateTime.now().plusMinutes(1));
        createProject(seller2.getId(),"Project 2",  200, LocalDateTime.now().plusMinutes(2));
        createProject(seller3.getId(),"Project 3",  300, LocalDateTime.now().plusMinutes(3));

        //default bids
        log.info("add new bids for projectId={} buyerID={}", project1.getId(), buyer1.getId());
        addNewBid(100, project1.getId(), buyer1.getId(), false, 0);
        addNewBid(200, project1.getId(), buyer1.getId(), false, 0);
        addNewBid(10, project1.getId(), buyer1.getId(), false, 0);
    }

    @Transactional
    public List<Project> getProjects() {
        return projectRepository.findAll();
    }

    @Transactional
    public List<Seller> getSellers() {
        return sellerRepository.findAll();
    }

    @Transactional
    public List<Buyer> getBuyers() {
        return buyerRepository.findAll();
    }

    @Transactional
    public List<Bid> getBids() {
        return bidRepository.findAll();
    }

    @Transactional
    public List<Bid> getBidsByProjectId(long projectId) {
        return bidRepository.findAllByProject(projectId);
    }

    /** returns a project with the lowest bid amount or null if project not found. This method does not calculate auto bid */
    // need to determine lowest bid and winner bid (real winner if the lowest bid was entered before bid deadline
    // and current time is after bid deadline)
    @Transactional
    public Project getProjectById(long id) {
        log.info("getProjectById() id={}", id);
        Project project = projectRepository.find(id);
        if (project == null) {
            return null;
        }

        //if current date/time is after deadline and bidstatus is defined, final min bid is calculated.
        LocalDateTime currentDate = LocalDateTime.now();
        if (currentDate.compareTo(project.getBidDeadline()) > 0 && project.getBidStatus() != Project.BidStatus.NONE) {
            log.info("getProjectById() use stored result");
            return project;
        }

        //process the unprocessed bids to save processing
        List<Bid> bids = bidRepository.findProcessedByProject(project.getId(), false);

        if (bids != null) {
            long minBid = project.getMinBid();
            long minBidId = project.getMinBidId();
            boolean foundMinBid = false;
            for (Bid bid : bids) {
                //check bid was submitted earlier than bid deadline
                log.info("getProjectById() bids {}", bid.toString());
                LocalDateTime bidDate = bid.getBidDate();

                if( (bidDate != null && bidDate.compareTo(project.getBidDeadline()) < 0) && bid.getAmount() < minBid) {
                    minBid = bid.getAmount();
                    minBidId = bid.getId();
                    foundMinBid = true;
                }
                bid.setProcessed(true);
                bidRepository.persist(bid);
            }
            if(foundMinBid) {
                project.setMinBid(minBid);
                project.setMinBidId(minBidId);
                projectRepository.persist(project);
            }
        }

        //calculate the final minimum bid only when the deadline is passed.
        if (currentDate.compareTo(project.getBidDeadline()) > 0) {
            log.info("getProjectById() calculate miniBid after bid deadline");
            if(project.getBidStatus() == Project.BidStatus.NONE) {
                long minBid = project.getMinBid();
                if (minBid == project.DEFAULT_MIN_BID) {
                    project.setBidStatus(Project.BidStatus.MINIMUM_NOT_FOUND);
                } else
                if (minBid > project.getMaxBudget()) {
                    project.setBidStatus(Project.BidStatus.MINIMUM_TOO_HIGH);
                } else
                if (minBid < project.getMaxBudget()) {
                    project.setBidStatus(Project.BidStatus.MINIMUM_FOUND);
                }
                projectRepository.persist(project);
            }

        }

        return project;
    }

    protected void updateBidAndProject(Project project, Bid bid, long minBid) {
        bid.setWinningBid(minBid);
        bidRepository.persist(bid);
        project.setMinBid(minBid);
        project.setMinBidId(bid.getId());
        projectRepository.persist(project);
    }

    /** returns a project with the lowest bid amount or null if project not found. This method calculates auto bid */
    @Transactional
    public Project getProjectById2(long id) {
        log.info("getProjectById2() id={}", id);
        Project project = projectRepository.find(id);
        if (project == null) {
            return null;
        }

        //if current date/time is after deadline and bidstatus is defined, final min bid is calculated.
        LocalDateTime currentDate = LocalDateTime.now();
        if (currentDate.compareTo(project.getBidDeadline()) > 0 && project.getBidStatus() != Project.BidStatus.NONE) {
            log.info("getProjectById2() use stored result");
            return project;
        }

        //process the unprocessed bids to save processing
        //AY, let this process fixed & auto bid on "amount": List<Bid> bids = bidRepository.findNonAutoProcessedByProject(project.getId(), false);
        List<Bid> bids = bidRepository.findProcessedByProject(project.getId(), false);
        if (bids != null) {
            long minBid = project.getMinBid();
            long minBidId = project.getMinBidId();
            boolean foundMinBid = false;
            for (Bid bid : bids) {
                //check bid was submitted earlier than bid deadline
                log.info("getProjectById2() bids {}", bid);
                LocalDateTime bidDate = bid.getBidDate();
                if(bid.getAmount() < minBid) {
                    minBid = bid.getAmount();
                    minBidId = bid.getId();
                    foundMinBid = true;
                }
                bid.setProcessed(true);
                bidRepository.persist(bid);
            }
            if(foundMinBid) {
                project.setMinBid(minBid);
                project.setMinBidId(minBidId);
                projectRepository.persist(project);
            }
        }

        if (currentDate.compareTo(project.getBidDeadline()) > 0) {
            log.info("getProjectById2() calculate miniBid after bid deadline");
            List<Bid> autoBids = bidRepository.findAutoByProject(project.getId());
            if (autoBids != null) {
                long minBid = project.getMinBid();
                if (autoBids.size() == 1) {
                    //case 2
                    Bid bid0 = autoBids.get(0);
                    long minBidId = project.getMinBidId();
                    //if minimum bid is the same as bid0, skip update amount
                    if (minBidId != bid0.getId()) {
                        if (bid0.getMinAmount() <= (minBid - BID_OFFSET)) {
                            minBid = minBid - BID_OFFSET;
                            updateBidAndProject(project, bid0, minBid);
                        } else {
                            //minBid, fixed bid already wins, no action
                        }
                    }
                } else
                if (autoBids.size() > 1) {
                    //case 3
                    Bid bid0 = autoBids.get(0);
                    Bid bid1 = autoBids.get(1);

                    if (bid0.getMinAmount() <= (bid1.getMinAmount() - BID_OFFSET)) {
                        long autoMinBid = bid1.getMinAmount() - BID_OFFSET;
                        if (autoMinBid < minBid) {
                            updateBidAndProject(project, bid0, autoMinBid);
                        }
                    } else
                    if (bid0.getMinAmount() == bid1.getMinAmount()){
                        long autoMinBid = bid0.getMinAmount();
                        if (autoMinBid < minBid) {
                            updateBidAndProject(project, bid0, autoMinBid);
                        }
                    }
                }
            }

            if(project.getBidStatus() == Project.BidStatus.NONE) {
                long minBid = project.getMinBid();
                if (minBid == project.DEFAULT_MIN_BID) {
                    project.setBidStatus(Project.BidStatus.MINIMUM_NOT_FOUND);
                } else
                if (minBid > project.getMaxBudget()) {
                    project.setBidStatus(Project.BidStatus.MINIMUM_TOO_HIGH);
                } else
                if (minBid < project.getMaxBudget()) {
                    project.setBidStatus(Project.BidStatus.MINIMUM_FOUND);
                }
                projectRepository.persist(project);
            }

        }

        return project;
    }

    @Transactional
    public Project createProject(long sellerId, String description, long maxBudget, LocalDateTime bidDeadline) {
        Seller seller = sellerRepository.find(sellerId);
        if(seller == null) {
            log.info("createProject() buyerId {} not found", sellerId);
            throw new IllegalArgumentException("seller for sellerId not found");
        }
        if(maxBudget <= 0) {
            log.info("createProject() maxBudget {} <=0", maxBudget);
            throw new IllegalArgumentException("maxBudget <=0");
        }
        if(bidDeadline == null) {
            log.info("createProject() bidDeadline is null");
            throw new IllegalArgumentException("bidDeadline is null");
        }
        Project project = new Project(sellerId, description, maxBudget, bidDeadline);
        projectRepository.save(project);

        return project;
    }

    /**
     * creates new bid for a project
     *
     * @amount the bid amount which cannot be smaller than minAmount when autoBid is true.
     * @throws IllegalArgumentException if projectId does not map to a project, buyerId does not map to a buyer,
     * or date time of addNewBid call is after project's bidding deadline
     */
    @Transactional
    public Bid addNewBid(long amount, long projectId, long buyerId, boolean autoBid, long minAmount) {
        //check if projectId, and buyerId are valid
        Project project = projectRepository.find(projectId);
        if(project == null) {
            log.info("addNewBid() projectId {} not found", projectId);
            throw new IllegalArgumentException("project for projectId not found");
        }

        Buyer buyer = buyerRepository.find(buyerId);
        if(buyer == null) {
            log.info("addNewBid() buyerId {} not found", buyerId);
            throw new IllegalArgumentException("buyer for buyerId not found");
        }

        if(autoBid && amount < minAmount) {
            log.info("addNewBid() autoBid is true and amount < minAmount", buyerId);
            throw new IllegalArgumentException("autoBid is true and amount < minAmount");
        }
        //if project's bid deadline is passed, reject bid.
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime bidDeadline = project.getBidDeadline();
        if (currentDate.compareTo(bidDeadline) > 0) {
            log.info("addNewBid() currentDate after bid deadline");
            throw new IllegalArgumentException("new bid after deadline");
        }
        Bid bid = new Bid(amount, currentDate, projectId, buyerId, autoBid, minAmount);
        bidRepository.save(bid);

        return bid;
    }

    @Transactional
    public Buyer addBuyer(String name) {
        Buyer buyer = new Buyer(name);
        buyerRepository.save(buyer);

        return buyer;
    }

    @Transactional
    public Seller addSeller(String name) {
        Seller seller = new Seller(name);
        sellerRepository.save(seller);

        return seller;
    }

}