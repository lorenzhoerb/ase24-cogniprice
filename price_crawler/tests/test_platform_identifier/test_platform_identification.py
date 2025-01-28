import pytest
from crawler.web_crawler.PlatformIdentifier import PlatformIdentifier
from crawler.utils.LoggingUtil import setup_logging
from crawler.utils.ConfigReader import ConfigReader
from crawler.web_crawler.parsers.ShopifyParser import ShopifyParser
from crawler.web_crawler.parsers.WoocommerceParser import WoocommerceParser
import logging

import os


class TestPlatformIdentification:

    @pytest.fixture
    def platform_identifier(self):
        config = ConfigReader("config.dev.ini")
        logger = setup_logging(log_level=logging.INFO)
        platform_identifier: PlatformIdentifier = PlatformIdentifier(logger, config)

        return platform_identifier


    def test_shopify_platform_identification(self, platform_identifier):

        html_directory = str(os.getcwd()) + "/tests/test_platform_identifier/htmls/shopify"

        # List all files in the html directory
        files = [f for f in os.listdir(html_directory) if os.path.isfile(os.path.join(html_directory, f))]

        for file_name in files:
            file_path = os.path.join(html_directory, file_name)
            with open(file_path, "r", encoding="utf-8") as file:
                html_page = file.read()

            try:
                site_parser = platform_identifier.get_correct_parser(html_page)
            except Exception as e:
                pytest.fail("site_parser raised an exception: {}".format(e))
                exit()

            assert isinstance(site_parser, ShopifyParser), "The file " + file_path + " is not a ShopifyParser instance"
            print(f"\nTest successful for file: {file_path}")

    def test_woocommerce_platform_identification(self, platform_identifier):
        html_directory = str(os.getcwd()) + "/tests/test_platform_identifier/htmls/woocommerce"

        # List all files in the html directory
        files = [f for f in os.listdir(html_directory) if os.path.isfile(os.path.join(html_directory, f))]

        for file_name in files:
            file_path = os.path.join(html_directory, file_name)
            with open(file_path, "r", encoding="utf-8") as file:
                html_page = file.read()

            try:
                site_parser = platform_identifier.get_correct_parser(html_page)
            except Exception as e:
                pytest.fail("site_parser raised an exception: {}".format(e))
                exit()

            assert isinstance(site_parser, WoocommerceParser), "The file " + file_path + " is not a WoocommerceParser instance"
            print(f"\nTest successful for file: {file_path}")
