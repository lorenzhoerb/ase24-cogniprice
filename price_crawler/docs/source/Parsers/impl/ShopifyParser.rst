Shopify Parser
==============

The `ShopifyParser <#crawler.web_crawler.parsers.ShopifyParser.ShopifyParser>`_ class extracts product price and currency information from the HTML page using `BeautifulSoup` to search for specific meta tags:

* **Currency Extraction:** The `get_currency() <#crawler.web_crawler.parsers.ShopifyParser.ShopifyParser.get_currency>`_ method searches for a meta tag with the property `"og:price:currency"` and extracts its value if available.
* **Price Extraction:** The `get_price() <#crawler.web_crawler.parsers.ShopifyParser.ShopifyParser.get_price>`_ method looks for a meta tag with the property `"og:price:amount"` and extracts its value if available.

.. automodule:: crawler.web_crawler.parsers.ShopifyParser