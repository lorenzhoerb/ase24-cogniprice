from crawler.utils.ConfigReader import ConfigReader
from crawler.web_crawler.parsers.IParser import IParser
from crawler.web_crawler.parsers.ShopifyParser import ShopifyParser
from crawler.web_crawler.parsers.WoocommerceParser import WoocommerceParser

from bs4 import BeautifulSoup
from typing import Optional

import logging
import re

class PlatformIdentifier:

    def __init__(self, logger: logging.Logger, config: ConfigReader) -> None:
        """
        Initializes the class with the provided logger and configuration.

        :param logger: A logger instance for logging information, warnings, errors, etc.
        :type logger: logging.Logger
        :param config: A configuration reader that provides access to application settings.
        :type config: ConfigReader
        """
        self.logger = logger
        self.config = config

    def get_correct_parser(self, html_page: str) -> Optional[IParser]:
        """
        Determines the correct parser for the provided HTML page based on the content.

        This method parses the given HTML page and looks for specific identifiers
        in the script tags to identify the type of website or framework (e.g., Shopify).
        If a matching identifier is found, the appropriate parser is returned.

        :param html_page: The HTML content of the webpage to parse.
        :type html_page: str
        :return: An appropriate parser object if a match is found, otherwise None.
        :rtype: Optional[IParser]
        :raises Exception: If there is an error parsing the HTML content.
        """
        # PARSE HTML
        try:
            soup = BeautifulSoup(html_page, 'html.parser')
        except Exception as e:
            self.logger.error("Failed to parse html page.")
            raise

        # SEARCH HTML

        ## Search the HTMLs script tags
        shopify_script_line = "var Shopify = Shopify || {};"

        # returns an empty list if nothing was found
        script_tags = soup.find_all("script")

        # only search when results were returned
        if script_tags:
            for script in script_tags:
                if script.string:
                    # search predefined strings
                    # SHOPIFY
                    if shopify_script_line in script.string:
                        if self.__verify_shopify_via_script(script.string):
                            # return shopify parser object
                            return ShopifyParser(self.logger, self.config, soup)

        ## Search the HTML
        if self.__verify_woocommerce_via_classnames(html_page):
            return WoocommerceParser(self.logger, self.config, soup)


        # no compatible parser found
        return None


    def __verify_woocommerce_via_classnames(self, html: str) -> bool:
        """
        Verifies if the provided html contains multiple strings with "woocommerce" in it.


        :param html: The html content of the webpage.
        :type html: str
        :return: True if more than ten strings with woocommerce are in the html, otherwise False.
        :rtype: bool
        """
        woo_count = 0
        try:
            woo_count = len(re.findall("woocommerce", html, re.IGNORECASE))
        except Exception as e:
            self.logger.error("Failed to parse html page. Exception: {}".format(e))
            return False

        if woo_count > 10:
            return True
        else:
            return False


    def __verify_shopify_via_script(self, script_content: str) -> bool:
        """
        Verifies if the provided script content contains key Shopify-related properties.

        What this function looks for in the html file:
            var Shopify = Shopify || {};
            Shopify.shop = "eaf526-3.myshopify.com";
            Shopify.locale = "de";
            Shopify.currency = {"active":"EUR","rate":"1.0"};
            Shopify.country = "AT";
            Shopify.theme = {"name":"Flaminguin Online Store Gurtband 02.11.","id":174641971545,"schema_name":"Shapes","schema_version":"2.2.5","theme_store_id":1535,"role":"main"};
            Shopify.theme.handle = "null";
            Shopify.theme.style = {"id":null,"handle":null};
            Shopify.cdnHost = "flaminguin.net/cdn";
            Shopify.routes = Shopify.routes || {};
            Shopify.routes.root = "/";

        :param script_content: The content of the script tag to be checked.
        :type script_content: str
        :return: True if more than three Shopify-related properties are found, otherwise False.
        :rtype: bool
        """
        properties_to_check = [
            "Shopify.shop",
            "Shopify.locale",
            "Shopify.currency",
            "Shopify.country",
            "Shopify.theme",
            "Shopify.cdnHost",
            "Shopify.routes.root"
        ]
        found_properties = []

        for prop in properties_to_check:
            # Create a regex to match the property
            regex = re.compile(rf"{re.escape(prop)}\s*=")
            # Check if the property exists in the script
            if regex.search(script_content):
                found_properties.append(prop)

        if len(found_properties) > 3:
            # more than three variables found in script tag -> match
            return True
        else:
            # less than four variables found in script tag -> no match
            return False
