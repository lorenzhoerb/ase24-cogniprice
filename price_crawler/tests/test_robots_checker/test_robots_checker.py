import unittest

from unittest.mock import patch, MagicMock

import requests

from crawler.utils.ConfigReader import ConfigReader
from crawler.web_crawler.CrawlerExceptions import URLParseException, RobotParseException
from crawler.utils.RobotsChecker import RobotsChecker

class TestRobotsChecker(unittest.TestCase):

    def setUp(self):
        # Mock the configuration reader
        self.mock_config = MagicMock(spec=ConfigReader)
        self.mock_config.get.side_effect = lambda section, key: {
            'General': {'robots_crawl_timeout': 5, 'crawler_name': 'test_crawler'},
            'Crawler': {'robots_txt_cache_time': 600}
        }[section][key]
        self.robots_checker = RobotsChecker(self.mock_config)

    @patch('crawler.utils.RobotsChecker.requests.get')
    def test_check_crawl_allowance_cache_hit(self, mock_requests):
        # Simulate a cache hit
        self.robots_checker._store_cache_allowance("https://example.com/robots.txt", True)
        self.assertTrue(self.robots_checker.check_crawl_allowance("https://example.com"))

    @patch('crawler.utils.RobotsChecker.requests.get')
    def test_check_crawl_allowance_cache_miss_allowed(self, mock_requests):
        # Mock response for requests.get
        mock_response = MagicMock()
        mock_response.text = "User-agent: *\nAllow: /"
        mock_requests.return_value = mock_response

        # Test crawl allowance for a new URL
        self.assertTrue(self.robots_checker.check_crawl_allowance("https://example.com"))

    @patch('crawler.utils.RobotsChecker.requests.get')
    def test_check_crawl_allowance_cache_miss_disallowed(self, mock_requests):
        # Mock response for requests.get
        mock_response = MagicMock()
        mock_response.text = "User-agent: *\nDisallow: /"
        mock_requests.return_value = mock_response

        # Test crawl disallowance
        self.assertFalse(self.robots_checker.check_crawl_allowance("https://example.com"))

    def test_get_robots_url_valid(self):
        # Test valid URL conversion
        result = self.robots_checker._get_robots_url("https://example.com/page")
        self.assertEqual(result, "https://example.com/robots.txt")


    @patch('crawler.utils.RobotsChecker.requests.get')
    def test_check_crawl_allowance_fetch_error(self, mock_requests):
        # Mock requests.get to raise a ConnectionError
        mock_requests.side_effect = requests.ConnectionError

        with self.assertRaises(requests.ConnectionError):
            self.robots_checker.check_crawl_allowance("https://example.com")

    @patch('crawler.utils.RobotsChecker.requests.get')
    @patch('crawler.utils.RobotsChecker.Protego.parse')
    def test_check_crawl_allowance_parse_error(self, mock_parse, mock_requests):
        # Mock requests.get with invalid robots.txt
        mock_response = MagicMock()
        mock_response.text = "invalid robots"  # Content that Protego can't parse
        mock_requests.return_value = mock_response

        # Mock Protego.parse to raise an exception
        mock_parse.side_effect = Exception("Failed to parse robots.txt")

        # Verify RobotParseException is raised
        with self.assertRaises(RobotParseException):
            self.robots_checker.check_crawl_allowance("https://example.com")

    @patch('requests.get')
    def test_missing_schema_error(self, mock_get):
        """
        Test that MissingSchema exception is handled correctly.
        """
        mock_get.side_effect = requests.exceptions.MissingSchema

        with self.assertRaises(requests.exceptions.MissingSchema):
            self.robots_checker.check_crawl_allowance("example.com")

    @patch('requests.get')
    def test_connection_error(self, mock_get):
        """
        Test that ConnectionError exception is handled correctly.
        """
        mock_get.side_effect = requests.ConnectionError

        with self.assertRaises(requests.ConnectionError):
            self.robots_checker.check_crawl_allowance("https://example.com")

    @patch('requests.get')
    def test_too_many_redirects_error(self, mock_get):
        """
        Test that TooManyRedirects exception is handled correctly.
        """
        mock_get.side_effect = requests.TooManyRedirects

        with self.assertRaises(requests.TooManyRedirects):
            self.robots_checker.check_crawl_allowance("https://example.com")

    @patch('requests.get')
    def test_timeout_error(self, mock_get):
        """
        Test that Timeout exception is handled correctly.
        """
        mock_get.side_effect = requests.Timeout

        with self.assertRaises(requests.Timeout):
            self.robots_checker.check_crawl_allowance("https://example.com")

    @patch('requests.get')
    def test_generic_request_exception(self, mock_get):
        """
        Test that a generic exception during requests.get is handled correctly.
        """
        mock_get.side_effect = Exception("Generic Request Exception")

        with self.assertRaises(Exception) as context:
            self.robots_checker.check_crawl_allowance("https://example.com")
        self.assertEqual(str(context.exception), "Generic Request Exception")

    @patch('requests.get')
    @patch('protego.Protego.parse')
    def test_parsing_error(self, mock_parse, mock_get):
        """
        Test that an error during Protego.parse is handled correctly.
        """
        mock_response = MagicMock()
        mock_response.text = "invalid robots"  # Content Protego cannot parse
        mock_get.return_value = mock_response
        mock_parse.side_effect = Exception("Parsing Failed")

        with self.assertRaises(RobotParseException):
            self.robots_checker.check_crawl_allowance("https://example.com")


if __name__ == "__main__":
    unittest.main()
