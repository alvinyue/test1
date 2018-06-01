package com.intuit.cg.backendtechassessment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application for seller to post/create projects for buyer to bid or auto bid against.
 *
 * Technology/Design trade-offs:
 *		1) Used the given Spring Boot project template - It is a popular platform for Java servers ide applications and
 *	       I was already doing projects with it. Downside is that it could take more time to configure correctly.
 *	    2) Used the preset Hibernate for ORM, embedded Tomcat for running as they are industry standards that can do
 *	    	the job. they provide easy transfer of knowledge.
 *		3) Used the preset for in memory database - I was familiar this approach for prototyping. The downside is
 *			deployment to real environment will require real DB and behavior may not be the same.
 *		4) Since the application is not too complicated, tried not to add new dependencies or configurations
 *		5) To keep things simple, didn't try to optimize queries with DB indexes.
 *
 * Architecture:
 *		This application was written as a typical Spring REST Web application with database access handled by JPA. Like
 * most Spring application, the "Inversion of Control" approach is used a lot. Also, the classes utilized Spring
 * and Java's annotations to denote roles and behaviors. A MVC pattern in the app where M is the data model, V is the
 * JSON rendering in the web response, C is the controller that handles the web requests. Web requests are forwarded to
 * the @RestController to dispatch/handle. Delegation pattern is used so a lot of the business logic were performed by
 * the JobMarketplaceService.java class. If we want different implementations, we can extract the interface from
 * JobMarketplaceService.java On the other hand, delegation or strategy pattern was not used for the 2 getProjectById()
 * and getProjectById2() minBid calculation methods because it's easier to keep them together with the database access
 * objects. When the auto bid implementation is fully tested, we can remove the other one to simplify. Data access is
 * handled with Data objects annotated as @Entity and stored/retrieved with the corresponding "Data Access Object"
 * classes annotated and implemented as @Repository. The responses were using objects in models package directly. To
 * have better control of the data model versus the returned field/values, one could use different JSON objects to
 * represent some of the JSON responses to control values exponsed in the response. Here we use objects in models to
 * keep the exercise simple.
 *
 * Auto Bid:
 *		The details for auto bidding is in the class comment of JobMarketplaceService.java
 *
 * Tests:
 *		Actual unit tests are in BackendTechAssessmentApplicationTests.java The tests are based around
 *
 *		1) 0, 1, 2...n objects
 *		2) edge conditions like min, max values, pass deadline.
 *		3) different combination of values, large + small, small and large.
 *	    4) handling of invalid values, missing values.
 *
 * 		test finding minimum bid with fixed bids and/or auto bits.
 *			just "static bids" (find min)
 *			just "auto bids"   (find min)
 *			just "static bids" + new "auto bid" (static smaller)
 *			just "static bids" + new "auto bid" (static larger)
 *			just "static bids" + new "auto bid" + "static bids" (2 static smaller)
 *			just "static bids" + new "auto bid" + "static bids" (2 static larger)
 *			just "static bids" + new "auto bid" + new "auto bid" (2 auto smaller)
 *			just "static bids" + new "auto bid" + new "auto bid" (2 auto larger)
 *
 * 		DB test to check storing and retrieving Buyer, Seller, Project, Bid, and any additional data objects/table.
 *
 *	    Integration tests can be performed with external scripts or tool so that:
 *	    1) script send a legit request to the web app.
 *	    2) script received a response from web app.
 *	    3) verify the data and/or syntax of the response.
 *	    4) check values used in request with min/max/invalid values and repeat step 1)
 *
 */
@SpringBootApplication
public class BackendTechAssessmentApplication {
	public static void main(String[] args) {
		SpringApplication.run(BackendTechAssessmentApplication.class, args);
	}
}
