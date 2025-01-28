from crawler.utils.ConfigReader import ConfigReader
from crawler.web_crawler.CrawlerExceptions import *

from protego import Protego
import requests
from urllib.parse import urlparse

import logging
import time

class RobotsChecker:

    def __init__(self, config: ConfigReader) -> None:
        """
        Initializes the WebCrawler with a logger and a configuration reader. It sets up logging and loads
        the configuration provided by the ConfigReader. Additionally, it initializes the robots.txt cache.

        :param config: The configuration reader object that provides the necessary settings for the web crawler.
        :type config: ConfigReader
        """
        # setup logging
        self.logger = logging.getLogger('web_crawl_logger')
        # read config file
        self.config = config

        # structure robots_cache[robots_url] = {'allowed': bool, 'expiration': float}
        # example: robots_cache[https://flaminguin.net/robots.txt] = {'allowed': true, 'expiration': 1733150106.876341}
        self.robots_cache = {}

    @staticmethod
    def validate_url(url: str) -> str:
        """
        Validates a given URL to ensure it follows the correct format (e.g., checking for 'http' or 'https').
        If the URL is valid, it returns the URL as a string. If invalid, it should return None.

        :param url: The URL to be validated.
        :type url: str
        :return: The validated URL as a string if it is valid, or None if it is invalid.
        :rtype: str
        """

        # Specific url validation can be implemented here.
        # per default, faulty url will be filtered by the check_crawl_allowance function
        return str(url)


    def check_crawl_allowance(self, url: str) -> bool:
        """
        Checks whether crawling is allowed for a given URL according to the robots.txt policy of the host.
        It first checks the robots.txt file for the domain, either from cache or by downloading and parsing it.
        If the website does not allow crawling an appropriate exception is raised.
        If a website does not have a robots.txt it is assumed that the website does not allow crawling.

        :param url: The URL to be crawled.
        :type url: str
        :return: `True` if crawling is allowed for the URL, `False` if not.
        :rtype: bool
        :raises Exception: If there is an error fetching or parsing the robots.txt file or if the URL is invalid.
        """
        # get robots.txt: e.g. https://flaminguin.net/robots.txt
        try:
            robots_url = self._get_robots_url(url)
        except Exception as e:
            # pass the Exception along to be caught in main
            raise

        # check if url is already in cache
        if self._check_robots_cache(robots_url):
            self.logger.info("Cache HIT for robots.txt allowance on url: " + str(robots_url))
            return self._get_cache_allowance(robots_url)

        # url not in cache or expired
        self.logger.info("Cache MISS for robots.txt allowance on url: " + str(robots_url))
        # download the robots.txt file from host
        try:
            robots_response = requests.get(robots_url, timeout=int(self.config.get('General', 'robots_crawl_timeout')))
        except requests.exceptions.MissingSchema:
            self.logger.error("Schema missing (http, https) from url. Url: " + str(robots_url))
            raise
        except requests.ConnectionError:
            self.logger.error("Could not connect to url: " + str(robots_url))
            raise
        except requests.TooManyRedirects:
            self.logger.error("TooManyRedirects from url: " + str(robots_url))
            raise
        except requests.Timeout:
            self.logger.error("Timeout from url: " + str(robots_url))
            raise
        except Exception as e:
            self.logger.error("Error when reading robots policy from url: " + str(robots_url) + " error:" + str(e))
            raise

        # parse robots.txt
        try:
            robots_parsed = Protego.parse(robots_response.text)
            # check if the crawler is allowed
            crawl_allowed = robots_parsed.can_fetch(url, str(self.config.get('General', 'crawler_name')))
        except Exception as e:
            self.logger.error("Error during parsing the robots.txt file. Exception: " + str(e) + " url: " + str(robots_url))
            raise RobotParseException(str(e))

        # store result in cache
        self._store_cache_allowance(robots_url, crawl_allowed)
        return crawl_allowed

    def _get_robots_url(self, url: str) -> str:
        """
        Constructs the URL for the robots.txt file based on the provided URL by parsing its domain.

        :param url: The URL from which to derive the robots.txt URL.
        :type url: str
        :return: The URL of the robots.txt file for the provided URL's domain.
        :rtype: str
        :raises URLParseException: If there is an error parsing the provided URL.
        """
        self.logger.debug("Starting parsing the url: " + str(url))
        try:
            return "https://" + urlparse(url).netloc + "/robots.txt"
        except Exception as e:
            self.logger.error("Could not parse the url: " + str(url) + " Error: " + str(e))
            raise URLParseException("Could not parse the url: " + str(url)+ " Error: " + str(e))


    def _check_robots_cache(self, robots_url: str) -> bool:
        """
        Checks if the robots.txt file for a given URL is present in the cache and whether it is still valid.

        :param robots_url: The URL of the robots.txt file to check in the cache.
        :type robots_url: str
        :return: `True` if the robots.txt URL is found in the cache and is still valid, `False` otherwise.
        :rtype: bool
        """
        if robots_url in self.robots_cache.keys():
            # Cache HIT
            if time.time() > self.robots_cache[robots_url]['expiration']:
                # cache expired
                self.logger.debug("robots url in cache has expired. URL: " + str(robots_url))
                return False
            else:
                # cache still good
                self.logger.debug("robots url cache hit. Serving from cache. URL: " + str(robots_url))
                return True
        else:
            self.logger.debug("robots url has not been cached before. URL: " + str(robots_url))
            return False

    def _get_cache_allowance(self, robots_url: str) -> bool:
        """
        Retrieves the crawl allowance (whether the crawler is allowed to crawl the URL) from the cache
        for a given robots.txt URL.

        :param robots_url: The URL of the robots.txt file to get the crawl allowance from the cache.
        :type robots_url: str
        :return: `True` if the crawler is allowed to crawl the URL, `False` otherwise.
        :rtype: bool
        """
        return self.robots_cache[robots_url]['allowed']

    def _store_cache_allowance(self, robots_url: str, is_allowed: bool) -> None:
        """
        Retrieves the crawl allowance (whether the crawler is allowed to crawl the URL) from the cache
        for a given robots.txt URL.

        :param robots_url: The URL of the robots.txt file to get the crawl allowance from the cache.
        :type robots_url: str
        :return: `True` if the crawler is allowed to crawl the URL, `False` otherwise.
        :rtype: bool
        """
        self.logger.debug("Storing allowance in cache for robots.txt url: " + str(robots_url))
        time_in_ten_minutes: float = time.time() + int(self.config.get('Crawler', 'robots_txt_cache_time'))
        self.robots_cache[robots_url] = {'allowed': is_allowed, 'expiration': time_in_ten_minutes}
