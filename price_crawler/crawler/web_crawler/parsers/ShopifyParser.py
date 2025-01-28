from crawler.web_crawler.parsers.IParser import IParser
from crawler.web_crawler.parsers.ParserHelper import ParserHelper
from crawler.utils.ConfigReader import ConfigReader


from typing import Optional
from bs4 import BeautifulSoup

import logging
import re


class ShopifyParser(IParser):

    def __init__(self, logger: logging.Logger, config: ConfigReader, soup: BeautifulSoup) -> None:
        """
        Initializes the object with the provided logger, configuration, and BeautifulSoup object.

        :param logger: The logger object used for logging messages and errors.
        :type logger: logging.Logger
        :param config: The configuration reader used for accessing settings.
        :type config: ConfigReader
        :param soup: The BeautifulSoup object used for parsing HTML content.
        :type soup: BeautifulSoup
        """
        self.logger = logger
        self.config = config

        self.soup = soup


    def get_price(self) -> Optional[float]:
        """
        Extracts the product price from the HTML page by
        1. searching for the meta tag with the property "og:price:amount".
        2. searching for the window.ShopifyAnalytics.lib.track object
        If the tag is found, it parses the price
        value and returns it as a float.

        If the meta tag is not found or cannot be parsed, the method returns None.

        :return: The parsed price of the product, or None if no price is found or an error occurs.
        :rtype: Optional[float]
        :raises: Any exceptions raised during the HTML parsing will be logged and re-raised.
        """

        # 1. searching for the meta tag with the property "og:price:amount".
        try:
            meta_tag = self.soup.find("meta", property="og:price:amount")
            if meta_tag:
                return ParserHelper.parser_price_to_float(self.logger, meta_tag.get("content"))
        except Exception as e:
            self.logger.error("Could not find meta tag which declares the price.")
            raise

        # 2. searching for the window.ShopifyAnalytics.lib.track object
        try:
            script_tag = self.soup.find('script', class_='analytics', string=re.compile(r'window.ShopifyAnalytics.lib.track'))
            if script_tag:
                script_content = script_tag.string
                # extract the price
                price_match = re.search(r'window\.ShopifyAnalytics\.lib\.track\(.*\"price\":\"(\d+\.\d+)\"', script_content)
                if price_match:
                    # returns the first capture group
                    price = price_match.group(1)
                    return ParserHelper.parser_price_to_float(self.logger, price)
        except Exception as e:
            self.logger.error("Could not find window.ShopifyAnalytics.lib.track object which includes the price.")
            raise

        # No price could be found
        return None


    def get_currency(self):
        """
        Extracts the product's currency from the HTML page by
        1. searching for the meta tag with the property "og:price:currency"
        2. searching for the window.ShopifyAnalytics.lib.track object
        If the tag is found, it returns the currency
        as a string.

        If the meta tag is not found, the method returns None.

        :return: The currency of the product, or None if no currency is found or an error occurs.
        :rtype: Optional[str]
        :raises: Any exceptions raised during the HTML parsing will be logged and re-raised.
        """

        # 1. searching for the meta tag with the property "og:price:currency"
        try:
            meta_tag = self.soup.find("meta", property="og:price:currency")
            if meta_tag:
                return meta_tag.get("content")
        except Exception as e:
            self.logger.error("Could not find meta tag which declares the currency.")
            raise

        # 2. searching for the window.ShopifyAnalytics.lib.track object
        try:
            script_tag = self.soup.find('script', class_='analytics', string=re.compile(r'window.ShopifyAnalytics.lib.track'))
            if script_tag:
                script_content = script_tag.string
                # extract the currency
                currency_match = re.search(r'window\.ShopifyAnalytics\.lib\.track\(.*\"currency\":\"([a-zA-Z]*)\"', script_content)
                if currency_match:
                    # returns the first capture group
                    currency = currency_match.group(1)
                    return str(currency)
        except Exception as e:
            self.logger.error("Could not find window.ShopifyAnalytics.lib.track object which includes the price.")
            raise

        # no currency could be found
        return None