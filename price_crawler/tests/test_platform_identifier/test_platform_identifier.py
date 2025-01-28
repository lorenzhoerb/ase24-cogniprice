import unittest
from unittest.mock import MagicMock
from crawler.utils.ConfigReader import ConfigReader
from crawler.web_crawler.parsers.IParser import IParser
from crawler.web_crawler.parsers.ShopifyParser import ShopifyParser
from crawler.web_crawler.PlatformIdentifier import PlatformIdentifier
import logging

class TestPlatformIdentifier(unittest.TestCase):

    def setUp(self):
        # Mock logger and config
        self.logger = logging.getLogger("test_logger")
        self.config = MagicMock(spec=ConfigReader)

        # Create PlatformIdentifier instance
        self.platform_identifier = PlatformIdentifier(self.logger, self.config)

    def test_shopify_parser_identification(self):
        shopify_html = """
        <html>
            <head></head>
            <body>
                <script>
                    var Shopify = Shopify || {};
                    Shopify.shop = "example.myshopify.com";
                    Shopify.locale = "en";
                    Shopify.currency = {"active":"USD","rate":"1.0"};
                    Shopify.country = "US";  <!-- Add this line -->
                </script>
            </body>
        </html>
        """
        parser = self.platform_identifier.get_correct_parser(shopify_html)
        self.assertIsInstance(parser, ShopifyParser)

    def test_woocommerce_parser_identification(self):
        # Mock WooCommerce parser behavior for future implementation
        woocommerce_html = """
        <html>
            <head></head>
            <body>
                <meta name="generator" content="WooCommerce 7.1">
            </body>
        </html>
        """
        # TODO only null returned for now, change when woocommerce is implemented
        parser = self.platform_identifier.get_correct_parser(woocommerce_html)
        self.assertIsNone(parser)  # Update this when WooCommerceParser is implemented

    def test_generic_parser_for_unknown_platform(self):
        unknown_html = """
        <html>
            <head></head>
            <body>
                <p>This is an unknown platform.</p>
            </body>
        </html>
        """
        parser = self.platform_identifier.get_correct_parser(unknown_html)
        self.assertIsNone(parser) #TODO also needs to be changed to the later specifications on generic crawlers


    def test_partial_shopify_match(self):
        partial_shopify_html = """
        <html>
            <head></head>
            <body>
                <script>
                    var Shopify = Shopify || {};
                    Shopify.shop = "example.myshopify.com";
                </script>
            </body>
        </html>
        """
        parser = self.platform_identifier.get_correct_parser(partial_shopify_html)
        self.assertIsNone(parser)  # Not enough Shopify properties to confirm a match

    def test_shopify_script_verification(self):
        shopify_script = """
        var Shopify = Shopify || {};
        Shopify.shop = "example.myshopify.com";
        Shopify.locale = "en";
        Shopify.currency = {"active":"USD","rate":"1.0"};
        Shopify.country = "US";
        Shopify.theme = {"name":"Default","id":123};
        Shopify.cdnHost = "cdn.shopify.com";
        Shopify.routes.root = "/";
        """
        self.assertTrue(self.platform_identifier._PlatformIdentifier__verify_shopify_via_script(shopify_script))

    def test_invalid_shopify_script_verification(self):
        invalid_script = """
        var SomeOtherFramework = SomeOtherFramework || {};
        """
        self.assertFalse(self.platform_identifier._PlatformIdentifier__verify_shopify_via_script(invalid_script))

if __name__ == "__main__":
    unittest.main()
