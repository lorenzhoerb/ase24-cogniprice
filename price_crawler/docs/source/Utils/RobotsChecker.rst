Robots.txt Checker
==================

The `RobotsChecker <#crawler.utils.RobotsChecker.RobotsChecker>`_ class handles the validation and checking of a given URL's crawl allowance based on the robots.txt file of the corresponding domain. It includes the following key methods:

    * `check_crawl_allowance() <#crawler.utils.RobotsChecker.RobotsChecker.check_crawl_allowance>`_: Checks whether the crawler is allowed to crawl a given URL by checking the domain's robots.txt file, either from the cache or by downloading and parsing the file if it's not cached or has expired.
    * `validate_url() <#crawler.utils.RobotsChecker.RobotsChecker.validate_url>`_: Checks if a URL is correctly formatted, specifically ensuring that it begins with "http" or "https."

These methods ensure that the crawler respects domain-specific crawling policies defined in robots.txt and manages caching for efficiency. If a website does not have a robots.txt it is assumed that the website does not allow crawling.

.. automodule:: crawler.utils.RobotsChecker