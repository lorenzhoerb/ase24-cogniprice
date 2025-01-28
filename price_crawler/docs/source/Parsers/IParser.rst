Website Parser Interface
========================

The `IParser <#crawler.web_crawler.parsers.IParser.IParser>`_ class is an abstract base class that defines the essential methods for extracting data from a web page, such as price and currency. It provides the following key methods, which must be implemented by any subclass:

    * `get_currency() <#crawler.web_crawler.parsers.IParser.IParser.get_currency>`_: Retrieves the currency used on the website. This method should return a string representing the currency, or None if the currency is not available.
    * `get_price() <#crawler.web_crawler.parsers.IParser.IParser.get_price>`_: Retrieves the price for the crawled product. The method should return a float value representing the price, or None if the price is not available.

This class serves as a blueprint for creating concrete implementations that parse different types of web pages and extract pricing information.


.. automodule:: crawler.web_crawler.parsers.IParser