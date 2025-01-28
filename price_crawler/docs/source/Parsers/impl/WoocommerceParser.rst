Woocommerce Parser
==================

The `WoocommerceParser <#crawler.web_crawler.parsers.WoocommerceParser.WoocommerceParser>`_ class extracts product price and currency information from the HTML page using `BeautifulSoup` to search for specific script tags:

* **Currency Extraction:** The `get_currency() <#crawler.web_crawler.parsers.WoocommerceParser.WoocommerceParser.get_currency>`_ method searches for a script tag with the type `type="application/ld+json"` and extracts the currency if available.
* **Price Extraction:** The `get_price() <#crawler.web_crawler.parsers.WoocommerceParser.WoocommerceParser.get_price>`_ method looks for a script tag with the type `type="application/ld+json"` and extracts its value if available.

.. automodule:: crawler.web_crawler.parsers.WoocommerceParser