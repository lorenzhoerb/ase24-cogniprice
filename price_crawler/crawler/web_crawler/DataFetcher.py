import time

from selenium.common import WebDriverException

from crawler.utils.ConfigReader import ConfigReader
from selenium import webdriver

import logging


class DataFetcher:

    def __init__(self, logger: logging.Logger, config: ConfigReader) -> None:
        """
        Initializes the WebDriver with the given configuration and options.

        :param logger: The logger instance used to log messages and errors during
                       WebDriver initialization.
        :type logger: logging.Logger
        :param config: The config reader used to fetch configuration settings, specifically
                       the page load timeout for Selenium.
        :type config: ConfigReader
        :raises Exception: If an error occurs during the initialization of the Chrome WebDriver.
        """
        self.logger = logger
        self.config = config
        self.options = webdriver.ChromeOptions()
        # add options for the Chrome web driver
        self.options.add_argument("--disable-client-side-phishing-detection")
        self.options.add_argument("--disable-component-extensions-with-background-pages")
        self.options.add_argument("--disable-default-apps")
        self.options.add_argument("--disable-extensions")
        self.options.add_argument("--disable-features=InterestFeedContentSuggestions")
        self.options.add_argument("--disable-features=Translate")
        self.options.add_argument("--hide-scrollbars")
        self.options.add_argument("--mute-audio")
        self.options.add_argument("--no-default-browser-check")
        self.options.add_argument("--no-first-run")
        self.options.add_argument("--ash-no-nudges")
        self.options.add_argument("--disable-search-engine-choice-screen")
        self.options.add_argument("--propagate-iph-for-testing")
        self.options.add_argument("--headless=new")
        self.options.add_argument("--no-sandbox")
        self.options.add_argument("--disable-dev-shm-usage")
        self.options.add_argument("--disable-gpu")
        self.options.add_argument("--disable-software-rasterizer")

        self.options.timeouts = {'pageLoad': int(config.get('Selenium', 'page_load_timeout'))}

        try:
            self.driver = webdriver.Chrome(options=self.options)
        except Exception as e:
            self.logger.critical("Could not initialize chrome webdriver for data fetching. Error: " + str(e))
            raise

    def fetch_data(self, url: str, retries: int = 3) -> str:
        """
        Fetches the HTML source of a given URL using Selenium WebDriver.

        :param url: The URL of the page to fetch.
        :param retries: The maximum number of retry attempts in case of session crashes.
        :return: The HTML source of the page.
        :raises Exception: If an error occurs while loading the page or retrieving the HTML.
        """
        attempt = 0
        while attempt < retries:
            try:
                self.driver.get(url)
                return self.driver.page_source

            except WebDriverException as e:
                if "session deleted because of page crash" in str(e):
                    self.logger.error(
                        f"Page crashed for {url}. Attempt {attempt + 1}/{retries}. Reinitializing WebDriver.")
                    attempt += 1
                    self.driver.quit()
                    self.driver = webdriver.Chrome(options=self.options)

                    if attempt >= retries:
                        self.logger.error(f"Failed to fetch {url} after {retries} attempts due to page crash.")
                        raise

                    time.sleep(0.5)
                else:
                    # If the error is not related to the session crash, log and re-raise the exception
                    self.logger.error(f"Error fetching {url}: {e}")
                    raise

        # If we exhausted the retry attempts, log and raise
        self.logger.error(f"Failed to fetch {url} after {retries} attempts.")
        raise Exception(f"Failed to fetch {url} after {retries} attempts due to repeated session crashes.")