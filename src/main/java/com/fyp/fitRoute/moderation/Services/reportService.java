package com.fyp.fitRoute.moderation.Services;

import com.fyp.fitRoute.moderation.Entity.reports;
import com.fyp.fitRoute.moderation.Repositories.reportRepo;
import com.fyp.fitRoute.moderation.Utilities.reportCard;
import com.fyp.fitRoute.moderation.Utilities.reportResponse;
import com.fyp.fitRoute.notifications.Entity.userConfig;
import com.fyp.fitRoute.notifications.Repositories.userConfigRepo;
import com.fyp.fitRoute.security.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class reportService {
    @Autowired
    private reportRepo reportRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private userConfigRepo userConfigRepository;

    public void saveReport(String reason, String reportedUserId , String reporterId, String reportedUsername) {

        userConfig userCon = userConfigRepository.findById(reportedUserId).orElse(null);

        if (userCon == null) {
            // If userConfig does not exist, create a new one
            userCon= new userConfig();
            userCon.setNotificationsTokens(new ArrayList<>());

            userCon.setUsername(reportedUsername);
            userCon.setSuspended(false);
            userCon.setSuspensionEndDate(null);
            userConfigRepository.save(userCon);
        }

        reports report = new reports();
        report.setReportedBy(reporterId);
        report.setReportedUserId(reportedUserId);
        report.setReason(reason);
        reportRepository.save(report);

        if (reportRepository.findByReportedUserId(reportedUserId).size() > 10 && !userCon.isSuspended()) {
            // Suspend the user if they have more than 10 reports
            userCon.setSuspended(true);
            userCon.setSuspensionEndDate(Date.from(Instant.now().plusSeconds(86400*7L))); // Suspend for 7 days
            userConfigRepository.save(userCon);
        }
    }


    public void deleteReportByUserId(String reportedUserId) {
        Query query = new Query(Criteria.where("reportedUserId").is(reportedUserId));
        mongoTemplate.findAllAndRemove(query, reports.class);
    }

    public List<reportResponse> getAllReportsOfUser(String reportedUserId) {
        List<reportResponse> reportList = new ArrayList<>();
        reportRepository.findByReportedUserId(reportedUserId).stream()
                .map(report -> {
                    reportResponse reportResponse = new reportResponse();
                    User reporter = mongoTemplate.findById(report.getReportedBy(), User.class);
                    if (reporter != null) {
                        reportResponse.setReporterUsername(reporter.getUsername());
                        reportResponse.setReporterImage(reporter.getImage());
                        reportResponse.setReason(report.getReason());
                    }
                    reportList.add(reportResponse);
                    return report;
                }).toList();
        return reportList;
    }

    // get all user's report cards
    public List<reportCard> getAllReportCards() {
        List<reports> allReports = reportRepository.findAll();
        Map<String, reportCard> reportCards = new HashMap<>();

        for (reports report : allReports) {
           if (reportCards.containsKey(report.getReportedUserId())){
               // Increment the report count for existing user
                reportCard existingCard = reportCards.get(report.getReportedUserId());
                existingCard.setReportCount(existingCard.getReportCount() + 1);
           } else {
                User user = mongoTemplate.findById(report.getReportedUserId(), User.class);
                if (user != null) {
                    reportCard newCard = new reportCard();
                    newCard.setUsername(user.getUsername());
                    newCard.setReportCount(1);
                    newCard.setImageUrl(user.getImage());
                    reportCards.put(report.getReportedUserId(), newCard);
                }
           }
        }

        return new ArrayList<>(reportCards.values());
    }

    // get number of unsuspended accounts
    public int getNumberOfUnsuspendedAccounts() {
        List<User> users = mongoTemplate.findAll(User.class);
        for (User user : users) {
            userConfig existingUserConfig = userConfigRepository.findByUsername(user.getUsername()).orElse(null);
            if (existingUserConfig == null) {
                // If userConfig does not exist, create a new one
                existingUserConfig = new userConfig();
                existingUserConfig.setNotificationsTokens(new ArrayList<>());
                existingUserConfig.setUsername(user.getUsername());
                existingUserConfig.setSuspended(false);
                existingUserConfig.setSuspensionEndDate(null);
                userConfigRepository.save(existingUserConfig);
            }
        }
        return userConfigRepository.findAllBySuspended(false).size();
    }

    @Scheduled(fixedRate = 86400000) // Runs every 24 hours
    public void unsuspendAccounts() {
        List<userConfig> suspendedUsers = userConfigRepository.findAllBySuspended(true);
        suspendedUsers.forEach(user -> {
            if (user.getSuspensionEndDate().before(Date.from(Instant.now()))) {
                user.setSuspended(false);
                user.setSuspensionEndDate(null);
                userConfigRepository.save(user);
                //delete all reports for this user
                String id = mongoTemplate.find(new Query(
                        Criteria.where("username").is(user.getUsername())
                ), User.class).get(0).getId();
                deleteReportByUserId(id);
            }
        });
    }

    public int getNumberOfReports() {
        return (int) reportRepository.count();
    }
}
