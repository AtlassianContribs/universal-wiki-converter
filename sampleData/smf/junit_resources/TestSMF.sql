-- MySQL dump 10.10
--
-- Host: localhost    Database: TestSMF
-- ------------------------------------------------------
-- Server version	5.0.24-standard

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
-- Table structure for table `smf2_attachments`
--

DROP TABLE IF EXISTS `smf2_attachments`;
CREATE TABLE `smf2_attachments` (
  `ID_ATTACH` int(10) unsigned NOT NULL auto_increment,
  `ID_THUMB` int(10) unsigned NOT NULL default '0',
  `ID_MSG` int(10) unsigned NOT NULL default '0',
  `ID_MEMBER` mediumint(8) unsigned NOT NULL default '0',
  `attachmentType` tinyint(3) unsigned NOT NULL default '0',
  `filename` tinytext NOT NULL,
  `file_hash` varchar(40) NOT NULL default '',
  `size` int(10) unsigned NOT NULL default '0',
  `downloads` mediumint(8) unsigned NOT NULL default '0',
  `width` mediumint(8) unsigned NOT NULL default '0',
  `height` mediumint(8) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ID_ATTACH`),
  UNIQUE KEY `ID_MEMBER` (`ID_MEMBER`,`ID_ATTACH`),
  KEY `ID_MSG` (`ID_MSG`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_attachments`
--


/*!40000 ALTER TABLE `smf2_attachments` DISABLE KEYS */;
LOCK TABLES `smf2_attachments` WRITE;
INSERT INTO `smf2_attachments` VALUES (1,2,16,0,0,'cow.jpg','1369c5b06a8394704b9bd20d8e6e9191eda82494',23096,6,450,319),(2,0,16,0,3,'cow.jpg_thumb','df23fe19b1dc2e9c406e1ac5aaf62d2820ed3126',32256,0,150,106),(3,0,20,0,0,'ed.jpeg','0e1b4e5b7a679b7da4325e8725602f9c179a782a',10801,5,100,100),(4,5,21,0,0,'doublefacepalm.jpg','3ba8c5523756e7113c4bb5e5a06abf69f05223bb',67002,20,500,400),(5,0,21,0,3,'doublefacepalm.jpg_thumb','1e9446775ba7fee1326d070a546cc0762ce976f4',24288,0,150,120);
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_attachments` ENABLE KEYS */;

--
-- Table structure for table `smf2_ban_groups`
--

DROP TABLE IF EXISTS `smf2_ban_groups`;
CREATE TABLE `smf2_ban_groups` (
  `ID_BAN_GROUP` mediumint(8) unsigned NOT NULL auto_increment,
  `name` varchar(20) NOT NULL default '',
  `ban_time` int(10) unsigned NOT NULL default '0',
  `expire_time` int(10) unsigned default NULL,
  `cannot_access` tinyint(3) unsigned NOT NULL default '0',
  `cannot_register` tinyint(3) unsigned NOT NULL default '0',
  `cannot_post` tinyint(3) unsigned NOT NULL default '0',
  `cannot_login` tinyint(3) unsigned NOT NULL default '0',
  `reason` tinytext NOT NULL,
  `notes` text NOT NULL,
  PRIMARY KEY  (`ID_BAN_GROUP`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_ban_groups`
--


/*!40000 ALTER TABLE `smf2_ban_groups` DISABLE KEYS */;
LOCK TABLES `smf2_ban_groups` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_ban_groups` ENABLE KEYS */;

--
-- Table structure for table `smf2_ban_items`
--

DROP TABLE IF EXISTS `smf2_ban_items`;
CREATE TABLE `smf2_ban_items` (
  `ID_BAN` mediumint(8) unsigned NOT NULL auto_increment,
  `ID_BAN_GROUP` smallint(5) unsigned NOT NULL default '0',
  `ip_low1` tinyint(3) unsigned NOT NULL default '0',
  `ip_high1` tinyint(3) unsigned NOT NULL default '0',
  `ip_low2` tinyint(3) unsigned NOT NULL default '0',
  `ip_high2` tinyint(3) unsigned NOT NULL default '0',
  `ip_low3` tinyint(3) unsigned NOT NULL default '0',
  `ip_high3` tinyint(3) unsigned NOT NULL default '0',
  `ip_low4` tinyint(3) unsigned NOT NULL default '0',
  `ip_high4` tinyint(3) unsigned NOT NULL default '0',
  `hostname` tinytext NOT NULL,
  `email_address` tinytext NOT NULL,
  `ID_MEMBER` mediumint(8) unsigned NOT NULL default '0',
  `hits` mediumint(8) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ID_BAN`),
  KEY `ID_BAN_GROUP` (`ID_BAN_GROUP`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_ban_items`
--


/*!40000 ALTER TABLE `smf2_ban_items` DISABLE KEYS */;
LOCK TABLES `smf2_ban_items` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_ban_items` ENABLE KEYS */;

--
-- Table structure for table `smf2_board_permissions`
--

DROP TABLE IF EXISTS `smf2_board_permissions`;
CREATE TABLE `smf2_board_permissions` (
  `ID_GROUP` smallint(5) NOT NULL default '0',
  `ID_BOARD` smallint(5) unsigned NOT NULL default '0',
  `permission` varchar(30) NOT NULL default '',
  `addDeny` tinyint(4) NOT NULL default '1',
  PRIMARY KEY  (`ID_GROUP`,`ID_BOARD`,`permission`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_board_permissions`
--


/*!40000 ALTER TABLE `smf2_board_permissions` DISABLE KEYS */;
LOCK TABLES `smf2_board_permissions` WRITE;
INSERT INTO `smf2_board_permissions` VALUES (-1,0,'poll_view',1),(0,0,'remove_own',1),(0,0,'lock_own',1),(0,0,'mark_any_notify',1),(0,0,'mark_notify',1),(0,0,'modify_own',1),(0,0,'poll_add_own',1),(0,0,'poll_edit_own',1),(0,0,'poll_lock_own',1),(0,0,'poll_post',1),(0,0,'poll_view',1),(0,0,'poll_vote',1),(0,0,'post_attachment',1),(0,0,'post_new',1),(0,0,'post_reply_any',1),(0,0,'post_reply_own',1),(0,0,'delete_own',1),(0,0,'report_any',1),(0,0,'send_topic',1),(0,0,'view_attachments',1),(2,0,'moderate_board',1),(2,0,'post_new',1),(2,0,'post_reply_own',1),(2,0,'post_reply_any',1),(2,0,'poll_post',1),(2,0,'poll_add_any',1),(2,0,'poll_remove_any',1),(2,0,'poll_view',1),(2,0,'poll_vote',1),(2,0,'poll_edit_any',1),(2,0,'report_any',1),(2,0,'lock_own',1),(2,0,'send_topic',1),(2,0,'mark_any_notify',1),(2,0,'mark_notify',1),(2,0,'delete_own',1),(2,0,'modify_own',1),(2,0,'make_sticky',1),(2,0,'lock_any',1),(2,0,'remove_any',1),(2,0,'move_any',1),(2,0,'merge_any',1),(2,0,'split_any',1),(2,0,'delete_any',1),(2,0,'modify_any',1),(3,0,'moderate_board',1),(3,0,'post_new',1),(3,0,'post_reply_own',1),(3,0,'post_reply_any',1),(3,0,'poll_post',1),(3,0,'poll_add_own',1),(3,0,'poll_remove_any',1),(3,0,'poll_view',1),(3,0,'poll_vote',1),(3,0,'report_any',1),(3,0,'lock_own',1),(3,0,'send_topic',1),(3,0,'mark_any_notify',1),(3,0,'mark_notify',1),(3,0,'delete_own',1),(3,0,'modify_own',1),(3,0,'make_sticky',1),(3,0,'lock_any',1),(3,0,'remove_any',1),(3,0,'move_any',1),(3,0,'merge_any',1),(3,0,'split_any',1),(3,0,'delete_any',1),(3,0,'modify_any',1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_board_permissions` ENABLE KEYS */;

--
-- Table structure for table `smf2_boards`
--

DROP TABLE IF EXISTS `smf2_boards`;
CREATE TABLE `smf2_boards` (
  `ID_BOARD` smallint(5) unsigned NOT NULL auto_increment,
  `ID_CAT` tinyint(4) unsigned NOT NULL default '0',
  `childLevel` tinyint(4) unsigned NOT NULL default '0',
  `ID_PARENT` smallint(5) unsigned NOT NULL default '0',
  `boardOrder` smallint(5) NOT NULL default '0',
  `ID_LAST_MSG` int(10) unsigned NOT NULL default '0',
  `ID_MSG_UPDATED` int(10) unsigned NOT NULL default '0',
  `memberGroups` varchar(255) NOT NULL default '-1,0',
  `name` tinytext NOT NULL,
  `description` text NOT NULL,
  `numTopics` mediumint(8) unsigned NOT NULL default '0',
  `numPosts` mediumint(8) unsigned NOT NULL default '0',
  `countPosts` tinyint(4) NOT NULL default '0',
  `ID_THEME` tinyint(4) unsigned NOT NULL default '0',
  `permission_mode` tinyint(4) unsigned NOT NULL default '0',
  `override_theme` tinyint(4) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ID_BOARD`),
  UNIQUE KEY `categories` (`ID_CAT`,`ID_BOARD`),
  KEY `ID_PARENT` (`ID_PARENT`),
  KEY `ID_MSG_UPDATED` (`ID_MSG_UPDATED`),
  KEY `memberGroups` (`memberGroups`(48))
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_boards`
--


/*!40000 ALTER TABLE `smf2_boards` DISABLE KEYS */;
LOCK TABLES `smf2_boards` WRITE;
INSERT INTO `smf2_boards` VALUES (1,1,0,0,1,23,23,'-1,0','General Discussion','Feel free to talk about anything and everything in this board.',7,13,0,0,0,0),(2,1,0,0,2,22,22,'-1,0,2','New Board','Testing Hierarchy',1,9,0,0,0,0),(3,1,1,2,3,0,0,'-1,0,2','Child Board','Testing Hierarchy',0,0,0,0,0,0),(4,2,0,0,4,0,0,'-1,0,2','Category Board','Testing Hierarchy',0,0,0,0,0,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_boards` ENABLE KEYS */;

--
-- Table structure for table `smf2_calendar`
--

DROP TABLE IF EXISTS `smf2_calendar`;
CREATE TABLE `smf2_calendar` (
  `ID_EVENT` smallint(5) unsigned NOT NULL auto_increment,
  `startDate` date NOT NULL default '0001-01-01',
  `endDate` date NOT NULL default '0001-01-01',
  `ID_BOARD` smallint(5) unsigned NOT NULL default '0',
  `ID_TOPIC` mediumint(8) unsigned NOT NULL default '0',
  `title` varchar(48) NOT NULL default '',
  `ID_MEMBER` mediumint(8) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ID_EVENT`),
  KEY `startDate` (`startDate`),
  KEY `endDate` (`endDate`),
  KEY `topic` (`ID_TOPIC`,`ID_MEMBER`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_calendar`
--


/*!40000 ALTER TABLE `smf2_calendar` DISABLE KEYS */;
LOCK TABLES `smf2_calendar` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_calendar` ENABLE KEYS */;

--
-- Table structure for table `smf2_calendar_holidays`
--

DROP TABLE IF EXISTS `smf2_calendar_holidays`;
CREATE TABLE `smf2_calendar_holidays` (
  `ID_HOLIDAY` smallint(5) unsigned NOT NULL auto_increment,
  `eventDate` date NOT NULL default '0001-01-01',
  `title` varchar(30) NOT NULL default '',
  PRIMARY KEY  (`ID_HOLIDAY`),
  KEY `eventDate` (`eventDate`)
) ENGINE=MyISAM AUTO_INCREMENT=168 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_calendar_holidays`
--


/*!40000 ALTER TABLE `smf2_calendar_holidays` DISABLE KEYS */;
LOCK TABLES `smf2_calendar_holidays` WRITE;
INSERT INTO `smf2_calendar_holidays` VALUES (1,'0004-01-01','New Year\'s'),(2,'0004-12-25','Christmas'),(3,'0004-02-14','Valentine\'s Day'),(4,'0004-03-17','St. Patrick\'s Day'),(5,'0004-04-01','April Fools'),(6,'0004-04-22','Earth Day'),(7,'0004-10-24','United Nations Day'),(8,'0004-10-31','Halloween'),(9,'2004-05-09','Mother\'s Day'),(10,'2005-05-08','Mother\'s Day'),(11,'2006-05-14','Mother\'s Day'),(12,'2007-05-13','Mother\'s Day'),(13,'2008-05-11','Mother\'s Day'),(14,'2009-05-10','Mother\'s Day'),(15,'2010-05-09','Mother\'s Day'),(16,'2011-05-08','Mother\'s Day'),(17,'2012-05-13','Mother\'s Day'),(18,'2013-05-12','Mother\'s Day'),(19,'2014-05-11','Mother\'s Day'),(20,'2015-05-10','Mother\'s Day'),(21,'2016-05-08','Mother\'s Day'),(22,'2017-05-14','Mother\'s Day'),(23,'2018-05-13','Mother\'s Day'),(24,'2019-05-12','Mother\'s Day'),(25,'2020-05-10','Mother\'s Day'),(26,'2004-06-20','Father\'s Day'),(27,'2005-06-19','Father\'s Day'),(28,'2006-06-18','Father\'s Day'),(29,'2007-06-17','Father\'s Day'),(30,'2008-06-15','Father\'s Day'),(31,'2009-06-21','Father\'s Day'),(32,'2010-06-20','Father\'s Day'),(33,'2011-06-19','Father\'s Day'),(34,'2012-06-17','Father\'s Day'),(35,'2013-06-16','Father\'s Day'),(36,'2014-06-15','Father\'s Day'),(37,'2015-06-21','Father\'s Day'),(38,'2016-06-19','Father\'s Day'),(39,'2017-06-18','Father\'s Day'),(40,'2018-06-17','Father\'s Day'),(41,'2019-06-16','Father\'s Day'),(42,'2020-06-21','Father\'s Day'),(43,'2004-06-20','Summer Solstice'),(44,'2005-06-20','Summer Solstice'),(45,'2006-06-21','Summer Solstice'),(46,'2007-06-21','Summer Solstice'),(47,'2008-06-20','Summer Solstice'),(48,'2009-06-20','Summer Solstice'),(49,'2010-06-21','Summer Solstice'),(50,'2011-06-21','Summer Solstice'),(51,'2012-06-20','Summer Solstice'),(52,'2013-06-21','Summer Solstice'),(53,'2014-06-21','Summer Solstice'),(54,'2015-06-21','Summer Solstice'),(55,'2016-06-20','Summer Solstice'),(56,'2017-06-20','Summer Solstice'),(57,'2018-06-21','Summer Solstice'),(58,'2019-06-21','Summer Solstice'),(59,'2020-06-20','Summer Solstice'),(60,'2004-03-19','Vernal Equinox'),(61,'2005-03-20','Vernal Equinox'),(62,'2006-03-20','Vernal Equinox'),(63,'2007-03-20','Vernal Equinox'),(64,'2008-03-19','Vernal Equinox'),(65,'2009-03-20','Vernal Equinox'),(66,'2010-03-20','Vernal Equinox'),(67,'2011-03-20','Vernal Equinox'),(68,'2012-03-20','Vernal Equinox'),(69,'2013-03-20','Vernal Equinox'),(70,'2014-03-20','Vernal Equinox'),(71,'2015-03-20','Vernal Equinox'),(72,'2016-03-19','Vernal Equinox'),(73,'2017-03-20','Vernal Equinox'),(74,'2018-03-20','Vernal Equinox'),(75,'2019-03-20','Vernal Equinox'),(76,'2020-03-19','Vernal Equinox'),(77,'2004-12-21','Winter Solstice'),(78,'2005-12-21','Winter Solstice'),(79,'2006-12-22','Winter Solstice'),(80,'2007-12-22','Winter Solstice'),(81,'2008-12-21','Winter Solstice'),(82,'2009-12-21','Winter Solstice'),(83,'2010-12-21','Winter Solstice'),(84,'2011-12-22','Winter Solstice'),(85,'2012-12-21','Winter Solstice'),(86,'2013-12-21','Winter Solstice'),(87,'2014-12-21','Winter Solstice'),(88,'2015-12-21','Winter Solstice'),(89,'2016-12-21','Winter Solstice'),(90,'2017-12-21','Winter Solstice'),(91,'2018-12-21','Winter Solstice'),(92,'2019-12-21','Winter Solstice'),(93,'2020-12-21','Winter Solstice'),(94,'2004-09-22','Autumnal Equinox'),(95,'2005-09-22','Autumnal Equinox'),(96,'2006-09-22','Autumnal Equinox'),(97,'2007-09-23','Autumnal Equinox'),(98,'2008-09-22','Autumnal Equinox'),(99,'2009-09-22','Autumnal Equinox'),(100,'2010-09-22','Autumnal Equinox'),(101,'2011-09-23','Autumnal Equinox'),(102,'2012-09-22','Autumnal Equinox'),(103,'2013-09-22','Autumnal Equinox'),(104,'2014-09-22','Autumnal Equinox'),(105,'2015-09-23','Autumnal Equinox'),(106,'2016-09-22','Autumnal Equinox'),(107,'2017-09-22','Autumnal Equinox'),(108,'2018-09-22','Autumnal Equinox'),(109,'2019-09-23','Autumnal Equinox'),(110,'2020-09-22','Autumnal Equinox'),(111,'0004-07-04','Independence Day'),(112,'0004-05-05','Cinco de Mayo'),(113,'0004-06-14','Flag Day'),(114,'0004-11-11','Veterans Day'),(115,'0004-02-02','Groundhog Day'),(116,'2004-11-25','Thanksgiving'),(117,'2005-11-24','Thanksgiving'),(118,'2006-11-23','Thanksgiving'),(119,'2007-11-22','Thanksgiving'),(120,'2008-11-27','Thanksgiving'),(121,'2009-11-26','Thanksgiving'),(122,'2010-11-25','Thanksgiving'),(123,'2011-11-24','Thanksgiving'),(124,'2012-11-22','Thanksgiving'),(125,'2013-11-21','Thanksgiving'),(126,'2014-11-20','Thanksgiving'),(127,'2015-11-26','Thanksgiving'),(128,'2016-11-24','Thanksgiving'),(129,'2017-11-23','Thanksgiving'),(130,'2018-11-22','Thanksgiving'),(131,'2019-11-21','Thanksgiving'),(132,'2020-11-26','Thanksgiving'),(133,'2004-05-31','Memorial Day'),(134,'2005-05-30','Memorial Day'),(135,'2006-05-29','Memorial Day'),(136,'2007-05-28','Memorial Day'),(137,'2008-05-26','Memorial Day'),(138,'2009-05-25','Memorial Day'),(139,'2010-05-31','Memorial Day'),(140,'2011-05-30','Memorial Day'),(141,'2012-05-28','Memorial Day'),(142,'2013-05-27','Memorial Day'),(143,'2014-05-26','Memorial Day'),(144,'2015-05-25','Memorial Day'),(145,'2016-05-30','Memorial Day'),(146,'2017-05-29','Memorial Day'),(147,'2018-05-28','Memorial Day'),(148,'2019-05-27','Memorial Day'),(149,'2020-05-25','Memorial Day'),(150,'2004-09-06','Labor Day'),(151,'2005-09-05','Labor Day'),(152,'2006-09-04','Labor Day'),(153,'2007-09-03','Labor Day'),(154,'2008-09-01','Labor Day'),(155,'2009-09-07','Labor Day'),(156,'2010-09-06','Labor Day'),(157,'2011-09-05','Labor Day'),(158,'2012-09-03','Labor Day'),(159,'2013-09-09','Labor Day'),(160,'2014-09-08','Labor Day'),(161,'2015-09-07','Labor Day'),(162,'2016-09-05','Labor Day'),(163,'2017-09-04','Labor Day'),(164,'2018-09-03','Labor Day'),(165,'2019-09-09','Labor Day'),(166,'2020-09-07','Labor Day'),(167,'0004-06-06','D-Day');
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_calendar_holidays` ENABLE KEYS */;

--
-- Table structure for table `smf2_categories`
--

DROP TABLE IF EXISTS `smf2_categories`;
CREATE TABLE `smf2_categories` (
  `ID_CAT` tinyint(4) unsigned NOT NULL auto_increment,
  `catOrder` tinyint(4) NOT NULL default '0',
  `name` tinytext NOT NULL,
  `canCollapse` tinyint(1) NOT NULL default '1',
  PRIMARY KEY  (`ID_CAT`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_categories`
--


/*!40000 ALTER TABLE `smf2_categories` DISABLE KEYS */;
LOCK TABLES `smf2_categories` WRITE;
INSERT INTO `smf2_categories` VALUES (1,0,'General Category',1),(2,1,'New Category',1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_categories` ENABLE KEYS */;

--
-- Table structure for table `smf2_collapsed_categories`
--

DROP TABLE IF EXISTS `smf2_collapsed_categories`;
CREATE TABLE `smf2_collapsed_categories` (
  `ID_CAT` tinyint(4) unsigned NOT NULL default '0',
  `ID_MEMBER` mediumint(8) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ID_CAT`,`ID_MEMBER`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_collapsed_categories`
--


/*!40000 ALTER TABLE `smf2_collapsed_categories` DISABLE KEYS */;
LOCK TABLES `smf2_collapsed_categories` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_collapsed_categories` ENABLE KEYS */;

--
-- Table structure for table `smf2_log_actions`
--

DROP TABLE IF EXISTS `smf2_log_actions`;
CREATE TABLE `smf2_log_actions` (
  `ID_ACTION` int(10) unsigned NOT NULL auto_increment,
  `logTime` int(10) unsigned NOT NULL default '0',
  `ID_MEMBER` mediumint(8) unsigned NOT NULL default '0',
  `ip` char(16) NOT NULL default '',
  `action` varchar(30) NOT NULL default '',
  `extra` text NOT NULL,
  PRIMARY KEY  (`ID_ACTION`),
  KEY `logTime` (`logTime`),
  KEY `ID_MEMBER` (`ID_MEMBER`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_log_actions`
--


/*!40000 ALTER TABLE `smf2_log_actions` DISABLE KEYS */;
LOCK TABLES `smf2_log_actions` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_log_actions` ENABLE KEYS */;

--
-- Table structure for table `smf2_log_activity`
--

DROP TABLE IF EXISTS `smf2_log_activity`;
CREATE TABLE `smf2_log_activity` (
  `date` date NOT NULL default '0001-01-01',
  `hits` mediumint(8) unsigned NOT NULL default '0',
  `topics` smallint(5) unsigned NOT NULL default '0',
  `posts` smallint(5) unsigned NOT NULL default '0',
  `registers` smallint(5) unsigned NOT NULL default '0',
  `mostOn` smallint(5) unsigned NOT NULL default '0',
  PRIMARY KEY  (`date`),
  KEY `hits` (`hits`),
  KEY `mostOn` (`mostOn`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_log_activity`
--


/*!40000 ALTER TABLE `smf2_log_activity` DISABLE KEYS */;
LOCK TABLES `smf2_log_activity` WRITE;
INSERT INTO `smf2_log_activity` VALUES ('2009-06-22',0,6,10,0,1),('2009-06-23',0,0,0,0,1),('2009-06-24',0,0,0,0,1),('2009-06-25',0,0,0,0,1),('2009-06-29',0,0,0,1,2),('2009-06-30',0,0,0,0,1),('2009-07-01',0,0,0,0,1),('2009-07-02',0,0,8,0,1),('2009-07-06',0,0,2,0,1),('2009-07-07',0,0,0,0,1),('2009-07-08',0,0,1,0,1),('2009-07-10',0,1,1,0,1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_log_activity` ENABLE KEYS */;

--
-- Table structure for table `smf2_log_banned`
--

DROP TABLE IF EXISTS `smf2_log_banned`;
CREATE TABLE `smf2_log_banned` (
  `ID_BAN_LOG` mediumint(8) unsigned NOT NULL auto_increment,
  `ID_MEMBER` mediumint(8) unsigned NOT NULL default '0',
  `ip` char(16) NOT NULL default '',
  `email` tinytext NOT NULL,
  `logTime` int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ID_BAN_LOG`),
  KEY `logTime` (`logTime`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_log_banned`
--


/*!40000 ALTER TABLE `smf2_log_banned` DISABLE KEYS */;
LOCK TABLES `smf2_log_banned` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_log_banned` ENABLE KEYS */;

--
-- Table structure for table `smf2_log_boards`
--

DROP TABLE IF EXISTS `smf2_log_boards`;
CREATE TABLE `smf2_log_boards` (
  `ID_MEMBER` mediumint(8) unsigned NOT NULL default '0',
  `ID_BOARD` smallint(5) unsigned NOT NULL default '0',
  `ID_MSG` int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ID_MEMBER`,`ID_BOARD`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_log_boards`
--


/*!40000 ALTER TABLE `smf2_log_boards` DISABLE KEYS */;
LOCK TABLES `smf2_log_boards` WRITE;
INSERT INTO `smf2_log_boards` VALUES (1,1,23),(1,2,22),(1,3,11);
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_log_boards` ENABLE KEYS */;

--
-- Table structure for table `smf2_log_errors`
--

DROP TABLE IF EXISTS `smf2_log_errors`;
CREATE TABLE `smf2_log_errors` (
  `ID_ERROR` mediumint(8) unsigned NOT NULL auto_increment,
  `logTime` int(10) unsigned NOT NULL default '0',
  `ID_MEMBER` mediumint(8) unsigned NOT NULL default '0',
  `ip` char(16) NOT NULL default '',
  `url` text NOT NULL,
  `message` text NOT NULL,
  `session` char(32) NOT NULL default '',
  PRIMARY KEY  (`ID_ERROR`),
  KEY `logTime` (`logTime`),
  KEY `ID_MEMBER` (`ID_MEMBER`),
  KEY `ip` (`ip`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_log_errors`
--


/*!40000 ALTER TABLE `smf2_log_errors` DISABLE KEYS */;
LOCK TABLES `smf2_log_errors` WRITE;
INSERT INTO `smf2_log_errors` VALUES (1,1245941367,0,'127.0.0.1','?PHPSESSID=bb6q9te3gu92enbkhe6ciig5o2&amp;action=login2','Password incorrect - &lt;span class=&quot;remove&quot;&gt;admin&lt;/span&gt;','79ed70ebd7940bcd577be64b51e477ee');
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_log_errors` ENABLE KEYS */;

--
-- Table structure for table `smf2_log_floodcontrol`
--

DROP TABLE IF EXISTS `smf2_log_floodcontrol`;
CREATE TABLE `smf2_log_floodcontrol` (
  `ip` char(16) NOT NULL default '',
  `logTime` int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ip`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_log_floodcontrol`
--


/*!40000 ALTER TABLE `smf2_log_floodcontrol` DISABLE KEYS */;
LOCK TABLES `smf2_log_floodcontrol` WRITE;
INSERT INTO `smf2_log_floodcontrol` VALUES ('127.0.0.1',1247249470);
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_log_floodcontrol` ENABLE KEYS */;

--
-- Table structure for table `smf2_log_karma`
--

DROP TABLE IF EXISTS `smf2_log_karma`;
CREATE TABLE `smf2_log_karma` (
  `ID_TARGET` mediumint(8) unsigned NOT NULL default '0',
  `ID_EXECUTOR` mediumint(8) unsigned NOT NULL default '0',
  `logTime` int(10) unsigned NOT NULL default '0',
  `action` tinyint(4) NOT NULL default '0',
  PRIMARY KEY  (`ID_TARGET`,`ID_EXECUTOR`),
  KEY `logTime` (`logTime`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_log_karma`
--


/*!40000 ALTER TABLE `smf2_log_karma` DISABLE KEYS */;
LOCK TABLES `smf2_log_karma` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_log_karma` ENABLE KEYS */;

--
-- Table structure for table `smf2_log_mark_read`
--

DROP TABLE IF EXISTS `smf2_log_mark_read`;
CREATE TABLE `smf2_log_mark_read` (
  `ID_MEMBER` mediumint(8) unsigned NOT NULL default '0',
  `ID_BOARD` smallint(5) unsigned NOT NULL default '0',
  `ID_MSG` int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ID_MEMBER`,`ID_BOARD`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_log_mark_read`
--


/*!40000 ALTER TABLE `smf2_log_mark_read` DISABLE KEYS */;
LOCK TABLES `smf2_log_mark_read` WRITE;
INSERT INTO `smf2_log_mark_read` VALUES (1,2,7);
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_log_mark_read` ENABLE KEYS */;

--
-- Table structure for table `smf2_log_notify`
--

DROP TABLE IF EXISTS `smf2_log_notify`;
CREATE TABLE `smf2_log_notify` (
  `ID_MEMBER` mediumint(8) unsigned NOT NULL default '0',
  `ID_TOPIC` mediumint(8) unsigned NOT NULL default '0',
  `ID_BOARD` smallint(5) unsigned NOT NULL default '0',
  `sent` tinyint(1) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ID_MEMBER`,`ID_TOPIC`,`ID_BOARD`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_log_notify`
--


/*!40000 ALTER TABLE `smf2_log_notify` DISABLE KEYS */;
LOCK TABLES `smf2_log_notify` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_log_notify` ENABLE KEYS */;

--
-- Table structure for table `smf2_log_online`
--

DROP TABLE IF EXISTS `smf2_log_online`;
CREATE TABLE `smf2_log_online` (
  `session` varchar(32) NOT NULL default '',
  `logTime` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `ID_MEMBER` mediumint(8) unsigned NOT NULL default '0',
  `ip` int(10) unsigned NOT NULL default '0',
  `url` text NOT NULL,
  PRIMARY KEY  (`session`),
  KEY `logTime` (`logTime`),
  KEY `ID_MEMBER` (`ID_MEMBER`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_log_online`
--


/*!40000 ALTER TABLE `smf2_log_online` DISABLE KEYS */;
LOCK TABLES `smf2_log_online` WRITE;
INSERT INTO `smf2_log_online` VALUES ('933n7s2q400q7h9bkc0oa024q6','2009-07-13 13:14:49',1,2130706433,'a:3:{s:5:\"topic\";i:6;s:5:\"board\";i:2;s:10:\"USER_AGENT\";s:99:\"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.0.11) Gecko/2009060214 Firefox/3.0.11\";}');
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_log_online` ENABLE KEYS */;

--
-- Table structure for table `smf2_log_polls`
--

DROP TABLE IF EXISTS `smf2_log_polls`;
CREATE TABLE `smf2_log_polls` (
  `ID_POLL` mediumint(8) unsigned NOT NULL default '0',
  `ID_MEMBER` mediumint(8) unsigned NOT NULL default '0',
  `ID_CHOICE` tinyint(3) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ID_POLL`,`ID_MEMBER`,`ID_CHOICE`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_log_polls`
--


/*!40000 ALTER TABLE `smf2_log_polls` DISABLE KEYS */;
LOCK TABLES `smf2_log_polls` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_log_polls` ENABLE KEYS */;

--
-- Table structure for table `smf2_log_search_messages`
--

DROP TABLE IF EXISTS `smf2_log_search_messages`;
CREATE TABLE `smf2_log_search_messages` (
  `ID_SEARCH` tinyint(3) unsigned NOT NULL default '0',
  `ID_MSG` int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ID_SEARCH`,`ID_MSG`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_log_search_messages`
--


/*!40000 ALTER TABLE `smf2_log_search_messages` DISABLE KEYS */;
LOCK TABLES `smf2_log_search_messages` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_log_search_messages` ENABLE KEYS */;

--
-- Table structure for table `smf2_log_search_results`
--

DROP TABLE IF EXISTS `smf2_log_search_results`;
CREATE TABLE `smf2_log_search_results` (
  `ID_SEARCH` tinyint(3) unsigned NOT NULL default '0',
  `ID_TOPIC` mediumint(8) unsigned NOT NULL default '0',
  `ID_MSG` int(10) unsigned NOT NULL default '0',
  `relevance` smallint(5) unsigned NOT NULL default '0',
  `num_matches` smallint(5) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ID_SEARCH`,`ID_TOPIC`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_log_search_results`
--


/*!40000 ALTER TABLE `smf2_log_search_results` DISABLE KEYS */;
LOCK TABLES `smf2_log_search_results` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_log_search_results` ENABLE KEYS */;

--
-- Table structure for table `smf2_log_search_subjects`
--

DROP TABLE IF EXISTS `smf2_log_search_subjects`;
CREATE TABLE `smf2_log_search_subjects` (
  `word` varchar(20) NOT NULL default '',
  `ID_TOPIC` mediumint(8) unsigned NOT NULL default '0',
  PRIMARY KEY  (`word`,`ID_TOPIC`),
  KEY `ID_TOPIC` (`ID_TOPIC`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_log_search_subjects`
--


/*!40000 ALTER TABLE `smf2_log_search_subjects` DISABLE KEYS */;
LOCK TABLES `smf2_log_search_subjects` WRITE;
INSERT INTO `smf2_log_search_subjects` VALUES ('#;',3),('bad',3),('bad',7),('characters',3),('characters',7),('collisions',4),('collisions',5),('encoding',8),('hierarchy',2),('in',3),('in',7),('namespace',4),('namespace',5),('page',6),('SMF',1),('syntax',6),('test',2),('test',6),('testing',4),('testing',5),('title',3),('title',7),('to',1),('Welcome',1),('|',3);
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_log_search_subjects` ENABLE KEYS */;

--
-- Table structure for table `smf2_log_search_topics`
--

DROP TABLE IF EXISTS `smf2_log_search_topics`;
CREATE TABLE `smf2_log_search_topics` (
  `ID_SEARCH` tinyint(3) unsigned NOT NULL default '0',
  `ID_TOPIC` mediumint(9) NOT NULL default '0',
  PRIMARY KEY  (`ID_SEARCH`,`ID_TOPIC`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_log_search_topics`
--


/*!40000 ALTER TABLE `smf2_log_search_topics` DISABLE KEYS */;
LOCK TABLES `smf2_log_search_topics` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_log_search_topics` ENABLE KEYS */;

--
-- Table structure for table `smf2_log_topics`
--

DROP TABLE IF EXISTS `smf2_log_topics`;
CREATE TABLE `smf2_log_topics` (
  `ID_MEMBER` mediumint(8) unsigned NOT NULL default '0',
  `ID_TOPIC` mediumint(8) unsigned NOT NULL default '0',
  `ID_MSG` int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ID_MEMBER`,`ID_TOPIC`),
  KEY `ID_TOPIC` (`ID_TOPIC`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_log_topics`
--


/*!40000 ALTER TABLE `smf2_log_topics` DISABLE KEYS */;
LOCK TABLES `smf2_log_topics` WRITE;
INSERT INTO `smf2_log_topics` VALUES (1,1,23),(1,2,3),(1,3,5),(1,4,6),(1,5,7),(1,6,23),(1,7,9),(1,8,23);
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_log_topics` ENABLE KEYS */;

--
-- Table structure for table `smf2_membergroups`
--

DROP TABLE IF EXISTS `smf2_membergroups`;
CREATE TABLE `smf2_membergroups` (
  `ID_GROUP` smallint(5) unsigned NOT NULL auto_increment,
  `groupName` varchar(80) NOT NULL default '',
  `onlineColor` varchar(20) NOT NULL default '',
  `minPosts` mediumint(9) NOT NULL default '-1',
  `maxMessages` smallint(5) unsigned NOT NULL default '0',
  `stars` tinytext NOT NULL,
  PRIMARY KEY  (`ID_GROUP`),
  KEY `minPosts` (`minPosts`)
) ENGINE=MyISAM AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_membergroups`
--


/*!40000 ALTER TABLE `smf2_membergroups` DISABLE KEYS */;
LOCK TABLES `smf2_membergroups` WRITE;
INSERT INTO `smf2_membergroups` VALUES (1,'Administrator','#FF0000',-1,0,'5#staradmin.gif'),(2,'Global Moderator','#0000FF',-1,0,'5#stargmod.gif'),(3,'Moderator','',-1,0,'5#starmod.gif'),(4,'Newbie','',0,0,'1#star.gif'),(5,'Jr. Member','',50,0,'2#star.gif'),(6,'Full Member','',100,0,'3#star.gif'),(7,'Sr. Member','',250,0,'4#star.gif'),(8,'Hero Member','',500,0,'5#star.gif');
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_membergroups` ENABLE KEYS */;

--
-- Table structure for table `smf2_members`
--

DROP TABLE IF EXISTS `smf2_members`;
CREATE TABLE `smf2_members` (
  `ID_MEMBER` mediumint(8) unsigned NOT NULL auto_increment,
  `memberName` varchar(80) NOT NULL default '',
  `dateRegistered` int(10) unsigned NOT NULL default '0',
  `posts` mediumint(8) unsigned NOT NULL default '0',
  `ID_GROUP` smallint(5) unsigned NOT NULL default '0',
  `lngfile` tinytext NOT NULL,
  `lastLogin` int(10) unsigned NOT NULL default '0',
  `realName` tinytext NOT NULL,
  `instantMessages` smallint(5) NOT NULL default '0',
  `unreadMessages` smallint(5) NOT NULL default '0',
  `buddy_list` text NOT NULL,
  `pm_ignore_list` tinytext NOT NULL,
  `messageLabels` text NOT NULL,
  `passwd` varchar(64) NOT NULL default '',
  `emailAddress` tinytext NOT NULL,
  `personalText` tinytext NOT NULL,
  `gender` tinyint(4) unsigned NOT NULL default '0',
  `birthdate` date NOT NULL default '0001-01-01',
  `websiteTitle` tinytext NOT NULL,
  `websiteUrl` tinytext NOT NULL,
  `location` tinytext NOT NULL,
  `ICQ` tinytext NOT NULL,
  `AIM` varchar(16) NOT NULL default '',
  `YIM` varchar(32) NOT NULL default '',
  `MSN` tinytext NOT NULL,
  `hideEmail` tinyint(4) NOT NULL default '0',
  `showOnline` tinyint(4) NOT NULL default '1',
  `timeFormat` varchar(80) NOT NULL default '',
  `signature` text NOT NULL,
  `timeOffset` float NOT NULL default '0',
  `avatar` tinytext NOT NULL,
  `pm_email_notify` tinyint(4) NOT NULL default '0',
  `karmaBad` smallint(5) unsigned NOT NULL default '0',
  `karmaGood` smallint(5) unsigned NOT NULL default '0',
  `usertitle` tinytext NOT NULL,
  `notifyAnnouncements` tinyint(4) NOT NULL default '1',
  `notifyOnce` tinyint(4) NOT NULL default '1',
  `notifySendBody` tinyint(4) NOT NULL default '0',
  `notifyTypes` tinyint(4) NOT NULL default '2',
  `memberIP` tinytext NOT NULL,
  `memberIP2` tinytext NOT NULL,
  `secretQuestion` tinytext NOT NULL,
  `secretAnswer` varchar(64) NOT NULL default '',
  `ID_THEME` tinyint(4) unsigned NOT NULL default '0',
  `is_activated` tinyint(3) unsigned NOT NULL default '1',
  `validation_code` varchar(10) NOT NULL default '',
  `ID_MSG_LAST_VISIT` int(10) unsigned NOT NULL default '0',
  `additionalGroups` tinytext NOT NULL,
  `smileySet` varchar(48) NOT NULL default '',
  `ID_POST_GROUP` smallint(5) unsigned NOT NULL default '0',
  `totalTimeLoggedIn` int(10) unsigned NOT NULL default '0',
  `passwordSalt` varchar(5) NOT NULL default '',
  PRIMARY KEY  (`ID_MEMBER`),
  KEY `memberName` (`memberName`(30)),
  KEY `dateRegistered` (`dateRegistered`),
  KEY `ID_GROUP` (`ID_GROUP`),
  KEY `birthdate` (`birthdate`),
  KEY `posts` (`posts`),
  KEY `lastLogin` (`lastLogin`),
  KEY `lngfile` (`lngfile`(30)),
  KEY `ID_POST_GROUP` (`ID_POST_GROUP`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_members`
--


/*!40000 ALTER TABLE `smf2_members` DISABLE KEYS */;
LOCK TABLES `smf2_members` WRITE;
INSERT INTO `smf2_members` VALUES (1,'admin',1245692276,21,1,'',1247490886,'admin',0,0,'','','','8626029e3250e272bb5ae961b474ee43398998d0','laura.kolker@gmail.com','',0,'0001-01-01','','','','','','','',0,1,'','',0,'',0,0,0,'',1,1,0,2,'127.0.0.1','127.0.0.1','','',0,1,'',23,'','',4,7583,'8635'),(2,'test;',1246311492,0,0,'',1246311493,'test;',0,0,'','','','eb2ccbf4133de2548d26424591484baaaba4f576','test@192.168.2.114','',0,'0001-01-01','','','','','','','',0,1,'','',0,'',1,0,0,'',1,1,0,2,'127.0.0.1','127.0.0.1','','',0,1,'',11,'','',4,0,'aac5');
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_members` ENABLE KEYS */;

--
-- Table structure for table `smf2_message_icons`
--

DROP TABLE IF EXISTS `smf2_message_icons`;
CREATE TABLE `smf2_message_icons` (
  `ID_ICON` smallint(5) unsigned NOT NULL auto_increment,
  `title` varchar(80) NOT NULL default '',
  `filename` varchar(80) NOT NULL default '',
  `ID_BOARD` mediumint(8) unsigned NOT NULL default '0',
  `iconOrder` smallint(5) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ID_ICON`),
  KEY `ID_BOARD` (`ID_BOARD`)
) ENGINE=MyISAM AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_message_icons`
--


/*!40000 ALTER TABLE `smf2_message_icons` DISABLE KEYS */;
LOCK TABLES `smf2_message_icons` WRITE;
INSERT INTO `smf2_message_icons` VALUES (1,'Standard','xx',0,0),(2,'Thumb Up','thumbup',0,1),(3,'Thumb Down','thumbdown',0,2),(4,'Exclamation point','exclamation',0,3),(5,'Question mark','question',0,4),(6,'Lamp','lamp',0,5),(7,'Smiley','smiley',0,6),(8,'Angry','angry',0,7),(9,'Cheesy','cheesy',0,8),(10,'Grin','grin',0,9),(11,'Sad','sad',0,10),(12,'Wink','wink',0,11);
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_message_icons` ENABLE KEYS */;

--
-- Table structure for table `smf2_messages`
--

DROP TABLE IF EXISTS `smf2_messages`;
CREATE TABLE `smf2_messages` (
  `ID_MSG` int(10) unsigned NOT NULL auto_increment,
  `ID_TOPIC` mediumint(8) unsigned NOT NULL default '0',
  `ID_BOARD` smallint(5) unsigned NOT NULL default '0',
  `posterTime` int(10) unsigned NOT NULL default '0',
  `ID_MEMBER` mediumint(8) unsigned NOT NULL default '0',
  `ID_MSG_MODIFIED` int(10) unsigned NOT NULL default '0',
  `subject` tinytext NOT NULL,
  `posterName` tinytext NOT NULL,
  `posterEmail` tinytext NOT NULL,
  `posterIP` tinytext NOT NULL,
  `smileysEnabled` tinyint(4) NOT NULL default '1',
  `modifiedTime` int(10) unsigned NOT NULL default '0',
  `modifiedName` tinytext NOT NULL,
  `body` text NOT NULL,
  `icon` varchar(16) NOT NULL default 'xx',
  PRIMARY KEY  (`ID_MSG`),
  UNIQUE KEY `topic` (`ID_TOPIC`,`ID_MSG`),
  UNIQUE KEY `ID_BOARD` (`ID_BOARD`,`ID_MSG`),
  UNIQUE KEY `ID_MEMBER` (`ID_MEMBER`,`ID_MSG`),
  KEY `ipIndex` (`posterIP`(15),`ID_TOPIC`),
  KEY `participation` (`ID_MEMBER`,`ID_TOPIC`),
  KEY `showPosts` (`ID_MEMBER`,`ID_BOARD`),
  KEY `ID_TOPIC` (`ID_TOPIC`)
) ENGINE=MyISAM AUTO_INCREMENT=24 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_messages`
--


/*!40000 ALTER TABLE `smf2_messages` DISABLE KEYS */;
LOCK TABLES `smf2_messages` WRITE;
INSERT INTO `smf2_messages` VALUES (1,1,1,1245692231,0,1,'Welcome to SMF!','Simple Machines','info@simplemachines.org','127.0.0.1',1,0,'','Welcome to Simple Machines Forum!<br /><br />We hope you enjoy using your forum.&nbsp; If you have any problems, please feel free to [url=http://www.simplemachines.org/community/index.php]ask us for assistance[/url].<br /><br />Thanks!<br />Simple Machines','xx'),(2,1,1,1245693332,1,2,'Re: Welcome to SMF!','admin','laura.kolker@gmail.com','127.0.0.1',1,0,'','Testing discussion structure <br /><br />Child 1','xx'),(3,2,1,1245693435,1,3,'Hierarchy Test','admin','laura.kolker@gmail.com','127.0.0.1',1,0,'','New Topic<br /><br />Topic Root','xx'),(4,3,1,1245696264,1,4,'Bad characters in title :@/\\|^#;[]{}&lt;&gt;','admin','laura.kolker@gmail.com','127.0.0.1',1,0,'','Testing bad chars in title','xx'),(5,4,1,1245696311,1,5,'Testing Namespace Collisions','admin','laura.kolker@gmail.com','127.0.0.1',1,0,'','Testing Namespace Collisions 1','xx'),(6,5,1,1245696325,1,6,'Testing Namespace Collisions','admin','laura.kolker@gmail.com','127.0.0.1',1,0,'','Testing Namespace Collisions 2','xx'),(7,1,1,1245696506,1,7,'Re: Welcome to SMF! Changing reply subject','admin','laura.kolker@gmail.com','127.0.0.1',1,0,'','One _can_ change the reply subject, but probably won&#039;t','xx'),(8,6,2,1245696721,1,8,'Syntax Test Page','admin','laura.kolker@gmail.com','127.0.0.1',1,0,'','Links:<br /><br />How do we link to other pages? How do we deal with namespace collisions?<br />[url=http://www.google.com]http://www.google.com[/url]<br />Maybe we don&#039;t link to other pages? &#039;cause it&#039;s not a wiki?','xx'),(9,7,1,1245704936,1,9,'~Bad characters in title','admin','laura.kolker@gmail.com','127.0.0.1',1,1245705214,'admin','Tilde started subject<br />Testing modifiedtime','xx'),(10,1,1,1245706817,1,10,'Re: Welcome to SMF!','admin','laura.kolker@gmail.com','127.0.0.1',1,0,'','Basic reply to original topic','xx'),(11,1,1,1245706848,1,11,'Re: Welcome to SMF! Changing reply subject','admin','laura.kolker@gmail.com','127.0.0.1',1,0,'','[quote author=admin link=topic=1.msg7#msg7 date=1245696506]<br />One _can_ change the reply subject, but probably won&#039;t<br />[/quote]<br /><br />Replying to a specific reply','xx'),(12,6,2,1246561256,1,12,'Basic Syntax','admin','laura.kolker@gmail.com','127.0.0.1',1,0,'','Bold<br />[b]bold[/b]<br /><br />Italics<br />[i]italics[/i]<br /><br />Underline<br />[u]underline[/u]<br /><br />Mixed<br />[b][i]Bold and Italics[/i][/b]<br /><br />With Newlines<br />[b]<br />&nbsp;  [i]<br />With Newlines<br />&nbsp;  [/i]<br />[/b]<br />','xx'),(13,6,2,1246561582,1,13,'Lists','admin','laura.kolker@gmail.com','127.0.0.1',1,0,'','Simple list:<br />[list]<br />[li]Testing[/li]<br />[li]123[/li]<br />[li]abc[/li]<br />[li]def[/li]<br />[/list]<br /><br />Nesting:<br />[list]<br />[li]level1<br />&nbsp;  [list]<br />&nbsp; &nbsp;  [li]level 2[/li]<br />&nbsp;  [/list]<br />[/li]<br />[li]foobar[/li]<br />[/list]<br />','xx'),(14,6,2,1246561802,1,14,'Links','admin','laura.kolker@gmail.com','127.0.0.1',1,0,'','Links<br />[url=http://www.google.com]http://www.google.com[/url]<br />[url=http://somesite/]Site Name[/url]<br />[iurl=http://simplemachines.org]http://simplemachines.org[/iurl]<br />[iurl=http://simplemachines.org]text or more BB Code[/iurl]<br />[email]laura.kolker@gmail.com[/email]<br />[email=someone@somesite]Somename[/email]<br />[ftp=ftp://somesite]ftp://somesite[/ftp]<br />[ftp=ftp://somesite]somesite[/ftp]','xx'),(15,6,2,1246561890,1,15,'Tables','admin','laura.kolker@gmail.com','127.0.0.1',1,0,'','[table][tr]<br />[td]A<br />[/td][td]B[/td][td]C[/td][/tr]<br />[tr][td]Row2[/td][/tr][/table]','xx'),(16,6,2,1246561983,1,21,'Images and Attachments','admin','laura.kolker@gmail.com','127.0.0.1',1,1246998313,'admin','Inline:<br />[img]http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=1;image[/img]<br />Thumbnail:<br />[img]http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=2;image[/img]<br /><br />Link<br />[url=http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=1;image]http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=1;image[/url]<br />[url=http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=2;image]http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=2;image[/url]<br /><br />Note: There&#039;s an image attached to this message? topic? and I don&#039;t necessarily have to refer to it in the page content.','xx'),(18,6,2,1246565158,1,18,'Syntax Everything Else','admin','laura.kolker@gmail.com','127.0.0.1',1,0,'','Entities<br />&amp;quot; &amp;amp; &amp;gt; &amp;lt;<br /><br />Newline&lt;br/&gt;Newline&lt;br&gt;<br />[iurl=#test]Link to anchor[/iurl]<br /><br />Lorem Ipsum<br />Lorem Ipsum<br />Lorem Ipsum<br />Lorem IpsumLorem Ipsum<br />Lorem Ipsum<br />Lorem Ipsum<br />Lorem Ipsum<br />Lorem Ipsum<br />Lorem Ipsum<br />Lorem Ipsum<br />Anchor is here:<br />[anchor=test][/anchor]	<br /><br />Lorem Ipsum<br />Lorem Ipsum<br />Lorem Ipsum<br />Lorem IpsumLorem Ipsum<br />Lorem Ipsum<br />Lorem Ipsum<br />Lorem Ipsum<br />Lorem Ipsum<br />Lorem Ipsum<br />Lorem Ipsum<br /><br />Lorem Ipsum<br />Lorem Ipsum<br />Lorem Ipsum<br />Lorem IpsumLorem Ipsum<br />Lorem Ipsum<br />Lorem Ipsum<br />Lorem Ipsum<br />Lorem Ipsum<br />Lorem Ipsum<br />Lorem Ipsum','xx'),(19,6,2,1246565484,1,19,'Syntax Other Stuff','admin','laura.kolker@gmail.com','127.0.0.1',1,0,'','Probably do:<br />[s]strike[/s]<br />[pre]pre<br />&nbsp; &nbsp; testing<br />[/pre]<br />[hr]<br />[color=red]color[/color]<br />[sup]sup[/sup]<br />[sub]sub[/sub]<br />[tt]mono[/tt]<br />[code]code[/code]<br />[quote]quote[/quote]<br />[quote=Author link=http://somesite/]text[/quote]<br />[nobbc] [/nobbc]<br /><br />Probably don&#039;t:<br />[glow=red,2,300]test[/glow]<br />[shadow=red,left]test[/shadow]<br />[move]test[/move]<br />[left]left[/left]<br />[center]center[/center]<br />[right]right[/right]<br />[size=10pt]fontsize[/size]<br />[flash=200,200][/flash]<br />[font=Verdana]fontface[/font]<br />[list]<br />[o]circle<br />[O]circle<br />[0]circle<br />[*]disc<br />[@]disc<br />[+]square<br />[x]square<br />[#]square<br />[/list]<br />[abbr=exemlpi gratia]eg[/abbr]<br />[acronym=Simple Machines Forum]SMF[/acronym]<br />[html]<br />[/html]<br />[time]1132812640[/time]','xx'),(20,1,1,1246896846,1,20,'Re: Welcome to SMF!','admin','laura.kolker@gmail.com','127.0.0.1',1,0,'','Attaching a 2nd attachment: ed.jpeg','xx'),(21,1,1,1246896957,1,21,'Re: Welcome to SMF!','admin','laura.kolker@gmail.com','127.0.0.1',1,0,'','Attaching a 3rd attachment: doublefacepalm.jpg','xx'),(22,6,2,1247076390,1,22,'Re: Syntax Test PageOther Page Attachment','admin','laura.kolker@gmail.com','127.0.0.1',1,1247076890,'admin','[img]http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=2;image[/img]<br /><br />[url=http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=2;image]other page image[/url]<br /><br />Other Topic:<br />[img]http://localhost:8081/SMF/index.php?action=dlattach;topic=1.0;attach=4;image[/img]<br /><br />[url=http://localhost:8081/SMF/index.php?action=dlattach;topic=1.0;attach=4;image]other topic image[/url]<br /><br />What&#039;s necessary to get the link to resolve? action and attach definitely. what about topic and image?<br />without topic:<br />[img]http://localhost:8081/SMF/index.php?action=dlattach;attach=4;image[/img]<br />without image:<br />[img]http://localhost:8081/SMF/index.php?action=dlattach;topic=1.0;attach=4;[/img]<br />withouth both:<br />[img]http://localhost:8081/SMF/index.php?action=dlattach;attach=4;[/img]<br />','xx'),(23,8,1,1247249470,1,23,'Encoding','admin','laura.kolker@gmail.com','127.0.0.1',1,0,'','嘗思人道之大','xx');
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_messages` ENABLE KEYS */;

--
-- Table structure for table `smf2_moderators`
--

DROP TABLE IF EXISTS `smf2_moderators`;
CREATE TABLE `smf2_moderators` (
  `ID_BOARD` smallint(5) unsigned NOT NULL default '0',
  `ID_MEMBER` mediumint(8) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ID_BOARD`,`ID_MEMBER`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_moderators`
--


/*!40000 ALTER TABLE `smf2_moderators` DISABLE KEYS */;
LOCK TABLES `smf2_moderators` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_moderators` ENABLE KEYS */;

--
-- Table structure for table `smf2_package_servers`
--

DROP TABLE IF EXISTS `smf2_package_servers`;
CREATE TABLE `smf2_package_servers` (
  `ID_SERVER` smallint(5) unsigned NOT NULL auto_increment,
  `name` tinytext NOT NULL,
  `url` tinytext NOT NULL,
  PRIMARY KEY  (`ID_SERVER`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_package_servers`
--


/*!40000 ALTER TABLE `smf2_package_servers` DISABLE KEYS */;
LOCK TABLES `smf2_package_servers` WRITE;
INSERT INTO `smf2_package_servers` VALUES (1,'Simple Machines Third-party Mod Site','http://mods.simplemachines.org');
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_package_servers` ENABLE KEYS */;

--
-- Table structure for table `smf2_permissions`
--

DROP TABLE IF EXISTS `smf2_permissions`;
CREATE TABLE `smf2_permissions` (
  `ID_GROUP` smallint(5) NOT NULL default '0',
  `permission` varchar(30) NOT NULL default '',
  `addDeny` tinyint(4) NOT NULL default '1',
  PRIMARY KEY  (`ID_GROUP`,`permission`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_permissions`
--


/*!40000 ALTER TABLE `smf2_permissions` DISABLE KEYS */;
LOCK TABLES `smf2_permissions` WRITE;
INSERT INTO `smf2_permissions` VALUES (-1,'search_posts',1),(-1,'calendar_view',1),(-1,'view_stats',1),(-1,'profile_view_any',1),(0,'view_mlist',1),(0,'search_posts',1),(0,'profile_view_own',1),(0,'profile_view_any',1),(0,'pm_read',1),(0,'pm_send',1),(0,'calendar_view',1),(0,'view_stats',1),(0,'who_view',1),(0,'profile_identity_own',1),(0,'profile_extra_own',1),(0,'profile_remove_own',1),(0,'profile_server_avatar',1),(0,'profile_upload_avatar',1),(0,'profile_remote_avatar',1),(0,'karma_edit',1),(2,'view_mlist',1),(2,'search_posts',1),(2,'profile_view_own',1),(2,'profile_view_any',1),(2,'pm_read',1),(2,'pm_send',1),(2,'calendar_view',1),(2,'view_stats',1),(2,'who_view',1),(2,'profile_identity_own',1),(2,'profile_extra_own',1),(2,'profile_remove_own',1),(2,'profile_server_avatar',1),(2,'profile_upload_avatar',1),(2,'profile_remote_avatar',1),(2,'profile_title_own',1),(2,'calendar_post',1),(2,'calendar_edit_any',1),(2,'karma_edit',1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_permissions` ENABLE KEYS */;

--
-- Table structure for table `smf2_personal_messages`
--

DROP TABLE IF EXISTS `smf2_personal_messages`;
CREATE TABLE `smf2_personal_messages` (
  `ID_PM` int(10) unsigned NOT NULL auto_increment,
  `ID_MEMBER_FROM` mediumint(8) unsigned NOT NULL default '0',
  `deletedBySender` tinyint(3) unsigned NOT NULL default '0',
  `fromName` tinytext NOT NULL,
  `msgtime` int(10) unsigned NOT NULL default '0',
  `subject` tinytext NOT NULL,
  `body` text NOT NULL,
  PRIMARY KEY  (`ID_PM`),
  KEY `ID_MEMBER` (`ID_MEMBER_FROM`,`deletedBySender`),
  KEY `msgtime` (`msgtime`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_personal_messages`
--


/*!40000 ALTER TABLE `smf2_personal_messages` DISABLE KEYS */;
LOCK TABLES `smf2_personal_messages` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_personal_messages` ENABLE KEYS */;

--
-- Table structure for table `smf2_pm_recipients`
--

DROP TABLE IF EXISTS `smf2_pm_recipients`;
CREATE TABLE `smf2_pm_recipients` (
  `ID_PM` int(10) unsigned NOT NULL default '0',
  `ID_MEMBER` mediumint(8) unsigned NOT NULL default '0',
  `labels` varchar(60) NOT NULL default '-1',
  `bcc` tinyint(3) unsigned NOT NULL default '0',
  `is_read` tinyint(3) unsigned NOT NULL default '0',
  `deleted` tinyint(3) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ID_PM`,`ID_MEMBER`),
  UNIQUE KEY `ID_MEMBER` (`ID_MEMBER`,`deleted`,`ID_PM`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_pm_recipients`
--


/*!40000 ALTER TABLE `smf2_pm_recipients` DISABLE KEYS */;
LOCK TABLES `smf2_pm_recipients` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_pm_recipients` ENABLE KEYS */;

--
-- Table structure for table `smf2_poll_choices`
--

DROP TABLE IF EXISTS `smf2_poll_choices`;
CREATE TABLE `smf2_poll_choices` (
  `ID_POLL` mediumint(8) unsigned NOT NULL default '0',
  `ID_CHOICE` tinyint(3) unsigned NOT NULL default '0',
  `label` tinytext NOT NULL,
  `votes` smallint(5) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ID_POLL`,`ID_CHOICE`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_poll_choices`
--


/*!40000 ALTER TABLE `smf2_poll_choices` DISABLE KEYS */;
LOCK TABLES `smf2_poll_choices` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_poll_choices` ENABLE KEYS */;

--
-- Table structure for table `smf2_polls`
--

DROP TABLE IF EXISTS `smf2_polls`;
CREATE TABLE `smf2_polls` (
  `ID_POLL` mediumint(8) unsigned NOT NULL auto_increment,
  `question` tinytext NOT NULL,
  `votingLocked` tinyint(1) NOT NULL default '0',
  `maxVotes` tinyint(3) unsigned NOT NULL default '1',
  `expireTime` int(10) unsigned NOT NULL default '0',
  `hideResults` tinyint(3) unsigned NOT NULL default '0',
  `changeVote` tinyint(3) unsigned NOT NULL default '0',
  `ID_MEMBER` mediumint(8) unsigned NOT NULL default '0',
  `posterName` tinytext NOT NULL,
  PRIMARY KEY  (`ID_POLL`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_polls`
--


/*!40000 ALTER TABLE `smf2_polls` DISABLE KEYS */;
LOCK TABLES `smf2_polls` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_polls` ENABLE KEYS */;

--
-- Table structure for table `smf2_sessions`
--

DROP TABLE IF EXISTS `smf2_sessions`;
CREATE TABLE `smf2_sessions` (
  `session_id` char(32) NOT NULL,
  `last_update` int(10) unsigned NOT NULL,
  `data` text NOT NULL,
  PRIMARY KEY  (`session_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_sessions`
--


/*!40000 ALTER TABLE `smf2_sessions` DISABLE KEYS */;
LOCK TABLES `smf2_sessions` WRITE;
INSERT INTO `smf2_sessions` VALUES ('b4gsikmit55ih66temickq7l92',1247381969,'rand_code|s:32:\"04b137ea2df1bfb0962391f14c05668a\";ID_MSG_LAST_VISIT|s:2:\"19\";log_time|i:1247260217;timeOnlineUpdated|i:1247260217;unread_messages|i:0;old_url|s:45:\"http://localhost:8081/SMF/index.php?topic=1.0\";USER_AGENT|s:99:\"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.0.11) Gecko/2009060214 Firefox/3.0.11\";last_read_topic|i:1;temp_attachments|a:0:{}forms|a:5:{i:0;i:6810091;i:1;i:11444543;i:2;i:1884162;i:3;i:5111669;i:4;i:15380252;}'),('933n7s2q400q7h9bkc0oa024q6',1247508898,'rand_code|s:32:\"ae5c50e5d481c26f64729f23075e673e\";ID_MSG_LAST_VISIT|s:2:\"21\";log_time|i:1247490889;timeOnlineUpdated|i:1247490889;last_read_topic|i:6;unread_messages|i:0;old_url|s:49:\"http://localhost:8081/SMF/index.php?topic=6.msg22\";USER_AGENT|s:99:\"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.0.11) Gecko/2009060214 Firefox/3.0.11\";');
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_sessions` ENABLE KEYS */;

--
-- Table structure for table `smf2_settings`
--

DROP TABLE IF EXISTS `smf2_settings`;
CREATE TABLE `smf2_settings` (
  `variable` tinytext NOT NULL,
  `value` text NOT NULL,
  PRIMARY KEY  (`variable`(30))
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_settings`
--


/*!40000 ALTER TABLE `smf2_settings` DISABLE KEYS */;
LOCK TABLES `smf2_settings` WRITE;
INSERT INTO `smf2_settings` VALUES ('smfVersion','1.1.9'),('news','SMF - Just Installed!'),('compactTopicPagesContiguous','5'),('compactTopicPagesEnable','1'),('enableStickyTopics','1'),('todayMod','1'),('karmaMode','0'),('karmaTimeRestrictAdmins','1'),('enablePreviousNext','1'),('pollMode','1'),('enableVBStyleLogin','1'),('enableCompressedOutput','1'),('karmaWaitTime','1'),('karmaMinPosts','0'),('karmaLabel','Karma:'),('karmaSmiteLabel','[smite]'),('karmaApplaudLabel','[applaud]'),('attachmentSizeLimit','128'),('attachmentPostLimit','192'),('attachmentNumPerPostLimit','4'),('attachmentDirSizeLimit','10240'),('attachmentUploadDir','/Volumes/Spike/Work/Apps/SMF/attachments'),('attachmentExtensions','doc,gif,jpg,mpg,pdf,png,txt,zip'),('attachmentCheckExtensions','0'),('attachmentShowImages','1'),('attachmentEnable','1'),('attachmentEncryptFilenames','1'),('attachmentThumbnails','1'),('attachmentThumbWidth','150'),('attachmentThumbHeight','150'),('censorIgnoreCase','1'),('mostOnline','2'),('mostOnlineToday','1'),('mostDate','1246311493'),('allow_disableAnnounce','1'),('trackStats','1'),('userLanguage','1'),('titlesEnable','1'),('topicSummaryPosts','15'),('enableErrorLogging','1'),('max_image_width','0'),('max_image_height','0'),('onlineEnable','0'),('cal_holidaycolor','000080'),('cal_bdaycolor','920AC4'),('cal_eventcolor','078907'),('cal_enabled','0'),('cal_maxyear','2010'),('cal_minyear','2004'),('cal_daysaslink','0'),('cal_defaultboard',''),('cal_showeventsonindex','0'),('cal_showbdaysonindex','0'),('cal_showholidaysonindex','0'),('cal_showeventsoncalendar','1'),('cal_showbdaysoncalendar','1'),('cal_showholidaysoncalendar','1'),('cal_showweeknum','0'),('cal_maxspan','7'),('smtp_host',''),('smtp_port','25'),('smtp_username',''),('smtp_password',''),('mail_type','0'),('timeLoadPageEnable','0'),('totalTopics','8'),('totalMessages','22'),('simpleSearch','0'),('censor_vulgar',''),('censor_proper',''),('enablePostHTML','0'),('theme_allow','1'),('theme_default','1'),('theme_guests','1'),('enableEmbeddedFlash','0'),('xmlnews_enable','1'),('xmlnews_maxlen','255'),('hotTopicPosts','15'),('hotTopicVeryPosts','25'),('registration_method','0'),('send_validation_onChange','0'),('send_welcomeEmail','1'),('allow_editDisplayName','1'),('allow_hideOnline','1'),('allow_hideEmail','1'),('guest_hideContacts','0'),('spamWaitTime','5'),('pm_spam_settings','10,5,20'),('reserveWord','0'),('reserveCase','1'),('reserveUser','1'),('reserveName','1'),('reserveNames','Admin\nWebmaster\nGuest\nroot'),('autoLinkUrls','1'),('banLastUpdated','0'),('smileys_dir','/Volumes/Spike/Work/Apps/SMF/Smileys'),('smileys_url','http://localhost:8081/SMF/Smileys'),('avatar_directory','/Volumes/Spike/Work/Apps/SMF/avatars'),('avatar_url','http://localhost:8081/SMF/avatars'),('avatar_max_height_external','65'),('avatar_max_width_external','65'),('avatar_action_too_large','option_html_resize'),('avatar_max_height_upload','65'),('avatar_max_width_upload','65'),('avatar_resize_upload','1'),('avatar_download_png','1'),('failed_login_threshold','3'),('oldTopicDays','120'),('edit_wait_time','90'),('edit_disable_time','0'),('autoFixDatabase','1'),('allow_guestAccess','1'),('time_format','%B %d, %Y, %I:%M:%S %p'),('number_format','1234.00'),('enableBBC','1'),('max_messageLength','20000'),('max_signatureLength','300'),('autoOptDatabase','7'),('autoOptMaxOnline','0'),('autoOptLastOpt','1247508897'),('defaultMaxMessages','15'),('defaultMaxTopics','20'),('defaultMaxMembers','30'),('enableParticipation','1'),('recycle_enable','0'),('recycle_board','0'),('maxMsgID','23'),('enableAllMessages','0'),('fixLongWords','0'),('knownThemes','1,2,3'),('who_enabled','1'),('time_offset','0'),('cookieTime','60'),('lastActive','15'),('smiley_sets_known','default,classic'),('smiley_sets_names','Default\nClassic'),('smiley_sets_default','default'),('cal_days_for_index','7'),('requireAgreement','1'),('unapprovedMembers','0'),('default_personalText',''),('package_make_backups','1'),('databaseSession_enable','1'),('databaseSession_loose','1'),('databaseSession_lifetime','2880'),('search_cache_size','50'),('search_results_per_page','30'),('search_weight_frequency','30'),('search_weight_age','25'),('search_weight_length','20'),('search_weight_subject','15'),('search_weight_first_message','10'),('search_max_results','1200'),('permission_enable_deny','0'),('permission_enable_postgroups','0'),('permission_enable_by_board','0'),('global_character_set','UTF-8'),('globalCookies','1'),('default_timezone','Etc/GMT+5'),('memberlist_updated','1246311492'),('latestMember','2'),('totalMembers','2'),('latestRealName','test;'),('rand_seed','136916235'),('mostOnlineUpdated','2009-07-10'),('cal_today_updated','20090702'),('cal_today_holiday','a:1:{s:10:\"2009-07-04\";a:1:{i:0;s:16:\"Independence Day\";}}'),('cal_today_birthday','a:0:{}'),('cal_today_event','a:0:{}');
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_settings` ENABLE KEYS */;

--
-- Table structure for table `smf2_smileys`
--

DROP TABLE IF EXISTS `smf2_smileys`;
CREATE TABLE `smf2_smileys` (
  `ID_SMILEY` smallint(5) unsigned NOT NULL auto_increment,
  `code` varchar(30) NOT NULL default '',
  `filename` varchar(48) NOT NULL default '',
  `description` varchar(80) NOT NULL default '',
  `smileyRow` tinyint(4) unsigned NOT NULL default '0',
  `smileyOrder` smallint(5) unsigned NOT NULL default '0',
  `hidden` tinyint(4) unsigned NOT NULL default '0',
  PRIMARY KEY  (`ID_SMILEY`)
) ENGINE=MyISAM AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_smileys`
--


/*!40000 ALTER TABLE `smf2_smileys` DISABLE KEYS */;
LOCK TABLES `smf2_smileys` WRITE;
INSERT INTO `smf2_smileys` VALUES (1,':)','smiley.gif','Smiley',0,0,0),(2,';)','wink.gif','Wink',0,1,0),(3,':D','cheesy.gif','Cheesy',0,2,0),(4,';D','grin.gif','Grin',0,3,0),(5,'>:(','angry.gif','Angry',0,4,0),(6,':(','sad.gif','Sad',0,5,0),(7,':o','shocked.gif','Shocked',0,6,0),(8,'8)','cool.gif','Cool',0,7,0),(9,'???','huh.gif','Huh?',0,8,0),(10,'::)','rolleyes.gif','Roll Eyes',0,9,0),(11,':P','tongue.gif','Tongue',0,10,0),(12,':-[','embarrassed.gif','Embarrassed',0,11,0),(13,':-X','lipsrsealed.gif','Lips Sealed',0,12,0),(14,':-\\','undecided.gif','Undecided',0,13,0),(15,':-*','kiss.gif','Kiss',0,14,0),(16,':\'(','cry.gif','Cry',0,15,0),(17,'>:D','evil.gif','Evil',0,16,1),(18,'^-^','azn.gif','Azn',0,17,1),(19,'O0','afro.gif','Afro',0,18,1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_smileys` ENABLE KEYS */;

--
-- Table structure for table `smf2_themes`
--

DROP TABLE IF EXISTS `smf2_themes`;
CREATE TABLE `smf2_themes` (
  `ID_MEMBER` mediumint(8) NOT NULL default '0',
  `ID_THEME` tinyint(4) unsigned NOT NULL default '1',
  `variable` tinytext NOT NULL,
  `value` text NOT NULL,
  PRIMARY KEY  (`ID_THEME`,`ID_MEMBER`,`variable`(30)),
  KEY `ID_MEMBER` (`ID_MEMBER`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_themes`
--


/*!40000 ALTER TABLE `smf2_themes` DISABLE KEYS */;
LOCK TABLES `smf2_themes` WRITE;
INSERT INTO `smf2_themes` VALUES (0,1,'name','SMF Default Theme - Core'),(0,1,'theme_url','http://localhost:8081/SMF/Themes/default'),(0,1,'images_url','http://localhost:8081/SMF/Themes/default/images'),(0,1,'theme_dir','/Volumes/Spike/Work/Apps/SMF/Themes/default'),(0,1,'show_bbc','1'),(0,1,'show_latest_member','1'),(0,1,'show_modify','1'),(0,1,'show_user_images','1'),(0,1,'show_blurb','1'),(0,1,'show_gender','0'),(0,1,'show_newsfader','0'),(0,1,'number_recent_posts','0'),(0,1,'show_member_bar','1'),(0,1,'linktree_link','1'),(0,1,'show_profile_buttons','1'),(0,1,'show_mark_read','1'),(0,1,'show_sp1_info','1'),(0,1,'linktree_inline','0'),(0,1,'show_board_desc','1'),(0,1,'newsfader_time','5000'),(0,1,'allow_no_censored','0'),(0,1,'additional_options_collapsable','1'),(0,1,'use_image_buttons','1'),(0,1,'enable_news','1'),(0,2,'name','Classic YaBB SE Theme'),(0,2,'theme_url','http://localhost:8081/SMF/Themes/classic'),(0,2,'images_url','http://localhost:8081/SMF/Themes/classic/images'),(0,2,'theme_dir','/Volumes/Spike/Work/Apps/SMF/Themes/classic'),(0,3,'name','Babylon Theme'),(0,3,'theme_url','http://localhost:8081/SMF/Themes/babylon'),(0,3,'images_url','http://localhost:8081/SMF/Themes/babylon/images'),(0,3,'theme_dir','/Volumes/Spike/Work/Apps/SMF/Themes/babylon');
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_themes` ENABLE KEYS */;

--
-- Table structure for table `smf2_topics`
--

DROP TABLE IF EXISTS `smf2_topics`;
CREATE TABLE `smf2_topics` (
  `ID_TOPIC` mediumint(8) unsigned NOT NULL auto_increment,
  `isSticky` tinyint(4) NOT NULL default '0',
  `ID_BOARD` smallint(5) unsigned NOT NULL default '0',
  `ID_FIRST_MSG` int(10) unsigned NOT NULL default '0',
  `ID_LAST_MSG` int(10) unsigned NOT NULL default '0',
  `ID_MEMBER_STARTED` mediumint(8) unsigned NOT NULL default '0',
  `ID_MEMBER_UPDATED` mediumint(8) unsigned NOT NULL default '0',
  `ID_POLL` mediumint(8) unsigned NOT NULL default '0',
  `numReplies` int(10) unsigned NOT NULL default '0',
  `numViews` int(10) unsigned NOT NULL default '0',
  `locked` tinyint(4) NOT NULL default '0',
  PRIMARY KEY  (`ID_TOPIC`),
  UNIQUE KEY `lastMessage` (`ID_LAST_MSG`,`ID_BOARD`),
  UNIQUE KEY `firstMessage` (`ID_FIRST_MSG`,`ID_BOARD`),
  UNIQUE KEY `poll` (`ID_POLL`,`ID_TOPIC`),
  KEY `isSticky` (`isSticky`),
  KEY `ID_BOARD` (`ID_BOARD`)
) ENGINE=MyISAM AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `smf2_topics`
--


/*!40000 ALTER TABLE `smf2_topics` DISABLE KEYS */;
LOCK TABLES `smf2_topics` WRITE;
INSERT INTO `smf2_topics` VALUES (1,0,1,1,21,0,1,0,6,18,0),(2,0,1,3,3,1,1,0,0,3,0),(3,0,1,4,4,1,1,0,0,1,0),(4,0,1,5,5,1,1,0,0,0,0),(5,0,1,6,6,1,1,0,0,0,0),(6,0,2,8,22,1,1,0,8,17,0),(7,0,1,9,9,1,1,0,0,1,0),(8,0,1,23,23,1,1,0,0,1,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `smf2_topics` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

