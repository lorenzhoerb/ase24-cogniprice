from crawler.web_crawler.parsers.ShopifyParser import ShopifyParser
from crawler.utils.LoggingUtil import setup_logging
from crawler.utils.ConfigReader import ConfigReader
from bs4 import BeautifulSoup
import os
import pytest
import logging

class TestShopifyParser:

    @pytest.fixture
    def config_object(self):
        return ConfigReader("config.dev.ini")

    @pytest.fixture
    def logger_object(self):
        return setup_logging(log_level=logging.INFO)


    def test_shopify_parser(self, config_object, logger_object):

        html_directory = str(os.getcwd()) + "/tests/test_platform_identifier/htmls/shopify"

        # List all files in the html directory
        files = [f for f in os.listdir(html_directory) if os.path.isfile(os.path.join(html_directory, f))]

        for file_name in files:
            file_path = os.path.join(html_directory, file_name)
            with open(file_path, "r", encoding="utf-8") as file:
                html_page = file.read()

            try:
                soup = BeautifulSoup(html_page, 'html.parser')
            except Exception as e:
                pytest.fail("Beautiful soup could not parse html: {}".format(e))
                exit()

            sparser = ShopifyParser(logger_object, config_object, soup)

            try:
                price = sparser.get_price()
                if not price:
                    pytest.fail("Could not extract PRICE from file: {}".format(file_name))
            except Exception as e:
                pytest.fail("Could not extract PRICE from file: {}".format(file_name))
                exit()



            try:
                currency = sparser.get_currency()
                if not currency:
                    pytest.fail("Could not extract CURRENCY from file: {}".format(file_name))
            except Exception as e:
                pytest.fail("Could not extract CURRENCY from file: {}".format(file_name))
                exit()

