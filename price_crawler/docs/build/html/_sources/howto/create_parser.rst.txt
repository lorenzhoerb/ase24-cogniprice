HOWTO: Add a Website Parser
===========================

To add a new parser to a web crawler, start by creating a Python file dedicated to handling the extraction logic for the new website. This file will contain the functionality required to parse the HTML source code and retrieve the relevant data.
After implementing the extraction logic, update the web crawler to identify and route requests to the new parser based on the target website. This ensures the crawler can correctly apply the new logic when processing pages from the specified source.


---------------------------
1. Create a new Parser File
---------------------------

1. **Create a New Parser File**:
   The new file should be named based on the target website. The format for the filename is `[WebsiteName]Parser.py`, where `[WebsiteName]` is the name of the website you're targeting (e.g., `WebsiteNameParser.py`).

   Place this new file in the directory:
   ``24ws-ase-pr-qse-02/price_crawler/crawler/web_crawler/parsers``.

2. **Implement the `IParser` Interface**:
   Your parser file must implement the `IParser` interface, which is located in the same directory. This interface defines the required methods for extracting data from the website's HTML.

Example Parser File: `WebsiteNameParser.py`

.. code-block:: python

    from crawler.web_crawler.parsers.IParser import IParser
    from crawler.web_crawler.parsers.ParserHelper import ParserHelper
    from crawler.utils.ConfigReader import ConfigReader

    from typing import Optional
    from bs4 import BeautifulSoup

    import logging

    class WebsiteNameParser(IParser):

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
            return


        def get_currency(self):
            """
            Extracts the product's currency from the HTML page

            :return: The currency of the product, or None if no currency is found or an error occurs.
            :rtype: Optional[str]
            :raises: Any exceptions raised during the HTML parsing will be logged and re-raised.
            """
            pass

-----------------------------------------
2. Implement the Logic for the new Parser
-----------------------------------------

Implement the two methods from the ``IParser`` interface: ``get_price`` and ``get_currency``. The HTML source code of the website is already parsed with BeautifulSoup and stored in the ``self.soup`` variable. You should use this variable to extract the required information, such as the product price and currency, from the website's HTML structure. Make sure to handle any variations in the HTML and return appropriate values or ``None`` if the data is not found.

------------------------------------
3. Add the Platform Identifier Logic
------------------------------------

To enable the crawler to recognize which parser should be used for a specific website, the logic must be implemented in the function ``get_correct_parser(self, html_page: str) -> Optional[IParser]`` located in the file:

``24ws-ase-pr-qse-02/price_crawler/crawler/web_crawler/PlatformIdentifier.py``.

This function takes the HTML source code of a website as input. Within this function, you should implement the logic to identify a website based on its HTML source code.

Once you are confident that the HTML source code belongs to the website you want to crawl, return an instance of the corresponding parser object.

For example, if you want to return a ``WoocommerceParser`` object, the return of the parser object would look like this:

.. code-block:: python

    return WoocommerceParser(self.logger, self.config, soup)

