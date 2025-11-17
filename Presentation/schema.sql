-- MySQL schema generated from Java JPA entities in the project
-- Database: myleague

CREATE DATABASE IF NOT EXISTS `myleague` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `myleague`;

-- Table: `user`
CREATE TABLE `user` (
  `userId` CHAR(36) NOT NULL,
  `username` VARCHAR(255),
  `password` VARCHAR(255),
  `email` VARCHAR(255),
  `phonenumber` VARCHAR(100),
  `fullname` VARCHAR(255),
  `imgPath` VARCHAR(512),
  `isBan` TINYINT(1) DEFAULT 0,
  `role` VARCHAR(50),
  PRIMARY KEY (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: `club`
CREATE TABLE `club` (
  `clubId` CHAR(36) NOT NULL,
  `clubName` VARCHAR(255),
  `clubLogoPath` VARCHAR(512),
  `clubDescription` TEXT,
  `clubPrimaryColor` VARCHAR(50),
  `clubSecondaryColor` VARCHAR(50),
  `clubFounded` VARCHAR(100),
  `clubHomeKit` VARCHAR(255),
  `clubAwayKit` VARCHAR(255),
  `clubThirdKit` VARCHAR(255),
  `isActive` TINYINT(1),
  `clubStadium` VARCHAR(255),
  `clubStadiumCapacity` INT,
  `userId` CHAR(36),
  PRIMARY KEY (`clubId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: `product`
CREATE TABLE `product` (
  `productId` CHAR(36) NOT NULL,
  `productName` VARCHAR(255),
  `productDescription` TEXT,
  `productSize` VARCHAR(50),
  `productPrice` DOUBLE,
  `productAmount` INT,
  `productImgPath` VARCHAR(512),
  `categoryProduct` VARCHAR(50),
  PRIMARY KEY (`productId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: `orders`
CREATE TABLE `orders` (
  `orderId` CHAR(36) NOT NULL,
  `orderCode` BIGINT,
  `orderInfo` TEXT,
  `orderTotalMoney` DOUBLE,
  `orderDateCreated` DATETIME,
  `orderStatus` VARCHAR(50),
  `userId` CHAR(36),
  PRIMARY KEY (`orderId`),
  INDEX `idx_orders_userId` (`userId`),
  CONSTRAINT `fk_orders_user` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: `blog`
CREATE TABLE `blog` (
  `blogId` CHAR(36) NOT NULL,
  `blogTitle` VARCHAR(255),
  `blogContent` MEDIUMTEXT,
  `blogDateCreated` DATETIME,
  `blogThumnailPath` VARCHAR(512),
  `blogCategory` VARCHAR(50),
  `club_id` CHAR(36),
  PRIMARY KEY (`blogId`),
  INDEX `idx_blog_club` (`club_id`),
  CONSTRAINT `fk_blog_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`clubId`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: `match`  (note: table name is `match` to match JPA mapping)
CREATE TABLE `match` (
  `matchId` CHAR(36) NOT NULL,
  `matchTitle` VARCHAR(255),
  `matchStartTime` DATETIME,
  `matchDescription` TEXT,
  `matchLinkLivestream` VARCHAR(512),
  `matchReferenceInformation` TEXT,
  `matchMOM` CHAR(36),
  PRIMARY KEY (`matchId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: `ticket`
CREATE TABLE `ticket` (
  `ticketId` CHAR(36) NOT NULL,
  `ticketTitle` VARCHAR(255),
  `ticketPrice` DOUBLE,
  `ticketAmount` INT,
  `ticketType` VARCHAR(50),
  `ticketArea` VARCHAR(50),
  `matchId` CHAR(36),
  PRIMARY KEY (`ticketId`),
  INDEX `idx_ticket_matchId` (`matchId`),
  CONSTRAINT `fk_ticket_match` FOREIGN KEY (`matchId`) REFERENCES `match` (`matchId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: `user_ticket`
CREATE TABLE `user_ticket` (
  `userTicketId` CHAR(36) NOT NULL,
  `userTicketAmount` INT,
  `userId` CHAR(36),
  `ticketId` CHAR(36),
  PRIMARY KEY (`userTicketId`),
  INDEX `idx_userticket_userId` (`userId`),
  INDEX `idx_userticket_ticketId` (`ticketId`),
  CONSTRAINT `fk_userticket_user` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE SET NULL,
  CONSTRAINT `fk_userticket_ticket` FOREIGN KEY (`ticketId`) REFERENCES `ticket` (`ticketId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: `player`
CREATE TABLE `player` (
  `playerId` CHAR(36) NOT NULL,
  `playerFullName` VARCHAR(255),
  `playerNumber` INT,
  `playerImgPath` VARCHAR(512),
  `playerInformation` TEXT,
  `playerNationaly` VARCHAR(255),
  `playerScores` INT,
  `playerAssist` INT,
  `playerAppearances` INT,
  `playerCleanSheets` INT,
  `playerPosition` VARCHAR(50),
  `clubId` CHAR(36),
  PRIMARY KEY (`playerId`),
  INDEX `idx_player_clubId` (`clubId`),
  CONSTRAINT `fk_player_club` FOREIGN KEY (`clubId`) REFERENCES `club` (`clubId`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: `match_event`
CREATE TABLE `match_event` (
  `matchEventId` CHAR(36) NOT NULL,
  `matchEventTitle` VARCHAR(255),
  `matchEventMinute` INT,
  `icon` VARCHAR(255),
  `vidUrl` VARCHAR(512),
  `matchEventThumnails` VARCHAR(512),
  `dateCreated` DATETIME,
  `matchEventType` VARCHAR(50),
  `matchId` CHAR(36),
  `playerId` CHAR(36),
  PRIMARY KEY (`matchEventId`),
  INDEX `idx_matchevent_matchId` (`matchId`),
  INDEX `idx_matchevent_playerId` (`playerId`),
  CONSTRAINT `fk_matchevent_match` FOREIGN KEY (`matchId`) REFERENCES `match` (`matchId`) ON DELETE CASCADE,
  CONSTRAINT `fk_matchevent_player` FOREIGN KEY (`playerId`) REFERENCES `player` (`playerId`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: `match_player_stat`
CREATE TABLE `match_player_stat` (
  `matchPlayerStatId` CHAR(36) NOT NULL,
  `matchPlayerMinutedPlayed` INT,
  `matchPlayerGoal` INT,
  `matchPlayerAssist` INT,
  `matchPlayerPass` INT,
  `matchPlayerShoots` INT,
  `rating` DOUBLE,
  `isStarter` TINYINT(1),
  `matchId` CHAR(36),
  `playerId` CHAR(36),
  PRIMARY KEY (`matchPlayerStatId`),
  INDEX `idx_mps_matchId` (`matchId`),
  INDEX `idx_mps_playerId` (`playerId`),
  CONSTRAINT `fk_mps_match` FOREIGN KEY (`matchId`) REFERENCES `match` (`matchId`) ON DELETE CASCADE,
  CONSTRAINT `fk_mps_player` FOREIGN KEY (`playerId`) REFERENCES `player` (`playerId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: `match_club_stat`
CREATE TABLE `match_club_stat` (
  `matchClubStatId` CHAR(36) NOT NULL,
  `matchClubStatYellowCard` INT,
  `matchClubStatRedCard` INT,
  `matchClubStatShoots` INT,
  `matchClubStatPass` INT,
  `matchClubStatCorners` INT,
  `matchClubStatBallTimes` INT,
  `matchClubStatScore` INT,
  `matchId` CHAR(36),
  `clubId` CHAR(36),
  PRIMARY KEY (`matchClubStatId`),
  INDEX `idx_mcs_matchId` (`matchId`),
  INDEX `idx_mcs_clubId` (`clubId`),
  CONSTRAINT `fk_mcs_match` FOREIGN KEY (`matchId`) REFERENCES `match` (`matchId`) ON DELETE CASCADE,
  CONSTRAINT `fk_mcs_club` FOREIGN KEY (`clubId`) REFERENCES `club` (`clubId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: `notification`
CREATE TABLE `notification` (
  `notificationId` CHAR(36) NOT NULL,
  `notificationTitle` VARCHAR(255),
  `notificationContent` TEXT,
  `notificationDateCreated` VARCHAR(255),
  `userReceived` CHAR(36),
  PRIMARY KEY (`notificationId`),
  INDEX `idx_notification_user` (`userReceived`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`userReceived`) REFERENCES `user` (`userId`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: `comment`
CREATE TABLE `comment` (
  `commentId` CHAR(36) NOT NULL,
  `commentContent` TEXT,
  `commentDateCreated` DATETIME,
  `commentCreatedBy` CHAR(36),
  `blogId` CHAR(36),
  `matchId` CHAR(36),
  PRIMARY KEY (`commentId`),
  INDEX `idx_comment_user` (`commentCreatedBy`),
  INDEX `idx_comment_blog` (`blogId`),
  INDEX `idx_comment_match` (`matchId`),
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`commentCreatedBy`) REFERENCES `user` (`userId`) ON DELETE SET NULL,
  CONSTRAINT `fk_comment_blog` FOREIGN KEY (`blogId`) REFERENCES `blog` (`blogId`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_match` FOREIGN KEY (`matchId`) REFERENCES `match` (`matchId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: `club_subscriber`
CREATE TABLE `club_subscriber` (
  `id` CHAR(36) NOT NULL,
  `email` VARCHAR(255),
  `club_id` CHAR(36),
  `createdAt` DATETIME,
  PRIMARY KEY (`id`),
  INDEX `idx_clubsubscriber_club` (`club_id`),
  CONSTRAINT `fk_clubsubscriber_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`clubId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: `request`
CREATE TABLE `request` (
  `requestId` CHAR(36) NOT NULL,
  `requestTitle` VARCHAR(255),
  `requestInfor` TEXT,
  `requestDateCreate` DATETIME,
  `requestDateUpdate` DATETIME,
  `requestStatus` VARCHAR(50),
  PRIMARY KEY (`requestId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- End of schema
