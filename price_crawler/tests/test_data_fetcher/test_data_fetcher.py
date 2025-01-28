import unittest
from unittest.mock import patch, MagicMock
from crawler.web_crawler.DataFetcher import DataFetcher
from crawler.utils.ConfigReader import ConfigReader
import logging


class TestDataFetcher(unittest.TestCase):

    def setUp(self):
        self.logger = logging.getLogger("test_logger")
        self.config = MagicMock(spec=ConfigReader)
        self.config.get.return_value = '30'  # Mock page load timeout as 30 seconds

    @patch('crawler.web_crawler.DataFetcher.webdriver.Chrome')
    def test_fetch_data_with_valid_url(self, mock_chrome):
        # Mock the WebDriver and its methods
        mock_driver = MagicMock()
        mock_driver.page_source = "<html><body>Valid Page</body></html>"
        mock_chrome.return_value = mock_driver

        # Instantiate DataFetcher
        fetcher = DataFetcher(self.logger, self.config)

        # Mock a validated URL
        valid_url = "http://valid-url.com"

        # Call fetch_data and verify
        html = fetcher.fetch_data(valid_url)
        mock_driver.get.assert_called_once_with(valid_url)
        self.assertEqual(html, "<html><body>Valid Page</body></html>")

    @unittest.skip("Temporarily skipping this test till iplemented")
    @patch('crawler.web_crawler.DataFetcher.webdriver.Chrome')
    def test_fetch_data_with_invalid_url(self, mock_chrome): #TODO: i think this is not implemented yet, will have a look later
        # Mock the WebDriver to simulate exception
        mock_driver = MagicMock()
        mock_driver.get.side_effect = Exception("Invalid URL")
        mock_chrome.return_value = mock_driver

        fetcher = DataFetcher(self.logger, self.config)

        # Simulate an invalid URL
        invalid_url = "http://invalid-url.com"

        with self.assertRaises(Exception) as context:
            fetcher.fetch_data(invalid_url)

        self.assertIn("Could not load html of page", str(context.exception))
        mock_driver.get.assert_called_once_with(invalid_url)


if __name__ == "__main__":
    unittest.main()
