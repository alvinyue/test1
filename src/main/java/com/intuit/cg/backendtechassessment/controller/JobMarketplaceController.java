package com.intuit.cg.backendtechassessment.controller;

import com.intuit.cg.backendtechassessment.controller.requestmappings.RequestMappings;
import com.intuit.cg.backendtechassessment.json.JsonResult;
import com.intuit.cg.backendtechassessment.models.*;
import com.intuit.cg.backendtechassessment.repository.JobMarketplaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.List;

@RestController
public class JobMarketplaceController {
    private static Logger log = LoggerFactory.getLogger(JobMarketplaceController.class);
    private JobMarketplaceService jobMarketplaceService;

    public JobMarketplaceController(JobMarketplaceService jobMarketplaceService) {
        assert(jobMarketplaceService != null);
        this.jobMarketplaceService = jobMarketplaceService;
    }

    @RequestMapping(value = RequestMappings.INIT, produces = MediaType.APPLICATION_JSON_VALUE)
    //public JsonResult init() {
    public ResponseEntity<Object> init() {
        jobMarketplaceService.init();
        //return new JsonResult("ok");
        //return new JsonResult(new ResponseEntity<Object>("ok", HttpStatus.OK));
        return new ResponseEntity<Object>(new JsonResult("ok"), HttpStatus.OK);
    }

    @RequestMapping(value = RequestMappings.PROJECTS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getProjects() {
        return new ResponseEntity<Object>(new JsonResult(jobMarketplaceService.getProjects()), HttpStatus.OK);
    }

    @RequestMapping(value = RequestMappings.SELLERS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getSellers() {
        return new ResponseEntity<Object>(new JsonResult(jobMarketplaceService.getSellers()), HttpStatus.OK);
    }

    @RequestMapping(value = RequestMappings.BUYERS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getBuyers() {
        return new ResponseEntity<Object>(new JsonResult(jobMarketplaceService.getBuyers()), HttpStatus.OK);
    }

    @RequestMapping(value = RequestMappings.BIDS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getBids() {
        return new ResponseEntity<Object> (new JsonResult(jobMarketplaceService.getBids()), HttpStatus.OK);
    }

    @RequestMapping(value = RequestMappings.GET_PROJECT_BY_ID, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getProjectById(@RequestParam(value="projectId", defaultValue="-1") long projectId) {
        if (projectId < 0) {
            return new ResponseEntity<Object> (new ErrorMessage("param projectId undefined or invalid"), HttpStatus.OK);
        }
        //AY, use getProjectById() to handle bids as fixed, and getProjectById2() to handle auto bid
        return new ResponseEntity<Object>(new JsonResult(jobMarketplaceService.getProjectById2(projectId)), HttpStatus.OK);
    }

    //debugging use
    @RequestMapping(value = "/get-bid-by-project-id", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getBidsByProjectId(@RequestParam(value="projectId", defaultValue="-1") long projectId) {
        if (projectId < 0) {
            return new ResponseEntity<Object> (new ErrorMessage("param projectId undefined or invalid"), HttpStatus.OK);
        }
        return new ResponseEntity<Object>(new JsonResult(jobMarketplaceService.getBidsByProjectId(projectId)), HttpStatus.OK);
    }

    @RequestMapping(value = RequestMappings.CREATE_PROJECT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createProject(
            @RequestParam(value="sellerId", defaultValue="-1") long sellerId,
            @RequestParam(value="description", defaultValue="") String description,
            @RequestParam(value="maxBudget", defaultValue="-1") long maxBudget,
            @RequestParam(value="bidDeadline", defaultValue="") String bidDeadline) {

        //TODO validate descriptions, and deadline are well formed (e.g. no illegal strings)
        if (sellerId < 0) {
            return new ResponseEntity<Object>(new ErrorMessage("param sellerId undefined"), HttpStatus.OK);
        }
        if (maxBudget < 0) {
            return new ResponseEntity<Object>(new ErrorMessage("param maxBudget undefined"), HttpStatus.OK);
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime bidDeadlineConverted = LocalDateTime.parse(bidDeadline, formatter);
            return new ResponseEntity<Object>(new JsonResult(jobMarketplaceService.createProject(
                    sellerId, description, maxBudget, bidDeadlineConverted)), HttpStatus.OK);
        } catch (DateTimeParseException | IllegalArgumentException e) {
            return new ResponseEntity<Object> (new ErrorMessage(e.getMessage()), HttpStatus.OK);
        }
    }

    @RequestMapping(value = RequestMappings.ADD_NEW_BID, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addNewBid(
            @RequestParam(value="amount", defaultValue="-1") long amount,
            @RequestParam(value="projectId", defaultValue="-1") long projectId,
            @RequestParam(value="buyerId", defaultValue="-1") long buyerId,
            @RequestParam(value="autoBid", defaultValue="false") boolean autoBid,
            @RequestParam(value="minAmount", defaultValue="-1") long minAmount) {
        if (amount < 0) {
            return new ResponseEntity<Object> (new ErrorMessage("param amount undefined"), HttpStatus.OK);
        }
        if (projectId < 0) {
            return new ResponseEntity<Object> (new ErrorMessage("param projectId undefined or invalid"), HttpStatus.OK);
        }
        if (buyerId < 0) {
            return new ResponseEntity<Object> (new ErrorMessage("param buyerId undefined"), HttpStatus.OK);
        }
        if (autoBid && minAmount < 0) {
            return new ResponseEntity<Object> (new ErrorMessage("param minAmount undefined"), HttpStatus.OK);
        }
        log.info("addNewBid() amount={} projectId={} buyerId={} autoBid={} minAmount={}",
                amount, projectId, buyerId, autoBid, minAmount);
        try {
            return new ResponseEntity<Object>(new JsonResult(jobMarketplaceService.addNewBid(
                    amount, projectId, buyerId, autoBid, minAmount)), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<Object> (new ErrorMessage(e.getMessage()), HttpStatus.OK);
        }
    }

}