import unittest
from unittest.mock import MagicMock, patch
import configparser
import os
from tempfile import NamedTemporaryFile
from crawler.utils.ConfigReader import ConfigReader


class TestConfigReader(unittest.TestCase):

    def setUp(self):
        # Mock the logger to avoid unnecessary log outputs during tests
        self.mock_logger = MagicMock()

    def test_valid_config(self):
        # Create a valid temporary config file
        config_content = """
        [General]
        crawler_name = TestCrawler
        robots_crawl_timeout = 10

        [Kafka]
        bootstrap_servers = localhost:9092
        kafka_topic = test_topic
        kafka_response_topic = test_response_topic

        [Crawler]
        robots_txt_cache_time = 5

        [Selenium]
        page_load_timeout = 30
        """
        with NamedTemporaryFile(delete=False) as tmp_config:
            tmp_config.write(config_content.encode())
            tmp_config_path = tmp_config.name

        try:
            reader = ConfigReader(config_file=tmp_config_path)
            reader.logger = self.mock_logger

            # Check if the values are correctly read
            self.assertEqual(reader.get("General", "crawler_name"), "TestCrawler")
            self.assertEqual(reader.get("General", "robots_crawl_timeout"), "10")
        finally:
            os.remove(tmp_config_path)  # Clean up

    def test_missing_config_file(self):
        with self.assertRaises(FileNotFoundError):
            ConfigReader(config_file="non_existent_config.ini").logger = self.mock_logger

    def test_missing_mandatory_section(self):
        config_content = """
        [General]
        crawler_name = TestCrawler
        robots_crawl_timeout = 10
        """
        with NamedTemporaryFile(delete=False) as tmp_config:
            tmp_config.write(config_content.encode())
            tmp_config_path = tmp_config.name

        try:
            with self.assertRaises(configparser.ParsingError):
                ConfigReader(config_file=tmp_config_path).logger = self.mock_logger
        finally:
            os.remove(tmp_config_path)

    def test_missing_mandatory_key(self):
        config_content = """
        [General]
        crawler_name = TestCrawler

        [Kafka]
        bootstrap_servers = localhost:9092
        kafka_response_topic = test_response_topic

        [Crawler]
        robots_txt_cache_time = 5

        [Selenium]
        page_load_timeout = 30
        """
        with NamedTemporaryFile(delete=False) as tmp_config:
            tmp_config.write(config_content.encode())
            tmp_config_path = tmp_config.name

        try:
            with self.assertRaises(configparser.ParsingError):
                ConfigReader(config_file=tmp_config_path).logger = self.mock_logger
        finally:
            os.remove(tmp_config_path)

    def test_invalid_type_in_config(self):
        config_content = """
        [General]
        crawler_name = TestCrawler
        robots_crawl_timeout = NotANumber

        [Kafka]
        bootstrap_servers = localhost:9092
        kafka_topic = test_topic
        kafka_response_topic = test_response_topic

        [Crawler]
        robots_txt_cache_time = 5

        [Selenium]
        page_load_timeout = 30
        """
        with NamedTemporaryFile(delete=False) as tmp_config:
            tmp_config.write(config_content.encode())
            tmp_config_path = tmp_config.name

        try:
            with self.assertRaises(configparser.ParsingError):
                ConfigReader(config_file=tmp_config_path).logger = self.mock_logger
        finally:
            os.remove(tmp_config_path)

    def test_get_invalid_section_or_option(self):
        config_content = """
        [General]
        crawler_name = TestCrawler
        robots_crawl_timeout = 10

        [Kafka]
        bootstrap_servers = localhost:9092
        kafka_topic = test_topic
        kafka_response_topic = test_response_topic

        [Crawler]
        robots_txt_cache_time = 5

        [Selenium]
        page_load_timeout = 30
        """
        with NamedTemporaryFile(delete=False) as tmp_config:
            tmp_config.write(config_content.encode())
            tmp_config_path = tmp_config.name

        try:
            reader = ConfigReader(config_file=tmp_config_path)
            reader.logger = self.mock_logger

            # Try accessing a non-existent section or key
            self.assertIsNone(reader.get("InvalidSection", "some_key"))
            self.assertIsNone(reader.get("General", "non_existent_key"))
        finally:
            os.remove(tmp_config_path)


if __name__ == "__main__":
    unittest.main()
