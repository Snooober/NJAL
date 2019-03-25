-- MySQL dump 10.13  Distrib 5.7.25, for Linux (x86_64)
--
-- Host: localhost    Database: njal
-- ------------------------------------------------------
-- Server version	5.7.25-0ubuntu0.18.04.2

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `archive_t`
--

DROP TABLE IF EXISTS `archive_t`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `archive_t` (
  `tourn_id` int(11) NOT NULL,
  `date` varchar(255) NOT NULL,
  `current_round_id` int(11) NOT NULL,
  `tourn` blob NOT NULL,
  PRIMARY KEY (`tourn_id`,`current_round_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `current_t`
--

DROP TABLE IF EXISTS `current_t`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `current_t` (
  `current_round_id` int(11) NOT NULL,
  `tourn` blob NOT NULL,
  PRIMARY KEY (`current_round_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player_info`
--

DROP TABLE IF EXISTS `player_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player_info` (
  `player_id` int(11) NOT NULL,
  `discord_id` varchar(255) NOT NULL,
  `discrim` int(11) NOT NULL,
  `steam_id` varchar(255) DEFAULT NULL,
  `discord_name` varchar(255) NOT NULL,
  `steam_name` varchar(255) DEFAULT NULL,
  `pend_reg` tinyint(1) NOT NULL DEFAULT '1',
  `role` varchar(255) DEFAULT NULL,
  `wins` int(11) NOT NULL DEFAULT '0',
  `games_played` int(11) NOT NULL DEFAULT '0',
  `byes` int(11) NOT NULL DEFAULT '0',
  `tourn_wins` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`player_id`),
  UNIQUE KEY `discord_id` (`discord_id`),
  UNIQUE KEY `steam_id` (`steam_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tourn_players`
--

DROP TABLE IF EXISTS `tourn_players`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tourn_players` (
  `order_reg` int(11) NOT NULL,
  `player_id` int(11) NOT NULL,
  `wins` int(11) NOT NULL DEFAULT '0',
  `games_played` int(11) NOT NULL DEFAULT '0',
  `byes` int(11) NOT NULL DEFAULT '0',
  `current_game_id` int(11) DEFAULT NULL,
  `invite_link` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`order_reg`),
  UNIQUE KEY `player_id` (`player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tourn_players_181209`
--

DROP TABLE IF EXISTS `tourn_players_181209`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tourn_players_181209` (
  `order_reg` int(11) NOT NULL,
  `player_id` int(11) NOT NULL,
  `wins` int(11) NOT NULL DEFAULT '0',
  `games_played` int(11) NOT NULL DEFAULT '0',
  `byes` int(11) NOT NULL DEFAULT '0',
  `current_game_id` int(11) DEFAULT NULL,
  `invite_link` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`order_reg`),
  UNIQUE KEY `player_id` (`player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-03-25  8:46:10
