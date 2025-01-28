from crawler.web_crawler.parsers.IParser import IParser
from crawler.web_crawler.parsers.ParserHelper import ParserHelper
from crawler.utils.ConfigReader import ConfigReader

from typing import Optional
from bs4 import BeautifulSoup
import re

import logging

class WoocommerceParser(IParser):

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
        Extracts the product price from the HTML page

        :return: The parsed price of the product, or None if no price is found or an error occurs.
        :rtype: Optional[float]
        :raises: Any exceptions raised during the HTML parsing will be logged and re-raised.
        """
        # 1. search for: <script type="application/ld+json"
        try:
            script_tags = self.soup.find_all('script', {'type': 'application/ld+json'})
            if script_tags:
                for script_tag in script_tags:
                    # match for: ,"price":"24.99",
                    price_match = re.search(r',\"price\":\"(.*?)\",', str(script_tag))
                    if price_match:
                        price = price_match.group(1)
                        return ParserHelper.parser_price_to_float(self.logger, price)
        except Exception as e:
            self.logger.error("Could not find script tag which declares the price.")
            raise

        # No price could be found
        return None


    def get_currency(self):
        """
        Extracts the product's currency from the HTML page

        :return: The currency of the product, or None if no currency is found or an error occurs.
        :rtype: Optional[str]
        :raises: Any exceptions raised during the HTML parsing will be logged and re-raised.
        """
        # 1. search for: <script type="application/ld+json"
        try:
            script_tags = self.soup.find_all('script', {'type': 'application/ld+json'})
            if script_tags:
                for script_tag in script_tags:
                    # match for: ,"priceCurrency":"USD",
                    currency_match = re.search(r',\"priceCurrency\":\"(.*?)\",', str(script_tag))
                    if currency_match:
                        currency = currency_match.group(1)
                        return str(currency)
        except Exception as e:
            self.logger.error("Could not find script tag which declares the currency.")
            raise

        # no currency could be found
        return None